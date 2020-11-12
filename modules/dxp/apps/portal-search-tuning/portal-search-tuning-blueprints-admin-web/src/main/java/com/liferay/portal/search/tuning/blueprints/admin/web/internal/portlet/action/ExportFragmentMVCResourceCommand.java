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

package com.liferay.portal.search.tuning.blueprints.admin.web.internal.portlet.action;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminWebKeys;
import com.liferay.portal.search.tuning.blueprints.constants.BlueprintsPortletKeys;
import com.liferay.portal.search.tuning.blueprints.exception.NoSuchBlueprintException;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;

import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + BlueprintsPortletKeys.BLUEPRINTS_ADMIN,
		"mvc.command.name=" + BlueprintsAdminMVCCommandNames.EXPORT_FRAGMENT
	},
	service = MVCResourceCommand.class
)
public class ExportFragmentMVCResourceCommand extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		long blueprintId = ParamUtil.getLong(
			resourceRequest, BlueprintsAdminWebKeys.BLUEPRINT_ID);

		try {
			Blueprint blueprint = _blueprintService.getBlueprint(blueprintId);

			String configuration = _getConfiguration(blueprint);

			String title = _getFileTitle(resourceRequest, blueprint);

			_writeResponse(
				resourceRequest, resourceResponse, title, configuration);
		}
		catch (NoSuchBlueprintException noSuchBlueprintException) {
			_log.error(
				"Blueprint " + blueprintId + " not found",
				noSuchBlueprintException);

			SessionErrors.add(
				resourceRequest, BlueprintsAdminWebKeys.ERROR_DETAILS,
				noSuchBlueprintException);

			_log.error(
				noSuchBlueprintException.getMessage(),
				noSuchBlueprintException);
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);

			SessionErrors.add(
				resourceRequest, BlueprintsAdminWebKeys.ERROR_DETAILS,
				portalException);
		}
	}

	private String _getConfiguration(Blueprint blueprint) {
		String configuration = blueprint.getConfiguration();

		try {
			JSONObject jsonObject = _jsonFactory.createJSONObject(
				configuration);

			return jsonObject.toString(4);
		}
		catch (JSONException jsonException) {
			_log.error(jsonException.getMessage(), jsonException);
		}

		return configuration;
	}

	private String _getFileTitle(
		ResourceRequest resourceRequest, Blueprint blueprint) {

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String title = blueprint.getTitle(themeDisplay.getLocale(), true);

		return title + ".json";
	}

	private void _writeResponse(
		ResourceRequest resourceRequest, ResourceResponse resourceResponse,
		String title, String configuration) {

		HttpServletResponse httpServletResponse =
			_portal.getHttpServletResponse(resourceResponse);

		try {
			PrintWriter out = httpServletResponse.getWriter();

			httpServletResponse.setContentType("application/json");
			httpServletResponse.setCharacterEncoding("UTF-8");
			httpServletResponse.setHeader(
				"Content-disposition", "attachment; filename=" + title);

			out.print(configuration);
			out.flush();
		}
		catch (IOException ioException) {
			_log.error(ioException.getMessage(), ioException);

			SessionErrors.add(
				resourceRequest, BlueprintsAdminWebKeys.ERROR_DETAILS,
				ioException);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ExportFragmentMVCResourceCommand.class);

	@Reference
	private BlueprintService _blueprintService;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Portal _portal;

}