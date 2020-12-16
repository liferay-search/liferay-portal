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

package com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.display.context;

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
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.constants.MisspellingsMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.MisspellingSet;

import java.util.List;
import java.util.Objects;

import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public class MisspellingSetsManagementToolbarDisplayContext
	extends SearchContainerManagementToolbarDisplayContext {

	public MisspellingSetsManagementToolbarDisplayContext(
		HttpServletRequest httpServletRequest,
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		SearchContainer<MisspellingSet> searchContainer, String displayStyle) {

		super(
			httpServletRequest, liferayPortletRequest, liferayPortletResponse,
			searchContainer);

		_httpServletRequest = httpServletRequest;
		_displayStyle = displayStyle;
	}

	@Override
	public List<DropdownItem> getActionDropdownItems() {
		return DropdownItemListBuilder.add(
			dropdownItem -> {
				dropdownItem.putData("action", "deleteMisspellingSets");

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
		return CreationMenuBuilder.addDropdownItem(
			dropdownItem -> {
				dropdownItem.setHref(
					liferayPortletResponse.createRenderURL(),
					"mvcRenderCommandName",
					MisspellingsMVCCommandNames.EDIT_MISSPELLING_SET,
					"redirect", currentURLObj.toString());
				dropdownItem.setLabel(
					LanguageUtil.get(
						_httpServletRequest, "add-misspelling-set"));
			}
		).build();
	}

	@Override
	public String getDefaultEventHandler() {
		return "MISSPELLING_SETS_MANAGEMENT_TOOLBAR_DEFAULT_EVENT_HANDLER";
	}

	@Override
	public String getSearchActionURL() {
		PortletURL searchURL = liferayPortletResponse.createRenderURL();

		searchURL.setProperty(
			"mvcRenderCommandName",
			MisspellingsMVCCommandNames.VIEW_MISSPELLING_SETS);

		searchURL.setProperty("orderByCol", getOrderByCol());
		searchURL.setProperty("orderByType", getOrderByType());

		return searchURL.toString();
	}

	@Override
	public List<ViewTypeItem> getViewTypeItems() {
		PortletURL portletURL = liferayPortletResponse.createRenderURL();

		portletURL.setProperty(
			"mvcRenderCommandName",
			MisspellingsMVCCommandNames.VIEW_MISSPELLING_SETS);

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
			MisspellingsMVCCommandNames.VIEW_MISSPELLING_SETS);

		sortingURL.setProperty(SearchContainer.DEFAULT_CUR_PARAM, "0");

		String keywords = ParamUtil.getString(
			liferayPortletRequest, "keywords");

		if (Validator.isNotNull(keywords)) {
			sortingURL.setProperty("keywords", keywords);
		}

		return sortingURL;
	}

	private final String _displayStyle;
	private final HttpServletRequest _httpServletRequest;

}