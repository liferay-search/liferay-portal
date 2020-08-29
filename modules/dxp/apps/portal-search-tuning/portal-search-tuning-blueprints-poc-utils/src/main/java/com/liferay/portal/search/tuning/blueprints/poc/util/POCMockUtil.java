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

package com.liferay.portal.search.tuning.blueprints.poc.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.BlueprintKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.AdvancedConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.HighlightingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.QueryProcessingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.SearchResultsConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.SourceConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.aggregation.AggregationConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.aggregation.TermsAggregationBodyConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.requestparameter.CustomRequestParameterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.requestparameter.KeywordsConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.requestparameter.RequestParameterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.requestparameter.SortConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.KeywordIndexingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.KeywordSuggestionsConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.PhraseSuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.SuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.SuggestersConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.RequestParameterType;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.SuggesterType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * TESTING & MOCKING.
 *
 * TODO: TO BE REMOVED
 *
 * @author Petteri Karttunen
 *
 */
@Component(immediate = true, service = POCMockUtil.class)
public class POCMockUtil {
	
	public static JSONArray getFacetsParameterConfigurationMock() {

		// Facets

		JSONArray facetsConfigurationJsonArray =
			JSONFactoryUtil.createJSONArray();

		JSONObject facetConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		facetConfigurationJsonObject.put(
			"aggregation_name", "termsAggregation");
		facetConfigurationJsonObject.put("field", "assetTagNames.raw");
		facetConfigurationJsonObject.put("filter_mode", "PRE");
		facetConfigurationJsonObject.put("parameter_name", "assetTagNames");

		facetsConfigurationJsonArray.put(facetConfigurationJsonObject);

		return facetsConfigurationJsonArray;
	}


	public void mockAdvancedConfiguration(
		JSONObject bluePrintJsonObject, long companyId) {

		JSONObject advancedConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		// Search results.

		JSONObject searchResultsconfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		searchResultsconfigurationJsonObject.put(
			SearchResultsConfigurationKeys.PAGE_SIZE.getJsonKey(), 10);

		advancedConfigurationJsonObject.put(
			AdvancedConfigurationKeys.SEARCH_RESULTS.getJsonKey(),
			searchResultsconfigurationJsonObject);

		// Highlighting.

		JSONObject highlightingConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		highlightingConfigurationJsonObject.put(
			HighlightingConfigurationKeys.ENABLED.getJsonKey(), true);

		JSONArray highlightFieldsJsonArray = JSONFactoryUtil.createJSONArray();

		highlightFieldsJsonArray.put(
			Field.CONTENT + StringPool.UNDERLINE + "en_US");
		highlightFieldsJsonArray.put(
			Field.TITLE + StringPool.UNDERLINE + "en_US");
		highlightingConfigurationJsonObject.put(
			HighlightingConfigurationKeys.FIELD_NAMES.getJsonKey(),
			highlightFieldsJsonArray);

		highlightingConfigurationJsonObject.put(
			HighlightingConfigurationKeys.FRAGMENT_SIZE.getJsonKey(), 5);
		highlightingConfigurationJsonObject.put(
			HighlightingConfigurationKeys.SNIPPET_SIZE.getJsonKey(), 60);
		highlightingConfigurationJsonObject.put(
			HighlightingConfigurationKeys.REQUIRE_FIELD_MATCH.getJsonKey(),
			true);

		advancedConfigurationJsonObject.put(
			AdvancedConfigurationKeys.HIGHLIGHTING.getJsonKey(),
			highlightingConfigurationJsonObject);

		// Index names

		advancedConfigurationJsonObject.put(
			AdvancedConfigurationKeys.INDEX_NAMES.getJsonKey(),
			_stringArrayToJsonArray(_getIndexNames(companyId)));

		// Query processing.

		JSONObject queryProcessingConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		queryProcessingConfigurationJsonObject.put(
			QueryProcessingConfigurationKeys.EXCLUDE_QUERY_CONTRIBUTORS.
				getJsonKey(),
			"");
		queryProcessingConfigurationJsonObject.put(
			QueryProcessingConfigurationKeys.EXCLUDE_QUERY_POST_PROCESSORS.
				getJsonKey(),
			"");
		advancedConfigurationJsonObject.put(
			AdvancedConfigurationKeys.QUERY_PROCESSING.getJsonKey(),
			queryProcessingConfigurationJsonObject);

		// Source.

		JSONObject sourceConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		sourceConfigurationJsonObject.put(
			SourceConfigurationKeys.FETCH_SOURCE.getJsonKey(), true);
		sourceConfigurationJsonObject.put(
			SourceConfigurationKeys.SOURCE_EXCLUDES.getJsonKey(), "");
		sourceConfigurationJsonObject.put(
			SourceConfigurationKeys.SOURCE_INCLUDES.getJsonKey(), "");

		advancedConfigurationJsonObject.put(
			AdvancedConfigurationKeys.SOURCE.getJsonKey(),
			sourceConfigurationJsonObject);

		bluePrintJsonObject.put(
			BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
			advancedConfigurationJsonObject);
	}

