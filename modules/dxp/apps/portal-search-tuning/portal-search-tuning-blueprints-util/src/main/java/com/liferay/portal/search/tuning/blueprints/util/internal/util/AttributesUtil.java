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

package com.liferay.portal.search.tuning.blueprints.util.internal.util;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.parameter.KeywordsConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.parameter.PageConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.parameter.ParameterConfigurationKeys;

/**
 * @author Petteri Karttunen
 */
public class AttributesUtil {

	public static String getKeywordParameterName(
		JSONObject parameterConfigurationJSONObject) {

		String defaultParameterName = "q";

		JSONObject keywordConfigurationJSONObject =
			parameterConfigurationJSONObject.getJSONObject(
				ParameterConfigurationKeys.KEYWORDS.getJsonKey());

		if (keywordConfigurationJSONObject == null) {
			return defaultParameterName;
		}

		return keywordConfigurationJSONObject.getString(
			KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey(),
			defaultParameterName);
	}

	public static String getPageParameterName(
		JSONObject configurationJSONObject) {

		String defaultParameterName = "page";

		JSONObject pagingConfigurationJSONObject =
			configurationJSONObject.getJSONObject(
				ParameterConfigurationKeys.PAGE.getJsonKey());

		if (pagingConfigurationJSONObject == null) {
			return defaultParameterName;
		}

		return pagingConfigurationJSONObject.getString(
			PageConfigurationKeys.PARAMETER_NAME.getJsonKey(),
			defaultParameterName);
	}

}