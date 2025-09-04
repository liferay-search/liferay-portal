/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.ingest.web.internal.util;

import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.expando.kernel.util.ExpandoBridgeFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.portlet.PortletRequest;

/**
 * @author Petteri Karttunen
 */
public class ExpandoUtil {

	public static void createGeoLocationExpandoAttribute(
			String expandoAttributeName, Class<?> clazz,
			PortletRequest portletRequest)
		throws PortalException {

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		ExpandoBridge expandoBridge = ExpandoBridgeFactoryUtil.getExpandoBridge(
			themeDisplay.getCompanyId(), clazz.getName());

		if (!expandoBridge.hasAttribute(expandoAttributeName)) {
			expandoBridge.addAttribute(
				expandoAttributeName, ExpandoColumnConstants.GEOLOCATION,
				JSONUtil.put(
					"latitude", 0D
				).put(
					"longitude", 0D
				),
				false);

			UnicodeProperties unicodeProperties =
				expandoBridge.getAttributeProperties(expandoAttributeName);

			unicodeProperties.setProperty(
				ExpandoColumnConstants.INDEX_TYPE,
				String.valueOf(ExpandoColumnConstants.INDEX_TYPE_KEYWORD));

			unicodeProperties.setProperty(
				ExpandoColumnConstants.PROPERTY_LOCALIZE_FIELD_NAME, "false");

			expandoBridge.setAttributeProperties(
				expandoAttributeName, unicodeProperties);
		}
	}

}