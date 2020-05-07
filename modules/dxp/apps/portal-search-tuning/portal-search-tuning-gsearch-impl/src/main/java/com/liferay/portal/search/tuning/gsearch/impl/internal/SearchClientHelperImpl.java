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

package com.liferay.portal.search.tuning.gsearch.impl.internal;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.json.keys.AdvancedConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.json.keys.CommonConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.json.keys.SearchConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.json.values.RequestParameterRoles;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SearchConfiguration;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationService;
import com.liferay.portal.search.tuning.gsearch.context.SearchRequestContext;
import com.liferay.portal.search.tuning.gsearch.exception.SearchRequestDataException;
import com.liferay.portal.search.tuning.gsearch.impl.internal.executor.SearchExecutor;
import com.liferay.portal.search.tuning.gsearch.impl.internal.keywords.KeywordsProcessor;
import com.liferay.portal.search.tuning.gsearch.impl.internal.parameter.SearchParameterDataImpl;
import com.liferay.portal.search.tuning.gsearch.impl.internal.parameter.contributor.ParameterContributors;
import com.liferay.portal.search.tuning.gsearch.impl.internal.request.parameter.RequestParameterBuilder;
import com.liferay.portal.search.tuning.gsearch.impl.internal.results.ResultsBuilder;
import com.liferay.portal.search.tuning.gsearch.impl.internal.searchrequest.SearchRequestContextBuilder;
import com.liferay.portal.search.tuning.gsearch.impl.internal.searchrequest.SearchRequestDataBuilder;
import com.liferay.portal.search.tuning.gsearch.impl.util.GSearchJsonUtil;
import com.liferay.portal.search.tuning.gsearch.parameter.Parameter;
import com.liferay.portal.search.tuning.gsearch.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.gsearch.parameter.StringParameter;
import com.liferay.portal.search.tuning.gsearch.results.ResultAttributes;
import com.liferay.portal.search.tuning.gsearch.searchrequest.SearchRequestData;
import com.liferay.portal.search.tuning.gsearch.util.SearchClientHelper;

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
			HttpServletRequest httpServletRequest, long searchConfigurationId)
		throws JSONException, PortalException {

		JSONObject searchConfigurationJsonObject = _getSearchConfiguration(
			searchConfigurationId);

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		SearchParameterData searchParameterData =
			_requestParameterBuilder.build(
				httpServletRequest, searchConfigurationJsonObject);

		_parameterContributors.contribute(
			httpServletRequest, searchParameterData);
		
		return _getSearchRequestContext(
			searchConfigurationJsonObject, searchParameterData,
			themeDisplay.getLocale(), themeDisplay.getCompanyId(),
			themeDisplay.getUserId(), searchConfigurationId);
	}

	@Override
	public SearchRequestContext getSearchRequestContext(
			SearchContext searchContext, long searchConfigurationId)
		throws JSONException, PortalException {

		JSONObject searchConfigurationJsonObject = _getSearchConfiguration(
			searchConfigurationId);

		SearchParameterData searchParameterData = new SearchParameterDataImpl();

		// TODO: TESTING
		
		StringParameter q = new StringParameter("q", "keywords", "${keywords.keywords}", 
				searchContext.getKeywords());
		searchParameterData.addParameter(q);
		
		_parameterContributors.contribute(searchContext, searchParameterData);

		return _getSearchRequestContext(
			searchConfigurationJsonObject, searchParameterData,
			searchContext.getLocale(), searchContext.getCompanyId(),
			searchContext.getUserId(), searchConfigurationId);
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
		ResultAttributes resultAttributes) {

		return _resultsBuilder.build(
			searchRequestContext, searchResponse, resultAttributes);
	}

	@Override
	public JSONObject search(
			HttpServletRequest httpServletRequest,
			ResultAttributes resultAttributes, long searchConfigurationId)
		throws PortalException, SearchRequestDataException {

		SearchRequestContext searchRequestContext = getSearchRequestContext(
			httpServletRequest, searchConfigurationId);

		SearchRequestData searchRequestData = _searchRequestDataBuilder.build(
			searchRequestContext);

		SearchSearchResponse searchResponse = _searchExecutor.execute(
			searchRequestContext, searchRequestData);

		return _resultsBuilder.build(
			searchRequestContext, searchResponse, resultAttributes);
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

	private JSONObject _getSearchConfiguration(long searchConfigurationId)
		throws JSONException, PortalException {

		SearchConfiguration searchConfiguration =
			_searchConfigurationService.getSearchConfiguration(
				searchConfigurationId);

		String configurationString = searchConfiguration.getConfiguration();

		return JSONFactoryUtil.createJSONObject(configurationString);
	}

	private SearchRequestContext _getSearchRequestContext(
			JSONObject searchConfigurationJsonObject,
			SearchParameterData searchParameterData, Locale locale,
			long companyId, long userId, long searchConfigurationId)
		throws PortalException {

		SearchRequestContextBuilder searchRequestContextBuilder =
			new SearchRequestContextBuilder();

		JSONArray aggregationConfigurationJsonArray =
			searchConfigurationJsonObject.getJSONArray(
				SearchConfigurationKeys.AGGREGATION_CONFIGURATION.getJsonKey());

		searchRequestContextBuilder.aggregationConfiguration(
			aggregationConfigurationJsonArray);

		JSONArray clauseConfigurationJsonArray =
			searchConfigurationJsonObject.getJSONArray(
				SearchConfigurationKeys.CLAUSE_CONFIGURATION.getJsonKey());

		searchRequestContextBuilder.clauseConfiguration(
			clauseConfigurationJsonArray);

		searchRequestContextBuilder.companyId(companyId);

		JSONArray excludeQueryContributorsJsonArray =
			searchConfigurationJsonObject.getJSONArray(
				AdvancedConfigurationKeys.EXCLUDE_QUERY_CONTRIBUTORS.
					getJsonKey());

		List<String> excludeQueryContributors =
			GSearchJsonUtil.jsonArrayToStringList(
				excludeQueryContributorsJsonArray);

		searchRequestContextBuilder.excludeQueryContributors(
			excludeQueryContributors);

		JSONArray excludeQueryPostProcessorsJsonArray =
			searchConfigurationJsonObject.getJSONArray(
				AdvancedConfigurationKeys.EXCLUDE_QUERY_POST_PROCESSORS.
					getJsonKey());

		List<String> excludeQueryPostProcessor =
			GSearchJsonUtil.jsonArrayToStringList(
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
				searchConfigurationJsonObject.get(
					AdvancedConfigurationKeys.FETCH_SOURCE.getJsonKey()))) {

			searchRequestContextBuilder.fetchSource(
				searchConfigurationJsonObject.getBoolean(
					AdvancedConfigurationKeys.FETCH_SOURCE.getJsonKey()));
		}

		if (Validator.isNotNull(
				searchConfigurationJsonObject.get(
					AdvancedConfigurationKeys.SOURCE_EXCLUDES.getJsonKey()))) {

			String[] fetchSourceExcludes =
				GSearchJsonUtil.jsonArrayToStringArray(
					searchConfigurationJsonObject.getJSONArray(
						AdvancedConfigurationKeys.SOURCE_EXCLUDES.
							getJsonKey()));

			searchRequestContextBuilder.fetchSourceExcludes(
				fetchSourceExcludes);
		}

		if (Validator.isNotNull(
				searchConfigurationJsonObject.get(
					AdvancedConfigurationKeys.SOURCE_INCLUDES.getJsonKey()))) {

			String[] fetchSourceIncludes =
				GSearchJsonUtil.jsonArrayToStringArray(
					searchConfigurationJsonObject.getJSONArray(
						AdvancedConfigurationKeys.SOURCE_INCLUDES.
							getJsonKey()));

			searchRequestContextBuilder.fetchSourceExcludes(
				fetchSourceIncludes);
		}

		JSONObject highlightConfigurationJsonObject =
			searchConfigurationJsonObject.getJSONObject(
				SearchConfigurationKeys.HIGHLIGHT_CONFIGURATION.getJsonKey());

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
			searchConfigurationJsonObject.getJSONObject(
				SearchConfigurationKeys.KEYWORD_INDEXING_CONFIGURATION.
					getJsonKey());

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

		JSONObject keywordSuggestionsConfigurationJsonObject =
			searchConfigurationJsonObject.getJSONObject(
				SearchConfigurationKeys.KEYWORD_SUGGESTIONS_CONFIGURATION.
					getJsonKey());

		searchRequestContextBuilder.keywordSuggestionsConfiguration(
			keywordSuggestionsConfigurationJsonObject);

		searchRequestContextBuilder.locale(locale);

		searchRequestContextBuilder.searchConfigurationId(
			searchConfigurationId);

		searchRequestContextBuilder.searchParameterData(searchParameterData);

		int size = searchConfigurationJsonObject.getInt(
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

		JSONArray sortConfigurationJsonArray =
			searchConfigurationJsonObject.getJSONArray(
				SearchConfigurationKeys.SORT_CONFIGURATION.getJsonKey());

		searchRequestContextBuilder.sortConfiguration(
			sortConfigurationJsonArray);

		JSONArray spellCheckerConfigurationJsonArray =
			searchConfigurationJsonObject.getJSONArray(
				SearchConfigurationKeys.SPELLCHECKER_CONFIGURATION.
					getJsonKey());

		searchRequestContextBuilder.spellCheckerConfiguration(
			spellCheckerConfigurationJsonArray);

		searchRequestContextBuilder.userId(userId);

		return searchRequestContextBuilder.build();
	}

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private KeywordsProcessor _keywordsProcessor;

	@Reference
	private ParameterContributors _parameterContributors;

	@Reference
	private RequestParameterBuilder _requestParameterBuilder;

	@Reference
	private ResultsBuilder _resultsBuilder;

	@Reference
	private SearchConfigurationService _searchConfigurationService;

	@Reference
	private SearchExecutor _searchExecutor;

	@Reference
	private SearchRequestDataBuilder _searchRequestDataBuilder;

}