/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.web.internal.search.results.portlet.action;

import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.portlet.ConfigurationAction;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.engine.SearchEngineInformation;
import com.liferay.portal.search.web.constants.SearchResultsPortletKeys;
import com.liferay.portal.search.web.internal.search.results.portlet.SearchResultsPortletPreferences;
import com.liferay.portlet.display.template.portlet.action.BaseConfigurationAction;

import jakarta.portlet.ActionRequest;
import jakarta.portlet.ActionResponse;
import jakarta.portlet.PortletConfig;

import jakarta.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Lino Alves
 */
@Component(
	property = "jakarta.portlet.name=" + SearchResultsPortletKeys.SEARCH_RESULTS,
	service = ConfigurationAction.class
)
public class SearchResultsConfigurationAction extends BaseConfigurationAction {

	@Override
	public String getJspPath(HttpServletRequest httpServletRequest) {
		return "/search/results/configuration.jsp";
	}

	@Override
	public void processAction(
			PortletConfig portletConfig, ActionRequest actionRequest,
			ActionResponse actionResponse)
		throws Exception {

		SearchEngineInformation searchEngineInformation =
			_searchEngineInformationSnapshot.get();

		if (searchEngineInformation != null) {
			int indexMaxResultWindow =
				searchEngineInformation.getIndexMaxResultWindow();
			int maximumSearchDepth = GetterUtil.getInteger(
				getParameter(
					actionRequest,
					SearchResultsPortletPreferences.
						PREFERENCE_KEY_MAXIMUM_SEARCH_DEPTH));

			if (maximumSearchDepth > indexMaxResultWindow) {
				SessionErrors.add(
					actionRequest,
					SearchResultsPortletPreferences.
						PREFERENCE_KEY_MAXIMUM_SEARCH_DEPTH,
					indexMaxResultWindow);

				return;
			}
		}

		super.processAction(portletConfig, actionRequest, actionResponse);
	}

	private static final Snapshot<SearchEngineInformation>
		_searchEngineInformationSnapshot = new Snapshot<>(
			SearchResultsConfigurationAction.class,
			SearchEngineInformation.class, null, true);

}