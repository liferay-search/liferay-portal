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

package com.liferay.portal.search.tuning.blueprints.admin.web.poc.internal.portlet.action;

import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.tuning.blueprints.admin.web.poc.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.admin.web.poc.internal.constants.BlueprintsAdminPortletKeys;
import com.liferay.portal.search.tuning.blueprints.admin.web.poc.internal.constants.BlueprintsAdminWebKeys;
import com.liferay.portal.search.tuning.blueprints.admin.web.poc.internal.display.context.BlueprintEntriesDisplayContext;
import com.liferay.portal.search.tuning.blueprints.admin.web.poc.internal.display.context.BlueprintEntriesManagementToolbarDisplayContext;
import com.liferay.portal.search.tuning.blueprints.constants.BlueprintTypes;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;

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
		"javax.portlet.name=" + BlueprintsAdminPortletKeys.BLUEPRINTS_ADMIN,
		"mvc.command.name=" + BlueprintsAdminMVCCommandNames.VIEW_BLUEPRINT,
		"mvc.command.name=/"
	},
	service = MVCRenderCommand.class
)
public class ViewBlueprintsMVCRenderCommand
	implements MVCRenderCommand {

	@Override
	public String render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		int blueprintType = ParamUtil.getInteger(
			renderRequest, BlueprintsAdminWebKeys.BLUEPRINT_TYPE,
			BlueprintTypes.BLUEPRINT);

		BlueprintEntriesDisplayContext
			blueprintEntriesDisplayContext =
				new BlueprintEntriesDisplayContext(
					_portal.getLiferayPortletRequest(renderRequest),
					_portal.getLiferayPortletResponse(renderResponse),
					blueprintType);

		renderRequest.setAttribute(
			BlueprintsAdminWebKeys.BLUEPRINT_ENTRIES_DISPLAY_CONTEXT,
			blueprintEntriesDisplayContext);

		try {
			BlueprintEntriesManagementToolbarDisplayContext
				blueprintsManagementToolbarDisplayContext =
					_getBlueprintsManagementToolbar(
						renderRequest, renderResponse,
						blueprintEntriesDisplayContext.
							getSearchContainer(),
						blueprintEntriesDisplayContext.
							getDisplayStyle(),
						blueprintType);

			renderRequest.setAttribute(
				BlueprintsAdminWebKeys.BLUEPRINT_ENTRIES_MANAGEMENT_TOOLBAR_DISPLAY_CONTEXT,
				blueprintsManagementToolbarDisplayContext);
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);

			SessionErrors.add(renderRequest, "errorDetails", portalException);
		}

		return "/view.jsp";
	}

	private BlueprintEntriesManagementToolbarDisplayContext
		_getBlueprintsManagementToolbar(
			RenderRequest renderRequest, RenderResponse renderResponse,
			SearchContainer<Blueprint> searchContainer,
			String displayStyle, int blueprintType) {

		return new BlueprintEntriesManagementToolbarDisplayContext(
			_portal.getHttpServletRequest(renderRequest),
			_portal.getLiferayPortletRequest(renderRequest),
			_portal.getLiferayPortletResponse(renderResponse), searchContainer,
			displayStyle, blueprintType);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ViewBlueprintsMVCRenderCommand.class);

	@Reference
	private Portal _portal;

}