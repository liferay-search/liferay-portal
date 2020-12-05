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
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.BlueprintKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.AdvancedConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.QueryIndexingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.QueryProcessingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.parameter.KeywordsConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.parameter.PageConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.parameter.ParameterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.sort.SortConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintHelper.class)
public class BlueprintHelperImpl implements BlueprintHelper {

	@Override
	public Optional<JSONArray> getAggsConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				configurationJsonObjectOptional.get(),
				"JSONArray/" +
					BlueprintKeys.AGGREGATION_CONFIGURATION.getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	@Override
	public Optional<JSONArray> getCustomParameterConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				configurationJsonObjectOptional.get(),
				"JSONObject/" +
					BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
				"JSONArray/" + ParameterConfigurationKeys.CUSTOM.getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	@Override
	public Optional<JSONArray> getDefaultSortConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		return BlueprintJSONUtil.getValueAsJSONArrayOptional(
			configurationJsonObjectOptional.get(),
			"JSONObject/" + BlueprintKeys.SORT_CONFIGURATION.getJsonKey(),
			"JSONArray/" + SortConfigurationKeys.DEFAULT.getJsonKey());
	}

	@Override
	public Optional<JSONArray> getEntryClassNamesOptional(Blueprint blueprint) {
		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		return BlueprintJSONUtil.getValueAsJSONArrayOptional(
			configurationJsonObjectOptional.get(),
			"JSONObject/" + BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
			"JSONArray/" +
				AdvancedConfigurationKeys.ENTRY_CLASS_NAMES.getJsonKey());
	}

	@Override
	public Optional<List<String>> getExcludedQueryContributorsOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		String key =
			QueryProcessingConfigurationKeys.EXCLUDE_QUERY_CONTRIBUTORS.
				getJsonKey();

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				configurationJsonObjectOptional.get(),
				"JSONObject/" +
					BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
				"JSONObject/" +
					AdvancedConfigurationKeys.QUERY_PROCESSING.getJsonKey(),
				"JSONArray/" + key);

		if (jsonArrayOptional.isPresent() &&
			(jsonArrayOptional.get(
			).length() > 0)) {

			return Optional.of(JSONUtil.toStringList(jsonArrayOptional.get()));
		}

		return Optional.empty();
	}

	@Override
	public Optional<List<String>> getExcludedQueryPostProcessorsOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				configurationJsonObjectOptional.get(),
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

	@Override
	public Optional<JSONObject> getHighlightConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		return BlueprintJSONUtil.getValueAsJSONObjectOptional(
			configurationJsonObjectOptional.get(),
			"JSONObject/" + BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
			"JSONObject/" +
				AdvancedConfigurationKeys.HIGHLIGHTING.getJsonKey());
	}

	@Override
	public Optional<JSONArray> getJSONArrayConfigurationOptional(
		Blueprint blueprint, String... paths) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				configurationJsonObjectOptional.get(), paths);

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	@Override
	public Optional<JSONObject> getJSONObjectConfigurationOptional(
		Blueprint blueprint, String... paths) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		return BlueprintJSONUtil.getValueAsJSONObjectOptional(
			configurationJsonObjectOptional.get(), paths);
	}

	@Override
	public Optional<String> getKeywordsParameterNameOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
				getParameterConfigurationOptional(blueprint);

		return BlueprintJSONUtil.getValueAsStringOptional(
			configurationJsonObjectOptional.get(),
			"JSONObject/" + ParameterConfigurationKeys.KEYWORDS.getJsonKey(),
			"Object/" + KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey());
	}

	@Override
	public Optional<JSONArray> getMisspellingsConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		String key =
			AdvancedConfigurationKeys.MISSPELLINGS_DEFINITION_IDS.getJsonKey();

		return BlueprintJSONUtil.getValueAsJSONArrayOptional(
			configurationJsonObjectOptional.get(),
			"JSONObject/" + BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
			"JSONArray/" + key);
	}

	@Override
	public Optional<String> getPageParameterNameOptional(Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
			getParameterConfigurationOptional(blueprint);

		return BlueprintJSONUtil.getValueAsStringOptional(
			configurationJsonObjectOptional.get(),
			"JSONObject/" + ParameterConfigurationKeys.PAGE.getJsonKey(),
			"Object/" + PageConfigurationKeys.PARAMETER_NAME.getJsonKey());
	}

	@Override
	public Optional<JSONObject> getParameterConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}
		
		JSONObject configurationJsonObject = 
				configurationJsonObjectOptional.get();
		
		if (!configurationJsonObject.has(
				BlueprintKeys.PARAMETER_CONFIGURATION.getJsonKey())) {
			return Optional.of(getDefaultParameterConfiguration());
		}
		
		JSONObject parameterConfigurationJsonObject =
				configurationJsonObject.getJSONObject(
						BlueprintKeys.PARAMETER_CONFIGURATION.getJsonKey());
		
		if (parameterConfigurationJsonObject.length() > 0) {
			return Optional.of(parameterConfigurationJsonObject);
		}

		return Optional.of(getDefaultParameterConfiguration());
	}

	@Override
	public Optional<JSONArray> getQueryConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				configurationJsonObjectOptional.get(),
				"JSONArray/" + BlueprintKeys.QUERY_CONFIGURATION.getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	@Override
	public Optional<String> getQueryIndexConfigurationIdOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		String key =
			QueryIndexingConfigurationKeys.QUERY_INDEX_CONFIGURATION_ID.
				getJsonKey();

		return BlueprintJSONUtil.getValueAsStringOptional(
			configurationJsonObjectOptional.get(),
			"JSONObject/" + BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
			"JSONObject/" +
				AdvancedConfigurationKeys.QUERY_INDEXING.getJsonKey(),
			"Object/" + key);
	}

	@Override
	public Optional<JSONObject> getQueryIndexingConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		return BlueprintJSONUtil.getValueAsJSONObjectOptional(
			configurationJsonObjectOptional.get(),
			"JSONObject/" + BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
			"JSONObject/" +
				AdvancedConfigurationKeys.QUERY_INDEXING.getJsonKey());
	}

	@Override
	public int getSize(Blueprint blueprint) {
		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return _DEFAULT_PAGE_SIZE;
		}

		Optional<Integer> sizeOptional =
			BlueprintJSONUtil.getValueAsIntegerOptional(
				configurationJsonObjectOptional.get(),
				"JSONObject/" +
					BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
				"Object/" + AdvancedConfigurationKeys.PAGE_SIZE.getJsonKey());

		return sizeOptional.orElse(_DEFAULT_PAGE_SIZE);
	}

	@Override
	public Optional<JSONObject> getSortConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		return BlueprintJSONUtil.getValueAsJSONObjectOptional(
			configurationJsonObjectOptional.get(),
			"JSONObject/" + BlueprintKeys.SORT_CONFIGURATION.getJsonKey());
	}

	@Override
	public Optional<JSONArray> getSortParameterConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		return BlueprintJSONUtil.getValueAsJSONArrayOptional(
			configurationJsonObjectOptional.get(),
			"JSONObject/" + BlueprintKeys.SORT_CONFIGURATION.getJsonKey(),
			"JSONArray/" + SortConfigurationKeys.PARAMETERS.getJsonKey());
	}

	@Override
	public Optional<JSONArray> getSuggestConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return Optional.empty();
		}

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				configurationJsonObjectOptional.get(),
				"JSONObject/" +
					BlueprintKeys.SUGGEST_CONFIGURATION.getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	protected JSONObject getDefaultParameterConfiguration() {
		
		JSONObject parameterConfigurationJsonObject =
				JSONFactoryUtil.createJSONObject();

		JSONObject keywordsConfigurationJsonObject = JSONFactoryUtil.createJSONObject();
		
		keywordsConfigurationJsonObject.put(
				KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey(), "q");

		parameterConfigurationJsonObject.put(
				ParameterConfigurationKeys.KEYWORDS.getJsonKey(),
				keywordsConfigurationJsonObject);

		JSONObject pagingConfigurationJsonObject =
				JSONFactoryUtil.createJSONObject();

		pagingConfigurationJsonObject.put(
			KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey(), "page");

		parameterConfigurationJsonObject.put(
			ParameterConfigurationKeys.PAGE.getJsonKey(),
			pagingConfigurationJsonObject);
		
		return parameterConfigurationJsonObject;
	}
	
	private Optional<JSONObject> _getConfiguration(Blueprint blueprint) {
		try {
			JSONObject configurationJsonObject =
				JSONFactoryUtil.createJSONObject(blueprint.getConfiguration());

			return Optional.of(configurationJsonObject);
		}
		catch (JSONException jsonException) {
			_log.error(
				"Error in getting Blueprint configuration as JSON",
				jsonException);
		}

		return Optional.empty();
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

	private static final int _DEFAULT_PAGE_SIZE = 10;

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintHelperImpl.class);

}