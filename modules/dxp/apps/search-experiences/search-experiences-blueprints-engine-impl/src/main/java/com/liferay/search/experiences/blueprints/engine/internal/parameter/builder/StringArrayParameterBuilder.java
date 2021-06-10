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
import com.liferay.search.experiences.blueprints.constants.json.keys.parameter.CustomParameterConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.internal.attributes.util.BlueprintsAttributesHelper;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.StringArrayParameter;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.util.util.BlueprintJSONUtil;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=string_array",
	service = ParameterBuilder.class
)
public class StringArrayParameterBuilder implements ParameterBuilder {

	@Override
	public Optional<Parameter> build(
		BlueprintsAttributes blueprintsAttributes, JSONObject jsonObject,
		Messages messages) {

		String parameterName = jsonObject.getString(
			CustomParameterConfigurationKeys.PARAMETER_NAME.getJsonKey());

		Optional<String[]> valueOptional = _getValueOptional(
			blueprintsAttributes, jsonObject, parameterName);

		if (!valueOptional.isPresent()) {
			return Optional.empty();
		}

		return Optional.of(
			new StringArrayParameter(
				parameterName, "${parameter." + parameterName + "}",
				valueOptional.get()));
	}

	private Optional<String[]> _getValueOptional(
		BlueprintsAttributes blueprintsAttributes, JSONObject jsonObject,
		String parameterName) {

		Optional<String[]> optional =
			_blueprintsAttributesHelper.getStringArrayOptional(
				blueprintsAttributes, parameterName);

		if (!optional.isPresent()) {
			optional = BlueprintJSONUtil.getStringArray(
				jsonObject,
				CustomParameterConfigurationKeys.DEFAULT.getJsonKey());
		}

		return optional;
	}

	@Reference
	private BlueprintsAttributesHelper _blueprintsAttributesHelper;

}