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

package com.liferay.portal.search.tuning.gsearch.configuration.web.internal.portlet.action;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SearchConfigurationPortletKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationService;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.constants.SearchConfigurationMVCCommandNames;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.constants.SearchConfigurationWebKeys;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + SearchConfigurationPortletKeys.SEARCH_CONFIGURATION_ADMIN,
		"mvc.command.name=" + SearchConfigurationMVCCommandNames.DELETE_SEARCH_CONFIGURATIONS
	},
	service = MVCActionCommand.class
)
public class DeleteSearchConfigurationMVCActionCommand
	extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		_doDelete(actionRequest, actionResponse);

		String redirect = ParamUtil.getString(actionRequest, "redirect");

		sendRedirect(actionRequest, actionResponse, redirect);
	}

	private void _doDelete(
		ActionRequest actionRequest, ActionResponse actionResponse) {

		long[] deleteConfigurationIds = _getConfigurationIds(actionRequest);

		try {
			for (long searchConfigurationId : deleteConfigurationIds) {
				_searchConfigurationService.deleteSearchConfiguration(
					searchConfigurationId);
			}
		}
		catch (PortalException pe) {
			SessionErrors.add(
				actionRequest, SearchConfigurationWebKeys.ERROR_DETAILS, pe);
			_log.error(pe.getMessage(), pe);
		}
	}
	
	private long[] _getConfigurationIds(ActionRequest actionRequest) {
		long[] configurationIds = null;

		long configurationId = ParamUtil.getLong(
			actionRequest, SearchConfigurationWebKeys.SEARCH_CONFIGURATION_ID);

		if (configurationId > 0) {
			configurationIds = new long[] {configurationId};
		}
		else {
			configurationIds = ParamUtil.getLongValues(
				actionRequest, SearchConfigurationWebKeys.ROW_IDS);
		}

		return configurationIds;
	}

	private static final Logger _log = LoggerFactory.getLogger(
		DeleteSearchConfigurationMVCActionCommand.class);

	@Reference
	private SearchConfigurationService _searchConfigurationService;
}