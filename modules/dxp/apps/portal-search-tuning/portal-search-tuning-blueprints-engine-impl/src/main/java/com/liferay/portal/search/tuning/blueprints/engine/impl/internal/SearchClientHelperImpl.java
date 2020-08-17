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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.AdvancedConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.BlueprintKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.CommonConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.RequestParameterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.RequestParameterRoles;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.exception.SearchRequestDataException;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.executor.SearchExecutor;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.keywords.KeywordsProcessor;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.parameter.contributor.ParameterContributors;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.request.parameter.RequestParameterBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.response.ResponseBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.searchrequest.SearchRequestContextBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.searchrequest.SearchRequestDataBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.util.JsonUtil;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.response.ResponseAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.searchrequest.SearchRequestData;
import com.liferay.portal.search.tuning.blueprints.engine.util.SearchClientHelper;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SearchClientHelper.class)
public class SearchClientHelperImpl implements SearchClientHelper {

	public SearchRequestContext getSearchRequestContext(
			HttpServletRequest httpServletRequest, long blueprintId)
		throws JSONException, PortalException {

		JSONObject blueprintJsonObject = _getBlueprint(blueprintId);

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		SearchParameterData searchParameterData =
			_requestParameterBuilder.build(
				httpServletRequest, blueprintJsonObject);

		_parameterContributors.contribute(
			httpServletRequest, searchParameterData);

		return _getSearchRequestContext(
			blueprintJsonObject, searchParameterData, themeDisplay.getLocale(),
			themeDisplay.getCompanyId(), themeDisplay.getUserId(), blueprintId);
	}

	@Override
	public SearchRequestContext getSearchRequestContext(
			SearchContext searchContext, long blueprintId)
		throws JSONException, PortalException {

		JSONObject blueprintJsonObject = _getBlueprint(blueprintId);

		SearchParameterData searchParameterData = new SearchParameterData();

		_parameterContributors.contribute(searchContext, searchParameterData);

		return _getSearchRequestContext(
			blueprintJsonObject, searchParameterData, searchContext.getLocale(),
			searchContext.getCompanyId(), searchContext.getUserId(),
			blueprintId);
	}

	@Override
	public SearchRequestData getSearchRequestData(
			SearchRequestContext searchRequestContext)
		throws SearchRequestDataException {

		return _searchRequestDataBuilder.build(searchRequestContext);
	}

	@Override
	public SearchSearchResponse getSearchResponse(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData) {

		return _searchExecutor.execute(searchRequestContext, searchRequestData);
	}

	@Override
	public JSONObject getSearchResults(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse,
		ResponseAttributes resultAttributes) {

		return _resultsBuilder.build(
			searchRequestContext, searchResponse, resultAttributes);
	}

	@Override
	public JSONObject search(
			HttpServletRequest httpServletRequest,
			ResponseAttributes resultAttributes, long blueprintId)
		throws PortalException, SearchRequestDataException {

		SearchRequestContext searchRequestContext = getSearchRequestContext(
			httpServletRequest, blueprintId);

		SearchRequestData searchRequestData = _searchRequestDataBuilder.build(
			searchRequestContext);

		SearchSearchResponse searchResponse = _searchExecutor.execute(
			searchRequestContext, searchRequestData);

		return _resultsBuilder.build(
			searchRequestContext, searchResponse, resultAttributes);
	}

	private JSONObject _getBlueprint(long blueprintId)
		throws JSONException, PortalException {

		Blueprint blueprint = _blueprintService.getBlueprint(blueprintId);

		String configurationString = blueprint.getConfiguration();

		// return JSONFactoryUtil.createJSONObject(configurationString);

		// TODO: TESTING ONLY

		JSONObject blueprintJsonObject = JSONFactoryUtil.createJSONObject(
			configurationString);

		// Request parameter configuration. See https://drive.google.com/drive/u/0/folders/1vyJHgMX0QnTBPF_SmXkAA6k6PnZUrdYG

		JSONArray parameterConfigurationJsonArray =
			JSONFactoryUtil.createJSONArray();
		JSONObject keywordParameterJsonObject =
			JSONFactoryUtil.createJSONObject();

		keywordParameterJsonObject.put(
			RequestParameterConfigurationKeys.PARAMETER_NAME.getJsonKey(), "q");
		keywordParameterJsonObject.put(
			RequestParameterConfigurationKeys.TYPE.getJsonKey(), "string");
		keywordParameterJsonObject.put(
			RequestParameterConfigurationKeys.ROLE.getJsonKey(), "keywords");

		parameterConfigurationJsonArray.put(keywordParameterJsonObject);

		blueprintJsonObject.put(
			BlueprintKeys.REQUEST_PARAMETER_CONFIGURATION.getJsonKey(),
			parameterConfigurationJsonArray);

		return blueprintJsonObject;
	}

