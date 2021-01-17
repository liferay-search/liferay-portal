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
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminTabNames;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminWebKeys;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.display.context.ViewBlueprintsDisplayContext;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.display.context.ViewBlueprintsManagementToolbarDisplayContext;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.display.context.ViewFragmentsManagementToolbarDisplayContext;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.util.BlueprintsAdminIndexHelper;
import com.liferay.portal.search.tuning.blueprints.constants.BlueprintsPortletKeys;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + BlueprintsPortletKeys.BLUEPRINTS_ADMIN,
		"mvc.command.name=" + BlueprintsAdminMVCCommandNames.VIEW,
		"mvc.command.name=/"
	},
	service = MVCRenderCommand.class
)
public class ViewBlueprintsMVCRenderCommand implements MVCRenderCommand {

	@Override
	public String render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		String tab = ParamUtil.getString(
			renderRequest, "tabs", BlueprintsAdminTabNames.BLUEPRINTS);

		ViewBlueprintsDisplayContext viewBlueprintsDisplayContext =
			_getViewBlueprintsDisplayContext(
				renderRequest, renderResponse, tab);

		renderRequest.setAttribute(
			BlueprintsAdminWebKeys.VIEW_BLUEPRINTS_DISPLAY_CONTEXT,
			viewBlueprintsDisplayContext);

		if (tab.equals(BlueprintsAdminTabNames.BLUEPRINTS)) {
			_setBlueprintsManagementToolbar(
				renderRequest, renderResponse, viewBlueprintsDisplayContext);
		}
		else if (tab.equals(BlueprintsAdminTabNames.FRAGMENTS)) {
			_setFragmentsManagementToolbar(
				renderRequest, renderResponse, viewBlueprintsDisplayContext);
		}

		return "/view.jsp";
	}

	private ViewBlueprintsDisplayContext _getViewBlueprintsDisplayContext(
		RenderRequest renderRequest, RenderResponse renderResponse,
		String tab) {

		return new ViewBlueprintsDisplayContext(
			_blueprintsAdminIndexHelper, _blueprintService,
			_portal.getLiferayPortletRequest(renderRequest),
			_portal.getLiferayPortletResponse(renderResponse), tab);
	}

	private void _setBlueprintsManagementToolbar(
		RenderRequest renderRequest, RenderResponse renderResponse,
		ViewBlueprintsDisplayContext viewBlueprintsDisplayContext) {

		try {
			ViewBlueprintsManagementToolbarDisplayContext
				viewBlueprintsManagementToolbarDisplayContext =
					new ViewBlueprintsManagementToolbarDisplayContext(
						_portal.getHttpServletRequest(renderRequest),
						_portal.getLiferayPortletRequest(renderRequest),
						_portal.getLiferayPortletResponse(renderResponse),
						viewBlueprintsDisplayContext.getSearchContainer(),
						viewBlueprintsDisplayContext.getDisplayStyle());

			renderRequest.setAttribute(
				BlueprintsAdminWebKeys.
					VIEW_BLUEPRINTS_MANAGEMENT_TOOLBAR_DISPLAY_CONTEXT,
				viewBlueprintsManagementToolbarDisplayContext);
		}
		catch (PortalException | PortletException exception) {
			_log.error(exception.getMessage(), exception);

			SessionErrors.add(
				renderRequest, BlueprintsAdminWebKeys.ERROR,
				exception.getMessage());
		}
	}

	private void _setFragmentsManagementToolbar(
		RenderRequest renderRequest, RenderResponse renderResponse,
		ViewBlueprintsDisplayContext viewBlueprintsDisplayContext) {

		try {
			ViewFragmentsManagementToolbarDisplayContext
				viewFragmentEntriesManagementToolbarDisplayContext =
					new ViewFragmentsManagementToolbarDisplayContext(
						_portal.getHttpServletRequest(renderRequest),
						_portal.getLiferayPortletRequest(renderRequest),
						_portal.getLiferayPortletResponse(renderResponse),
						viewBlueprintsDisplayContext.getSearchContainer(),
						viewBlueprintsDisplayContext.getDisplayStyle());

			renderRequest.setAttribute(
				BlueprintsAdminWebKeys.
					VIEW_FRAGMENTS_MANAGEMENT_TOOLBAR_DISPLAY_CONTEXT,
				viewFragmentEntriesManagementToolbarDisplayContext);
		}
		catch (PortalException | PortletException exception) {
			_log.error(exception.getMessage(), exception);

			SessionErrors.add(
				renderRequest, BlueprintsAdminWebKeys.ERROR,
				exception.getMessage());
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ViewBlueprintsMVCRenderCommand.class);

	@Reference
	private BlueprintsAdminIndexHelper _blueprintsAdminIndexHelper;

	@Reference
	private BlueprintService _blueprintService;

	@Reference
	private Portal _portal;

}