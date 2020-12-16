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

package com.liferay.portal.search.tuning.blueprints.test.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.BlueprintKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.AdvancedConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.HighlightingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.QueryProcessingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.SourceConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.aggregation.AggregationConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.aggregation.TermsAggregationBodyConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.parameter.CustomParameterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.parameter.KeywordsConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.parameter.ParameterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.query.ClauseConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.sort.SortConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.PhraseSuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.SuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.ClauseContext;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.Occur;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.ParameterType;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.SuggesterType;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;

import org.osgi.service.component.annotations.Component;

/**
 * TESTING & MOCKING.
 *
 * @author Petteri Karttunen
 *
 */
@Component(immediate = true, service = BlueprintJSONConfigurationUtil.class)
public class BlueprintJSONConfigurationUtil {

	public static void mockFacetsConfiguration(JSONObject bluePrintJsonObject) {

		// Facets

		JSONArray facetsConfigurationJsonArray =
			JSONFactoryUtil.createJSONArray();

		JSONObject jsonObject1 = JSONFactoryUtil.createJSONObject();

		jsonObject1.put("field", "assetTagNames.raw");
		jsonObject1.put("filter_mode", "PRE");
		jsonObject1.put("label", "tag");
		jsonObject1.put("multi_value_operator", "AND");
		jsonObject1.put("parameter_name", "tag");

		facetsConfigurationJsonArray.put(jsonObject1);

		JSONObject jsonObject2 = JSONFactoryUtil.createJSONObject();

		jsonObject2.put("field", "userName");
		jsonObject2.put("filter_mode", "PRE");
		jsonObject2.put("label", "user");
		jsonObject2.put("parameter_name", "userName");

		facetsConfigurationJsonArray.put(jsonObject2);

		JSONObject jsonObject3 = JSONFactoryUtil.createJSONObject();

		jsonObject3.put("field", "ddmStructureKey");
		jsonObject3.put("filter_mode", "PRE");
		jsonObject3.put("handler", "ddm_structure_name");
		jsonObject3.put("parameter_name", "ddmStructureKey");

		facetsConfigurationJsonArray.put(jsonObject3);

		JSONObject jsonObject4 = JSONFactoryUtil.createJSONObject();

		jsonObject4.put("field", "assetCategoryIds");
		jsonObject4.put("filter_mode", "POST");
		jsonObject4.put("handler", "category_tree");
		jsonObject4.put("label", "category-in-a-tree");
		jsonObject4.put("parameter_name", "treeTest");

		JSONObject jsonObject41 = JSONFactoryUtil.createJSONObject();

		jsonObject41.put("root_vocabulary_id", "43604");

		jsonObject4.put("handler_parameters", jsonObject41);

		facetsConfigurationJsonArray.put(jsonObject4);

		JSONObject jsonObject5 = JSONFactoryUtil.createJSONObject();

		jsonObject5.put("field", "assetCategoryIds");
		jsonObject5.put("filter_mode", "PRE");
		jsonObject5.put("handler", "category");
		jsonObject5.put("label", "category");
		jsonObject5.put("parameter_name", "categoryId");

		facetsConfigurationJsonArray.put(jsonObject5);

		bluePrintJsonObject.put(
			"facet_configuration", facetsConfigurationJsonArray);
	}

	public static void mockQueryConfiguration(JSONObject jsonObject) {
		JSONArray queryConfigurationJSONArray =
			JSONFactoryUtil.createJSONArray();

		// Fragment

		JSONObject fragmentJSONObject = JSONFactoryUtil.createJSONObject();

		// Fragment / clauses array

		JSONArray clausesJSONArray = JSONFactoryUtil.createJSONArray();

		// Fragment / clauses array / clause

		JSONObject clauseJSONObject = JSONFactoryUtil.createJSONObject();

		clauseJSONObject.put(
			ClauseConfigurationKeys.OCCUR.getJsonKey(),
			Occur.MUST.getjsonValue());
		clauseJSONObject.put(
			ClauseConfigurationKeys.CONTEXT.getJsonKey(),
			ClauseContext.QUERY.getJsonValue());
		clauseJSONObject.put(
			ClauseConfigurationKeys.TYPE.getJsonKey(), "wrapper");

		// Fragment / clauses array / clause / query body

		JSONObject queryJSONObject = JSONFactoryUtil.createJSONObject();
		JSONObject queryInnerJSONObject = JSONFactoryUtil.createJSONObject();
		JSONObject matchQueryJSONObject = JSONFactoryUtil.createJSONObject();
		JSONObject matchQueryInnerJSONObject =
			JSONFactoryUtil.createJSONObject();

		matchQueryInnerJSONObject.put("operator", "or");
		matchQueryInnerJSONObject.put("query", "${keywords}");

		matchQueryJSONObject.put("title_en_US", matchQueryInnerJSONObject);

		queryInnerJSONObject.put("match", matchQueryJSONObject);

		queryJSONObject.put("query", queryInnerJSONObject);

		clauseJSONObject.put("query", queryJSONObject);

		clausesJSONArray.put(clauseJSONObject);

		fragmentJSONObject.put("clauses", clausesJSONArray);

		queryConfigurationJSONArray.put(fragmentJSONObject);
	}

