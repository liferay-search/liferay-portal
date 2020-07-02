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

package com.liferay.portal.search.tuning.gsearch.impl.internal.request.parameter.contributor;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.ParameterConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.parameter.LongArrayParameter;
import com.liferay.portal.search.tuning.gsearch.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.gsearch.util.GSearchJsonUtil;

import java.util.Arrays;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "type=long_array",
	service = RequestParameterContributor.class
)
public class LongArrayParameterContributor
	implements RequestParameterContributor {

	@Override
	public void contribute(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData,
		JSONObject configurationJsonObject) {

		String parameterName = configurationJsonObject.getString(
			ParameterConfigurationKeys.PARAMETER_NAME);

		String[] stringValueArray = ParamUtil.getStringValues(
			httpServletRequest, parameterName);

		if ((stringValueArray == null) || (stringValueArray.length == 0)) {
			Optional<String[]> valueOptional =
				GSearchJsonUtil.getStringArrayOptional(
					configurationJsonObject,
					ParameterConfigurationKeys.DEFAULT);

			if (valueOptional.isPresent()) {
				stringValueArray = valueOptional.get();
			}
		}

		try {
			Long[] longArray = Arrays.stream(
				stringValueArray
			).map(
				Long::valueOf
			).toArray(
				Long[]::new
			);

			String parameterRole = configurationJsonObject.getString(
				ParameterConfigurationKeys.ROLE);

			searchParameterData.addParameter(
				new LongArrayParameter(
					parameterName, parameterRole,
					"${request." + parameterName + "}", longArray));
		}
		catch (NumberFormatException numberFormatException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					numberFormatException.getMessage(), numberFormatException);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LongArrayParameterContributor.class);

}