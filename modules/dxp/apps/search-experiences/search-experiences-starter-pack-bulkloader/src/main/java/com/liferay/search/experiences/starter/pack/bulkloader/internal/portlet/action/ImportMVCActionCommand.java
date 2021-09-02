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

package com.liferay.search.experiences.starter.pack.bulkloader.internal.portlet.action;

import com.liferay.exportimport.kernel.lar.ExportImportThreadLocal;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.search.experiences.starter.pack.bulkloader.internal.constants.BulkloaderPortletKeys;
import com.liferay.search.experiences.starter.pack.bulkloader.internal.constants.ImportTypeKeys;
import com.liferay.search.experiences.starter.pack.bulkloader.internal.constants.MVCActionCommandNames;
import com.liferay.search.experiences.starter.pack.bulkloader.internal.util.GooglePlacesImporter;
import com.liferay.search.experiences.starter.pack.bulkloader.internal.util.WikipediaImporter;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + BulkloaderPortletKeys.BULK_LOADER,
		"mvc.command.name=" + MVCActionCommandNames.IMPORT
	},
	service = MVCActionCommand.class
)
public class ImportMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		String importType = ParamUtil.getString(actionRequest, "type");

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		List<Long> userIds = _toIdList(
			ParamUtil.getString(
				actionRequest, "userIds",
				String.valueOf(themeDisplay.getUserId())));

		List<Long> groupIds = _toIdList(
			ParamUtil.getString(
				actionRequest, "groupIds",
				String.valueOf(themeDisplay.getScopeGroupId())));

		String languageId = ParamUtil.getString(
			actionRequest, "languageId", "en_US");

		// Disable link validation

		ExportImportThreadLocal.setPortletImportInProcess(true);

		long timeMillis = System.currentTimeMillis();

		if (importType.equals(ImportTypeKeys.WIKIPEDIA_ARTICLES)) {
			_wikipediaImporter.doImport(
				actionRequest, actionResponse, userIds, groupIds, languageId);
		}
		else {
			_googlePlacesImporter.doImport(
				actionRequest, actionResponse, userIds, groupIds, languageId,
				importType);
		}

		ExportImportThreadLocal.setPortletImportInProcess(false);

		if (_log.isInfoEnabled()) {
			_log.info("Finished data import in " + (timeMillis / 1000) + " s");
		}
	}

	private List<Long> _toIdList(String ids) throws Exception {
		String[] arr = ids.split(",");

		List<Long> values = new ArrayList<>();

		for (String s : arr) {
			s = StringUtil.trim(s);

			values.add(Long.valueOf(s));
		}

		return values;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ImportMVCActionCommand.class);

	@Reference
	private GooglePlacesImporter _googlePlacesImporter;

	@Reference
	private WikipediaImporter _wikipediaImporter;

}