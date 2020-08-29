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

package com.liferay.portal.search.tuning.blueprints.internal.util;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.BlueprintKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.AdvancedConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.QueryProcessingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.SearchResultsConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.requestparameter.KeywordsConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.requestparameter.RequestParameterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.KeywordSuggestionsConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.SpellCheckingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.SuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintHelper.class)
public class BlueprintHelperImpl implements BlueprintHelper {

	public Optional<JSONArray> getAggregationConfigurationOptional(
		JSONObject blueprintJsonObject) {

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				blueprintJsonObject,
				"JSONArray/" +
					BlueprintKeys.AGGREGATION_CONFIGURATION.getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	public Optional<JSONArray> getClauseConfigurationOptional(
		JSONObject blueprintJsonObject) {

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				blueprintJsonObject,
				"JSONArray/" + BlueprintKeys.CLAUSE_CONFIGURATION.getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	public Optional<JSONArray> getDefaultSortConfigurationOptional(
		JSONObject blueprintJsonObject) {

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				blueprintJsonObject,
				"JSONObject/" +
					BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
				"JSONObject/" +
					AdvancedConfigurationKeys.SEARCH_RESULTS.getJsonKey(),
				"JSONArray/" +
					SearchResultsConfigurationKeys.DEFAULT_SORTS.getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	public Optional<List<String>> getExcludedQueryContributorsOptional(
		JSONObject blueprintJsonObject) {

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				blueprintJsonObject,
				"JSONObject/" +
					BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
				"JSONObject/" +
					AdvancedConfigurationKeys.QUERY_PROCESSING.getJsonKey(),
				"JSONArray/" +
					QueryProcessingConfigurationKeys.EXCLUDE_QUERY_CONTRIBUTORS.
						getJsonKey());

		if (jsonArrayOptional.isPresent() &&
			(jsonArrayOptional.get(
			).length() > 0)) {

			return Optional.of(JSONUtil.toStringList(jsonArrayOptional.get()));
		}

		return Optional.empty();
	}

	public Optional<List<String>> getExcludedQueryPostProcessorsOptional(
		JSONObject blueprintJsonObject) {

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				blueprintJsonObject,
				"JSONObject/" +
					BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
				"JSONObject/" +
					AdvancedConfigurationKeys.QUERY_PROCESSING.getJsonKey(),
				"JSONArray/" +
					QueryProcessingConfigurationKeys.
						EXCLUDE_QUERY_POST_PROCESSORS.getJsonKey());

		if (jsonArrayOptional.isPresent() &&
			(jsonArrayOptional.get(
			).length() > 0)) {

			return Optional.of(JSONUtil.toStringList(jsonArrayOptional.get()));
		}

		return Optional.empty();
	}

	public Optional<JSONObject> getHighlightConfigurationOptional(
		JSONObject blueprintJsonObject) {

		return BlueprintJSONUtil.getValueAsJSONObjectOptional(
			blueprintJsonObject,
			"JSONObject/" + BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
			"JSONObject/" +
				AdvancedConfigurationKeys.HIGHLIGHTING.getJsonKey());
	}

	public Optional<String[]> getIndexNamesOptional(
		JSONObject blueprintJsonObject) {

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				blueprintJsonObject,
				"JSONObject/" +
					BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
				"JSONArray/" +
					AdvancedConfigurationKeys.INDEX_NAMES.getJsonKey());

		if (jsonArrayOptional.isPresent() &&
			(jsonArrayOptional.get(
			).length() > 0)) {

			return Optional.of(JSONUtil.toStringArray(jsonArrayOptional.get()));
		}

		return Optional.empty();
	}

	public Optional<JSONObject> getKeywordIndexingConfigurationOptional(
		JSONObject blueprintJsonObject) {

		return BlueprintJSONUtil.getValueAsJSONObjectOptional(
			blueprintJsonObject,
			"JSONObject/" + BlueprintKeys.SUGGESTER_CONFIGURATION.getJsonKey(),
			"JSONObject/" +
				SuggesterConfigurationKeys.KEYWORD_INDEXING.getJsonKey());
	}

