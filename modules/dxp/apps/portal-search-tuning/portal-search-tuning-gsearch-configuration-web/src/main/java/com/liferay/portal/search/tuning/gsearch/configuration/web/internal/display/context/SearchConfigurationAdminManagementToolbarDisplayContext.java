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

package com.liferay.portal.search.tuning.gsearch.configuration.web.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.SearchContainerManagementToolbarDisplayContext;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenuBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemListBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.ViewTypeItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.ViewTypeItemList;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.constants.SearchConfigurationMVCCommandNames;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.security.permission.resource.SearchConfigurationPermission;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.portlet.ActionRequest;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Sergio González
 */
public class SearchConfigurationAdminManagementToolbarDisplayContext
	extends SearchContainerManagementToolbarDisplayContext {

	public SearchConfigurationAdminManagementToolbarDisplayContext(
		HttpServletRequest httpServletRequest,
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		SearchContainer searchContainer, String displayStyle,
		int searchConfigurationType) {

		super(
			httpServletRequest, liferayPortletRequest, liferayPortletResponse,
			searchContainer);

		_displayStyle = displayStyle;
		_searchConfigurationType = searchConfigurationType;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	@Override
	public List<DropdownItem> getActionDropdownItems() {
		return DropdownItemListBuilder.add(
			dropdownItem -> {
				dropdownItem.putData("action", "deleteConfigurationEntries");

				dropdownItem.setLabel(LanguageUtil.get(request, "delete"));

				dropdownItem.setQuickAction(true);
			}
		).build();
	}

	@Override
	public String getClearResultsURL() {
		return getSearchActionURL();
	}

	public Map<String, Object> getComponentContext() throws PortalException {
		String cmd = Constants.DELETE;

		return HashMapBuilder.<String, Object>put(
			"deleteEntriesCmd", cmd
		).put(
			"deleteEntriesURL",
			() -> {
				PortletURL deleteEntriesURL =
					liferayPortletResponse.createActionURL();

				deleteEntriesURL.setParameter(
					ActionRequest.ACTION_NAME,
					SearchConfigurationMVCCommandNames.
						DELETE_SEARCH_CONFIGURATIONS);

				return deleteEntriesURL.toString();
			}
		).build();
	}

	@Override
	public CreationMenu getCreationMenu() {
		if (!SearchConfigurationPermission.contains(
				_themeDisplay.getPermissionChecker(),
				_themeDisplay.getScopeGroupId(), _searchConfigurationType,
				ActionKeys.ADD_ENTRY)) {

			return null;
		}

		return CreationMenuBuilder.addDropdownItem(
			dropdownItem -> {
				dropdownItem.setHref(
					liferayPortletResponse.createRenderURL(),
					"mvcRenderCommandName",
					SearchConfigurationMVCCommandNames.
						EDIT_SEARCH_CONFIGURATION,
					"redirect", currentURLObj.toString());
				dropdownItem.setLabel(
					LanguageUtil.get(request, "add-search-configuration"));
			}
		).build();
	}

	@Override
	public String getSearchActionURL() {
		PortletURL searchURL = liferayPortletResponse.createRenderURL();

		searchURL.setParameter(
			"mvcRenderCommandName",
			SearchConfigurationMVCCommandNames.VIEW_SEARCH_CONFIGURATIONS);

		String tabs = ParamUtil.getString(request, "tabs", "configurations");

		searchURL.setParameter("tabs", tabs);

		searchURL.setParameter("orderByCol", getOrderByCol());
		searchURL.setParameter("orderByType", getOrderByType());

		return searchURL.toString();
	}

	@Override
	public List<ViewTypeItem> getViewTypeItems() {
		PortletURL portletURL = liferayPortletResponse.createRenderURL();

		portletURL.setParameter(
			"mvcRenderCommandName",
			SearchConfigurationMVCCommandNames.VIEW_SEARCH_CONFIGURATIONS);

		if (searchContainer.getDelta() > 0) {
			portletURL.setParameter(
				"delta", String.valueOf(searchContainer.getDelta()));
		}

		portletURL.setParameter("orderBycol", searchContainer.getOrderByCol());
		portletURL.setParameter(
			"orderByType", searchContainer.getOrderByType());

		if (searchContainer.getCur() > 0) {
			portletURL.setParameter(
				"cur", String.valueOf(searchContainer.getCur()));
		}

		return new ViewTypeItemList(portletURL, _displayStyle) {
			{
				addCardViewTypeItem();

				addListViewTypeItem();

				addTableViewTypeItem();
			}
		};
	}

	@Override
	protected List<DropdownItem> getOrderByDropdownItems() {
		return DropdownItemListBuilder.add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(getOrderByCol(), "title"));
				dropdownItem.setHref(
					_getCurrentSortingURL(), "orderByCol", "title");
				dropdownItem.setLabel(LanguageUtil.get(request, "title"));
			}
		).add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(getOrderByCol(), "display-date"));
				dropdownItem.setHref(
					_getCurrentSortingURL(), "orderByCol", "display-date");
				dropdownItem.setLabel(
					LanguageUtil.get(request, "display-date"));
			}
		).build();
	}

	private PortletURL _getCurrentSortingURL() {
		PortletURL sortingURL = getPortletURL();

		sortingURL.setParameter(
			"mvcRenderCommandName",
			SearchConfigurationMVCCommandNames.VIEW_SEARCH_CONFIGURATIONS);

		sortingURL.setParameter(SearchContainer.DEFAULT_CUR_PARAM, "0");

		String keywords = ParamUtil.getString(request, "keywords");

		if (Validator.isNotNull(keywords)) {
			sortingURL.setParameter("keywords", keywords);
		}

		return sortingURL;
	}

	private final String _displayStyle;
	private final int _searchConfigurationType;
	private final ThemeDisplay _themeDisplay;

}