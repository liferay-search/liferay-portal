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

import com.liferay.exportimport.kernel.exception.NoSuchConfigurationException;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminWebKeys;
import com.liferay.portal.search.tuning.blueprints.constants.BlueprintsPortletKeys;
import com.liferay.portal.search.tuning.blueprints.constants.BlueprintTypes;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
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
		"mvc.command.name=" + BlueprintsAdminMVCCommandNames.EDIT_BLUEPRINT
	},
	service = MVCRenderCommand.class
)
public class EditBlueprintMVCRenderCommand
	implements MVCRenderCommand {

	@Override
	public String render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		long blueprintId = ParamUtil.getLong(
			renderRequest, BlueprintsAdminWebKeys.BLUEPRINT_ID);

		int blueprintType = ParamUtil.getInteger(
			renderRequest, BlueprintsAdminWebKeys.BLUEPRINT_TYPE,
			BlueprintTypes.BLUEPRINT);

		Blueprint blueprint = null;

		if (blueprintId > 0) {
			try {
				blueprint =
					_blueprintService.getBlueprint(
						blueprintId);

				blueprintType = blueprint.getType();
			}
			catch (NoSuchConfigurationException noSuchConfigurationException) {
				_log.error(
					"Search configuration " + blueprintId +
						" not found.",
					noSuchConfigurationException);

				SessionErrors.add(
					renderRequest, BlueprintsAdminWebKeys.ERROR_DETAILS,
					noSuchConfigurationException);
			}
			catch (PortalException portalException) {
				_log.error(portalException.getMessage(), portalException);

				SessionErrors.add(
					renderRequest, BlueprintsAdminWebKeys.ERROR_DETAILS,
					portalException);
			}
		}

		renderRequest.setAttribute(
			BlueprintsAdminWebKeys.BLUEPRINT,
			blueprint);

		ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

		portletDisplay.setShowBackIcon(true);

		String redirect = ParamUtil.getString(renderRequest, "redirect");

		portletDisplay.setURLBack(redirect);

		String pageTitleKey = _getPageTitleKey(
			blueprint != null, blueprintType);

		renderRequest.setAttribute(
			BlueprintsAdminWebKeys.PAGE_TITLE_KEY, pageTitleKey);

		return "/edit_blueprint.jsp";
	}

	private String _getPageTitleKey(boolean edit, int type) {
		StringBundler sb = new StringBundler(2);

		sb.append(edit ? "edit-" : "add-");

		if (type == BlueprintTypes.BLUEPRINT) {
			sb.append("blueprint");
		}
		else if (type == BlueprintTypes.AGGREGATION_FRAGMENT) {
			sb.append("aggregation-fragment");
		}
		else if (type == BlueprintTypes.QUERY_FRAGMENT) {
			sb.append("query-fragment");
		}
		else if (type == BlueprintTypes.TEMPLATE) {
			sb.append("template");
		}

		return sb.toString();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		EditBlueprintMVCRenderCommand.class);

	@Reference
	private BlueprintService _blueprintService;

}