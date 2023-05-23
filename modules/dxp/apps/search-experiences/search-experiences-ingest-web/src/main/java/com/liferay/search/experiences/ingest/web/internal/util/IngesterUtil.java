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

package com.liferay.search.experiences.ingest.web.internal.util;

import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.search.experiences.ingest.web.internal.iterator.LoopingIterator;

import javax.portlet.ActionRequest;

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