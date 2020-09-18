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

package com.liferay.portal.search.tuning.blueprints.facets.internal.request.handler;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterDataBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.StringArrayParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.StringParameter;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.spi.request.FacetRequestHandler;
import com.liferay.portal.search.tuning.blueprints.message.Messages;

import java.util.Optional;

/**
 * @author Petteri Karttunen
 */
public abstract class BaseFacetRequestHandler implements FacetRequestHandler {

	@Override
	public Optional<Parameter> getParameter(
		BlueprintsAttributes blueprintsAttributes,
		Messages messages, JSONObject configurationJsonObject) {

		String parameterName = configurationJsonObject.getString(
			FacetConfigurationKeys.PARAMETER_NAME.getJsonKey());

		Optional<Object> valueOptional =
			blueprintsAttributes.getAttributeOptional(parameterName);

		if (!valueOptional.isPresent()) {
			return Optional.empty();
		}

		boolean multiValue = configurationJsonObject.getBoolean(
			FacetConfigurationKeys.MULTI_VALUE.getJsonKey(), true);

		if (multiValue) {
			String[] value = GetterUtil.getStringValues(valueOptional.get());

			if (value.length > 0) {
				Parameter parameter = new StringArrayParameter(
					parameterName, null, value);

				return Optional.of(parameter);
			}
		}
		else {
			String value = GetterUtil.getString(valueOptional.get());

			if (!Validator.isBlank(value)) {
				Parameter parameter = new StringParameter(
					parameterName, null, value);

				return Optional.of(parameter);
			}
		}

		return Optional.empty();
	}

}