/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.list.internal.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.kernel.util.Validator;

import java.util.Objects;

/**
 * @author Felipe Lorenz
 */
public class AssetListFiltersMigrationUtil {

	public static String toFiltersTypeSettings(String typeSettings) {
		if (Validator.isNull(typeSettings)) {
			return typeSettings;
		}

		UnicodeProperties unicodeProperties = UnicodePropertiesBuilder.create(
			true
		).fastLoad(
			typeSettings
		).build();

		JSONArray filtersJSONArray = JSONFactoryUtil.createJSONArray();

		for (int i = 0; true; i++) {
			String[] queryValues = StringUtil.split(
				unicodeProperties.getProperty("queryValues" + i, null));

			if (queryValues.length == 0) {
				break;
			}

			String queryName = unicodeProperties.getProperty("queryName" + i);

			if (!_isAssetFilterQueryName(queryName)) {
				continue;
			}

			filtersJSONArray.put(
				_toFilterJSONObject(
					GetterUtil.getBoolean(
						unicodeProperties.getProperty("queryAndOperator" + i)),
					GetterUtil.getBoolean(
						unicodeProperties.getProperty("queryContains" + i)),
					queryName, queryValues));
		}

		if (filtersJSONArray.length() == 0) {
			return typeSettings;
		}

		unicodeProperties.put("filters", filtersJSONArray.toString());

		return unicodeProperties.toString();
	}

	private static boolean _isAssetFilterQueryName(String queryName) {
		if (Objects.equals(queryName, "assetCategories") ||
			Objects.equals(queryName, "assetTags") ||
			Objects.equals(queryName, "keywords")) {

			return true;
		}

		return false;
	}

	private static JSONObject _toFilterJSONObject(
		boolean queryAndOperator, boolean queryContains, String queryName,
		String[] queryValues) {

		JSONObject filterJSONObject = JSONUtil.put(
			"operatorName", queryContains ? "contains" : "not-contains"
		).put(
			"propertyName", queryName
		).put(
			"quantifier", queryAndOperator ? "all" : "any"
		);

		if (Objects.equals(queryName, "keywords")) {
			return filterJSONObject.put(
				"value", StringUtil.merge(queryValues, StringPool.SPACE));
		}

		JSONArray valueJSONArray = JSONFactoryUtil.createJSONArray();

		for (String queryValue : queryValues) {
			valueJSONArray.put(
				JSONUtil.put(
					"label", queryValue
				).put(
					"value", queryValue
				));
		}

		return filterJSONObject.put("value", valueJSONArray);
	}

}