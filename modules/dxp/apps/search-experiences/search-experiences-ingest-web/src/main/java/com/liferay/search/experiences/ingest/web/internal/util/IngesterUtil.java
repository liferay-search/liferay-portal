/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.ingest.web.internal.util;

import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.search.experiences.ingest.web.internal.iterator.LoopingIterator;

import jakarta.portlet.ActionRequest;

/**
 * @author Petteri Karttunen
 */
public class IngesterUtil {

	public static LoopingIterator<Long> getGroupIdsLoopingIterator(
		ActionRequest actionRequest) {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		return new LoopingIterator<>(
			CSVUtil.csvToLongList(
				ParamUtil.getString(
					actionRequest, "groupIds",
					String.valueOf(themeDisplay.getScopeGroupId()))));
	}

	public static ServiceContext getServiceContext(ActionRequest actionRequest)
		throws PortalException {

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			JournalArticle.class.getName(), actionRequest);

		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);
		serviceContext.setLanguageId(
			ParamUtil.getString(actionRequest, "languageId", "en_US"));

		return serviceContext;
	}

	public static LoopingIterator<Long> getUserIdsLoopingIterator(
		ActionRequest actionRequest) {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		return new LoopingIterator<>(
			CSVUtil.csvToLongList(
				ParamUtil.getString(
					actionRequest, "userIds",
					String.valueOf(themeDisplay.getUserId()))));
	}

}