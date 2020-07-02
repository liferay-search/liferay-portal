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
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.AggregationConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.ClauseConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.HighlightConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.KeywordIndexingConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.KeywordSuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.KeywordSuggestionsConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.ParameterRoles;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SearchConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SortConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SpellCheckerConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SearchConfiguration;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationService;
import com.liferay.portal.search.tuning.gsearch.context.SearchRequestContext;
import com.liferay.portal.search.tuning.gsearch.exception.SearchRequestDataException;
import com.liferay.portal.search.tuning.gsearch.impl.internal.executor.SearchExecutor;
import com.liferay.portal.search.tuning.gsearch.impl.internal.keywords.KeywordsProcessor;
import com.liferay.portal.search.tuning.gsearch.impl.internal.parameter.SearchParameterDataImpl;
import com.liferay.portal.search.tuning.gsearch.impl.internal.parameter.contributor.ParameterContributorsImpl;
import com.liferay.portal.search.tuning.gsearch.impl.internal.request.parameter.RequestParameterBuilder;
import com.liferay.portal.search.tuning.gsearch.impl.internal.results.ResultsBuilder;
import com.liferay.portal.search.tuning.gsearch.impl.internal.searchrequest.SearchRequestContextBuilder;
import com.liferay.portal.search.tuning.gsearch.impl.internal.searchrequest.SearchRequestDataBuilder;
import com.liferay.portal.search.tuning.gsearch.parameter.Parameter;
import com.liferay.portal.search.tuning.gsearch.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.gsearch.results.ResultAttributes;
import com.liferay.portal.search.tuning.gsearch.searchrequest.SearchRequestData;
import com.liferay.portal.search.tuning.gsearch.util.GSearchJsonUtil;
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
				AggregationConfigurationKeys.AGGREGATION_CONFIGURATION);

		searchRequestContextBuilder.aggregationConfiguration(
			aggregationConfigurationJsonArray);

		JSONArray clauseConfigurationJsonArray =
			searchConfigurationJsonObject.getJSONArray(
				ClauseConfigurationKeys.CLAUSE_CONFIGURATION);

		searchRequestContextBuilder.clauseConfiguration(
			clauseConfigurationJsonArray);

		searchRequestContextBuilder.companyId(companyId);

		JSONArray excludeQueryContributorsJsonArray =
			searchConfigurationJsonObject.getJSONArray(
				SearchConfigurationKeys.EXCLUDE_QUERY_CONTRIBUTORS);

		List<String> excludeQueryContributors =
			GSearchJsonUtil.jsonArrayToStringList(
				excludeQueryContributorsJsonArray);

		searchRequestContextBuilder.excludeQueryContributors(
			excludeQueryContributors);

		JSONArray excludeQueryPostProcessorsJsonArray =
			searchConfigurationJsonObject.getJSONArray(
				SearchConfigurationKeys.EXCLUDE_QUERY_POST_PROCESSORS);

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
					SearchConfigurationKeys.FETCH_SOURCE))) {

			searchRequestContextBuilder.fetchSource(
				searchConfigurationJsonObject.getBoolean(
					SearchConfigurationKeys.FETCH_SOURCE));
		}

		if (Validator.isNotNull(
				searchConfigurationJsonObject.get(
					SearchConfigurationKeys.SOURCE_EXCLUDES))) {

			String[] fetchSourceExcludes =
				GSearchJsonUtil.jsonArrayToStringArray(
					searchConfigurationJsonObject.getJSONArray(
						SearchConfigurationKeys.SOURCE_EXCLUDES));

			searchRequestContextBuilder.fetchSourceExcludes(
				fetchSourceExcludes);
		}

		if (Validator.isNotNull(
				searchConfigurationJsonObject.get(
					SearchConfigurationKeys.SOURCE_INCLUDES))) {

			String[] fetchSourceIncludes =
				GSearchJsonUtil.jsonArrayToStringArray(
					searchConfigurationJsonObject.getJSONArray(
						SearchConfigurationKeys.SOURCE_INCLUDES));

			searchRequestContextBuilder.fetchSourceExcludes(
				fetchSourceIncludes);
		}

		JSONObject highlightConfigurationJsonObject =
			searchConfigurationJsonObject.getJSONObject(
				HighlightConfigurationKeys.HIGHLIGHT_CONFIGURATION);

		searchRequestContextBuilder.highlightConfiguration(
			highlightConfigurationJsonObject);

		Optional<Parameter> includeResponseStringOptional =
			searchParameterData.getByRole(
				ParameterRoles.INCLUDE_RESPONSE_STRING);

		if (includeResponseStringOptional.isPresent()) {
			searchRequestContextBuilder.includeResponseString(
				GetterUtil.getBoolean(
					includeResponseStringOptional.get(
					).getValue()));
		}

		searchRequestContextBuilder.indexNames(_getIndexNames(companyId));

		JSONObject keywordIndexingConfigurationJsonObject =
			searchConfigurationJsonObject.getJSONObject(
				KeywordIndexingConfigurationKeys.
					KEYWORDS_INDEXING_CONFIGURATION);

		searchRequestContextBuilder.keywordIndexingConfiguration(
			keywordIndexingConfigurationJsonObject);

		Optional<Parameter> keywordsOptional = searchParameterData.getByRole(
			ParameterRoles.KEYWORDS);

		if (keywordsOptional.isPresent()) {
			String keywords = GetterUtil.getString(
				keywordsOptional.get(
				).getValue());

			String keywordsCleaned = _keywordsProcessor.clean(keywords);

			searchRequestContextBuilder.keywords(keywordsCleaned);
			searchRequestContextBuilder.rawKeywords(keywords);
		}

		JSONArray keywordSuggesterConfigurationJsonArray =
			searchConfigurationJsonObject.getJSONArray(
				KeywordSuggesterConfigurationKeys.
					KEYWORDS_SUGGESTER_CONFIGURATION);

		searchRequestContextBuilder.keywordSuggesterConfiguration(
			keywordSuggesterConfigurationJsonArray);

		JSONObject keywordSuggestionsConfigurationJsonObject =
			searchConfigurationJsonObject.getJSONObject(
				KeywordSuggestionsConfigurationKeys.
					KEYWORDS_SUGGESTIONS_CONFIGURATION);

		searchRequestContextBuilder.keywordSuggestionsConfiguration(
			keywordSuggestionsConfigurationJsonObject);

		searchRequestContextBuilder.locale(locale);

		searchRequestContextBuilder.searchConfigurationId(
			searchConfigurationId);

		searchRequestContextBuilder.searchParameterData(searchParameterData);

		int size = searchConfigurationJsonObject.getInt(
			SearchConfigurationKeys.SIZE, 10);
		searchRequestContextBuilder.size(size);

		Optional<Parameter> fromOptional = searchParameterData.getByRole(
			ParameterRoles.PAGE);

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
				SortConfigurationKeys.SORT_CONFIGURATION);

		searchRequestContextBuilder.sortConfiguration(
			sortConfigurationJsonArray);

		JSONArray spellCheckerConfigurationJsonArray =
			searchConfigurationJsonObject.getJSONArray(
				SpellCheckerConfigurationKeys.SPELLCHECKER_CONFIGURATION);

		searchRequestContextBuilder.spellCheckerConfiguration(
			spellCheckerConfigurationJsonArray);

		searchRequestContextBuilder.userId(userId);

		return searchRequestContextBuilder.build();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SearchClientHelperImpl.class);

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private KeywordsProcessor _keywordsProcessor;

	@Reference
	private ParameterContributorsImpl _parameterContributors;

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