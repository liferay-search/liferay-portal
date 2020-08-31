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

package com.liferay.portal.search.tuning.blueprints.engine.internal.executor;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.Aggregation;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.filter.ComplexQueryBuilderFactory;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.rescore.Rescore;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.sort.Sort;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.HighlightingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.constants.SearchRequestAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.searchrequest.SearchRequestData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.query.QueryPostProcessor;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SearchExecutor.class)
public class SearchExecutorImpl implements SearchExecutor {

	@Override
	public SearchSearchResponse execute(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData) {

		SearchRequest searchRequest2 = searchRequestData.getSearchRequestBuilder().build();

		SearchSearchRequest searchRequest = new SearchSearchRequest();

		List<Aggregation> aggregations = searchRequestData.getAggregations();

		if (!aggregations.isEmpty()) {
			_setAggregations(searchRequest, aggregations);
		}

		searchRequest.setExplain(_isExplain(searchRequestContext));

		searchRequest.setIncludeResponseString(
			_isIncludeResponseString(searchRequestContext));

		searchRequest.setLocale(searchRequestContext.getLocale());

		// Sorts cannot be used with rescorer (See Elasticsearch documentation)

		List<Sort> sorts = searchRequestData.getSorts();

		List<Rescore> rescores = searchRequestData.getRescores();

		if (!rescores.isEmpty()) {
			searchRequest.setRescores(rescores);
		}
		else if (!sorts.isEmpty()) {
			Stream<Sort> sortsStream = sorts.stream();

			searchRequest.addSorts(sortsStream.toArray(Sort[]::new));
		}

		BooleanQuery postFilterQuery = searchRequestData.getPostFilterQuery();

		if (postFilterQuery.hasClauses()) {
			searchRequest.setPostFilterQuery(postFilterQuery);
		}

		BooleanQuery booleanQuery = _queries.booleanQuery();

		Query query = _complexQueryBuilderFactory.builder()
			.addParts(searchRequest2.getComplexQueryParts())
			.root(booleanQuery)
			.build();

		if (booleanQuery.hasClauses()) {
			searchRequest.setQuery(query);
		}

		JSONObject blueprintJsonObject = searchRequestContext.getBlueprint();

		_setHighlight(blueprintJsonObject, searchRequest);

		_setIndexNames(blueprintJsonObject, searchRequest);

		searchRequest.setSize(_blueprintHelper.getSize(blueprintJsonObject));

		searchRequest.setStart(searchRequestContext.getFrom());

		SearchSearchResponse searchResponse = _searchEngineAdapter.execute(
			searchRequest);

		if (_log.isDebugEnabled() && (searchResponse != null)) {
			_log.debug(
				"Request string: " + searchResponse.getSearchRequestString());
			_log.debug("Hits: " + searchResponse.getCount());
			_log.debug("Time:" + searchResponse.getExecutionTime());
		}

		_executeQueryPostProcessors(searchRequestContext, searchResponse);

		return searchResponse;
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void registerQueryPostProcessor(
		QueryPostProcessor queryPostProcessor, Map<String, Object> properties) {

		String name = (String)properties.get("name");

		if (Validator.isBlank(name)) {
			if (_log.isWarnEnabled()) {
				Class<?> clazz = queryPostProcessor.getClass();

				_log.warn(
					"Unable to add query post processor " + clazz.getName() +
						". Name property empty.");
			}
		}

		_queryPostProcessors.put(name, queryPostProcessor);
	}

	protected void unregisterQueryPostProcessor(
		QueryPostProcessor queryPostProcessor, Map<String, Object> properties) {

		String name = (String)properties.get("name");

		if (Validator.isBlank(name)) {
			return;
		}

		_queryPostProcessors.remove(name);
	}

	private void _executeQueryPostProcessors(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse) {

		if (_log.isDebugEnabled()) {
			_log.debug("Executing query post processors");
		}

		if (_queryPostProcessors == null) {
			return;
		}

		List<String> excludedQueryPostProcessors =
			_getExcludedQueryPostProcessors(
				searchRequestContext.getBlueprint());

		Stream<String> stream = excludedQueryPostProcessors.stream();

		if (stream.anyMatch(s -> s.equals("*"))) {
			return;
		}

		for (Map.Entry<String, QueryPostProcessor> entry :
				_queryPostProcessors.entrySet()) {

			QueryPostProcessor queryPostProcessor = entry.getValue();

			Stream<String> stream2 = excludedQueryPostProcessors.stream();

			if (stream2.anyMatch(s -> s.equals(entry.getKey()))) {
				if (_log.isDebugEnabled()) {
					Class<?> clazz = queryPostProcessor.getClass();

					_log.debug(clazz.getName() + " is excluded.");
				}

				continue;
			}

			queryPostProcessor.process(searchRequestContext, searchResponse);
		}
	}

	private List<String> _getExcludedQueryPostProcessors(
		JSONObject blueprintJsonObject) {

		Optional<List<String>> excludedQueryPostProcessorsOptional =
			_blueprintHelper.getExcludedQueryPostProcessorsOptional(
				blueprintJsonObject);

		if (excludedQueryPostProcessorsOptional.isPresent()) {
			return excludedQueryPostProcessorsOptional.get();
		}

		return new ArrayList<>();
	}

	private boolean _isExplain(SearchRequestContext searchRequestContext) {
		Map<String, Object> searchRequestAttributes =
			searchRequestContext.getAttributes();

		return GetterUtil.getBoolean(
			searchRequestAttributes.get(SearchRequestAttributes.EXPLAIN));
	}

	private boolean _isIncludeResponseString(
		SearchRequestContext searchRequestContext) {

		Map<String, Object> searchRequestAttributes =
			searchRequestContext.getAttributes();

		return GetterUtil.getBoolean(
			searchRequestAttributes.get(
				SearchRequestAttributes.INCLUDE_RESPONSE_STRING));
	}

	private void _setAggregations(
		SearchSearchRequest searchRequest, List<Aggregation> aggregations) {

		for (Aggregation aggregation : aggregations) {
			searchRequest.addAggregation(aggregation);
		}
	}

	private void _setHighlight(
		JSONObject blueprintJsonObject, SearchSearchRequest searchRequest) {

		Optional<JSONObject> highlightConfigurationJsonObjectOptional =
			_blueprintHelper.getHighlightConfigurationOptional(
				blueprintJsonObject);

		if (!highlightConfigurationJsonObjectOptional.isPresent()) {
			return;
		}

		JSONObject highlightConfigurationJsonObject =
			highlightConfigurationJsonObjectOptional.get();

		if (highlightConfigurationJsonObject.has(
				HighlightingConfigurationKeys.ENABLED.getJsonKey())) {

			searchRequest.setHighlightEnabled(
				highlightConfigurationJsonObject.getBoolean(
					HighlightingConfigurationKeys.ENABLED.getJsonKey()));
		}

		if (highlightConfigurationJsonObject.has(
				HighlightingConfigurationKeys.FRAGMENT_SIZE.getJsonKey())) {

			searchRequest.setHighlightFragmentSize(
				highlightConfigurationJsonObject.getInt(
					HighlightingConfigurationKeys.FRAGMENT_SIZE.getJsonKey()));
		}

		if (highlightConfigurationJsonObject.has(
				HighlightingConfigurationKeys.SNIPPET_SIZE.getJsonKey())) {

			searchRequest.setHighlightSnippetSize(
				highlightConfigurationJsonObject.getInt(
					HighlightingConfigurationKeys.SNIPPET_SIZE.getJsonKey()));
		}

		if (highlightConfigurationJsonObject.has(
				HighlightingConfigurationKeys.REQUIRE_FIELD_MATCH.
					getJsonKey())) {

			searchRequest.setHighlightRequireFieldMatch(
				highlightConfigurationJsonObject.getBoolean(
					HighlightingConfigurationKeys.REQUIRE_FIELD_MATCH.
						getJsonKey()));
		}

		if (highlightConfigurationJsonObject.has(
				HighlightingConfigurationKeys.FIELD_NAMES.getJsonKey())) {

			JSONArray fieldNamesJsonArray =
				highlightConfigurationJsonObject.getJSONArray(
					HighlightingConfigurationKeys.FIELD_NAMES.getJsonKey());

			String[] fieldNames = JSONUtil.toStringArray(fieldNamesJsonArray);

			searchRequest.setHighlightFieldNames(fieldNames);
		}
	}

	private void _setIndexNames(
		JSONObject blueprintJsonObject, SearchSearchRequest searchRequest) {

		Optional<String[]> indexNamesOptional =
			_blueprintHelper.getIndexNamesOptional(blueprintJsonObject);

		if (indexNamesOptional.isPresent()) {
			searchRequest.setIndexNames(indexNamesOptional.get());
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SearchExecutorImpl.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private ComplexQueryBuilderFactory _complexQueryBuilderFactory;

	@Reference
	private Queries _queries;

	private volatile Map<String, QueryPostProcessor> _queryPostProcessors =
		new HashMap<>();

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}