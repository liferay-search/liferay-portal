package com.liferay.portal.search.tuning.gsearch.configuration.web.internal.portlet.action;

import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SearchConfigurationPortletKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SearchConfigurationTypes;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SearchConfiguration;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.constants.SearchConfigurationMVCCommandNames;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.constants.SearchConfigurationWebKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.display.context.SearchConfigurationAdminManagementToolbarDisplayContext;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.display.context.SearchConfigurationsDisplayContext;

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
		"javax.portlet.name=" + SearchConfigurationPortletKeys.SEARCH_CONFIGURATION_ADMIN,
		"mvc.command.name=" + SearchConfigurationMVCCommandNames.VIEW_SEARCH_CONFIGURATIONS,
		"mvc.command.name=/"
	},
	service = MVCRenderCommand.class
)
public class ViewSearchConfigurationsMVCRenderCommand
	implements MVCRenderCommand {

	@Override
	public String render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		int searchConfigurationType = ParamUtil.getInteger(
			renderRequest, SearchConfigurationWebKeys.SEARCH_CONFIGURATION_TYPE,
			SearchConfigurationTypes.CONFIGURATION);

		SearchConfigurationsDisplayContext searchConfigurationsDisplayContext =
			new SearchConfigurationsDisplayContext(
				_portal.getLiferayPortletRequest(renderRequest),
				_portal.getLiferayPortletResponse(renderResponse),
				searchConfigurationType);

		renderRequest.setAttribute(
			SearchConfigurationWebKeys.SEARCH_CONFIGURATIONS_DISPLAY_CONTEXT,
			searchConfigurationsDisplayContext);

		try {
			SearchConfigurationAdminManagementToolbarDisplayContext
				searchConfigurationsManagementToolbarDisplayContext =
					getSearchConfigurationsManagementToolbar(
						renderRequest, renderResponse,
						searchConfigurationsDisplayContext.getSearchContainer(),
						searchConfigurationsDisplayContext.getDisplayStyle(),
						searchConfigurationType);

			renderRequest.setAttribute(
				SearchConfigurationWebKeys.
					SEARCH_CONFIGURATIONS_MANAGEMENT_TOOLBAR_DISPLAY_CONTEXT,
				searchConfigurationsManagementToolbarDisplayContext);
		}
		catch (PortalException pe) {
			throw new RuntimeException(pe);
		}

		return "/view.jsp";
	}

	private SearchConfigurationAdminManagementToolbarDisplayContext
		getSearchConfigurationsManagementToolbar(
			RenderRequest renderRequest, RenderResponse renderResponse,
			SearchContainer<SearchConfiguration> searchContainer,
			String displayStyle, int searchConfigurationType) {

		LiferayPortletRequest liferayPortletRequest =
			_portal.getLiferayPortletRequest(renderRequest);

		LiferayPortletResponse liferayPortletResponse =
			_portal.getLiferayPortletResponse(renderResponse);

		return new SearchConfigurationAdminManagementToolbarDisplayContext(
			_portal.getHttpServletRequest(renderRequest), liferayPortletRequest,
			liferayPortletResponse, searchContainer, displayStyle,
			searchConfigurationType);
	}

	@Reference
	private Portal _portal;

}