	public void mockAdvancedConfiguration(
		JSONObject bluePrintJsonObject, long companyId) {

		JSONObject advancedConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		// Model indexer classes

		JSONArray indexersJsonArray = JSONFactoryUtil.createJSONArray();

		/*
				indexersJsonArray.put("com.liferay.commerce.product.model.CPDefinition");
				indexersJsonArray.put("com.liferay.wiki.model.WikiPage");
				indexersJsonArray.put("com.liferay.blogs.model.BlogsEntry");
				indexersJsonArray.put("com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecord");
		*/
		indexersJsonArray.put("com.liferay.journal.model.JournalArticle");
		/*
		indexersJsonArray.put("com.liferay.bookmarks.model.BookmarksEntry");
		indexersJsonArray.put("com.liferay.journal.model.JournalFolder");
		indexersJsonArray.put("com.liferay.calendar.model.CalendarBooking");
		indexersJsonArray.put("com.liferay.document.library.kernel.model.DLFileEntry");
		indexersJsonArray.put("com.liferay.portal.kernel.model.User");
		indexersJsonArray.put("com.liferay.bookmarks.model.BookmarksFolder");
		indexersJsonArray.put("com.liferay.document.library.kernel.model.DLFolder");
		indexersJsonArray.put("com.liferay.portal.kernel.model.Layout");
		indexersJsonArray.put("com.liferay.dynamic.data.lists.model.DDLRecord");
		indexersJsonArray.put("com.liferay.message.boards.model.MBMessage");
		indexersJsonArray.put( "com.liferay.knowledge.base.model.KBArticle");
		*/

		advancedConfigurationJsonObject.put(
			AdvancedConfigurationKeys.ENTRY_CLASS_NAMES.getJsonKey(),
			indexersJsonArray);

		// Page size

		advancedConfigurationJsonObject.put(
			AdvancedConfigurationKeys.PAGE_SIZE.getJsonKey(), 10);

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
			HighlightingConfigurationKeys.FIELDS.getJsonKey(),
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
			AggregationConfigurationKeys.NAME.getJsonKey(),
			"userNameAggregation");
		termAggregationConfigurationJsonObject.put(
			AggregationConfigurationKeys.TYPE.getJsonKey(), "terms");

		JSONObject bodyJsonObject = JSONFactoryUtil.createJSONObject();

		bodyJsonObject.put(
			TermsAggregationBodyConfigurationKeys.FIELD.getJsonKey(),
			"userName");

		termAggregationConfigurationJsonObject.put(
			AggregationConfigurationKeys.BODY.getJsonKey(), bodyJsonObject);

		aggregationConfigurationJsonArray.put(
			termAggregationConfigurationJsonObject);

