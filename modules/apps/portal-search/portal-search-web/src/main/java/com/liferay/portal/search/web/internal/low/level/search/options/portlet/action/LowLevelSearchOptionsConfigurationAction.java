/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.web.internal.low.level.search.options.portlet.action;

import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.portlet.ConfigurationAction;
import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.engine.ConnectionInformation;
import com.liferay.portal.search.engine.SearchEngineInformation;
import com.liferay.portal.search.web.internal.low.level.search.options.constants.LowLevelSearchOptionsPortletKeys;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Wade Cao
 */
@Component(
	property = "javax.portlet.name=" + LowLevelSearchOptionsPortletKeys.LOW_LEVEL_SEARCH_OPTIONS,
	service = ConfigurationAction.class
)
public class LowLevelSearchOptionsConfigurationAction
	extends DefaultConfigurationAction {

	@Override
	public String getJspPath(HttpServletRequest httpServletRequest) {
		User user = (User)httpServletRequest.getAttribute(WebKeys.USER);

		if (!_isAdmin(user)) {
			SessionErrors.add(
				httpServletRequest, PrincipalException.class.getName());

			return "/error.jsp";
		}

		ConfigurationDisplayContext configurationDisplayContext =
			new ConfigurationDisplayContext();

		LinkedList<String> connectionIds = new LinkedList<>();

		List<ConnectionInformation> connectionInformationList =
			searchEngineInformation.getConnectionInformationList();

		if (connectionInformationList != null) {
			for (ConnectionInformation connectionInformation :
					connectionInformationList) {

				connectionIds.add(connectionInformation.getConnectionId());
			}
		}

		configurationDisplayContext.setConnectionIds(connectionIds);

		httpServletRequest.setAttribute(
			WebKeys.PORTLET_DISPLAY_CONTEXT, configurationDisplayContext);

		return "/low/level/search/options/configuration.jsp";
	}

	@Reference
	protected SearchEngineInformation searchEngineInformation;

	private boolean _isAdmin(User user) {
		if (user == null) {
			return false;
		}

		List<Role> roles = user.getRoles();

		return roles.stream(
		).anyMatch(
			role -> Objects.equals(role.getName(), RoleConstants.ADMINISTRATOR)
		);
	}

}