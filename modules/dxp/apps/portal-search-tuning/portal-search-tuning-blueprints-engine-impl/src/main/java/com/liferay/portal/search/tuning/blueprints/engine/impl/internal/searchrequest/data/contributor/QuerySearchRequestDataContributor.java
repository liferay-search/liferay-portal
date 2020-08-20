/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.searchrequest.data.contributor;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.rescore.Rescore;
import com.liferay.portal.search.rescore.RescoreBuilder;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.ClauseConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.ClauseContext;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.Occur;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.Operator;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.exception.SearchRequestDataException;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.clause.ClauseBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.clause.condition.ClauseConditionHandlerFactory;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.util.ConfigurationUtil;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.util.ContextVariableUtil;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.searchrequest.SearchRequestData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.clause.ClauseBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.spi.clause.ClauseConditionHandler;
import com.liferay.portal.search.tuning.blueprints.engine.spi.query.QueryContributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SearchRequestDataContributor.class)
public class QuerySearchRequestDataContributor
	implements SearchRequestDataContributor {

	@Override
	public void contribute(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData) {

		JSONArray clauseConfigurationJSonArray =
			searchRequestContext.getClauseConfiguration();

		for (int i = 0; i < clauseConfigurationJSonArray.length(); i++) {
			JSONObject clauseConfigurationJsonObject =
				clauseConfigurationJSonArray.getJSONObject(i);

			if (!clauseConfigurationJsonObject.getBoolean(
					ClauseConfigurationKeys.ENABLED.getJsonKey(), false)) {

				continue;
			}

			boolean applyClauses = false;

			applyClauses = _isClauseConditionsTrue(
				searchRequestContext, clauseConfigurationJsonObject);

			if (applyClauses) {
				JSONArray clauseJsonArray =
					clauseConfigurationJsonObject.getJSONArray(
						ClauseConfigurationKeys.CLAUSES.getJsonKey());

				JSONObject clauseJsonObject = null;
				JSONObject queryJsonObject = null;

				for (int j = 0; j < clauseJsonArray.length(); j++) {
					clauseJsonObject = clauseJsonArray.getJSONObject(j);

					String type = clauseJsonObject.getString(
						ClauseConfigurationKeys.TYPE.getJsonKey());

					try {
						ClauseBuilder clauseBuilder =
							_clauseBuilderFactory.getBuilder(type);

						queryJsonObject =
							ContextVariableUtil.parseConfigurationVariables(
								searchRequestContext,
								clauseJsonObject.getJSONObject(
									ClauseConfigurationKeys.QUERY.
										getJsonKey()));

						Optional<Query> clauseOptional =
							clauseBuilder.buildClause(
								searchRequestContext, queryJsonObject);

						if (clauseOptional.isPresent()) {
							_addClause(
								searchRequestContext, searchRequestData,
								clauseJsonObject, clauseOptional.get());
						}
					}
					catch (IllegalArgumentException iae) {
						searchRequestContext.addMessage(
							new Message(
								Severity.ERROR, "core",
								"core.error.unknown-query-type",
								iae.getMessage(), iae, clauseJsonObject,
								ClauseConfigurationKeys.TYPE.getJsonKey(),
								type));

						if (_log.isWarnEnabled()) {
							_log.warn(iae.getMessage(), iae);
						}
					}
					catch (JSONException je) {
						searchRequestContext.addMessage(
							new Message(
								Severity.ERROR, "core",
								"core.error.error-in-parsing-configuration-parameters",
								je.getMessage(), je, queryJsonObject, null,
								null));
						_log.error(je.getMessage(), je);
					}
					catch (Exception e) {
						searchRequestContext.addMessage(
							new Message(
								Severity.ERROR, "core",
								"core.error.unknown-clause-building-error",
								e.getMessage(), e, clauseJsonObject, null,
								null));
						_log.error(e.getMessage(), e);
					}
				}
			}
		}

		_processQueryContributors(searchRequestContext, searchRequestData);
	}

	protected void addQueryContributor(QueryContributor queryContributor) {
		_queryContributors.add(queryContributor);
	}

	protected void removeQueryContributor(QueryContributor queryContributor) {
		_queryContributors.remove(queryContributor);
	}

	private void _addClause(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData, JSONObject queryJsonObject,
		Query clause) {

		ClauseContext clauseContext;
		String clauseContextString = queryJsonObject.getString(
			ClauseConfigurationKeys.CONTEXT.getJsonKey());

		try {
			clauseContext = ConfigurationUtil.getClauseContext(
				clauseContextString);
		}
		catch (IllegalArgumentException iae) {
			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.unknown-clause-context",
					iae.getMessage(), iae, queryJsonObject,
					ClauseConfigurationKeys.CONTEXT.getJsonKey(),
					clauseContextString));

			if (_log.isWarnEnabled()) {
				_log.warn(iae.getMessage(), iae);
			}

			return;
		}

		Occur occur;
		String occurString = queryJsonObject.getString(
			ClauseConfigurationKeys.OCCUR.getJsonKey());

		try {
			occur = ConfigurationUtil.getItemOccur(occurString);
		}
		catch (IllegalArgumentException iae) {
			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.unknown-occur-value",
					iae.getMessage(), iae, queryJsonObject,
					ClauseConfigurationKeys.OCCUR.getJsonKey(), occurString));

			return;
		}

		if (ClauseContext.POST_FILTER.equals(clauseContext)) {
			if (Occur.MUST.equals(occur)) {
				searchRequestData.getPostFilterQuery(
				).addMustQueryClauses(
					clause
				);
			}
			else if (Occur.MUST_NOT.equals(occur)) {
				searchRequestData.getPostFilterQuery(
				).addMustNotQueryClauses(
					clause
				);
			}
			else {
				searchRequestData.getPostFilterQuery(
				).addShouldQueryClauses(
					clause
				);
			}
		}
		else if (ClauseContext.PRE_FILTER.equals(clauseContext)) {
			searchRequestData.getQuery(
			).addFilterQueryClauses(
				clause
			);
		}
		else if (ClauseContext.QUERY.equals(clauseContext)) {
			if (Occur.MUST.equals(occur)) {
				searchRequestData.getQuery(
				).addMustQueryClauses(
					clause
				);
			}
			else if (Occur.MUST_NOT.equals(occur)) {
				searchRequestData.getQuery(
				).addMustNotQueryClauses(
					clause
				);
			}
			else {
				searchRequestData.getQuery(
				).addShouldQueryClauses(
					clause
				);
			}
		}
		else if (ClauseContext.RESCORE.equals(clauseContext) 
				&& _rescoreBuilder != null) {
			
			Integer windowSize = null;

			if (queryJsonObject.has(
					ClauseConfigurationKeys.WINDOW_SIZE.getJsonKey())) {

				windowSize = queryJsonObject.getInt(
					ClauseConfigurationKeys.WINDOW_SIZE.getJsonKey());
			}

			Rescore rescore = _rescoreBuilder.query(
				clause
			).windowSize(
				windowSize
			).build();

			searchRequestData.getRescores(
			).add(
				rescore
			);
		}
	}

	private boolean _isClauseConditionsTrue(
		SearchRequestContext searchRequestContext,
		JSONObject clauseConfigurationJsonObject) {

		JSONArray conditionsJsonArray =
			clauseConfigurationJsonObject.getJSONArray(
				ClauseConfigurationKeys.CONDITIONS.getJsonKey());

		if ((conditionsJsonArray == null) ||
			(conditionsJsonArray.length() == 0)) {

			return true;
		}

		boolean valid = false;

		for (int i = 0; i < conditionsJsonArray.length(); i++) {
			JSONObject conditionJsonObject = conditionsJsonArray.getJSONObject(
				i);

			String handler = conditionJsonObject.getString(
				ClauseConfigurationKeys.HANDLER.getJsonKey());

			try {
				ClauseConditionHandler clauseConditionHandler =
					_clauseConditionHandlerFactory.getHandler(handler);

				String operatorString = conditionJsonObject.getString(
					ClauseConfigurationKeys.OPERATOR.getJsonKey());

				Operator operator = ConfigurationUtil.getOperator(
					operatorString);

				JSONObject configurationJsonObject =
					conditionJsonObject.getJSONObject(
						ClauseConfigurationKeys.CONFIGURATION.getJsonKey());

				boolean conditionTrue = clauseConditionHandler.isTrue(
					searchRequestContext, configurationJsonObject);

				if (operator.equals(Operator.AND) && !conditionTrue) {
					return false;
				}
				else if (operator.equals(Operator.NOT) && conditionTrue) {
					return false;
				}
				else if (conditionTrue) {
					valid = true;
				}
			}
			catch (IllegalArgumentException iae) {

				// TODO _getOperator too

				searchRequestContext.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.unknown-clause-condition-handler",
						iae.getMessage(), iae, conditionJsonObject,
						ClauseConfigurationKeys.HANDLER.getJsonKey(), handler));

				if (_log.isWarnEnabled()) {
					_log.warn(iae.getMessage(), iae);
				}
			}
			catch (Exception e) {
				searchRequestContext.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.unknown-clause-condition-error",
						e.getMessage(), e, conditionJsonObject, null, null));

				_log.error(e.getMessage(), e);
			}
		}

		return valid;
	}

	private boolean _isQueryContributorExcluded(
		SearchRequestContext searchRequestContext, String name) {

		Optional<List<String>> excludedQueryContributorsOptional =
			searchRequestContext.getExcludeQueryContributors();

		if (!excludedQueryContributorsOptional.isPresent()) {
			return false;
		}

		if (excludedQueryContributorsOptional.filter(
				list -> list.contains(name) || list.contains("*")
			).isPresent()) {

			return true;
		}

		return false;
	}

	private void _processQueryContributors(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData) {

		if (_log.isDebugEnabled()) {
			_log.debug("Processing query contributors.");
		}

		if (_queryContributors.isEmpty()) {
			return;
		}

		BooleanQuery booleanQuery = searchRequestData.getQuery();

		for (QueryContributor queryContributor : _queryContributors) {
			if (_isQueryContributorExcluded(
					searchRequestContext,
					queryContributor.getClass(
					).getName())) {

				continue;
			}

			try {
				Optional<Query> contributorQueryOptional =
					queryContributor.build(searchRequestContext);

				if (contributorQueryOptional.isPresent()) {
					Query contributorQuery = contributorQueryOptional.get();

					Occur occur = queryContributor.getOccur();

					if (Occur.MUST.equals(occur)) {
						booleanQuery.addMustQueryClauses(contributorQuery);
					}
					else if (Occur.MUST_NOT.equals(occur)) {
						booleanQuery.addMustNotQueryClauses(contributorQuery);
					}
					else if (Occur.SHOULD.equals(occur)) {
						booleanQuery.addShouldQueryClauses(contributorQuery);
					}
				}
			}
			catch (SearchRequestDataException e) {
				_log.error(e.getMessage(), e);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		QuerySearchRequestDataContributor.class);

	@Reference
	private ClauseBuilderFactory _clauseBuilderFactory;

	@Reference
	private ClauseConditionHandlerFactory _clauseConditionHandlerFactory;

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC, service = QueryContributor.class,
		unbind = "removeQueryContributor"
	)
	private volatile List<QueryContributor> _queryContributors =
		new ArrayList<>();

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	private RescoreBuilder _rescoreBuilder;

}