		bluePrintJsonObject.put(
			BlueprintKeys.AGGREGATION_CONFIGURATION.getJsonKey(),
			aggregationConfigurationJsonArray);
	}

	public void mockConfigurations(Blueprint blueprint) throws Exception {
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
			blueprint.getConfiguration());

		long companyId = CompanyThreadLocal.getCompanyId();

		if (!jsonObject.has(
				BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey())) {

			mockAdvancedConfiguration(jsonObject, companyId);
		}

		if (!jsonObject.has(
				BlueprintKeys.AGGREGATION_CONFIGURATION.getJsonKey())) {

			mockAggregationConfiguration(jsonObject);
		}

		if (!jsonObject.has(
				BlueprintKeys.PARAMETER_CONFIGURATION.getJsonKey())) {

			mockParameterConfiguration(jsonObject);
		}

		if (!jsonObject.has(BlueprintKeys.QUERY_CONFIGURATION.getJsonKey())) {
			mockQueryConfiguration(jsonObject);
		}

		if (!jsonObject.has(BlueprintKeys.SORT_CONFIGURATION.getJsonKey())) {
			mockSortConfiguration(jsonObject);
		}

		if (!jsonObject.has(BlueprintKeys.SUGGEST_CONFIGURATION.getJsonKey())) {
			mockSuggestConfiguration(jsonObject);
		}

		if (!jsonObject.has("facets")) {
			mockFacetsConfiguration(jsonObject);
		}

		blueprint.setConfiguration(jsonObject.toString());
	}

	public void mockParameterConfiguration(JSONObject bluePrintJsonObject) {
		JSONObject parameterConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		// Keywords

		JSONObject keywordsConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		keywordsConfigurationJsonObject.put(
			KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey(), "q");
		parameterConfigurationJsonObject.put(
			ParameterConfigurationKeys.KEYWORDS.getJsonKey(),
			keywordsConfigurationJsonObject);

		// Paging

		JSONObject pagingConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		pagingConfigurationJsonObject.put(
			KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey(), "page");
		parameterConfigurationJsonObject.put(
			ParameterConfigurationKeys.PAGE.getJsonKey(),
			pagingConfigurationJsonObject);

		// Custom

		JSONArray customParameterConfigurationJsonArray =
			JSONFactoryUtil.createJSONArray();

		// "dateFrom"

		JSONObject customParameterConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		customParameterConfigurationJsonObject.put(
			CustomParameterConfigurationKeys.PARAMETER_NAME.getJsonKey(),
			"dateFrom");
		customParameterConfigurationJsonObject.put(
			CustomParameterConfigurationKeys.TYPE.getJsonKey(),
			ParameterType.DATE.getJsonValue());
		customParameterConfigurationJsonObject.put(
			CustomParameterConfigurationKeys.DATE_FORMAT.getJsonKey(),
			"yyyy-MM-dd");

		customParameterConfigurationJsonArray.put(
			customParameterConfigurationJsonObject);

		// "groupId"

		JSONObject customParameterConfigurationJsonObject1 =
			JSONFactoryUtil.createJSONObject();

		customParameterConfigurationJsonObject1.put(
			CustomParameterConfigurationKeys.PARAMETER_NAME.getJsonKey(),
			"groupId");
		customParameterConfigurationJsonObject1.put(
			CustomParameterConfigurationKeys.TYPE.getJsonKey(),
			ParameterType.LONG.getJsonValue());

		customParameterConfigurationJsonArray.put(
			customParameterConfigurationJsonObject1);

		// "time-range"

		JSONObject customParameterConfigurationJsonObject2 =
			JSONFactoryUtil.createJSONObject();

		customParameterConfigurationJsonObject2.put(
			CustomParameterConfigurationKeys.PARAMETER_NAME.getJsonKey(),
			"time");
		customParameterConfigurationJsonObject2.put(
			CustomParameterConfigurationKeys.TYPE.getJsonKey(),
			ParameterType.TIME_RANGE.getJsonValue());

		customParameterConfigurationJsonArray.put(
			customParameterConfigurationJsonObject2);

		parameterConfigurationJsonObject.put(
			ParameterConfigurationKeys.CUSTOM.getJsonKey(),
			customParameterConfigurationJsonArray);

		bluePrintJsonObject.put(
			BlueprintKeys.PARAMETER_CONFIGURATION.getJsonKey(),
			parameterConfigurationJsonObject);
	}

	public void mockSortConfiguration(JSONObject bluePrintJsonObject) {
		JSONObject sortConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		// Default

		JSONArray defaultSortsJsonArray = JSONFactoryUtil.createJSONArray();

		JSONObject defaultsortConfigurationJsonObject =
			JSONFactoryUtil.createJSONObject();

		defaultsortConfigurationJsonObject.put(
			SortConfigurationKeys.FIELD.getJsonKey(), "title_en_US");
		defaultsortConfigurationJsonObject.put(
			SortConfigurationKeys.ORDER.getJsonKey(), "desc");
		defaultsortConfigurationJsonObject.put(
			SortConfigurationKeys.LABEL.getJsonKey(), "title");

		defaultSortsJsonArray.put(defaultsortConfigurationJsonObject);

		sortConfigurationJsonObject.put(
			SortConfigurationKeys.DEFAULT.getJsonKey(), defaultSortsJsonArray);

		// Parameters

		JSONArray sortsConfigurationJsonArray =
			JSONFactoryUtil.createJSONArray();

		JSONObject sortConfigurationJsonObject1 =
			JSONFactoryUtil.createJSONObject();

		sortConfigurationJsonObject1.put(
			SortConfigurationKeys.PARAMETER_NAME.getJsonKey(), "sort1");
		sortConfigurationJsonObject1.put(
			SortConfigurationKeys.FIELD.getJsonKey(), "");
		sortConfigurationJsonObject1.put(
			SortConfigurationKeys.LABEL.getJsonKey(), "score");

		sortsConfigurationJsonArray.put(sortConfigurationJsonObject1);

		JSONObject sortConfigurationJsonObject2 =
			JSONFactoryUtil.createJSONObject();

		sortConfigurationJsonObject2.put(
			SortConfigurationKeys.PARAMETER_NAME.getJsonKey(), "sort2");
		sortConfigurationJsonObject2.put(
			SortConfigurationKeys.FIELD.getJsonKey(),
			"localized_title_${context.language_id}_sortable");
		sortConfigurationJsonObject2.put(
			SortConfigurationKeys.LABEL.getJsonKey(), "title");
		sortsConfigurationJsonArray.put(sortConfigurationJsonObject2);

		JSONObject sortConfigurationJsonObject3 =
			JSONFactoryUtil.createJSONObject();

		sortConfigurationJsonObject3.put(
			SortConfigurationKeys.PARAMETER_NAME.getJsonKey(), "sort3");
		sortConfigurationJsonObject3.put(
			SortConfigurationKeys.FIELD.getJsonKey(), "modified");
		sortConfigurationJsonObject2.put(
			SortConfigurationKeys.LABEL.getJsonKey(), "modified");

		sortsConfigurationJsonArray.put(sortConfigurationJsonObject3);

		sortConfigurationJsonObject.put(
			SortConfigurationKeys.PARAMETERS.getJsonKey(),
			sortsConfigurationJsonArray);

		bluePrintJsonObject.put(
			BlueprintKeys.SORT_CONFIGURATION.getJsonKey(),
			sortConfigurationJsonObject);
	}

	public void mockSuggestConfiguration(JSONObject bluePrintJsonObject) {
		JSONArray suggestConfigurationJsonArray =
			JSONFactoryUtil.createJSONArray();

		JSONObject suggester1JsonObject = JSONFactoryUtil.createJSONObject();

		suggester1JsonObject.put(
			SuggesterConfigurationKeys.ENABLED.getJsonKey(), true);
		suggester1JsonObject.put(
			SuggesterConfigurationKeys.TYPE.getJsonKey(),
			SuggesterType.PHRASE.getJsonValue());

		JSONObject suggester1ConfigurationJsonObject1 =
			JSONFactoryUtil.createJSONObject();

		suggester1ConfigurationJsonObject1.put(
			PhraseSuggesterConfigurationKeys.CONFIDENCE.getJsonKey(), 0.1);
		suggester1ConfigurationJsonObject1.put(
			PhraseSuggesterConfigurationKeys.FIELD.getJsonKey(),
			"keywordSearch_en_US");
		suggester1ConfigurationJsonObject1.put(
			PhraseSuggesterConfigurationKeys.GRAM_SIZE.getJsonKey(), 1);
		suggester1ConfigurationJsonObject1.put(
			PhraseSuggesterConfigurationKeys.MAX_ERRORS.getJsonKey(), 2.0);
		suggester1ConfigurationJsonObject1.put(
			PhraseSuggesterConfigurationKeys.REAL_WORLD_ERROR_LIKELIHOOD.
				getJsonKey(),
			0.90);
		suggester1ConfigurationJsonObject1.put(
			PhraseSuggesterConfigurationKeys.TEXT.getJsonKey(), "${keywords}");
		suggester1ConfigurationJsonObject1.put(
			PhraseSuggesterConfigurationKeys.SIZE.getJsonKey(), 10);

		suggester1JsonObject.put(
			SuggesterConfigurationKeys.CONFIGURATION.getJsonKey(),
			suggester1ConfigurationJsonObject1);

		suggestConfigurationJsonArray.put(suggester1JsonObject);

		// TODO: spellchecking

		bluePrintJsonObject.put(
			BlueprintKeys.SUGGEST_CONFIGURATION.getJsonKey(),
			suggestConfigurationJsonArray);
	}

}