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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	enabled = false,
	property = {
		"javax.portlet.name=" + IngestPortletKeys.INGEST,
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