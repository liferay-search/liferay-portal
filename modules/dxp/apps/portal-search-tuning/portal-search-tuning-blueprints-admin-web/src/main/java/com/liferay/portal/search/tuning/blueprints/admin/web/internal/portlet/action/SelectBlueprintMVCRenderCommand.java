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
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminWebKeys;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.display.context.SelectBlueprintDisplayContext;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.display.context.SelectBlueprintManagementToolbarDisplayContext;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.util.BlueprintsAdminIndexHelper;
import com.liferay.portal.search.tuning.blueprints.constants.BlueprintsPortletKeys;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Kevin Tan
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + BlueprintsPortletKeys.BLUEPRINTS_ADMIN,
		"mvc.command.name=" + BlueprintsAdminMVCCommandNames.SELECT_BLUEPRINT
	},
	service = MVCRenderCommand.class
)
public class SelectBlueprintMVCRenderCommand implements MVCRenderCommand {

	@Override
	public String render(
		RenderRequest renderRequest, RenderResponse renderResponse) {

		SelectBlueprintDisplayContext selectBlueprintDisplayContext =
			new SelectBlueprintDisplayContext(
				_blueprintsAdminIndexHelper, _blueprintService,
				_portal.getLiferayPortletRequest(renderRequest),
				_portal.getLiferayPortletResponse(renderResponse));

		renderRequest.setAttribute(
			BlueprintsAdminWebKeys.SELECT_BLUEPRINT_DISPLAY_CONTEXT,
			selectBlueprintDisplayContext);

		try {
			SelectBlueprintManagementToolbarDisplayContext
				selectBlueprintManagementToolbarDisplayContext =
					new SelectBlueprintManagementToolbarDisplayContext(
						_portal.getLiferayPortletRequest(renderRequest),
						_portal.getLiferayPortletResponse(renderResponse),
						selectBlueprintDisplayContext.getSearchContainer());

			renderRequest.setAttribute(
				BlueprintsAdminWebKeys.
					SELECT_BLUEPRINT_MANAGEMENT_TOOLBAR_DISPLAY_CONTEXT,
				selectBlueprintManagementToolbarDisplayContext);
		}
		catch (PortalException | PortletException exception) {
			_log.error(exception.getMessage(), exception);

			SessionErrors.add(
				renderRequest, BlueprintsAdminWebKeys.ERROR,
				exception.getMessage());
		}

		return "/select_blueprint.jsp";
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SelectBlueprintMVCRenderCommand.class);

	@Reference
	private BlueprintsAdminIndexHelper _blueprintsAdminIndexHelper;

	@Reference
	private BlueprintService _blueprintService;

	@Reference
	private Portal _portal;

}