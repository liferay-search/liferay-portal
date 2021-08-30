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

package com.liferay.search.experiences.blueprints.engine.internal.template.variable;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.search.experiences.blueprints.engine.internal.parameter.visitor.ToTemplateVariableStringVisitor;
import com.liferay.search.experiences.blueprints.engine.internal.util.ProblemBuilders;
import com.liferay.search.experiences.blueprints.engine.parameter.DateParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.template.variable.BlueprintTemplateVariableParser;
import com.liferay.search.experiences.problems.Problem;
import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintTemplateVariableParser.class)
public class BlueprintTemplateVariableParserImpl
	implements BlueprintTemplateVariableParser {

	@Override
	public Optional<Object> parse(
		Object object, ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		if (object == null) {
			return Optional.empty();
		}

		List<Parameter> parameters = parameterData.getParameters();

		if (parameters.isEmpty()) {
			if (_log.isDebugEnabled()) {
				_log.debug("No parameters available");
			}

			return Optional.of(object);
		}

		try {
			if (object instanceof JSONObject) {
				return Optional.ofNullable(
					_parseJSONObject(
						(JSONObject)object, parameterData,
						problemsHolderBuilder));
			}
			else if (object instanceof JSONArray) {
				return Optional.ofNullable(
					_parseJSONArray(
						(JSONArray)object, parameterData,
						problemsHolderBuilder));
			}
		}
		catch (JSONException jsonException) {
			_handleException(jsonException, object, problemsHolderBuilder);

			return Optional.empty();
		}

		return Optional.of(object);
	}

	@Override
	public Optional<JSONArray> parseArray(
		JSONArray jsonArray, ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		if (jsonArray == null) {
			return Optional.empty();
		}

		List<Parameter> parameters = parameterData.getParameters();

		if (parameters.isEmpty()) {
			if (_log.isDebugEnabled()) {
				_log.debug("No parameters available");
			}

			return Optional.of(jsonArray);
		}

		try {
			return Optional.ofNullable(
				_parseJSONArray(
					jsonArray, parameterData, problemsHolderBuilder));
		}
		catch (JSONException jsonException) {
			_handleException(jsonException, jsonArray, problemsHolderBuilder);
		}

		return Optional.empty();
	}

	@Override
	public Optional<JSONObject> parseObject(
		JSONObject jsonObject, ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		if (jsonObject == null) {
			return Optional.empty();
		}

		List<Parameter> parameters = parameterData.getParameters();

		if (parameters.isEmpty()) {
			if (_log.isDebugEnabled()) {
				_log.debug("No parameters available");
			}

			return Optional.of(jsonObject);
		}

		try {
			return Optional.ofNullable(
				_parseJSONObject(
					jsonObject, parameterData, problemsHolderBuilder));
		}
		catch (JSONException jsonException) {
			_handleException(jsonException, jsonObject, problemsHolderBuilder);
		}

		return Optional.empty();
	}

	private Map<String, String> _getParameterOptions(String optionsString)
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

	private String _getParametrizedVariableStem(String str) {
		StringBundler sb = new StringBundler(2);

		sb.append(str.substring(0, str.length() - 1));
		sb.append("|");

		return sb.toString();
	}

	private void _handleException(
		Exception exception, Object rootObject,
		ProblemsHolderBuilder problemsHolderBuilder) {

		ProblemBuilder problemBuilder = _problemBuilders2.error(
			getClass().getName(),
			"core.error.error-in-parsing-template-variables", rootObject, null,
			null, exception);

		problemsHolderBuilder.addProblem(problemBuilder.build());
	}

	private boolean _hasTemplateVariables(String str) {
		if (str.indexOf("${") > 0) {
			return true;
		}

		return false;
	}

	private JSONArray _parseJSONArray(
			JSONArray jsonArray, ParameterData parameterData,
			ProblemsHolderBuilder problemsHolderBuilder)
		throws JSONException {

		String json = jsonArray.toString();

		if (!_hasTemplateVariables(json)) {
			return jsonArray;
		}

		String parsed = _parseString(
			json, parameterData, problemsHolderBuilder);

		if (!Validator.isBlank(parsed)) {
			return _jsonFactory.createJSONArray(parsed);
		}

		return null;
	}

	private JSONObject _parseJSONObject(
			JSONObject jsonObject, ParameterData parameterData,
			ProblemsHolderBuilder problemsHolderBuilder)
		throws JSONException {

		String json = jsonObject.toString();

		if (!_hasTemplateVariables(json)) {
			return jsonObject;
		}

		String parsed = _parseString(
			json, parameterData, problemsHolderBuilder);

		if (!Validator.isBlank(parsed)) {
			return _jsonFactory.createJSONObject(parsed);
		}

		return null;
	}

	private String _parseString(
		String str, ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		try {
			ToTemplateVariableStringVisitor toStringVisitor =
				new ToTemplateVariableStringVisitor();

			for (Parameter parameter : parameterData.getParameters()) {
				String templateVariable = parameter.getTemplateVariable();

				if (Validator.isNull(templateVariable)) {
					continue;
				}

				String stem = _getParametrizedVariableStem(templateVariable);

				if (str.contains(stem)) {
					str = _processParametrizedTemplateVariables(
						str, parameter, toStringVisitor, stem);
				}

				if (str.contains(templateVariable)) {
					str = _processTemplateVariables(
						str, parameter, toStringVisitor);
				}

				if (!_hasTemplateVariables(str)) {
					break;
				}
			}

			if (!_validateResults(str, problemsHolderBuilder)) {
				return null;
			}

			return str;
		}
		catch (Exception exception) {
			_handleException(exception, str, problemsHolderBuilder);
		}

		return null;
	}

	private String _processParametrizedTemplateVariable(
			String str, Parameter parameter,
			ToTemplateVariableStringVisitor toStringVisitor,
			String templateVariable, int from)
		throws Exception {

		int end = str.indexOf("}", from);

		String optionsString = str.substring(
			from + templateVariable.length(), end);

		StringBuilder sb = new StringBuilder();

		sb.append(templateVariable);
		sb.append(optionsString);
		sb.append("}");

		String substitution = parameter.accept(
			toStringVisitor, _getParameterOptions(optionsString));

		if (substitution.startsWith("[")) {
			return _replaceArrayValue(str, sb.toString(), substitution);
		}

		return StringUtil.replace(str, sb.toString(), substitution);
	}

	private String _processParametrizedTemplateVariables(
			String str, Parameter parameter,
			ToTemplateVariableStringVisitor toStringVisitor,
			String templateVariable)
		throws Exception {

		if (!DateParameter.class.isAssignableFrom(parameter.getClass())) {
			return str;
		}

		int from = str.indexOf(templateVariable);

		while (from >= 0) {
			str = _processParametrizedTemplateVariable(
				str, parameter, toStringVisitor, templateVariable, from);

			from = str.indexOf(templateVariable, from);
		}

		return str;
	}

	private String _processTemplateVariables(
			String str, Parameter parameter,
			ToTemplateVariableStringVisitor toStringVisitor)
		throws Exception {

		String templateVariable = parameter.getTemplateVariable();

		String substitution = parameter.accept(toStringVisitor, null);

		if (substitution.startsWith("[")) {
			return _replaceArrayValue(str, templateVariable, substitution);
		}

		return StringUtil.replace(str, templateVariable, substitution);
	}

	private String _replaceArrayValue(
		String str, String templateVariable, String substitution) {

		StringBundler sb = new StringBundler(3);

		sb.append("\"");
		sb.append(templateVariable);
		sb.append("\"");

		return StringUtil.replace(str, sb.toString(), substitution);
	}

	private boolean _validateResults(
		String str, ProblemsHolderBuilder problemsHolderBuilder) {

		if (str.contains("${")) {
			ProblemBuilder problemBuilder = _problemBuilders1.warning(
				getClass().getName(),
				"core.error.unable-to-parse-template-variables",
				"Unable to parse some of the template variables", str, null,
				null);

			Problem problem = problemBuilder.build();

			problemsHolderBuilder.addProblem(problem);

			if (_log.isWarnEnabled()) {
				_log.warn(problem);
			}

			return false;
		}

		return true;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintTemplateVariableParserImpl.class);

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private ProblemBuilders _problemBuilders1;

	@Reference
	private com.liferay.search.experiences.problems.ProblemBuilders
		_problemBuilders2;

}