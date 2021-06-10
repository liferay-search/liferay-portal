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

package com.liferay.search.experiences.blueprints.internal.util;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.search.experiences.blueprints.constants.json.keys.BlueprintKeys;
import com.liferay.search.experiences.blueprints.constants.json.keys.framework.FrameworkConfigurationKeys;
import com.liferay.search.experiences.blueprints.constants.json.keys.parameter.KeywordsConfigurationKeys;
import com.liferay.search.experiences.blueprints.constants.json.keys.parameter.PageConfigurationKeys;
import com.liferay.search.experiences.blueprints.constants.json.keys.parameter.ParameterConfigurationKeys;
import com.liferay.search.experiences.blueprints.constants.json.keys.parameter.SizeConfigurationKeys;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.blueprints.util.util.BlueprintJSONUtil;

import java.util.Optional;
import java.util.function.Supplier;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
	public Optional<JSONObject> getAdvancedConfigurationOptional(
		Blueprint blueprint) {

		return BlueprintJSONUtil.getJSONObjectOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONObject/" + BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey());
	}

	@Override
	public Optional<JSONObject> getAggsConfigurationOptional(
		Blueprint blueprint) {

		return BlueprintJSONUtil.getJSONObjectOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONObject/" +
				BlueprintKeys.AGGREGATION_CONFIGURATION.getJsonKey());
	}

	@Override
	public Optional<JSONArray> getCustomParameterConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getJSONArrayOptional(
				_getBlueprintConfigurationJSONObject(blueprint),
				"JSONObject/" +
					BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
				"JSONArray/" + ParameterConfigurationKeys.CUSTOM.getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	@Override
	public int getDefaultSize(Blueprint blueprint) {
		Optional<JSONObject> configurationJSONObjectOptional =
			getParameterConfigurationOptional(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return _getDefaultSize().get();
		}

		Optional<Integer> optional = BlueprintJSONUtil.getIntegerOptional(
			configurationJSONObjectOptional.get(),
			"JSONObject/" + ParameterConfigurationKeys.SIZE.getJsonKey(),
			"Object/" + SizeConfigurationKeys.DEFAULT.getJsonKey());

		return optional.orElse(_getDefaultSize().get());
	}

	@Override
	public Optional<JSONArray> getDefaultSortConfigurationOptional(
		Blueprint blueprint) {

		return BlueprintJSONUtil.getJSONArrayOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONArray/" + BlueprintKeys.SORT_CONFIGURATION.getJsonKey());
	}

	@Override
	public Optional<JSONObject> getFrameworkConfigurationOptional(
		Blueprint blueprint) {

		JSONObject configurationJSONObject =
			_getBlueprintConfigurationJSONObject(blueprint);

		JSONObject frameworkConfigurationJSONObject =
			configurationJSONObject.getJSONObject(
				BlueprintKeys.FRAMEWORK_CONFIGURATION.getJsonKey());

		if (frameworkConfigurationJSONObject != null) {
			return Optional.of(frameworkConfigurationJSONObject);
		}

		return Optional.of(getDefaultFrameworkConfigurationJSONObject());
	}

	@Override
	public Optional<JSONObject> getHighlightConfigurationOptional(
		Blueprint blueprint) {

		return BlueprintJSONUtil.getJSONObjectOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONObject/" + BlueprintKeys.HIGHLIGHT_CONFIGURATION.getJsonKey());
	}

	@Override
	public Optional<JSONArray> getJSONArrayConfigurationOptional(
		Blueprint blueprint, String... paths) {

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getJSONArrayOptional(
				_getBlueprintConfigurationJSONObject(blueprint), paths);

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	@Override
	public Optional<JSONObject> getJSONObjectConfigurationOptional(
		Blueprint blueprint, String... paths) {

		return BlueprintJSONUtil.getJSONObjectOptional(
			_getBlueprintConfigurationJSONObject(blueprint), paths);
	}

	@Override
	public String getKeywordsParameterName(Blueprint blueprint) {
		Optional<JSONObject> configurationJSONObjectOptional =
			getParameterConfigurationOptional(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return _getDefaultKeywordParameterName().get();
		}

		Optional<String> optional = BlueprintJSONUtil.getStringOptional(
			configurationJSONObjectOptional.get(),
			"JSONObject/" + ParameterConfigurationKeys.KEYWORDS.getJsonKey(),
			"Object/" + KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey());

		return optional.orElse(_getDefaultKeywordParameterName().get());
	}

	@Override
	public String getPageParameterName(Blueprint blueprint) {
		Optional<JSONObject> configurationJSONObjectOptional =
			getParameterConfigurationOptional(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return _getDefaultPageParameterName().get();
		}

		Optional<String> optional = BlueprintJSONUtil.getStringOptional(
			configurationJSONObjectOptional.get(),
			"JSONObject/" + ParameterConfigurationKeys.PAGE.getJsonKey(),
			"Object/" + PageConfigurationKeys.PARAMETER_NAME.getJsonKey());

		return optional.orElse(_getDefaultPageParameterName().get());
	}

	@Override
	public Optional<JSONObject> getParameterConfigurationOptional(
		Blueprint blueprint) {

		JSONObject configurationJSONObject =
			_getBlueprintConfigurationJSONObject(blueprint);

		JSONObject parameterConfigurationJSONObject =
			configurationJSONObject.getJSONObject(
				BlueprintKeys.PARAMETER_CONFIGURATION.getJsonKey());

		if ((parameterConfigurationJSONObject != null) &&
			(parameterConfigurationJSONObject.length() > 0)) {

			if (!parameterConfigurationJSONObject.has(
					ParameterConfigurationKeys.KEYWORDS.getJsonKey())) {

				parameterConfigurationJSONObject.put(
					ParameterConfigurationKeys.KEYWORDS.getJsonKey(),
					getDefaultKeywordParameterConfigurationJSONObject());
			}

			if (!parameterConfigurationJSONObject.has(
					ParameterConfigurationKeys.PAGE.getJsonKey())) {

				parameterConfigurationJSONObject.put(
					ParameterConfigurationKeys.PAGE.getJsonKey(),
					getDefaultPageParameterConfigurationJSONObject());
			}

			if (!parameterConfigurationJSONObject.has(
					ParameterConfigurationKeys.SIZE.getJsonKey())) {

				parameterConfigurationJSONObject.put(
					ParameterConfigurationKeys.SIZE.getJsonKey(),
					getDefaultSizeParameterConfigurationJSONObject());
			}

			return Optional.of(parameterConfigurationJSONObject);
		}

		return Optional.of(getDefaultParameterConfigurationJSONObject());
	}

	@Override
	public Optional<JSONArray> getQueryConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONArray> jsonArrayOptional =
			BlueprintJSONUtil.getJSONArrayOptional(
				_getBlueprintConfigurationJSONObject(blueprint),
				"JSONArray/" + BlueprintKeys.QUERY_CONFIGURATION.getJsonKey());

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	@Override
	public Optional<JSONArray> getSearchableAssetTypesOptional(
		Blueprint blueprint) {

		Optional<JSONObject> configurationJSONObjectOptional =
			getFrameworkConfigurationOptional(blueprint);

		return BlueprintJSONUtil.getJSONArrayOptional(
			configurationJSONObjectOptional.get(),
			"JSONArray/" +
				FrameworkConfigurationKeys.SEARCHABLE_ASSET_TYPES.getJsonKey());
	}

	@Override
	public String getSizeParameterName(Blueprint blueprint) {
		Optional<JSONObject> configurationJSONObjectOptional =
			getParameterConfigurationOptional(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return _getDefaultSizeParameterName().get();
		}

		Optional<String> optional = BlueprintJSONUtil.getStringOptional(
			configurationJSONObjectOptional.get(),
			"JSONObject/" + ParameterConfigurationKeys.SIZE.getJsonKey(),
			"Object/" + SizeConfigurationKeys.PARAMETER_NAME.getJsonKey());

		return optional.orElse(_getDefaultSizeParameterName().get());
	}

	@Override
	public Optional<JSONObject> getSortConfigurationOptional(
		Blueprint blueprint) {

		return BlueprintJSONUtil.getJSONObjectOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONObject/" + BlueprintKeys.SORT_CONFIGURATION.getJsonKey());
	}

	@Override
	public Optional<JSONObject> getSortParameterConfigurationOptional(
		Blueprint blueprint) {

		return BlueprintJSONUtil.getJSONObjectOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONObject/" + BlueprintKeys.PARAMETER_CONFIGURATION.getJsonKey(),
			"JSONObject/" + ParameterConfigurationKeys.SORT.getJsonKey());
	}

	@Override
	public Optional<JSONObject> getSuggestConfigurationOptional(
		Blueprint blueprint) {

		return BlueprintJSONUtil.getJSONObjectOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONObject/" + BlueprintKeys.SUGGEST_CONFIGURATION.getJsonKey());
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

	protected JSONObject getDefaultKeywordParameterConfigurationJSONObject() {
		return JSONUtil.put(
			SizeConfigurationKeys.PARAMETER_NAME.getJsonKey(), "q");
	}

	protected JSONObject getDefaultPageParameterConfigurationJSONObject() {
		return JSONUtil.put(
			SizeConfigurationKeys.PARAMETER_NAME.getJsonKey(), "page");
	}

	protected JSONObject getDefaultParameterConfigurationJSONObject() {
		return JSONUtil.put(
			ParameterConfigurationKeys.KEYWORDS.getJsonKey(),
			getDefaultKeywordParameterConfigurationJSONObject()
		).put(
			ParameterConfigurationKeys.PAGE.getJsonKey(),
			getDefaultPageParameterConfigurationJSONObject()
		).put(
			ParameterConfigurationKeys.SIZE.getJsonKey(),
			getDefaultSizeParameterConfigurationJSONObject()
		);
	}

	protected JSONObject getDefaultSizeParameterConfigurationJSONObject() {
		return JSONUtil.put(
			SizeConfigurationKeys.PARAMETER_NAME.getJsonKey(), "size"
		).put(
			SizeConfigurationKeys.DEFAULT.getJsonKey(), 10
		).put(
			SizeConfigurationKeys.MAX_VALUE.getJsonKey(), 100
		).put(
			SizeConfigurationKeys.MIN_VALUE.getJsonKey(), 1
		);
	}

	private JSONObject _getBlueprintConfigurationJSONObject(
		Blueprint blueprint) {

		try {
			return _jsonFactory.createJSONObject(blueprint.getConfiguration());
		}
		catch (JSONException jsonException) {
			_log.error(
				"Error in getting Blueprint configuration as JSON",
				jsonException);

			throw new RuntimeException(jsonException);
		}
	}

	private Supplier<String> _getDefaultKeywordParameterName() {
		return () ->
			getDefaultKeywordParameterConfigurationJSONObject().getString(
				KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey());
	}

	private Supplier<String> _getDefaultPageParameterName() {
		return () -> getDefaultPageParameterConfigurationJSONObject().getString(
			PageConfigurationKeys.PARAMETER_NAME.getJsonKey());
	}

	private Supplier<Integer> _getDefaultSize() {
		return () -> getDefaultSizeParameterConfigurationJSONObject().getInt(
			SizeConfigurationKeys.DEFAULT.getJsonKey());
	}

	private Supplier<String> _getDefaultSizeParameterName() {
		return () -> getDefaultSizeParameterConfigurationJSONObject().getString(
			SizeConfigurationKeys.PARAMETER_NAME.getJsonKey());
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

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintHelperImpl.class);

	@Reference
	private JSONFactory _jsonFactory;

}