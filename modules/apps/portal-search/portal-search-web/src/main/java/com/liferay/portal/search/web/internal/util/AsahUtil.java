/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.search.web.internal.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Eduardo García
 */
public class AsahUtil {

	public static String getAsahFaroBackendDataSourceId(long companyId) {
		return PrefsPropsUtil.getString(
			companyId, "liferayAnalyticsDataSourceId");
	}

	public static String getAsahFaroBackendSecuritySignature(long companyId) {
		return PrefsPropsUtil.getString(
			companyId, "liferayAnalyticsFaroBackendSecuritySignature");
	}

	public static String getAsahFaroBackendURL(long companyId) {
		return PrefsPropsUtil.getString(
			companyId, "liferayAnalyticsFaroBackendURL");
	}

	public static boolean isAnalyticsEnabled(long companyId) {
		if (Validator.isNull(getAsahFaroBackendDataSourceId(companyId)) ||
			Validator.isNull(getAsahFaroBackendSecuritySignature(companyId)) ||
			Validator.isNull(getAsahFaroBackendURL(companyId))) {

			return false;
		}

		return true;
	}

	public static boolean isAnalyticsEnabled(long companyId, long groupId) {
		if (!isAnalyticsEnabled(companyId)) {
			return false;
		}

		if (PrefsPropsUtil.getBoolean(
				companyId, "liferayAnalyticsEnableAllGroupIds")) {

			return true;
		}

		String[] liferayAnalyticsGroupIds = PrefsPropsUtil.getStringArray(
			companyId, "liferayAnalyticsGroupIds", StringPool.COMMA);

		if (ArrayUtil.contains(
				liferayAnalyticsGroupIds, String.valueOf(groupId))) {

			return true;
		}

		return false;
	}

}