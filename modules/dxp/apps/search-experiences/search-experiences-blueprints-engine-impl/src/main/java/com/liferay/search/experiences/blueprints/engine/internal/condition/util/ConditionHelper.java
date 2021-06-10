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

package com.liferay.search.experiences.blueprints.engine.internal.condition.util;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.search.experiences.blueprints.constants.json.keys.query.ConditionConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.exception.ParameterEvaluationException;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.parameter.visitor.EvaluationVisitor;
import com.liferay.search.experiences.blueprints.engine.template.variable.BlueprintTemplateVariableParser;
import com.liferay.search.experiences.blueprints.message.Messages;

import java.util.Optional;
import java.util.function.Function;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ConditionHelper.class)
public class ConditionHelper {

	public boolean evaluate(
		Function<JSONObject, EvaluationVisitor> function, boolean negate,
		JSONObject jsonObject, ParameterData parameterData, Messages messages) {

		Optional<Parameter> parameterOptional = getParameterOptional(
			parameterData, jsonObject);

		if (!parameterOptional.isPresent()) {
			return false;
		}

		Optional<Object> parsedValueOptional = _getParsedValue(
			jsonObject, parameterData, messages);

		if (!parsedValueOptional.isPresent()) {
			return false;
		}

		jsonObject.put(
			ConditionConfigurationKeys.VALUE.getJsonKey(),
			parsedValueOptional.get());

		boolean match = _evaluate(
			parameterOptional.get(), function.apply(jsonObject), messages);

		if (negate) {
			return !match;
		}

		return match;
	}

	public Optional<Parameter> getParameterOptional(
		ParameterData parameterData, JSONObject jsonObject) {

		return parameterData.getByTemplateVariableNameOptional(
			jsonObject.getString(
				ConditionConfigurationKeys.PARAMETER_NAME.getJsonKey()));
	}

	private boolean _evaluate(
		Parameter parameter, EvaluationVisitor visitor, Messages messages) {

		try {
			return parameter.accept(visitor);
		}
		catch (ParameterEvaluationException parameterEvaluationException) {
			_log.error(
				parameterEvaluationException.getMessage(),
				parameterEvaluationException);

			messages.addMessage(
				parameterEvaluationException.getDetailsMessage());

			return false;
		}
	}

	private Optional<Object> _getParsedValue(
		JSONObject jsonObject, ParameterData parameterData, Messages messages) {

		return _blueprintTemplateVariableParser.parse(
			jsonObject.get(ConditionConfigurationKeys.VALUE.getJsonKey()),
			parameterData, messages);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ConditionHelper.class);

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

}