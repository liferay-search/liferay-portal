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

@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + SearchConfigurationPortletKeys.SEARCH_CONFIGURATION_ADMIN,
		"mvc.command.name=" + SearchConfigurationMVCCommandNames.DELETE_SEARCH_CONFIGURATIONS
	},
	service = MVCActionCommand.class
)
public class DeleteSearchConfigurationMVCActionCommand extends BaseMVCActionCommand {

	protected void doDelete(
		ActionRequest actionRequest, ActionResponse actionResponse) {

		long[] deleteConfigurationIds = _getConfigurationIds(actionRequest);

		try {
			for (long searchConfigurationId : deleteConfigurationIds) {
				_searchConfigurationService.deleteConfiguration(
					searchConfigurationId);
			}
		}
		catch (PortalException pe) {
			SessionErrors.add(
				actionRequest, SearchConfigurationWebKeys.ERROR_DETAILS, pe);
			_log.error(pe.getMessage(), pe);
		}
	}

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		doDelete(actionRequest, actionResponse);

		String redirect = ParamUtil.getString(actionRequest, "redirect");

		sendRedirect(actionRequest, actionResponse, redirect);
	}

	@Reference
	protected SearchConfigurationService _searchConfigurationService;

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

}