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
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.QueryProcessingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.framework.FrameworkConfigurationKeys;
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
	public boolean applyIndexerClauses(Blueprint blueprint) {
		Optional<JSONObject> configurationJSONObjectOptional =
			getFrameworkConfigurationOptional(blueprint);

		JSONObject configurationJSONObject =
			configurationJSONObjectOptional.get();

		return configurationJSONObject.getBoolean(
			FrameworkConfigurationKeys.APPLY_INDEXER_CLAUSES.getJsonKey(),
			true);
	}

	@Override
	public Optional<JSONArray> getAggsConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return Optional.empty();
		}

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				configurationJSONObjectOptional.get(),
				"JSONArray/" +
					BlueprintKeys.AGGREGATION_CONFIGURATION.getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	@Override
	public Optional<JSONArray> getCustomParameterConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return Optional.empty();
		}

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				configurationJSONObjectOptional.get(),
				"JSONObject/" +
					BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
				"JSONArray/" + ParameterConfigurationKeys.CUSTOM.getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	@Override
	public Optional<JSONArray> getDefaultSortConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return Optional.empty();
		}

		return BlueprintJSONUtil.getValueAsJSONArrayOptional(
			configurationJSONObjectOptional.get(),
			"JSONObject/" + BlueprintKeys.SORT_CONFIGURATION.getJsonKey(),
			"JSONArray/" + SortConfigurationKeys.DEFAULT.getJsonKey());
	}

	@Override
	public Optional<List<String>> getExcludedQueryContributorsOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return Optional.empty();
		}

		String key =
			QueryProcessingConfigurationKeys.EXCLUDE_QUERY_CONTRIBUTORS.
				getJsonKey();

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				configurationJSONObjectOptional.get(),
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

		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return Optional.empty();
		}

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				configurationJSONObjectOptional.get(),
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
	public Optional<JSONObject> getFrameworkConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return Optional.empty();
		}

		JSONObject configurationJSONObject =
			configurationJSONObjectOptional.get();

		JSONObject frameworkConfigurationJSONObject =
			configurationJSONObject.getJSONObject(
				BlueprintKeys.FRAMEWORK_CONFIGURATION.getJsonKey());

		if ((frameworkConfigurationJSONObject != null) &&
			frameworkConfigurationJSONObject.has(
				FrameworkConfigurationKeys.SEARCHABLE_ASSET_TYPES.
					getJsonKey())) {

			return Optional.of(frameworkConfigurationJSONObject);
		}

		return Optional.of(getDefaultFrameworkConfigurationJSONObject());
	}

	@Override
	public Optional<JSONObject> getHighlightConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return Optional.empty();
		}

		return BlueprintJSONUtil.getValueAsJSONObjectOptional(
			configurationJSONObjectOptional.get(),
			"JSONObject/" + BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
			"JSONObject/" +
				AdvancedConfigurationKeys.HIGHLIGHTING.getJsonKey());
	}

	@Override
	public Optional<JSONArray> getJSONArrayConfigurationOptional(
		Blueprint blueprint, String... paths) {

		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return Optional.empty();
		}

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				configurationJSONObjectOptional.get(), paths);

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	@Override
	public Optional<JSONObject> getJSONObjectConfigurationOptional(
		Blueprint blueprint, String... paths) {

		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return Optional.empty();
		}

		return BlueprintJSONUtil.getValueAsJSONObjectOptional(
			configurationJSONObjectOptional.get(), paths);
	}

	@Override
	public Optional<String> getKeywordsParameterNameOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJSONObjectOptional =
			getParameterConfigurationOptional(blueprint);

		return BlueprintJSONUtil.getValueAsStringOptional(
			configurationJSONObjectOptional.get(),
			"JSONObject/" + ParameterConfigurationKeys.KEYWORDS.getJsonKey(),
			"Object/" + KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey());
	}

	@Override
	public Optional<JSONArray> getMisspellingsConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return Optional.empty();
		}

		String key =
			AdvancedConfigurationKeys.MISSPELLINGS_DEFINITION_IDS.getJsonKey();

		return BlueprintJSONUtil.getValueAsJSONArrayOptional(
			configurationJSONObjectOptional.get(),
			"JSONObject/" + BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
			"JSONArray/" + key);
	}

	@Override
	public Optional<String> getPageParameterNameOptional(Blueprint blueprint) {
		Optional<JSONObject> configurationJSONObjectOptional =
			getParameterConfigurationOptional(blueprint);

		return BlueprintJSONUtil.getValueAsStringOptional(
			configurationJSONObjectOptional.get(),
			"JSONObject/" + ParameterConfigurationKeys.PAGE.getJsonKey(),
			"Object/" + PageConfigurationKeys.PARAMETER_NAME.getJsonKey());
	}

	@Override
	public Optional<JSONObject> getParameterConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return Optional.empty();
		}

		JSONObject configurationJSONObject =
			configurationJSONObjectOptional.get();

		JSONObject parameterConfigurationJSONObject =
			configurationJSONObject.getJSONObject(
				BlueprintKeys.PARAMETER_CONFIGURATION.getJsonKey());

		if ((parameterConfigurationJSONObject != null) &&
			(parameterConfigurationJSONObject.length() > 0)) {

			return Optional.of(parameterConfigurationJSONObject);
		}

		return Optional.of(getDefaultParameterConfigurationJSONObject());
	}

	@Override
	public Optional<JSONArray> getQueryConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return Optional.empty();
		}

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				configurationJSONObjectOptional.get(),
				"JSONArray/" + BlueprintKeys.QUERY_CONFIGURATION.getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	@Override
	public Optional<JSONArray> getSearchableAssetTypesOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJSONObjectOptional =
			getFrameworkConfigurationOptional(blueprint);

		return BlueprintJSONUtil.getValueAsJSONArrayOptional(
			configurationJSONObjectOptional.get(),
			"JSONArray/" +
				FrameworkConfigurationKeys.SEARCHABLE_ASSET_TYPES.getJsonKey());
	}

	@Override
	public int getSize(Blueprint blueprint) {
		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return _DEFAULT_PAGE_SIZE;
		}

		Optional<Integer> sizeOptional =
			BlueprintJSONUtil.getValueAsIntegerOptional(
				configurationJSONObjectOptional.get(),
				"JSONObject/" +
					BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
				"Object/" + AdvancedConfigurationKeys.PAGE_SIZE.getJsonKey());

		return sizeOptional.orElse(_DEFAULT_PAGE_SIZE);
	}

	@Override
	public Optional<JSONObject> getSortConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return Optional.empty();
		}

		return BlueprintJSONUtil.getValueAsJSONObjectOptional(
			configurationJSONObjectOptional.get(),
			"JSONObject/" + BlueprintKeys.SORT_CONFIGURATION.getJsonKey());
	}

	@Override
	public Optional<JSONArray> getSortParameterConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return Optional.empty();
		}

		return BlueprintJSONUtil.getValueAsJSONArrayOptional(
			configurationJSONObjectOptional.get(),
			"JSONObject/" + BlueprintKeys.SORT_CONFIGURATION.getJsonKey(),
			"JSONArray/" + SortConfigurationKeys.PARAMETERS.getJsonKey());
	}

	@Override
	public Optional<JSONArray> getSuggestConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJSONObjectOptional =
			_getConfiguration(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return Optional.empty();
		}

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getValueAsJSONArrayOptional(
				configurationJSONObjectOptional.get(),
				"JSONObject/" +
					BlueprintKeys.SUGGEST_CONFIGURATION.getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	protected JSONObject getDefaultFrameworkConfigurationJSONObject() {
		return JSONUtil.put(
			FrameworkConfigurationKeys.APPLY_INDEXER_CLAUSES.getJsonKey(), true
		).put(
			FrameworkConfigurationKeys.SEARCHABLE_ASSET_TYPES.getJsonKey(),
			JSONUtil.putAll(
				"com.liferay.journal.model.JournalArticle",
				"com.liferay.document.library.kernel.model.DLFileEntry")
		);
	}

	protected JSONObject getDefaultParameterConfigurationJSONObject() {
		JSONObject keywordsConfigurationJSONObject = JSONUtil.put(
			KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey(), "q");

		JSONObject pagingConfigurationJSONObject = JSONUtil.put(
			KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey(), "page");

		return JSONUtil.put(
			ParameterConfigurationKeys.KEYWORDS.getJsonKey(),
			keywordsConfigurationJSONObject
		).put(
			ParameterConfigurationKeys.PAGE.getJsonKey(),
			pagingConfigurationJSONObject
		);
	}

	private Optional<JSONObject> _getConfiguration(Blueprint blueprint) {
		try {
			JSONObject configurationJSONObject =
				JSONFactoryUtil.createJSONObject(blueprint.getConfiguration());

			return Optional.of(configurationJSONObject);
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