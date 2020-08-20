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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.search.tuning.blueprints.admin.web.poc.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.admin.web.poc.internal.constants.BlueprintsAdminPortletKeys;
import com.liferay.portal.search.tuning.blueprints.admin.web.poc.internal.constants.BlueprintsAdminWebKeys;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;

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
		"javax.portlet.name=" + BlueprintsAdminPortletKeys.BLUEPRINTS_ADMIN,
		"mvc.command.name=" + BlueprintsAdminMVCCommandNames.DELETE_BLUEPRINT
	},
	service = MVCActionCommand.class
)
public class DeleteBlueprintMVCActionCommand
	extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		_delete(actionRequest);

		String redirect = ParamUtil.getString(actionRequest, "redirect");

		sendRedirect(actionRequest, actionResponse, redirect);
	}

	private void _delete(ActionRequest actionRequest) {
		long[] deleteConfigurationIds = _getConfigurationIds(actionRequest);

		try {
			for (long blueprintId : deleteConfigurationIds) {
				_blueprintService.deleteBlueprint(
					blueprintId);
			}
		}
		catch (PortalException portalException) {
			SessionErrors.add(
				actionRequest, BlueprintsAdminWebKeys.ERROR_DETAILS,
				portalException);
			_log.error(portalException.getMessage(), portalException);
		}
	}

	private long[] _getConfigurationIds(ActionRequest actionRequest) {
		long[] configurationIds = null;

		long configurationId = ParamUtil.getLong(
			actionRequest, BlueprintsAdminWebKeys.BLUEPRINT_ID);

		if (configurationId > 0) {
			configurationIds = new long[] {configurationId};
		}
		else {
			configurationIds = ParamUtil.getLongValues(
				actionRequest, BlueprintsAdminWebKeys.ROW_IDS);
		}

		return configurationIds;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DeleteBlueprintMVCActionCommand.class);

	@Reference
	private BlueprintService _blueprintService;

}