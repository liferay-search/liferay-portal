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
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;

import java.util.Arrays;
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
	public Optional<JSONObject> getAdvancedConfigurationOptional(
		Blueprint blueprint) {

		return getJSONObjectOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONObject/advanced_configuration");
	}

	@Override
	public Optional<JSONObject> getAggsConfigurationOptional(
		Blueprint blueprint) {

		return getJSONObjectOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONObject/aggregation_configuration");
	}

	@Override
	public Optional<JSONArray> getCustomParameterConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONArray> jsonArrayOptional = getJSONArrayOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONObject/advanced_configuration", "JSONArray/custom");

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	@Override
	public int getDefaultSize(Blueprint blueprint) {
		Optional<JSONObject> configurationJSONObjectOptional =
			getParameterConfigurationOptional(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return _getDefaultSize().get();
		}

		Optional<Integer> optional = getIntegerOptional(
			configurationJSONObjectOptional.get(), "JSONObject/size",
			"Object/default");

		return optional.orElse(_getDefaultSize().get());
	}

	@Override
	public Optional<JSONArray> getDefaultSortConfigurationOptional(
		Blueprint blueprint) {

		return getJSONArrayOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONArray/sort_configuration");
	}

	@Override
	public Optional<JSONObject> getHighlightConfigurationOptional(
		Blueprint blueprint) {

		return getJSONObjectOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONObject/highlight_configuration");
	}

	@Override
	public Optional<JSONArray> getJSONArrayConfigurationOptional(
		Blueprint blueprint, String... paths) {

		Optional<JSONArray> jsonArrayOptional = getJSONArrayOptional(
			_getBlueprintConfigurationJSONObject(blueprint), paths);

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	@Override
	public Optional<JSONObject> getJSONObjectConfigurationOptional(
		Blueprint blueprint, String... paths) {

		return getJSONObjectOptional(
			_getBlueprintConfigurationJSONObject(blueprint), paths);
	}

	@Override
	public String getKeywordsParameterName(Blueprint blueprint) {
		Optional<JSONObject> configurationJSONObjectOptional =
			getParameterConfigurationOptional(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return _getDefaultKeywordParameterName().get();
		}

		Optional<String> optional = getStringOptional(
			configurationJSONObjectOptional.get(), "JSONObject/keywords",
			"Object/parameter_name");

		return optional.orElse(_getDefaultKeywordParameterName().get());
	}

	@Override
	public String getPageParameterName(Blueprint blueprint) {
		Optional<JSONObject> configurationJSONObjectOptional =
			getParameterConfigurationOptional(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return _getDefaultPageParameterName().get();
		}

		Optional<String> optional = getStringOptional(
			configurationJSONObjectOptional.get(), "JSONObject/page",
			"Object/parameter_name");

		return optional.orElse(_getDefaultPageParameterName().get());
	}

	@Override
	public Optional<JSONObject> getParameterConfigurationOptional(
		Blueprint blueprint) {

		JSONObject configurationJSONObject =
			_getBlueprintConfigurationJSONObject(blueprint);

		JSONObject parameterConfigurationJSONObject =
			configurationJSONObject.getJSONObject("parameter_configuration");

		if ((parameterConfigurationJSONObject != null) &&
			(parameterConfigurationJSONObject.length() > 0)) {

			if (!parameterConfigurationJSONObject.has("keywords")) {
				parameterConfigurationJSONObject.put(
					"keywords",
					getDefaultKeywordParameterConfigurationJSONObject());
			}

			if (!parameterConfigurationJSONObject.has("page")) {
				parameterConfigurationJSONObject.put(
					"page", getDefaultPageParameterConfigurationJSONObject());
			}

			if (!parameterConfigurationJSONObject.has("size")) {
				parameterConfigurationJSONObject.put(
					"size", getDefaultSizeParameterConfigurationJSONObject());
			}

			return Optional.of(parameterConfigurationJSONObject);
		}

		return Optional.of(getDefaultParameterConfigurationJSONObject());
	}

	@Override
	public Optional<JSONArray> getQueryConfigurationOptional(
		Blueprint blueprint) {

		Optional<JSONArray> jsonArrayOptional = getJSONArrayOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONArray/query_configuration");

		return _maybeJsonArrayOptional(jsonArrayOptional);
	}

	@Override
	public String getSizeParameterName(Blueprint blueprint) {
		Optional<JSONObject> configurationJSONObjectOptional =
			getParameterConfigurationOptional(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return _getDefaultSizeParameterName().get();
		}

		Optional<String> optional = getStringOptional(
			configurationJSONObjectOptional.get(), "JSONObject/size",
			"Object/parameter_name");

		return optional.orElse(_getDefaultSizeParameterName().get());
	}

	@Override
	public Optional<JSONObject> getSortConfigurationOptional(
		Blueprint blueprint) {

		return getJSONObjectOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONObject/sort_configuration");
	}

	@Override
	public Optional<JSONObject> getSortParameterConfigurationOptional(
		Blueprint blueprint) {

		return getJSONObjectOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONObject/parameter_configuration", "JSONObject/sort");
	}

	@Override
	public Optional<JSONObject> getSuggestConfigurationOptional(
		Blueprint blueprint) {

		return getJSONObjectOptional(
			_getBlueprintConfigurationJSONObject(blueprint),
			"JSONObject/suggest_configuration");
	}

	protected static Optional<Integer> getIntegerOptional(
		Object object, String... paths) {

		Object value = getValue(object, paths);

		if (value == null) {
			return Optional.empty();
		}

		return Optional.of(GetterUtil.getInteger(value));
	}

	protected static Optional<JSONArray> getJSONArrayOptional(
		Object object, String... paths) {

		Object value = getValue(object, paths);

		if (value == null) {
			return Optional.empty();
		}

		return Optional.of((JSONArray)value);
	}

	protected static Optional<JSONObject> getJSONObjectOptional(
		Object object, String... paths) {

		Object value = getValue(object, paths);

		if (value == null) {
			return Optional.empty();
		}

		return Optional.of((JSONObject)value);
	}

	protected static Optional<String> getStringOptional(
		Object object, String... paths) {

		Object value = getValue(object, paths);

		if (value == null) {
			return Optional.empty();
		}

		return Optional.of(String.valueOf(value));
	}

	protected static Object getValue(Object object, String... paths) {
		if (object == null) {
			return null;
		}

		Object value = null;

		String[] parts = paths[0].split("/");

		String type = parts[0];
		String key = parts[1];

		if (type.equals("JSONArray")) {
			JSONObject jsonObject = (JSONObject)object;

			value = jsonObject.getJSONArray(key);
		}
		else if (type.equals("JSONObject")) {
			JSONObject jsonObject = (JSONObject)object;

			value = jsonObject.getJSONObject(key);
		}
		else if (type.equals("Object")) {
			if (object instanceof JSONArray) {
				JSONArray jsonArray = (JSONArray)object;

				value = jsonArray.get(GetterUtil.getInteger(key));
			}
			else if (object instanceof JSONObject) {
				JSONObject jsonObject = (JSONObject)object;

				value = jsonObject.get(key);
			}
		}

		if (paths.length == 1) {
			return value;
		}

		if (value == null) {
			return null;
		}

		return getValue(value, Arrays.copyOfRange(paths, 1, paths.length));
	}

	protected JSONObject getDefaultKeywordParameterConfigurationJSONObject() {
		return JSONUtil.put("parameter_name", "q");
	}

	protected JSONObject getDefaultPageParameterConfigurationJSONObject() {
		return JSONUtil.put("parameter_name", "page");
	}

	protected JSONObject getDefaultParameterConfigurationJSONObject() {
		return JSONUtil.put(
			"keywords", getDefaultKeywordParameterConfigurationJSONObject()
		).put(
			"page", getDefaultPageParameterConfigurationJSONObject()
		).put(
			"size", getDefaultSizeParameterConfigurationJSONObject()
		);
	}

	protected JSONObject getDefaultSizeParameterConfigurationJSONObject() {
		return JSONUtil.put(
			"default", 10
		).put(
			"max_value", 100
		).put(
			"min_value", 1
		).put(
			"parameter_name", "size"
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
				"parameter_name");
	}

	private Supplier<String> _getDefaultPageParameterName() {
		return () -> getDefaultPageParameterConfigurationJSONObject().getString(
			"parameter_name");
	}

	private Supplier<Integer> _getDefaultSize() {
		return () -> getDefaultSizeParameterConfigurationJSONObject().getInt(
			"default");
	}

	private Supplier<String> _getDefaultSizeParameterName() {
		return () -> getDefaultSizeParameterConfigurationJSONObject().getString(
			"parameter_name");
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