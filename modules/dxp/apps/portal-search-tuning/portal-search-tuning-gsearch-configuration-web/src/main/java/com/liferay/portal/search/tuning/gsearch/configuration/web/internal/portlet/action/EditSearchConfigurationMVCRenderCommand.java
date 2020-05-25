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

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SearchConfigurationPortletKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SearchConfigurationTypes;
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchConfigurationException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SearchConfiguration;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationService;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.constants.SearchConfigurationMVCCommandNames;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.constants.SearchConfigurationWebKeys;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

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
		"mvc.command.name=" + SearchConfigurationMVCCommandNames.EDIT_SEARCH_CONFIGURATION
	},
	service = MVCRenderCommand.class
)
public class EditSearchConfigurationMVCRenderCommand
	implements MVCRenderCommand {

	@Override
	public String render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		long searchConfigurationId = ParamUtil.getLong(
			renderRequest, SearchConfigurationWebKeys.SEARCH_CONFIGURATION_ID,
			0);

		int searchConfigurationType = ParamUtil.getInteger(
			renderRequest, SearchConfigurationWebKeys.SEARCH_CONFIGURATION_TYPE,
			SearchConfigurationTypes.CONFIGURATION);

		SearchConfiguration searchConfiguration = null;

		if (searchConfigurationId > 0) {
			try {
				searchConfiguration =
					_searchConfigurationService.getSearchConfiguration(
						searchConfigurationId);
				searchConfigurationType = searchConfiguration.getType();
			}
			catch (NoSuchConfigurationException nsce) {

				_log.error("Search configuration " + searchConfigurationId + " not found.", nsce);

				SessionErrors.add(renderRequest, 
						SearchConfigurationWebKeys.ERROR_DETAILS, nsce);
			}
			catch (PortalException pe) {

				_log.error(pe.getMessage(), pe);

				SessionErrors.add(renderRequest, 
						SearchConfigurationWebKeys.ERROR_DETAILS, pe);
			}
		}

		renderRequest.setAttribute(
			SearchConfigurationWebKeys.SEARCH_CONFIGURATION,
			searchConfiguration);

		ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

		portletDisplay.setShowBackIcon(true);

		String redirect = ParamUtil.getString(renderRequest, "redirect");

		portletDisplay.setURLBack(redirect);

		String pageTitleKey = _getPageTitleKey(
			searchConfiguration != null, searchConfigurationType);

		renderRequest.setAttribute(
			SearchConfigurationWebKeys.PAGE_TITLE_KEY, pageTitleKey);

		return "/edit_search_configuration.jsp";
	}

	private String _getPageTitleKey(boolean edit, int type) {
		StringBundler sb = new StringBundler(2);

		sb.append(edit ? "edit-" : "add-");

		if (type == SearchConfigurationTypes.CONFIGURATION) {
			sb.append("search-configuration");
		}
		else if (type == SearchConfigurationTypes.SNIPPET) {
			sb.append("configuration-snippet");
		}
		else if (type == SearchConfigurationTypes.TEMPLATE) {
			sb.append("configuration-template");
		}

		return sb.toString();
	}

	private static final Logger _log = LoggerFactory.getLogger(
		EditSearchConfigurationMVCRenderCommand.class);

	@Reference
	private SearchConfigurationService _searchConfigurationService;

}