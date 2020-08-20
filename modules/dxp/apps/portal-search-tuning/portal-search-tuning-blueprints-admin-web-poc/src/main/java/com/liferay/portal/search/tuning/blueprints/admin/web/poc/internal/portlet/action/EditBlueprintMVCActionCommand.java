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
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.search.tuning.blueprints.admin.web.poc.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.admin.web.poc.internal.constants.BlueprintsAdminPortletKeys;
import com.liferay.portal.search.tuning.blueprints.admin.web.poc.internal.constants.BlueprintsAdminWebKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.BlueprintKeys;
import com.liferay.portal.search.tuning.blueprints.exception.BlueprintValidationException;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;

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
		"javax.portlet.name=" + BlueprintsAdminPortletKeys.BLUEPRINTS_ADMIN,
		"mvc.command.name=" + BlueprintsAdminMVCCommandNames.EDIT_BLUEPRINT
	},
	service = MVCActionCommand.class
)
public class EditBlueprintMVCActionCommand
	extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		long blueprintId = ParamUtil.getLong(
			actionRequest, BlueprintsAdminWebKeys.BLUEPRINT_ID);

		int type = ParamUtil.getInteger(
			actionRequest,
			BlueprintsAdminWebKeys.BLUEPRINT_TYPE);

		Map<Locale, String> titleMap = LocalizationUtil.getLocalizationMap(
			actionRequest, BlueprintsAdminWebKeys.TITLE);

		Map<Locale, String> descriptionMap =
			LocalizationUtil.getLocalizationMap(
				actionRequest, BlueprintsAdminWebKeys.DESCRIPTION);

		String configuration = _buildConfigurationFromRequest(actionRequest);

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		try {
			com.liferay.portal.kernel.service.ServiceContext serviceContext = ServiceContextFactory.getInstance(
				Blueprint.class.getName(), actionRequest);

			if (Constants.ADD.equals(cmd)) {
				_blueprintService.addCompanyBlueprint(
					titleMap, descriptionMap, configuration, type,
					serviceContext);
			}
			else if (blueprintId > 0) {
				_blueprintService.updateBlueprint(
					blueprintId, titleMap, descriptionMap,
					configuration, serviceContext);
			}

			sendRedirect(actionRequest, actionResponse);
		}
		catch (BlueprintValidationException
					blueprintValidationException) {

			_log.error(
				blueprintValidationException.getMessage(),
				blueprintValidationException);

			blueprintValidationException.getErrors(
			).forEach(
				key -> SessionErrors.add(actionRequest, key)
			);

			actionResponse.getRenderParameters(
			).setValue(
				"mvcRenderCommandName",
				BlueprintsAdminMVCCommandNames.EDIT_BLUEPRINT
			);
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);

			SessionErrors.add(
				actionRequest, BlueprintsAdminWebKeys.ERROR_DETAILS,
				portalException);

			actionResponse.getRenderParameters(
			).setValue(
				"mvcRenderCommandName",
				BlueprintsAdminMVCCommandNames.EDIT_BLUEPRINT
			);
		}
	}

	private String _buildConfigurationFromRequest(ActionRequest actionRequest)
		throws JSONException {

		JSONArray clauseConfiguration = _getAutoFieldValues(
			actionRequest, BlueprintsAdminWebKeys.CLAUSE_CONFIGURATION,
			BlueprintsAdminWebKeys.CLAUSE_CONFIGURATION_INDEXES);

		JSONObject configuration = JSONUtil.put(
			BlueprintKeys.CLAUSE_CONFIGURATION.getJsonKey(), clauseConfiguration);

		return configuration.toString();
	}

	private JSONArray _getAutoFieldValues(
			ActionRequest actionRequest, String valueParameterKey,
			String indexParameterKey)
		throws JSONException {

		JSONArray values = JSONFactoryUtil.createJSONArray();

		int[] rowIndexes = ParamUtil.getIntegerValues(
			actionRequest, indexParameterKey, new int[0]);

		for (int i : rowIndexes) {
			String value = ParamUtil.getString(
				actionRequest, valueParameterKey + i);

			JSONObject item = JSONFactoryUtil.createJSONObject(value);

			values.put(item);
		}

		return values;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		EditBlueprintMVCActionCommand.class);

	@Reference
	private BlueprintService _blueprintService;

}