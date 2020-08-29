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

package com.liferay.portal.search.tuning.blueprints.engine.internal.request.parameter.contributor;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.requestparameter.CommonRequestParameterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.internal.util.BlueprintValueUtil;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.StringParameter;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "type=string",
	service = RequestParameterContributor.class
)
public class StringParameterContributor implements RequestParameterContributor {

	@Override
	public void contribute(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData,
		JSONObject configurationJsonObject) {

		String parameterName = configurationJsonObject.getString(
			CommonRequestParameterConfigurationKeys.PARAMETER_NAME.
				getJsonKey());

		String value = ParamUtil.getString(httpServletRequest, parameterName);

		Optional<String> valueOptional = BlueprintValueUtil.toStringOptional(
			value);

		if (!valueOptional.isPresent()) {
			valueOptional = BlueprintValueUtil.toStringOptional(
				configurationJsonObject.getString(
					CommonRequestParameterConfigurationKeys.DEFAULT.
						getJsonKey()));
		}

		if (!valueOptional.isPresent()) {
			return;
		}

		searchParameterData.addParameter(
			new StringParameter(
				parameterName, "${request." + parameterName + "}", value));
	}

}