	private int _getFrom(int size, int page) {
		if (page == 1) {
			return 0;
		}

		return (page - 1) * size;
	}

	private String[] _getIndexNames(long companyId) {
		return new String[] {_indexNameBuilder.getIndexName(companyId)};
	}

	private SearchRequestContext _getSearchRequestContext(
			JSONObject blueprintJsonObject,
			SearchParameterData searchParameterData, Locale locale,
			long companyId, long userId, long blueprintId)
		throws PortalException {

		SearchRequestContextBuilder searchRequestContextBuilder =
			new SearchRequestContextBuilder();

		JSONArray aggregationConfigurationJsonArray =
			blueprintJsonObject.getJSONArray(
				BlueprintKeys.AGGREGATION_CONFIGURATION.getJsonKey());

		searchRequestContextBuilder.aggregationConfiguration(
			aggregationConfigurationJsonArray);

		JSONArray clauseConfigurationJsonArray =
			blueprintJsonObject.getJSONArray(
				BlueprintKeys.CLAUSE_CONFIGURATION.getJsonKey());

		searchRequestContextBuilder.clauseConfiguration(
			clauseConfigurationJsonArray);

		searchRequestContextBuilder.companyId(companyId);

		JSONArray excludeQueryContributorsJsonArray =
			blueprintJsonObject.getJSONArray(
				AdvancedConfigurationKeys.EXCLUDE_QUERY_CONTRIBUTORS.
					getJsonKey());

		List<String> excludeQueryContributors = JsonUtil.jsonArrayToStringList(
			excludeQueryContributorsJsonArray);

		searchRequestContextBuilder.excludeQueryContributors(
			excludeQueryContributors);

		JSONArray excludeQueryPostProcessorsJsonArray =
			blueprintJsonObject.getJSONArray(
				AdvancedConfigurationKeys.EXCLUDE_QUERY_POST_PROCESSORS.
					getJsonKey());

		List<String> excludeQueryPostProcessor = JsonUtil.jsonArrayToStringList(
			excludeQueryPostProcessorsJsonArray);

		searchRequestContextBuilder.excludeQueryPostProcessors(
			excludeQueryPostProcessor);

		Optional<Parameter> explainOptional = searchParameterData.getByRole(
			"explain");

		if (explainOptional.isPresent()) {
			searchRequestContextBuilder.explain(
				GetterUtil.getBoolean(
					explainOptional.get(
					).getValue()));
		}

		if (Validator.isNotNull(
				blueprintJsonObject.get(
					AdvancedConfigurationKeys.FETCH_SOURCE.getJsonKey()))) {

			searchRequestContextBuilder.fetchSource(
				blueprintJsonObject.getBoolean(
					AdvancedConfigurationKeys.FETCH_SOURCE.getJsonKey()));
		}

		if (Validator.isNotNull(
				blueprintJsonObject.get(
					AdvancedConfigurationKeys.SOURCE_EXCLUDES.getJsonKey()))) {

			String[] fetchSourceExcludes = JsonUtil.jsonArrayToStringArray(
				blueprintJsonObject.getJSONArray(
					AdvancedConfigurationKeys.SOURCE_EXCLUDES.getJsonKey()));

			searchRequestContextBuilder.fetchSourceExcludes(
				fetchSourceExcludes);
		}

		if (Validator.isNotNull(
				blueprintJsonObject.get(
					AdvancedConfigurationKeys.SOURCE_INCLUDES.getJsonKey()))) {

			String[] fetchSourceIncludes = JsonUtil.jsonArrayToStringArray(
				blueprintJsonObject.getJSONArray(
					AdvancedConfigurationKeys.SOURCE_INCLUDES.getJsonKey()));

			searchRequestContextBuilder.fetchSourceExcludes(
				fetchSourceIncludes);
		}

		JSONObject highlightConfigurationJsonObject =
			blueprintJsonObject.getJSONObject(
				BlueprintKeys.HIGHLIGHT_CONFIGURATION.getJsonKey());

		searchRequestContextBuilder.highlightConfiguration(
			highlightConfigurationJsonObject);

		Optional<Parameter> includeResponseStringOptional =
			searchParameterData.getByRole(
				RequestParameterRoles.INCLUDE_RESPONSE_STRING.getJsonValue());

		if (includeResponseStringOptional.isPresent()) {
			searchRequestContextBuilder.includeResponseString(
				GetterUtil.getBoolean(
					includeResponseStringOptional.get(
					).getValue()));
		}

		searchRequestContextBuilder.indexNames(_getIndexNames(companyId));

		JSONObject keywordIndexingConfigurationJsonObject =
			blueprintJsonObject.getJSONObject(
				BlueprintKeys.KEYWORD_INDEXING_CONFIGURATION.getJsonKey());

		searchRequestContextBuilder.keywordIndexingConfiguration(
			keywordIndexingConfigurationJsonObject);

		Optional<Parameter> keywordsOptional = searchParameterData.getByRole(
			RequestParameterRoles.KEYWORDS.getJsonValue());

		if (keywordsOptional.isPresent()) {
			String keywords = GetterUtil.getString(
				keywordsOptional.get(
				).getValue());

			String keywordsCleaned = _keywordsProcessor.clean(keywords);

			searchRequestContextBuilder.keywords(keywordsCleaned);
			searchRequestContextBuilder.rawKeywords(keywords);
		}

		JSONObject keywordSuggesterConfigurationJsonObject =
			blueprintJsonObject.getJSONObject(
				BlueprintKeys.KEYWORD_SUGGESTER_CONFIGURATION.getJsonKey());

		searchRequestContextBuilder.keywordSuggesterConfiguration(
			keywordSuggesterConfigurationJsonObject);

		searchRequestContextBuilder.locale(locale);

		searchRequestContextBuilder.blueprintId(blueprintId);

		searchRequestContextBuilder.searchParameterData(searchParameterData);

		int size = blueprintJsonObject.getInt(
			CommonConfigurationKeys.SIZE.getJsonKey(), 10);
		searchRequestContextBuilder.size(size);

		Optional<Parameter> fromOptional = searchParameterData.getByRole(
			RequestParameterRoles.PAGE.getJsonValue());

		if (fromOptional.isPresent()) {
			int page = GetterUtil.getInteger(
				fromOptional.get(
				).getValue());
			searchRequestContextBuilder.from(_getFrom(size, page));
		}
		else {
			searchRequestContextBuilder.from(0);
		}

		JSONArray sortConfigurationJsonArray = blueprintJsonObject.getJSONArray(
			BlueprintKeys.SORT_CONFIGURATION.getJsonKey());

		searchRequestContextBuilder.sortConfiguration(
			sortConfigurationJsonArray);

		JSONObject spellCheckerConfigurationJsonObject =
			blueprintJsonObject.getJSONObject(
				BlueprintKeys.SPELLCHECKER_CONFIGURATION.getJsonKey());

		searchRequestContextBuilder.spellCheckerConfiguration(
			spellCheckerConfigurationJsonObject);

		searchRequestContextBuilder.userId(userId);

		return searchRequestContextBuilder.build();
	}

	@Reference
	private BlueprintService _blueprintService;

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private KeywordsProcessor _keywordsProcessor;

	@Reference
	private ParameterContributors _parameterContributors;

	@Reference
	private RequestParameterBuilder _requestParameterBuilder;

	@Reference
	private ResponseBuilder _resultsBuilder;

	@Reference
	private SearchExecutor _searchExecutor;

	@Reference
	private SearchRequestDataBuilder _searchRequestDataBuilder;

}