	public void mockAggregationConfiguration(JSONObject bluePrintJsonObject) {
		JSONArray aggregationConfigurationJsonArray =
			JSONFactoryUtil.createJSONArray();

		JSONObject termAggregationConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		termAggregationConfigurationJsonObject.put(
			AggregationConfigurationKeys.ENABLED.getJsonKey(), true);
		termAggregationConfigurationJsonObject.put(
			AggregationConfigurationKeys.NAME.getJsonKey(), "termsAggregation");
		termAggregationConfigurationJsonObject.put(
			AggregationConfigurationKeys.TYPE.getJsonKey(), "terms");

		JSONObject bodyJsonObject = JSONFactoryUtil.createJSONObject();

		bodyJsonObject.put(
			TermsAggregationBodyConfigurationKeys.FIELD.getJsonKey(),
			"assetTagNames.raw");

		termAggregationConfigurationJsonObject.put(
			AggregationConfigurationKeys.BODY.getJsonKey(), bodyJsonObject);

		aggregationConfigurationJsonArray.put(
			termAggregationConfigurationJsonObject);

		bluePrintJsonObject.put(
			BlueprintKeys.AGGREGATION_CONFIGURATION.getJsonKey(),
			aggregationConfigurationJsonArray);
	}

	public JSONObject mockConfigurations(
		JSONObject bluePrintJsonObject, long companyId) {

		mockAdvancedConfiguration(bluePrintJsonObject, companyId);
		mockAggregationConfiguration(bluePrintJsonObject);
		mockRequestParameterConfiguration(bluePrintJsonObject);
		mockSuggesterConfiguration(bluePrintJsonObject);

		return bluePrintJsonObject;
	}

	public void mockRequestParameterConfiguration(
		JSONObject bluePrintJsonObject) {

		JSONObject requestParameterConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		// Keywords

		JSONObject keywordsConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		keywordsConfigurationJsonObject.put(
			KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey(), "q");
		requestParameterConfigurationJsonObject.put(
			RequestParameterConfigurationKeys.KEYWORDS.getJsonKey(),
			keywordsConfigurationJsonObject);

		// Paging

		JSONObject pagingConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		pagingConfigurationJsonObject.put(
			KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey(), "page");
		requestParameterConfigurationJsonObject.put(
			RequestParameterConfigurationKeys.PAGING.getJsonKey(),
			pagingConfigurationJsonObject);

		// Sorts

		JSONArray sortsConfigurationJsonArray =
			JSONFactoryUtil.createJSONArray();

		JSONObject sortConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		sortConfigurationJsonObject.put(
			SortConfigurationKeys.PARAMETER_NAME.getJsonKey(), "sort1");
		sortConfigurationJsonObject.put(
			SortConfigurationKeys.FIELD.getJsonKey(), "_score");
		sortConfigurationJsonObject.put(
			SortConfigurationKeys.PARAMETER_NAME.getJsonKey(), "sort2");
		sortConfigurationJsonObject.put(
			SortConfigurationKeys.PARAMETER_NAME.getJsonKey(), "title_en_US");

		sortsConfigurationJsonArray.put(sortConfigurationJsonObject);

		requestParameterConfigurationJsonObject.put(
			RequestParameterConfigurationKeys.SORTS.getJsonKey(),
			sortsConfigurationJsonArray);

		// Custom

		JSONArray customParameterConfigurationJsonArray =
			JSONFactoryUtil.createJSONArray();

		JSONObject customParameterConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		customParameterConfigurationJsonObject.put(
			CustomRequestParameterConfigurationKeys.PARAMETER_NAME.getJsonKey(),
			"dateFrom");
		customParameterConfigurationJsonObject.put(
			CustomRequestParameterConfigurationKeys.TYPE.getJsonKey(),
			RequestParameterType.DATE.getJsonValue());
		customParameterConfigurationJsonObject.put(
			CustomRequestParameterConfigurationKeys.DATE_FORMAT.getJsonKey(),
			"yyyy-MM-dd");

		customParameterConfigurationJsonArray.put(
			customParameterConfigurationJsonObject);

		requestParameterConfigurationJsonObject.put(
			RequestParameterConfigurationKeys.CUSTOM.getJsonKey(),
			customParameterConfigurationJsonArray);

		bluePrintJsonObject.put(
			BlueprintKeys.REQUEST_PARAMETER_CONFIGURATION.getJsonKey(),
			requestParameterConfigurationJsonObject);
	}

