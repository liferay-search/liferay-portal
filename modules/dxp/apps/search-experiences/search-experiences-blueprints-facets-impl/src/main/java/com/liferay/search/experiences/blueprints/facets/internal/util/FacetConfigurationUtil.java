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
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.util.Collections;
import java.util.List;

/**
 * @author Petteri Karttunen
 */
public class FacetConfigurationUtil {

	public static String getAggregationName(JSONObject jsonObject) {
		String name = jsonObject.getString("aggregation_name");

		if (!Validator.isBlank(name)) {
			return name;
		}

		return getFieldName(jsonObject);
	}

	public static List<String> getExcludeValues(JSONObject jsonObject) {
		JSONObject parametersJSONObject = jsonObject.getJSONObject(
			"parameters");

		if (parametersJSONObject == null) {
			return Collections.emptyList();
		}

		JSONArray excludeValuesJSONArray = parametersJSONObject.getJSONArray(
			"exclude_values");

		if (excludeValuesJSONArray == null) {
			Collections.emptyList();
		}

		return JSONUtil.toStringList(excludeValuesJSONArray);
	}

	public static String getFieldName(JSONObject jsonObject) {
		return jsonObject.getString("field");
	}

	public static FilterMode getFilterMode(
		JSONObject jsonObject, ProblemsHolderBuilder problemsHolderBuilder) {

		String s = jsonObject.getString("filter_mode", "pre");

		try {
			return FilterMode.valueOf(StringUtil.toUpperCase(s));
		}
		catch (IllegalArgumentException illegalArgumentException) {
			return null;
		}
	}

	public static List<String> getIncludeValues(JSONObject jsonObject) {
		JSONObject parametersJSONObject = jsonObject.getJSONObject(
			"parameters");

		if (parametersJSONObject == null) {
			return Collections.emptyList();
		}

		JSONArray excludeValuesJSONArray = parametersJSONObject.getJSONArray(
			"include_values");

		if (excludeValuesJSONArray == null) {
			Collections.emptyList();
		}

		return JSONUtil.toStringList(excludeValuesJSONArray);
	}

	public static String getLabel(JSONObject jsonObject) {
		String name = jsonObject.getString("label");

		if (!Validator.isBlank(name)) {
			return name;
		}

		return getFieldName(jsonObject);
	}

	public static Operator getOperator(
		JSONObject jsonObject, ProblemsHolderBuilder problemsHolderBuilder) {

		String s = jsonObject.getString("multi_value_operator", "and");

		try {
			return Operator.valueOf(StringUtil.toUpperCase(s));
		}
		catch (IllegalArgumentException illegalArgumentException) {
			return null;
		}
	}

	public static String getParameterName(JSONObject jsonObject) {
		String name = jsonObject.getString("parameters");

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
		return jsonObject.getBoolean("enabled", true);
	}

}