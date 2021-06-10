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

package com.liferay.search.experiences.blueprints.engine.internal.parameter.builder;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.search.experiences.blueprints.constants.json.keys.parameter.CustomParameterConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.internal.attributes.util.BlueprintsAttributesHelper;
import com.liferay.search.experiences.blueprints.engine.parameter.IntegerParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.util.util.BlueprintValueUtil;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=integer",
	service = ParameterBuilder.class
)
public class IntegerParameterBuilder implements ParameterBuilder {

	@Override
	public Optional<Parameter> build(
		BlueprintsAttributes blueprintsAttributes, JSONObject jsonObject,
		Messages messages) {

		String parameterName = jsonObject.getString(
			CustomParameterConfigurationKeys.PARAMETER_NAME.getJsonKey());

		Optional<Integer> valueOptional = _getValueOptional(
			blueprintsAttributes, jsonObject, parameterName);

		if (!valueOptional.isPresent()) {
			return Optional.empty();
		}

		return Optional.of(
			new IntegerParameter(
				parameterName, "${parameter." + parameterName + "}",
				_getAdjustedValue(valueOptional.get(), jsonObject)));
	}

	private int _getAdjustedValue(int value, JSONObject jsonObject) {
		Optional<Integer> minValue = BlueprintValueUtil.stringToIntegerOptional(
			jsonObject.getString(
				CustomParameterConfigurationKeys.MIN_VALUE.getJsonKey()));

		if (minValue.isPresent() &&
			(Integer.compare(value, minValue.get()) < 0)) {

			if (_log.isWarnEnabled()) {
				_log.warn(minValue.get() + " is below the minimum.");
			}

			value = minValue.get();
		}

		Optional<Integer> maxValue = BlueprintValueUtil.stringToIntegerOptional(
			jsonObject.getString(
				CustomParameterConfigurationKeys.MAX_VALUE.getJsonKey()));

		if (maxValue.isPresent() &&
			(Integer.compare(value, maxValue.get()) > 0)) {

			if (_log.isWarnEnabled()) {
				_log.warn(maxValue.get() + " is above the maximum.");
			}

			value = maxValue.get();
		}

		return value;
	}

	private Optional<Integer> _getValueOptional(
		BlueprintsAttributes blueprintsAttributes, JSONObject jsonObject,
		String parameterName) {

		Optional<String> valueOptional =
			_blueprintsAttributesHelper.getStringOptional(
				blueprintsAttributes, parameterName);

		if (!valueOptional.isPresent()) {
			valueOptional = BlueprintValueUtil.toStringOptional(
				jsonObject.getString(
					CustomParameterConfigurationKeys.DEFAULT.getJsonKey()));
		}

		if (!valueOptional.isPresent()) {
			return Optional.empty();
		}

		return BlueprintValueUtil.stringToIntegerOptional(valueOptional.get());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		IntegerParameterBuilder.class);

	@Reference
	private BlueprintsAttributesHelper _blueprintsAttributesHelper;

}