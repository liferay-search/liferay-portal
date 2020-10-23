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

package com.liferay.portal.search.tuning.blueprints.admin.web.internal.util;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;

/**
 * @author Petteri Karttunen
 */
public class BlueprintJSONUtil {

	public static String[] getConfigurationSection(
			Blueprint blueprint, String key)
		throws JSONException {

		JSONArray jsonArray = _getSectionJSONArray(blueprint, key);

		if (jsonArray == null) {
			return new String[0];
		}

		String[] array = new String[jsonArray.length()];

		for (int i = 0; i < jsonArray.length(); i++) {
			array[i] = jsonArray.getJSONObject(
				i
			).toString(
				4
			);
		}

		return array;
	}

	private static JSONArray _getSectionJSONArray(
			Blueprint blueprint, String key)
		throws JSONException {

		if ((blueprint == null) ||
			Validator.isBlank(blueprint.getConfiguration())) {

			return null;
		}

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
			blueprint.getConfiguration());

		if (jsonObject == null) {
			return null;
		}

		return jsonObject.getJSONArray(key);
	}

}