	public Optional<String> getKeywordParameterNameOptional(
		JSONObject blueprintJsonObject) {

		return BlueprintJSONUtil.getValueAsStringOptional(
			blueprintJsonObject,
			"JSONObject/" +
				BlueprintKeys.REQUEST_PARAMETER_CONFIGURATION.getJsonKey(),
			"JSONObject/" +
				RequestParameterConfigurationKeys.KEYWORDS.getJsonKey(),
			"Object/" + KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey());
	}

	public Optional<JSONArray> getKeywordSuggestersOptional(
		JSONObject blueprintJsonObject) {

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				blueprintJsonObject,
				"JSONObject/" +
					BlueprintKeys.SUGGESTER_CONFIGURATION.getJsonKey(),
				"JSONObject/" +
					SuggesterConfigurationKeys.KEYWORD_SUGGESTIONS.getJsonKey(),
				"JSONArray/" +
					KeywordSuggestionsConfigurationKeys.SUGGESTERS.
						getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	public Optional<String> getPagingParameterNameOptional(
		JSONObject blueprintJsonObject) {

		return BlueprintJSONUtil.getValueAsStringOptional(
			blueprintJsonObject,
			"JSONObject/" +
				BlueprintKeys.REQUEST_PARAMETER_CONFIGURATION.getJsonKey(),
			"JSONObject/" +
				RequestParameterConfigurationKeys.PAGING.getJsonKey(),
			"Object/" + KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey());
	}

	public Optional<JSONObject> getRequestParameterConfigurationOptional(
		JSONObject blueprintJsonObject) {

		return BlueprintJSONUtil.getValueAsJSONObjectOptional(
			blueprintJsonObject,
			"JSONObject/" +
				BlueprintKeys.REQUEST_PARAMETER_CONFIGURATION.getJsonKey());
	}

	public int getSize(JSONObject blueprintJsonObject) {
		Optional<Integer> sizeOptional =
			BlueprintJSONUtil.getValueAsIntegerOptional(
				blueprintJsonObject,
				"JSONObject/" +
					BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
				"JSONObject/" +
					AdvancedConfigurationKeys.SEARCH_RESULTS.getJsonKey(),
				"Object/" +
					SearchResultsConfigurationKeys.PAGE_SIZE.getJsonKey());

		return sizeOptional.orElse(10);
	}

	public Optional<JSONArray> getSortParameterConfigurationOptional(
		JSONObject blueprintJsonObject) {

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				blueprintJsonObject,
				"JSONObject/" +
					BlueprintKeys.REQUEST_PARAMETER_CONFIGURATION.getJsonKey(),
				"JSONArray/" +
					RequestParameterConfigurationKeys.SORTS.getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	public Optional<JSONObject> getSpellCheckingConfigurationOptional(
		JSONObject blueprintJsonObject) {

		return BlueprintJSONUtil.getValueAsJSONObjectOptional(
			blueprintJsonObject,
			"JSONObject/" + BlueprintKeys.SUGGESTER_CONFIGURATION.getJsonKey(),
			"JSONObject/" +
				SuggesterConfigurationKeys.SPELL_CHECKING.getJsonKey());
	}

	public Optional<JSONArray> getSpellCheckingSuggestersOptional(
		JSONObject blueprintJsonObject) {

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				blueprintJsonObject,
				"JSONObject/" +
					BlueprintKeys.SUGGESTER_CONFIGURATION.getJsonKey(),
				"JSONObject/" +
					SuggesterConfigurationKeys.SPELL_CHECKING.getJsonKey(),
				"JSONArray/" +
					SpellCheckingConfigurationKeys.SUGGESTERS.getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	private Optional<JSONArray> _maybeJsonArrayOptional(
		Optional<JSONArray> jsonArrayOptional) {

		if (jsonArrayOptional.isPresent() &&
			(jsonArrayOptional.get(
			).length() > 0)) {

			return jsonArrayOptional;
		}

		return Optional.empty();
	}

}