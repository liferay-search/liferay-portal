/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.publisher.web.internal.util;

import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;

/**
 * @author Akhash Ramprakash
 */
public class AssetListTypeSettingsUtil {

	public static String sanitizeClassNameIds(String name, String value) {
		return StringUtil.merge(
			TransformUtil.transform(
				StringUtil.split(value),
				part -> {
					long classNameId = GetterUtil.getLong(part);

					if (classNameId <= 0) {
						return part;
					}

					ClassName className =
						ClassNameLocalServiceUtil.fetchClassName(classNameId);

					if (className != null) {
						return part;
					}

					if (_log.isWarnEnabled()) {
						_log.warn(
							StringBundler.concat(
								"Dropping unresolvable class name ID ",
								classNameId, " from the \"", name,
								"\" preference"));
					}

					return null;
				},
				String.class));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AssetListTypeSettingsUtil.class);

}