	public void mockSuggesterConfiguration(JSONObject bluePrintJsonObject) {
		JSONObject suggesterConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		JSONObject keywordIndexingConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		keywordIndexingConfigurationJsonObject.put(
			KeywordIndexingConfigurationKeys.ENABLED.getJsonKey(), true);
		keywordIndexingConfigurationJsonObject.put(
			KeywordIndexingConfigurationKeys.HITS_THRESHOLD.getJsonKey(), 2);

		// TODO: blacklist

		suggesterConfigurationJsonObject.put(
			SuggesterConfigurationKeys.KEYWORD_INDEXING.getJsonKey(),
			keywordIndexingConfigurationJsonObject);

		// Keyword suggestions

		JSONObject keywordSuggestionsConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		keywordSuggestionsConfigurationJsonObject.put(
			KeywordSuggestionsConfigurationKeys.ENABLED.getJsonKey(), true);
		keywordSuggestionsConfigurationJsonObject.put(
			KeywordSuggestionsConfigurationKeys.SIZE.getJsonKey(), true);

		JSONArray keywordSuggestersConfigurationJsonArray =
			JSONFactoryUtil.createJSONArray();

		JSONObject suggester1JsonObject = JSONFactoryUtil.createJSONObject();

		suggester1JsonObject.put(
			SuggestersConfigurationKeys.ENABLED.getJsonKey(), true);
		suggester1JsonObject.put(
			SuggestersConfigurationKeys.TYPE.getJsonKey(),
			SuggesterType.PHRASE);

		JSONObject suggester1ConfigurationJsonObject1 =
			JSONFactoryUtil.createJSONObject();

		suggester1ConfigurationJsonObject1.put(
			PhraseSuggesterConfigurationKeys.FIELD.getJsonKey(),
			"keywordSearch_en_US");
		suggester1ConfigurationJsonObject1.put(
			PhraseSuggesterConfigurationKeys.TEXT.getJsonKey(), "${q}");

		suggester1JsonObject.put(
			SuggestersConfigurationKeys.CONFIGURATION.getJsonKey(),
			suggester1ConfigurationJsonObject1);

		keywordSuggestersConfigurationJsonArray.put(suggester1JsonObject);

		keywordSuggestionsConfigurationJsonObject.put(
			KeywordSuggestionsConfigurationKeys.SUGGESTERS.getJsonKey(),
			keywordSuggestersConfigurationJsonArray);

		suggesterConfigurationJsonObject.put(
			SuggesterConfigurationKeys.KEYWORD_SUGGESTIONS.getJsonKey(),
			keywordSuggestionsConfigurationJsonObject);

		// TODO: spellchecking

		bluePrintJsonObject.put(
			BlueprintKeys.SUGGESTER_CONFIGURATION.getJsonKey(),
			suggesterConfigurationJsonObject);
	}

	private String[] _getIndexNames(long companyId) {
		return new String[] {_indexNameBuilder.getIndexName(companyId)};
	}
	
	private JSONArray _stringArrayToJsonArray(String[] arr) {
		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		if ((arr == null) || (arr.length == 0)) {
			return jsonArray;
		}

		for (String s : arr) {
			jsonArray.put(s);
		}

		return jsonArray;
	}

	@Reference
	private IndexNameBuilder _indexNameBuilder;

}