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

package com.liferay.portal.search.tuning.blueprints.facets.internal.handler.request;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.StringArrayParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.StringParameter;
import com.liferay.portal.search.tuning.blueprints.engine.spi.facet.FacetRequestHandler;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public abstract class BaseFacetRequestHandler implements FacetRequestHandler {

	@Override
	public Optional<Parameter> getParameter(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData,
		JSONObject configurationJsonObject) {

		String parameterName = configurationJsonObject.getString(
			FacetConfigurationKeys.PARAMETER_NAME.getJsonKey());

		boolean multiValue = configurationJsonObject.getBoolean(
			FacetConfigurationKeys.MULTI_VALUE.getJsonKey(), true);

		if (multiValue) {
			String[] value = ParamUtil.getStringValues(
				httpServletRequest, parameterName, null);

			if (value != null) {
				Parameter parameter = new StringArrayParameter(
					parameterName, null, value);

				return Optional.of(parameter);
			}
		}
		else {
			String value = ParamUtil.getString(
				httpServletRequest, parameterName);

			if (!Validator.isBlank(value)) {
				Parameter parameter = new StringParameter(
					parameterName, null, value);

				return Optional.of(parameter);
			}
		}

		return Optional.empty();
	}

}