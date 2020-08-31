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

package com.liferay.portal.search.tuning.blueprints.engine.internal.searchrequest.data.contributor;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.rescore.Rescore;
import com.liferay.portal.search.rescore.RescoreBuilder;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.clause.ClauseConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.clause.ClausesConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.clause.ConditionsConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.ClauseContext;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.Occur;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.Operator;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.exception.SearchRequestDataException;
import com.liferay.portal.search.tuning.blueprints.engine.internal.clause.ClauseBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.engine.internal.clause.condition.ConditionHandlerFactory;
import com.liferay.portal.search.tuning.blueprints.engine.internal.util.BlueprintTemplateVariableUtil;
import com.liferay.portal.search.tuning.blueprints.engine.internal.util.BlueprintValueUtil;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.searchrequest.SearchRequestData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.clause.ClauseBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.spi.clause.ConditionHandler;
import com.liferay.portal.search.tuning.blueprints.engine.spi.query.QueryContributor;
import com.liferay.portal.search.tuning.blueprints.engine.spi.searchrequest.SearchRequestDataContributor;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

		_addClauses(searchRequestContext, searchRequestData);

		_processQueryContributors(searchRequestContext, searchRequestData);
	}

	protected void addQueryContributor(QueryContributor queryContributor) {
		_queryContributors.add(queryContributor);
	}

	protected void removeQueryContributor(QueryContributor queryContributor) {
		_queryContributors.remove(queryContributor);
	}

	private void _addClause(
		SearchRequestData searchRequestData, ClauseContext clauseContext,
		Occur occur, Query subquery, JSONObject queryJsonObject) {

		if (clauseContext.equals(ClauseContext.POST_FILTER)) {
			_addPostFilterClause(searchRequestData, subquery, occur);
		}
		else if (clauseContext.equals(ClauseContext.PRE_FILTER)) {
			_addPreFilterClause(searchRequestData, subquery, occur);
		}
		else if (clauseContext.equals(ClauseContext.QUERY)) {
			_addQueryClause(searchRequestData, clauseContext, occur, subquery);
		}
		else if (clauseContext.equals(ClauseContext.RESCORE)) {
			_addRescoreClause(
				searchRequestData, clauseContext, occur, subquery,
				_getWindoSize(queryJsonObject));
		}
	}

	private void _addClauses(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData) {

		Optional<JSONArray> clauseConfigurationJsonArrayOptional =
			_blueprintHelper.getClauseConfigurationOptional(
				searchRequestContext.getBlueprint());

		if (!clauseConfigurationJsonArrayOptional.isPresent()) {
			return;
		}

		JSONArray clauseConfigurationJsonArray =
			clauseConfigurationJsonArrayOptional.get();

		for (int i = 0; i < clauseConfigurationJsonArray.length(); i++) {
			JSONObject clauseConfigurationJsonObject =
				clauseConfigurationJsonArray.getJSONObject(i);

			if (!clauseConfigurationJsonObject.getBoolean(
					ClauseConfigurationKeys.ENABLED.getJsonKey(), false)) {

				continue;
			}

			boolean applyClauses = _isConditionsTrue(
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
						ClausesConfigurationKeys.TYPE.getJsonKey());

					try {
						ClauseBuilder clauseBuilder =
							_clauseBuilderFactory.getBuilder(type);

						queryJsonObject =
							BlueprintTemplateVariableUtil.
								parseTemplateVariables(
									searchRequestContext,
									clauseJsonObject.getJSONObject(
										ClausesConfigurationKeys.QUERY.
											getJsonKey()));

						Optional<Query> clauseOptional =
							clauseBuilder.buildClause(
								searchRequestContext, queryJsonObject);

						if (!clauseOptional.isPresent()) {
							continue;
						}

						ClauseContext clauseContext = _getClauseContext(
							searchRequestContext, clauseJsonObject);

						if (clauseContext == null) {
							continue;
						}

						Occur occur = _getOccur(
							searchRequestContext, clauseJsonObject);

						if (occur == null) {
							continue;
						}

						_addClause(
								searchRequestData, clauseContext, occur, clauseOptional.get(), queryJsonObject);
					}
					catch (IllegalArgumentException illegalArgumentException) {
						searchRequestContext.addMessage(
							new Message(
								Severity.ERROR, "core",
								"core.error.unknown-query-type",
								illegalArgumentException.getMessage(),
								illegalArgumentException, clauseJsonObject,
								ClausesConfigurationKeys.TYPE.getJsonKey(),
								type));

						_log.error(
							illegalArgumentException.getMessage(),
								illegalArgumentException);
					}
					catch (JSONException jsonException) {
						searchRequestContext.addMessage(
							new Message(
								Severity.ERROR, "core",
								"core.error.error-in-parsing-configuration-parameters",
								jsonException.getMessage(), jsonException,
								queryJsonObject, null, null));
						_log.error(jsonException.getMessage(), jsonException);
					}
					catch (Exception exception) {
						searchRequestContext.addMessage(
							new Message(
								Severity.ERROR, "core",
								"core.error.unknown-clause-building-error",
								exception.getMessage(), exception,
								clauseJsonObject, null, null));
						_log.error(exception.getMessage(), exception);
					}
				}
			}
		}
	}

	private void _addPostFilterClause(
		SearchRequestData searchRequestData, Query subquery, Occur occur) {

		BooleanQuery postFilterQuery = searchRequestData.getPostFilterQuery();

		if (Occur.MUST.equals(occur)) {
			postFilterQuery.addMustQueryClauses(subquery);
		}
		else if (Occur.MUST_NOT.equals(occur)) {
			postFilterQuery.addMustNotQueryClauses(subquery);
		}
		else {
			postFilterQuery.addShouldQueryClauses(subquery);
		}
	}

	private void _addPreFilterClause(
		SearchRequestData searchRequestData, Query subquery, Occur occur) {

		BooleanQuery query = searchRequestData.getQuery();

		query.addFilterQueryClauses(subquery);
	}

	private void _addQueryClause(
		SearchRequestData searchRequestData, ClauseContext clauseContext,
		Occur occur, Query subquery) {

		BooleanQuery query = searchRequestData.getQuery();

		if (Occur.MUST.equals(occur)) {
			query.addMustQueryClauses(subquery);
		}
		else if (Occur.MUST_NOT.equals(occur)) {
			query.addMustNotQueryClauses(subquery);
		}
		else {
			query.addShouldQueryClauses(subquery);
		}
	}

	private void _addRescoreClause(
		SearchRequestData searchRequestData, ClauseContext clauseContext,
		Occur occur, Query subquery, int windowSize) {

		if (_rescoreBuilder == null) {
			return;
		}

		Rescore rescore = _rescoreBuilder.query(
			subquery
		).windowSize(
			windowSize
		).build();

		List<Rescore> rescores = searchRequestData.getRescores();

		rescores.add(rescore);
	}

	private ClauseContext _getClauseContext(
		SearchRequestContext searchRequestContext, JSONObject queryJsonObject) {

		String clauseContextString = queryJsonObject.getString(
			ClausesConfigurationKeys.CONTEXT.getJsonKey());

		try {
			clauseContextString = StringUtil.toUpperCase(clauseContextString);

			return ClauseContext.valueOf(clauseContextString);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.unknown-clause-context",
					illegalArgumentException.getMessage(),
					illegalArgumentException, queryJsonObject,
					ClausesConfigurationKeys.CONTEXT.getJsonKey(),
					clauseContextString));

			if (_log.isWarnEnabled()) {
				_log.warn(
					illegalArgumentException.getMessage(),
					illegalArgumentException);
			}
		}

		return null;
	}

	private Occur _getOccur(
		SearchRequestContext searchRequestContext, JSONObject queryJsonObject) {

		String occurString = queryJsonObject.getString(
			ClausesConfigurationKeys.OCCUR.getJsonKey());

		try {
			occurString = StringUtil.toUpperCase(occurString);

			return Occur.valueOf(occurString);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.unknown-occur-value",
					illegalArgumentException.getMessage(),
					illegalArgumentException, queryJsonObject,
					ClausesConfigurationKeys.OCCUR.getJsonKey(), occurString));
		}

		return null;
	}

	private Integer _getWindoSize(JSONObject queryJsonObject) {
		if (queryJsonObject.has(
				ClausesConfigurationKeys.WINDOW_SIZE.getJsonKey())) {

			return queryJsonObject.getInt(
				ClausesConfigurationKeys.WINDOW_SIZE.getJsonKey());
		}

		return null;
	}

	private boolean _isConditionsTrue(
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
				ConditionsConfigurationKeys.HANDLER.getJsonKey());

			try {
				ConditionHandler clauseConditionHandler =
					_clauseConditionHandlerFactory.getHandler(handler);

				String operatorString = conditionJsonObject.getString(
					ConditionsConfigurationKeys.OPERATOR.getJsonKey());

				Operator operator = BlueprintValueUtil.getOperator(
					operatorString);

				JSONObject handlerConfigurationJsonObject =
					conditionJsonObject.getJSONObject(
						ConditionsConfigurationKeys.HANDLER_PARAMETERS.
							getJsonKey());

				boolean conditionTrue = clauseConditionHandler.isTrue(
					searchRequestContext, handlerConfigurationJsonObject);

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
			catch (IllegalArgumentException illegalArgumentException) {
				searchRequestContext.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.unknown-clause-condition-handler",
						illegalArgumentException.getMessage(),
						illegalArgumentException, conditionJsonObject,
						ConditionsConfigurationKeys.HANDLER.getJsonKey(),
						handler));

				if (_log.isWarnEnabled()) {
					_log.warn(
						illegalArgumentException.getMessage(),
						illegalArgumentException);
				}
			}
			catch (Exception exception) {
				searchRequestContext.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.unknown-clause-condition-error",
						exception.getMessage(), exception, conditionJsonObject,
						null, null));

				_log.error(exception.getMessage(), exception);
			}
		}

		return valid;
	}

	private boolean _isQueryContributorExcluded(
		SearchRequestContext searchRequestContext, String name) {

		Optional<List<String>> excludedQueryContributorsOptional =
			_blueprintHelper.getExcludedQueryContributorsOptional(
				searchRequestContext.getBlueprint());

		if (!excludedQueryContributorsOptional.isPresent()) {
			return false;
		}

		List<String> excludedQueryContributors =
			excludedQueryContributorsOptional.get();

		Stream<String> stream = excludedQueryContributors.stream();

		if (stream.anyMatch(s -> s.contentEquals(name) || s.equals("*"))) {
			return true;
		}

		return false;
	}

	private void _processQueryContributors(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData) {

		if (_log.isDebugEnabled()) {
			_log.debug("Processing query contributors");
		}

		if (_queryContributors.isEmpty()) {
			return;
		}

		for (QueryContributor queryContributor : _queryContributors) {
			Class<?> clazz = queryContributor.getClass();

			if (_isQueryContributorExcluded(
					searchRequestContext, clazz.getName())) {
				continue;
			}

			try {
				Optional<Query> contributorQueryOptional =
					queryContributor.build(searchRequestContext);

				if (!contributorQueryOptional.isPresent()) {
					continue;
				}
				
				Query clause = contributorQueryOptional.get();

				ClauseContext clauseContext =
					queryContributor.getClauseContext();

				Occur occur = queryContributor.getOccur();

				_addClause(
					searchRequestData, clauseContext, occur, clause, null);
			}
			catch (SearchRequestDataException searchRequestDataException) {
				_log.error(
					searchRequestDataException.getMessage(),
					searchRequestDataException);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		QuerySearchRequestDataContributor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private ClauseBuilderFactory _clauseBuilderFactory;

	@Reference
	private ConditionHandlerFactory _clauseConditionHandlerFactory;

	@Reference
	private Queries _queries;

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