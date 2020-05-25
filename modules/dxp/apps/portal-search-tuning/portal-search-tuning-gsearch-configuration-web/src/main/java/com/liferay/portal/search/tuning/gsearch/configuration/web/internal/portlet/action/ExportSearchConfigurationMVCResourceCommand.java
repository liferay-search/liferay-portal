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
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SearchConfigurationPortletKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchConfigurationException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SearchConfiguration;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationService;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.constants.SearchConfigurationMVCCommandNames;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.constants.SearchConfigurationWebKeys;

import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import javax.servlet.http.HttpServletResponse;

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
		"mvc.command.name=" + SearchConfigurationMVCCommandNames.EXPORT_SEARCH_CONFIGURATION
	},
	service = MVCResourceCommand.class
)
public class ExportSearchConfigurationMVCResourceCommand
	extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		long searchConfigurationId = ParamUtil.getLong(
			resourceRequest,
			SearchConfigurationWebKeys.SEARCH_CONFIGURATION_ID);

		try {

			SearchConfiguration searchConfiguration =
				_searchConfigurationService.getSearchConfiguration(
					searchConfigurationId);
			
			String configuration = searchConfiguration.getConfiguration();

			String title = _getFileTitle(resourceRequest, searchConfiguration);
			
			_writeResponse(resourceRequest, resourceResponse, title, configuration);
			
		}
		catch (NoSuchConfigurationException nsce) {

			_log.error("Search configuration " + searchConfigurationId + 
					" not found.", nsce);

			SessionErrors.add(resourceRequest, "errorDetails", nsce);

			_log.error(nsce.getMessage(), nsce);
		}
		catch (PortalException pe) {

			_log.error(pe.getMessage(), pe);
			
			SessionErrors.add(resourceRequest, "errorDetails", pe);
		}
	}
	
	private String _getFileTitle(
			ResourceRequest resourceRequest, 
			SearchConfiguration searchConfiguration) {
		
		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		String title = searchConfiguration.getTitle(
				themeDisplay.getLocale(), true);
		
		return title + ".json";

	}

	private void _writeResponse(ResourceRequest resourceRequest, 
			ResourceResponse resourceResponse, String title, String configuration) {
		
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

		} catch (IOException ioe) {

			_log.error(ioe.getMessage(), ioe);
			
			SessionErrors.add(resourceRequest, "errorDetails", ioe);
		}
	}
	
	private static final Logger _log = LoggerFactory.getLogger(
		ExportSearchConfigurationMVCResourceCommand.class);

	@Reference
	private Portal _portal;

	@Reference
	private SearchConfigurationService _searchConfigurationService;

}