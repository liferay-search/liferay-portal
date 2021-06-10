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

package com.liferay.search.experiences.blueprints.facets.internal.util;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.search.experiences.blueprints.constants.json.values.FilterMode;
import com.liferay.search.experiences.blueprints.constants.json.values.Operator;
import com.liferay.search.experiences.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.search.experiences.blueprints.message.Messages;

import java.util.Collections;
import java.util.List;

/**
 * @author Petteri Karttunen
 */
public class FacetConfigurationUtil {

	public static String getAggregationName(JSONObject jsonObject) {
		String name = jsonObject.getString(
			FacetConfigurationKeys.AGGREGATION_NAME.getJsonKey());

		if (!Validator.isBlank(name)) {
			return name;
		}

		return getFieldName(jsonObject);
	}

	public static List<String> getExcludeValues(JSONObject jsonObject) {
		JSONObject parametersJSONObject = jsonObject.getJSONObject(
			FacetConfigurationKeys.PARAMETERS.getJsonKey());

		if (parametersJSONObject == null) {
			return Collections.emptyList();
		}

		JSONArray excludeValuesJSONArray = parametersJSONObject.getJSONArray(
			FacetConfigurationKeys.EXCLUDE_VALUES.getJsonKey());

		if (excludeValuesJSONArray == null) {
			Collections.emptyList();
		}

		return JSONUtil.toStringList(excludeValuesJSONArray);
	}

	public static String getFieldName(JSONObject jsonObject) {
		return jsonObject.getString(FacetConfigurationKeys.FIELD.getJsonKey());
	}

	public static FilterMode getFilterMode(
		JSONObject jsonObject, Messages messages) {

		String s = jsonObject.getString(
			FacetConfigurationKeys.FILTER_MODE.getJsonKey(),
			FilterMode.PRE.getjsonValue());

		return FilterMode.valueOf(StringUtil.toUpperCase(s));
	}

	public static List<String> getIncludeValues(JSONObject jsonObject) {
		JSONObject parametersJSONObject = jsonObject.getJSONObject(
			FacetConfigurationKeys.PARAMETERS.getJsonKey());

		if (parametersJSONObject == null) {
			return Collections.emptyList();
		}

		JSONArray excludeValuesJSONArray = parametersJSONObject.getJSONArray(
			FacetConfigurationKeys.INCLUDE_VALUES.getJsonKey());

		if (excludeValuesJSONArray == null) {
			Collections.emptyList();
		}

		return JSONUtil.toStringList(excludeValuesJSONArray);
	}

	public static String getLabel(JSONObject jsonObject) {
		String name = jsonObject.getString(
			FacetConfigurationKeys.LABEL.getJsonKey());

		if (!Validator.isBlank(name)) {
			return name;
		}

		return getFieldName(jsonObject);
	}

	public static Operator getOperator(
		JSONObject jsonObject, Messages messages) {

		String s = jsonObject.getString(
			FacetConfigurationKeys.MULTI_VALUE_OPERATOR.getJsonKey(),
			Operator.AND.getjsonValue());

		return Operator.valueOf(StringUtil.toUpperCase(s));
	}

	public static String getParameterName(JSONObject jsonObject) {
		String name = jsonObject.getString(
			FacetConfigurationKeys.PARAMETER_NAME.getJsonKey());

		if (!Validator.isBlank(name)) {
			return name;
		}

		return getFieldName(jsonObject);
	}

	public static boolean includeValue(
		String value, List<String> includeValues, List<String> excludeValues) {

		if (!includeValues.isEmpty()) {
			if (!includeValues.contains(value)) {
				return false;
			}
		}
		else if (!excludeValues.isEmpty() && excludeValues.contains(value)) {
			return false;
		}

		return true;
	}

	public static boolean isEnabled(JSONObject jsonObject) {
		return jsonObject.getBoolean(
			FacetConfigurationKeys.ENABLED.getJsonKey(), true);
	}

}