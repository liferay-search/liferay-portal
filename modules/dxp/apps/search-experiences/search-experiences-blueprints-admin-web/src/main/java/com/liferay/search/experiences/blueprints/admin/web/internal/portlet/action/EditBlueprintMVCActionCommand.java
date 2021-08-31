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

package com.liferay.search.experiences.blueprints.admin.web.internal.portlet.action;

import com.liferay.petra.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.LiferayActionResponse;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.search.experiences.blueprints.admin.web.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.search.experiences.blueprints.admin.web.internal.constants.BlueprintsAdminWebKeys;
import com.liferay.search.experiences.blueprints.admin.web.internal.handler.BlueprintExceptionRequestHandler;
import com.liferay.search.experiences.blueprints.admin.web.internal.util.BlueprintsAdminRequestUtil;
import com.liferay.search.experiences.blueprints.constants.BlueprintsPortletKeys;
import com.liferay.search.experiences.blueprints.exception.BlueprintValidationException;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.service.BlueprintService;

import java.util.Locale;
import java.util.Map;

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
		"javax.portlet.name=" + BlueprintsPortletKeys.BLUEPRINTS_ADMIN,
		"mvc.command.name=" + BlueprintsAdminMVCCommandNames.EDIT_BLUEPRINT
	},
	service = MVCActionCommand.class
)
public class EditBlueprintMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		String configuration = BlueprintsAdminRequestUtil.getConfiguration(
			actionRequest);

		Map<Locale, String> descriptionMap =
			BlueprintsAdminRequestUtil.getDescription(actionRequest);

		String selectedElements =
			BlueprintsAdminRequestUtil.getSelectedElements(actionRequest);

		Map<Locale, String> titleMap = BlueprintsAdminRequestUtil.getTitle(
			actionRequest);

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		try {
			ServiceContext serviceContext = _createServiceContext(
				actionRequest);

			JSONObject jsonObject = JSONUtil.put("title", titleMap);

			if (Constants.ADD.equals(cmd)) {
				Blueprint blueprint = _blueprintService.addCompanyBlueprint(
					titleMap, descriptionMap, configuration, selectedElements,
					serviceContext);

				jsonObject = JSONUtil.put(
					"redirectURL",
					_getRedirectURL(
						actionRequest, actionResponse,
						blueprint.getBlueprintId()));
			}
			else {
				_blueprintService.updateBlueprint(
					BlueprintsAdminRequestUtil.getBlueprintId(actionRequest),
					titleMap, descriptionMap, configuration, selectedElements,
					serviceContext);
			}

			JSONPortletResponseUtil.writeJSON(
				actionRequest, actionResponse, jsonObject);
		}
		catch (BlueprintValidationException blueprintValidationException) {
			_log.error(
				blueprintValidationException.getMessage(),
				blueprintValidationException);

			SessionErrors.add(
				actionRequest, BlueprintsAdminWebKeys.ERROR,
				blueprintValidationException.getMessage());

			_blueprintExceptionRequestHandler.handlePortalException(
				actionRequest, actionResponse, blueprintValidationException);
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);

			SessionErrors.add(
				actionRequest, BlueprintsAdminWebKeys.ERROR,
				portalException.getMessage());

			_blueprintExceptionRequestHandler.handlePortalException(
				actionRequest, actionResponse, portalException);
		}
	}

	private ServiceContext _createServiceContext(ActionRequest actionRequest)
		throws PortalException {

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			Blueprint.class.getName(), actionRequest);

		serviceContext.setAttribute("skip.blueprint.validation", Boolean.TRUE);

		return serviceContext;
	}

	private String _getRedirectURL(
		ActionRequest actionRequest, ActionResponse actionResponse,
		long blueprintId) {

		LiferayActionResponse liferayActionResponse =
			(LiferayActionResponse)actionResponse;

		return PortletURLBuilder.createRenderURL(
			liferayActionResponse
		).setMVCRenderCommandName(
			BlueprintsAdminMVCCommandNames.EDIT_BLUEPRINT
		).setRedirect(
			ParamUtil.getString(actionRequest, "redirect")
		).setParameter(
			BlueprintsAdminWebKeys.BLUEPRINT_ID, blueprintId
		).buildString();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		EditBlueprintMVCActionCommand.class);

	@Reference
	private BlueprintExceptionRequestHandler _blueprintExceptionRequestHandler;

	@Reference
	private BlueprintService _blueprintService;

	@Reference
	private JSONFactory _jsonFactory;

}