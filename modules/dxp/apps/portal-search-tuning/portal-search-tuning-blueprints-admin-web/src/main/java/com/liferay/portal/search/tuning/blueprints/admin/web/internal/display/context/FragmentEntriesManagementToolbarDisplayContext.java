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

package com.liferay.portal.search.tuning.blueprints.admin.web.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.SearchContainerManagementToolbarDisplayContext;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenuBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemListBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.ViewTypeItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.ViewTypeItemList;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.security.permission.resource.BlueprintPermission;
import com.liferay.portal.search.tuning.blueprints.constants.BlueprintTypes;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;

import java.util.List;
import java.util.Objects;

import javax.portlet.ActionRequest;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public class FragmentEntriesManagementToolbarDisplayContext
	extends SearchContainerManagementToolbarDisplayContext {

	public FragmentEntriesManagementToolbarDisplayContext(
		HttpServletRequest httpServletRequest,
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		SearchContainer<Blueprint> searchContainer, String displayStyle,
		int blueprintType) {

		super(
			httpServletRequest, liferayPortletRequest, liferayPortletResponse,
			searchContainer);

		_httpServletRequest = httpServletRequest;
		_displayStyle = displayStyle;
		_blueprintType = blueprintType;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	@Override
	public List<DropdownItem> getActionDropdownItems() {
		return DropdownItemListBuilder.add(
			dropdownItem -> {
				dropdownItem.putData("action", "deleteFragmentEntries");

				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "delete"));

				dropdownItem.setQuickAction(true);
			}
		).build();
	}

	@Override
	public String getClearResultsURL() {
		return getSearchActionURL();
	}

	@Override
	public CreationMenu getCreationMenu() {
		if (!BlueprintPermission.contains(
				_themeDisplay.getPermissionChecker(),
				_themeDisplay.getScopeGroupId(), _blueprintType,
				ActionKeys.ADD_ENTRY)) {

			return null;
		}

		return CreationMenuBuilder.addDropdownItem(
			dropdownItem -> {
				dropdownItem.putData("action", "addFragment");
				dropdownItem.putData(
					"defaultLocale",
					LocaleUtil.toLanguageId(LocaleUtil.getDefault()));

				PortletURL editFragmentURL =
					liferayPortletResponse.createActionURL();

				editFragmentURL.setParameter(
					ActionRequest.ACTION_NAME,
					BlueprintsAdminMVCCommandNames.EDIT_FRAGMENT);
				editFragmentURL.setParameter(Constants.CMD, Constants.ADD);
				editFragmentURL.setParameter(
					"redirect", currentURLObj.toString());

				dropdownItem.putData(
					"editFragmentURL", editFragmentURL.toString());

				dropdownItem.putData(
					"type", String.valueOf(BlueprintTypes.QUERY_FRAGMENT));
				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "add-fragment"));
			}
		).build();
	}

	@Override
	public String getDefaultEventHandler() {
		return "FRAGMENT_ENTRIES_MANAGEMENT_TOOLBAR_DEFAULT_EVENT_HANDLER";
	}

	@Override
	public String getSearchActionURL() {
		PortletURL searchURL = liferayPortletResponse.createRenderURL();

		searchURL.setProperty(
			"mvcRenderCommandName",
			BlueprintsAdminMVCCommandNames.VIEW_BLUEPRINTS);

		searchURL.setParameter("tabs", "fragments");

		searchURL.setProperty("orderByCol", getOrderByCol());
		searchURL.setProperty("orderByType", getOrderByType());

		return searchURL.toString();
	}

	@Override
	public List<ViewTypeItem> getViewTypeItems() {
		PortletURL portletURL = liferayPortletResponse.createRenderURL();

		portletURL.setProperty(
			"mvcRenderCommandName",
			BlueprintsAdminMVCCommandNames.VIEW_BLUEPRINTS);

		portletURL.setParameter("tabs", "fragments");

		if (searchContainer.getDelta() > 0) {
			portletURL.setProperty(
				"delta", String.valueOf(searchContainer.getDelta()));
		}

		portletURL.setProperty("orderByCol", searchContainer.getOrderByCol());
		portletURL.setProperty("orderByType", searchContainer.getOrderByType());

		if (searchContainer.getCur() > 0) {
			portletURL.setProperty(
				"cur", String.valueOf(searchContainer.getCur()));
		}

		return new ViewTypeItemList(portletURL, _displayStyle) {
			{
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
				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "title"));
			}
		).add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(getOrderByCol(), "modified-date"));
				dropdownItem.setHref(
					_getCurrentSortingURL(), "orderByCol", "modified-date");
				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "modified-date"));
			}
		).build();
	}

	private PortletURL _getCurrentSortingURL() {
		PortletURL sortingURL = getPortletURL();

		sortingURL.setProperty(
			"mvcRenderCommandName",
			BlueprintsAdminMVCCommandNames.VIEW_BLUEPRINTS);

		sortingURL.setParameter("tabs", "fragments");

		sortingURL.setProperty(SearchContainer.DEFAULT_CUR_PARAM, "0");

		String keywords = ParamUtil.getString(
			liferayPortletRequest, "keywords");

		if (Validator.isNotNull(keywords)) {
			sortingURL.setProperty("keywords", keywords);
		}

		return sortingURL;
	}

	private final int _blueprintType;
	private final String _displayStyle;
	private final HttpServletRequest _httpServletRequest;
	private final ThemeDisplay _themeDisplay;

}