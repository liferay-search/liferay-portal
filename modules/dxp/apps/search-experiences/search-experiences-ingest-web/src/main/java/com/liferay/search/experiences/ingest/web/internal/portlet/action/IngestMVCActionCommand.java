/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.ingest.web.internal.portlet.action;

import com.liferay.exportimport.kernel.lar.ExportImportThreadLocal;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.search.experiences.ingest.web.internal.constants.IngestPortletKeys;
import com.liferay.search.experiences.ingest.web.internal.constants.MVCActionCommandNames;
import com.liferay.search.experiences.ingest.web.internal.ingester.Ingester;
import com.liferay.search.experiences.ingest.web.internal.ingester.IngesterFactory;
import com.liferay.search.experiences.ingest.web.internal.stats.IngestionStats;

import jakarta.portlet.ActionRequest;
import jakarta.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	enabled = false,
	property = {
		"jakarta.portlet.name=" + IngestPortletKeys.INGEST,
		"mvc.command.name=" + MVCActionCommandNames.INGEST
	},
	service = MVCActionCommand.class
)
public class IngestMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		ExportImportThreadLocal.setPortletImportInProcess(true);

		long startTimeMillis = System.currentTimeMillis();

		Ingester ingester = _ingesterFactory.getIngester(
			ParamUtil.getString(actionRequest, "type"));

		IngestionStats ingestionStats = ingester.ingest(actionRequest);

		ingestionStats.setSecondsElapsed(
			(System.currentTimeMillis() - startTimeMillis) / 1000);

		ExportImportThreadLocal.setPortletImportInProcess(false);

		actionRequest.setAttribute("ingestionStats", ingestionStats);
	}

	@Reference
	private IngesterFactory _ingesterFactory;

}