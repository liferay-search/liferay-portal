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

package com.liferay.portal.search.tuning.blueprints.engine.internal.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.engine.internal.parameter.visitor.ToTemplateVariableStringVisitor;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.DateParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.message.Messages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public class BlueprintTemplateVariableUtil {

	public static JSONObject parseTemplateVariables(
			ParameterData parameterData, Messages messages,
			JSONObject queryJsonObject)
		throws Exception {

		List<Parameter> parameters = parameterData.getParameters();

		if (parameters.isEmpty()) {
			if (_log.isDebugEnabled()) {
				_log.debug("No parameters available");
			}

			return queryJsonObject;
		}

		String queryString = queryJsonObject.toString();

		if (!_hasTemplateVariables(queryString)) {
			return queryJsonObject;
		}

		boolean changed = false;

		ToTemplateVariableStringVisitor toStringVisitor =
			new ToTemplateVariableStringVisitor();

		for (Parameter parameter : parameterData.getParameters()) {
			String str1 = parameter.getTemplateVariable();

			String str2 = _getParametrizedVariableStem(str1);

			if (queryString.contains(str2)) {
				queryString = _processParametrizedTemplateVariables(
					queryString, parameter, toStringVisitor, str2);
				changed = true;
			}

			if (queryString.contains(str1)) {
				queryString = _processTemplateVariables(
					queryString, parameter, toStringVisitor);
				changed = true;
			}

			if (!_hasTemplateVariables(queryString)) {
				break;
			}
		}

		// Fail if there were untranslated (not present variables)

		if (queryString.contains("${")) {
			throw new JSONException(
				"Some template variables were not be parsed [ " + queryString +
					" ]");
		}

		if (changed) {
			return JSONFactoryUtil.createJSONObject(queryString);
		}

		return queryJsonObject;
	}

	private static Map<String, String> _getParameterOptions(
			String optionsString)
		throws Exception {

		Map<String, String> map = new HashMap<>();

		if (Validator.isBlank(optionsString)) {
			return map;
		}

		String[] arr = optionsString.split(",");

		for (String str : arr) {
			String[] optionArr = str.split("=");

			if (optionArr.length == 1) {
				map.put(optionArr[0], null);
			}
			else {
				map.put(optionArr[0], optionArr[1]);
			}
		}

		return map;
	}

	private static String _getParametrizedVariableStem(String s) {
		StringBundler sb = new StringBundler(2);

		sb.append(s.substring(0, s.length() - 1));
		sb.append("|");

		return sb.toString();
	}

	private static boolean _hasTemplateVariables(String queryString) {
		if (queryString.indexOf("${") > 0) {
			return true;
		}

		return false;
	}

	private static String _processParametrizedTemplateVariable(
			String queryString, Parameter parameter,
			ToTemplateVariableStringVisitor toStringVisitor,
			String templateVariable, int from)
		throws Exception {

		int end = queryString.indexOf("}", from);

		String optionsString = queryString.substring(
			from + templateVariable.length(), end);

		StringBuilder sb = new StringBuilder();

		sb.append(templateVariable);
		sb.append(optionsString);
		sb.append("}");

		String substitution = parameter.accept(
			toStringVisitor, _getParameterOptions(optionsString));

		// Remove quotes from array values

		if (substitution.startsWith("[")) {
			return _replaceArrayValue(queryString, sb.toString(), substitution);
		}

		return StringUtil.replace(queryString, sb.toString(), substitution);
	}

	private static String _processParametrizedTemplateVariables(
			String queryString, Parameter parameter,
			ToTemplateVariableStringVisitor toStringVisitor,
			String templateVariable)
		throws Exception {

		if (!DateParameter.class.isAssignableFrom(parameter.getClass())) {
			return queryString;
		}

		int from = queryString.indexOf(templateVariable);

		while (from >= 0) {
			queryString = _processParametrizedTemplateVariable(
				queryString, parameter, toStringVisitor, templateVariable,
				from);

			from = queryString.indexOf(templateVariable, from);
		}

		return queryString;
	}

	private static String _processTemplateVariables(
			String queryString, Parameter parameter,
			ToTemplateVariableStringVisitor toStringVisitor)
		throws Exception {

		String templateVariable = parameter.getTemplateVariable();

		String substitution = parameter.accept(toStringVisitor, null);

		// Remove quotes if array values

		if (substitution.startsWith("[")) {
			return _replaceArrayValue(
				queryString, templateVariable, substitution);
		}

		return StringUtil.replace(queryString, templateVariable, substitution);
	}

	private static String _replaceArrayValue(
		String queryString, String templateVariable, String substitution) {

		StringBundler sb = new StringBundler(3);

		sb.append("\"");
		sb.append(templateVariable);
		sb.append("\"");

		return StringUtil.replace(queryString, sb.toString(), substitution);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintTemplateVariableUtil.class);

}