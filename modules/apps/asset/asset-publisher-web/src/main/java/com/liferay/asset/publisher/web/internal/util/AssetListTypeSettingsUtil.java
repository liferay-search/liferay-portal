/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.publisher.web.internal.util;

import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Akhash Ramprakash
 */
public class AssetListTypeSettingsUtil {

	public static void sanitizeClassNameIds(
		UnicodeProperties unicodeProperties) {

		String anyAssetType = unicodeProperties.getProperty("anyAssetType");

		if (!_isResolvable(GetterUtil.getLong(anyAssetType), "anyAssetType")) {
			unicodeProperties.remove("anyAssetType");

			anyAssetType = null;
		}

		String classNameIds = unicodeProperties.getProperty("classNameIds");

		if (Validator.isNull(classNameIds)) {
			return;
		}

		classNameIds = StringUtil.merge(
			TransformUtil.transform(
				StringUtil.split(classNameIds),
				classNameId -> {
					if (_isResolvable(
							GetterUtil.getLong(classNameId), "classNameIds")) {

						return classNameId;
					}

					return null;
				},
				String.class));

		if (Validator.isNotNull(classNameIds)) {
			unicodeProperties.put("classNameIds", classNameIds);

			return;
		}

		if (!GetterUtil.getBoolean(anyAssetType, true) &&
			(GetterUtil.getLong(anyAssetType) <= 0)) {

			return;
		}

		unicodeProperties.remove("classNameIds");
	}

	private static boolean _isResolvable(long classNameId, String name) {
		if ((classNameId <= 0) ||
			(ClassNameLocalServiceUtil.fetchClassName(classNameId) != null)) {

			return true;
		}

		if (_log.isWarnEnabled()) {
			_log.warn(
				StringBundler.concat(
					"Unable to resolve class name ID ", classNameId,
					" in the \"", name, "\" preference"));
		}

		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AssetListTypeSettingsUtil.class);

}