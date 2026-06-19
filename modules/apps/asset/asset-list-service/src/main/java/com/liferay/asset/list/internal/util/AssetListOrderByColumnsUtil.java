/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.list.internal.util;

import com.liferay.object.model.ObjectField;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Locale;

/**
 * @author Joshua Cords
 */
public class AssetListOrderByColumnsUtil {

	public static String toOrderByColumn(
		long companyId, String orderByColumn, Locale locale) {

		if (Validator.isNull(orderByColumn) ||
			!orderByColumn.startsWith(StringPool.OPEN_CURLY_BRACE)) {

			return orderByColumn;
		}

		JSONObject jsonObject = null;

		try {
			jsonObject = JSONFactoryUtil.createJSONObject(orderByColumn);
		}
		catch (JSONException jsonException) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Unable to parse order by column: " + orderByColumn,
					jsonException);
			}

			return orderByColumn;
		}

		ObjectField objectField = AssetListObjectFieldUtil.fetchObjectField(
			jsonObject.getLong("classNameId"), companyId,
			jsonObject.getString("propertyName"));

		if (objectField == null) {
			return orderByColumn;
		}

		String subfield = AssetListObjectFieldUtil.getSubfield(
			locale, objectField);

		return StringBundler.concat(
			"nestedFieldArray.", objectField.getName(), StringPool.PERIOD,
			subfield.substring(subfield.indexOf(CharPool.PERIOD) + 1));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AssetListOrderByColumnsUtil.class);

}