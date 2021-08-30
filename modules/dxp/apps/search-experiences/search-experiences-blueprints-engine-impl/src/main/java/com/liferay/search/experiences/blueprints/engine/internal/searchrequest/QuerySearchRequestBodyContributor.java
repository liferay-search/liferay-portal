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

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
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
import com.liferay.search.experiences.blueprints.constants.json.values.ClauseContext;
import com.liferay.search.experiences.blueprints.constants.json.values.Occur;
import com.liferay.search.experiences.blueprints.engine.internal.clause.util.ClauseHelper;
import com.liferay.search.experiences.blueprints.engine.internal.condition.util.ConditionsProcessor;
import com.liferay.search.experiences.blueprints.engine.internal.util.ProblemBuilders;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.query.QueryContributor;
import com.liferay.search.experiences.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

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
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<JSONArray> optional =
			_blueprintHelper.getQueryConfigurationOptional(blueprint);

		optional.ifPresent(
			jsonArray -> _contribute(
				searchRequestBuilder, jsonArray, parameterData,
				problemsHolderBuilder));

		_executeQueryContributors(
			searchRequestBuilder, parameterData, blueprint,
			problemsHolderBuilder);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_queryContributorServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, QueryContributor.class, "name");
	}

	@Deactivate
	protected void deactivate() {
		_queryContributorServiceTrackerMap.close();
	}

	private void _addPostFilterClause(
		SearchRequestBuilder searchRequestBuilder, Query query, Occur occur) {

		searchRequestBuilder.addPostFilterQueryPart(
			_complexQueryPartBuilderFactory.builder(
			).query(
				query
			).occur(
				_getOccurString(occur)
			).build());
	}

	private void _addQueryClause(
		SearchRequestBuilder searchRequestBuilder, Query query, Occur occur) {

		searchRequestBuilder.addComplexQueryPart(
			_complexQueryPartBuilderFactory.builder(
			).query(
				query
			).occur(
				_getOccurString(occur)
			).build());
	}

	private void _addRescoreClause(
		SearchRequestBuilder searchRequestBuilder, Query query,
		JSONObject jsonObject) {

		RescoreBuilder rescoreBuilder = _rescoreBuilderFactory.builder(query);

		if (jsonObject.has("window_size")) {
			rescoreBuilder.windowSize(jsonObject.getInt("window_size", 100));
		}

		if (jsonObject.has("query_weight")) {
			rescoreBuilder.queryWeight(
				GetterUtil.getFloat(
					jsonObject.getString("window_size", "1.0")));
		}

		if (jsonObject.has("rescore_query_weight")) {
			rescoreBuilder.queryWeight(
				GetterUtil.getFloat(
					jsonObject.getString("rescore_query_weight", "1.0")));
		}

		searchRequestBuilder.addRescore(rescoreBuilder.build());
	}

	private void _addRescoreClause(
		SearchRequestBuilder searchRequestBuilder, Query query,
		QueryContributor queryContributor) {

		RescoreBuilder rescoreBuilder = _rescoreBuilderFactory.builder(query);

		if (queryContributor.getAttributes() != null) {
			Map<String, Object> attributes = queryContributor.getAttributes();

			if (attributes.containsKey("window_size")) {
				rescoreBuilder.windowSize(
					GetterUtil.getInteger(attributes.get("window_size")));
			}

			if (attributes.containsKey("query_weight")) {
				rescoreBuilder.queryWeight(
					GetterUtil.getFloat(attributes.get("query_weight")));
			}

			if (attributes.containsKey("rescore_query_weight")) {
				rescoreBuilder.rescoreQueryWeight(
					GetterUtil.getFloat(
						attributes.get("rescore_query_weight")));
			}
		}

		searchRequestBuilder.addRescore(rescoreBuilder.build());
	}

	private void _contribute(
		SearchRequestBuilder searchRequestBuilder, JSONArray jsonArray,
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		for (int i = 0; i < jsonArray.length(); i++) {
			_contribute(
				"queryElement-" + i, jsonArray.getJSONObject(i),
				searchRequestBuilder, parameterData, problemsHolderBuilder);
		}
	}

	private void _contribute(
		String elementId, JSONObject jsonObject,
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		problemsHolderBuilder.setElementId(elementId);

		if (jsonObject.getBoolean("enabled", true) &&
			_isConditionsTrue(
				jsonObject, parameterData, problemsHolderBuilder)) {

			_processClauses(
				searchRequestBuilder, jsonObject.getJSONArray("clauses"),
				parameterData, problemsHolderBuilder);
		}

		problemsHolderBuilder.unsetElementId();
	}

	private void _executeQueryContributors(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, ProblemsHolderBuilder problemsHolderBuilder) {

		if (_log.isDebugEnabled()) {
			_log.debug("Processing query contributors");
		}

		Set<String> keySet = _queryContributorServiceTrackerMap.keySet();

		if (keySet.isEmpty()) {
			return;
		}

		for (String name : keySet) {
			QueryContributor queryContributor =
				_queryContributorServiceTrackerMap.getService(name);

			try {
				Optional<Query> optional = queryContributor.build(
					blueprint, parameterData, problemsHolderBuilder);

				if (!optional.isPresent()) {
					return;
				}

				ClauseContext clauseContext =
					queryContributor.getClauseContext();

				if (clauseContext.equals(ClauseContext.POST_FILTER)) {
					_addPostFilterClause(
						searchRequestBuilder, optional.get(),
						queryContributor.getOccur());
				}
				else if (clauseContext.equals(ClauseContext.QUERY)) {
					_addQueryClause(
						searchRequestBuilder, optional.get(),
						queryContributor.getOccur());
				}
				else if (clauseContext.equals(ClauseContext.RESCORE)) {
					_addRescoreClause(
						searchRequestBuilder, optional.get(), queryContributor);
				}
			}
			catch (Exception exception) {
				_log.error(exception);

				ProblemBuilder problemBuilder = _problemBuilders.unknownError(
					getClass().getName(), null, exception);

				problemsHolderBuilder.addProblem(problemBuilder.build());
			}
		}
	}

	private ClauseContext _getClauseContext(JSONObject jsonObject) {
		String context = jsonObject.getString("context");

		return ClauseContext.valueOf(StringUtil.toUpperCase(context));
	}

	private Occur _getOccur(JSONObject jsonObject) {
		String occur = jsonObject.getString("occur", "must");

		return Occur.valueOf(StringUtil.toUpperCase(occur));
	}

	private String _getOccurString(Occur occur) {
		if (occur.equals(Occur.FILTER)) {
			return "filter";
		}
		else if (occur.equals(Occur.MUST)) {
			return "must";
		}
		else if (occur.equals(Occur.MUST_NOT)) {
			return "must_not";
		}
		else if (occur.equals(Occur.SHOULD)) {
			return "should";
		}

		return null;
	}

	private boolean _isConditionsTrue(
		JSONObject jsonObject, ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		JSONObject conditionsJSONObject = jsonObject.getJSONObject(
			"conditions");

		if (conditionsJSONObject == null) {
			return true;
		}

		return _conditionsProcessor.processConditions(
			conditionsJSONObject, parameterData, null, problemsHolderBuilder);
	}

	private void _processClause(
		JSONObject jsonObject, SearchRequestBuilder searchRequestBuilder,
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<Query> optional = _clauseHelper.getQueryOptional(
			jsonObject.getJSONObject("query"), parameterData,
			problemsHolderBuilder);

		optional.ifPresent(
			query -> {
				ClauseContext clauseContext = _getClauseContext(jsonObject);

				Occur occur = _getOccur(jsonObject);

				if (clauseContext.equals(ClauseContext.POST_FILTER)) {
					_addPostFilterClause(searchRequestBuilder, query, occur);
				}
				else if (clauseContext.equals(ClauseContext.QUERY)) {
					_addQueryClause(searchRequestBuilder, query, occur);
				}
				else if (clauseContext.equals(ClauseContext.RESCORE)) {
					_addRescoreClause(searchRequestBuilder, query, jsonObject);
				}
			});
	}

	private void _processClauses(
		SearchRequestBuilder searchRequestBuilder, JSONArray jsonArray,
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		for (int j = 0; j < jsonArray.length(); j++) {
			_processClause(
				jsonArray.getJSONObject(j), searchRequestBuilder, parameterData,
				problemsHolderBuilder);
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
	private ProblemBuilders _problemBuilders;

	@Reference
	private Queries _queries;

	private ServiceTrackerMap<String, QueryContributor>
		_queryContributorServiceTrackerMap;

	@Reference
	private RescoreBuilderFactory _rescoreBuilderFactory;

}