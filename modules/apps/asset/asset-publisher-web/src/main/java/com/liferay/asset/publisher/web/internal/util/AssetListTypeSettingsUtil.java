/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.publisher.web.internal.util;

import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;

import jakarta.portlet.PortletPreferences;

import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * @author Akhash Ramprakash
 */
public class AssetListTypeSettingsUtil {

	public static String getTypeSettings(
		long defaultGroupId, PortletPreferences portletPreferences) {

		UnicodeProperties unicodeProperties = new UnicodeProperties(true);

		Enumeration<String> enumeration = portletPreferences.getNames();

		while (enumeration.hasMoreElements()) {
			String name = enumeration.nextElement();

			String value = StringUtil.merge(
				portletPreferences.getValues(name, null));

			if (Validator.isNull(value) || name.contains("email")) {
				continue;
			}

			if (!name.equals("scopeIds")) {
				unicodeProperties.put(name, value);

				continue;
			}

			List<Long> groupIds = TransformUtil.transformToList(
				value.split(StringPool.COMMA),
				part -> {
					if (part.equals("Group_default")) {
						return defaultGroupId;
					}

					if (!part.startsWith("Group_")) {
						return null;
					}

					long groupId = GetterUtil.getLong(
						StringUtil.removeSubstring(part, "Group_"), -1);

					if (groupId != -1) {
						return groupId;
					}

					return null;
				});

			if (groupIds.isEmpty()) {
				continue;
			}

			unicodeProperties.put(
				"groupIds", ListUtil.toString(groupIds, StringPool.BLANK));
		}

		_sanitizeClassNameIds(unicodeProperties);

		if (Validator.isNull(unicodeProperties.getProperty("anyAssetType"))) {
			unicodeProperties.put("anyAssetType", Boolean.TRUE.toString());
		}

		return unicodeProperties.toString();
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

	private static void _sanitizeClassNameIds(
		UnicodeProperties unicodeProperties) {

		String anyAssetType = unicodeProperties.getProperty("anyAssetType");

		if (!_isResolvable(GetterUtil.getLong(anyAssetType), "anyAssetType")) {
			unicodeProperties.remove("anyAssetType");
		}

		String[] classNameIds = StringUtil.split(
			unicodeProperties.getProperty("classNameIds"));

		if (ArrayUtil.isEmpty(classNameIds)) {
			return;
		}

		String[] existingClassNameIds = TransformUtil.transform(
			classNameIds,
			classNameId -> {
				if (_isResolvable(
						GetterUtil.getLong(classNameId), "classNameIds")) {

					return classNameId;
				}

				return null;
			},
			String.class);

		if (ArrayUtil.isNotEmpty(existingClassNameIds)) {
			unicodeProperties.put(
				"classNameIds", StringUtil.merge(existingClassNameIds));

			return;
		}

		unicodeProperties.remove("classNameIds");

		if (Objects.equals(anyAssetType, Boolean.FALSE.toString())) {
			unicodeProperties.remove("anyAssetType");
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AssetListTypeSettingsUtil.class);

}