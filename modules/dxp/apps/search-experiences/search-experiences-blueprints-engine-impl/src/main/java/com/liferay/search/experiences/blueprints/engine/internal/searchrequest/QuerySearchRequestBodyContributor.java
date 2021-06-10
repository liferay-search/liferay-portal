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

package com.liferay.search.experiences.blueprints.engine.internal.searchrequest;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.rescore.RescoreBuilder;
import com.liferay.portal.search.rescore.RescoreBuilderFactory;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.blueprints.constants.json.keys.query.ClauseConfigurationKeys;
import com.liferay.search.experiences.blueprints.constants.json.keys.query.QueryConfigurationKeys;
import com.liferay.search.experiences.blueprints.constants.json.values.ClauseContext;
import com.liferay.search.experiences.blueprints.constants.json.values.Occur;
import com.liferay.search.experiences.blueprints.engine.internal.clause.util.ClauseHelper;
import com.liferay.search.experiences.blueprints.engine.internal.condition.util.ConditionsProcessor;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.query.QueryContributor;
import com.liferay.search.experiences.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.blueprints.util.component.ServiceComponentReference;
import com.liferay.search.experiences.blueprints.util.component.ServiceComponentReferenceUtil;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=query",
	service = SearchRequestBodyContributor.class
)
public class QuerySearchRequestBodyContributor
	implements SearchRequestBodyContributor {

	@Override
	public void contribute(
		SearchRequestBuilder searchRequestBuilder, Blueprint blueprint,
		ParameterData parameterData, Messages messages) {

		Optional<JSONArray> optional =
			_blueprintHelper.getQueryConfigurationOptional(blueprint);

		if (optional.isPresent()) {
			_contribute(
				searchRequestBuilder, optional.get(), parameterData, messages);
		}

		_executeQueryContributors(
			searchRequestBuilder, parameterData, blueprint, messages);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void registerQueryContributor(
		QueryContributor queryContributor, Map<String, Object> properties) {

		ServiceComponentReferenceUtil.addToMapByName(
			_queryContributors, queryContributor, properties);
	}

	protected void unregisterQueryContributor(
		QueryContributor queryContributor, Map<String, Object> properties) {

		ServiceComponentReferenceUtil.removeFromMapByName(
			_queryContributors, queryContributor, properties);
	}

	private void _addPostFilterClause(
		SearchRequestBuilder searchRequestBuilder, Query query, Occur occur) {

		searchRequestBuilder.addPostFilterQueryPart(
			_complexQueryPartBuilderFactory.builder(
			).query(
				query
			).occur(
				occur.getjsonValue()
			).build());
	}

	private void _addQueryClause(
		SearchRequestBuilder searchRequestBuilder, Query query, Occur occur) {

		searchRequestBuilder.addComplexQueryPart(
			_complexQueryPartBuilderFactory.builder(
			).query(
				query
			).occur(
				occur.getjsonValue()
			).build());
	}

	private void _addRescoreClause(
		SearchRequestBuilder searchRequestBuilder, Query query,
		JSONObject jsonObject) {

		RescoreBuilder rescoreBuilder = _rescoreBuilderFactory.builder(query);

		if (jsonObject.has(ClauseConfigurationKeys.WINDOW_SIZE.getJsonKey())) {
			rescoreBuilder.windowSize(
				jsonObject.getInt(
					ClauseConfigurationKeys.WINDOW_SIZE.getJsonKey(), 100));
		}

		if (jsonObject.has(ClauseConfigurationKeys.QUERY_WEIGHT.getJsonKey())) {
			rescoreBuilder.queryWeight(
				GetterUtil.getFloat(
					jsonObject.getString(
						ClauseConfigurationKeys.WINDOW_SIZE.getJsonKey(),
						"1.0")));
		}

		if (jsonObject.has(
				ClauseConfigurationKeys.RESCORE_QUERY_WEIGHT.getJsonKey())) {

			rescoreBuilder.queryWeight(
				GetterUtil.getFloat(
					jsonObject.getString(
						ClauseConfigurationKeys.RESCORE_QUERY_WEIGHT.
							getJsonKey(),
						"1.0")));
		}

		searchRequestBuilder.addRescore(rescoreBuilder.build());
	}

	private void _addRescoreClause(
		SearchRequestBuilder searchRequestBuilder, Query query,
		QueryContributor queryContributor) {

		RescoreBuilder rescoreBuilder = _rescoreBuilderFactory.builder(query);

		if (queryContributor.getAttributes() != null) {
			Map<String, Object> attributes = queryContributor.getAttributes();

			if (attributes.containsKey(
					ClauseConfigurationKeys.WINDOW_SIZE.getJsonKey())) {

				rescoreBuilder.windowSize(
					GetterUtil.getInteger(
						attributes.get(
							ClauseConfigurationKeys.WINDOW_SIZE.getJsonKey())));
			}

			if (attributes.containsKey(
					ClauseConfigurationKeys.QUERY_WEIGHT.getJsonKey())) {

				rescoreBuilder.queryWeight(
					GetterUtil.getFloat(
						attributes.get(
							ClauseConfigurationKeys.QUERY_WEIGHT.
								getJsonKey())));
			}

			if (attributes.containsKey(
					ClauseConfigurationKeys.RESCORE_QUERY_WEIGHT.
						getJsonKey())) {

				rescoreBuilder.rescoreQueryWeight(
					GetterUtil.getFloat(
						attributes.get(
							ClauseConfigurationKeys.RESCORE_QUERY_WEIGHT.
								getJsonKey())));
			}
		}

		searchRequestBuilder.addRescore(rescoreBuilder.build());
	}

	private void _contribute(
		SearchRequestBuilder searchRequestBuilder,
		JSONArray configurationJSONArray, ParameterData parameterData,
		Messages messages) {

		for (int i = 0; i < configurationJSONArray.length(); i++) {
			JSONObject configurationJSONObject =
				configurationJSONArray.getJSONObject(i);

			messages.setElementId("queryElement-" + i);

			if (!configurationJSONObject.getBoolean(
					QueryConfigurationKeys.ENABLED.getJsonKey(), true) ||
				!_isConditionsTrue(
					configurationJSONObject, parameterData, messages)) {

				messages.unsetElementId();

				continue;
			}

			_processClauses(
				searchRequestBuilder,
				configurationJSONObject.getJSONArray(
					QueryConfigurationKeys.CLAUSES.getJsonKey()),
				parameterData, messages);

			messages.unsetElementId();
		}
	}

	private void _executeQueryContributors(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, Messages messages) {

		if (_log.isDebugEnabled()) {
			_log.debug("Processing query contributors");
		}

		if (_queryContributors.isEmpty()) {
			return;
		}

		for (Map.Entry<String, ServiceComponentReference<QueryContributor>>
				entry : _queryContributors.entrySet()) {

			try {
				ServiceComponentReference<QueryContributor> value =
					entry.getValue();

				QueryContributor queryContributor = value.getServiceComponent();

				Optional<Query> queryOptional = queryContributor.build(
					blueprint, parameterData, messages);

				if (!queryOptional.isPresent()) {
					continue;
				}

				ClauseContext clauseContext =
					queryContributor.getClauseContext();

				if (clauseContext.equals(ClauseContext.POST_FILTER)) {
					_addPostFilterClause(
						searchRequestBuilder, queryOptional.get(),
						queryContributor.getOccur());
				}
				else if (clauseContext.equals(ClauseContext.QUERY)) {
					_addQueryClause(
						searchRequestBuilder, queryOptional.get(),
						queryContributor.getOccur());
				}
				else if (clauseContext.equals(ClauseContext.RESCORE)) {
					_addRescoreClause(
						searchRequestBuilder, queryOptional.get(),
						queryContributor);
				}
			}
			catch (Exception exception) {
				MessagesUtil.unknownError(
					messages, getClass().getName(), exception, null, null,
					null);
			}
		}
	}

	private ClauseContext _getClauseContext(JSONObject jsonObject) {
		String context = jsonObject.getString(
			ClauseConfigurationKeys.CONTEXT.getJsonKey());

		return ClauseContext.valueOf(StringUtil.toUpperCase(context));
	}

	private Occur _getOccur(JSONObject jsonObject) {
		String occur = jsonObject.getString(
			ClauseConfigurationKeys.OCCUR.getJsonKey(),
			Occur.MUST.getjsonValue());

		return Occur.valueOf(StringUtil.toUpperCase(occur));
	}

	private boolean _isConditionsTrue(
		JSONObject jsonObject, ParameterData parameterData, Messages messages) {

		JSONObject conditionsJSONObject = jsonObject.getJSONObject(
			QueryConfigurationKeys.CONDITIONS.getJsonKey());

		if (conditionsJSONObject == null) {
			return true;
		}

		return _conditionsProcessor.processConditions(
			conditionsJSONObject, parameterData, messages);
	}

	private void _processClauses(
		SearchRequestBuilder searchRequestBuilder, JSONArray clausesJSONArray,
		ParameterData parameterData, Messages messages) {

		for (int j = 0; j < clausesJSONArray.length(); j++) {
			JSONObject clauseJSONObject = clausesJSONArray.getJSONObject(j);

			Optional<Query> clauseOptional = _clauseHelper.getClause(
				clauseJSONObject.getJSONObject(
					ClauseConfigurationKeys.QUERY.getJsonKey()),
				parameterData, messages);

			if (!clauseOptional.isPresent()) {
				continue;
			}

			ClauseContext clauseContext = _getClauseContext(clauseJSONObject);

			Occur occur = _getOccur(clauseJSONObject);

			if (clauseContext.equals(ClauseContext.POST_FILTER)) {
				_addPostFilterClause(
					searchRequestBuilder, clauseOptional.get(), occur);
			}
			else if (clauseContext.equals(ClauseContext.QUERY)) {
				_addQueryClause(
					searchRequestBuilder, clauseOptional.get(), occur);
			}
			else if (clauseContext.equals(ClauseContext.RESCORE)) {
				_addRescoreClause(
					searchRequestBuilder, clauseOptional.get(),
					clauseJSONObject);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		QuerySearchRequestBodyContributor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private ClauseHelper _clauseHelper;

	@Reference
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Reference
	private ConditionsProcessor _conditionsProcessor;

	@Reference
	private Queries _queries;

	private volatile Map<String, ServiceComponentReference<QueryContributor>>
		_queryContributors = new ConcurrentHashMap<>();

	@Reference
	private RescoreBuilderFactory _rescoreBuilderFactory;

}