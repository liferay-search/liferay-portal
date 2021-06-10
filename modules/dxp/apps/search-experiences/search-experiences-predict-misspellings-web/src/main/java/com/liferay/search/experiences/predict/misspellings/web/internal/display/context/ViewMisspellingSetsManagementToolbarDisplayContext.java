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

package com.liferay.search.experiences.predict.misspellings.web.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.SearchContainerManagementToolbarDisplayContext;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenuBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemListBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.ViewTypeItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.ViewTypeItemList;
import com.liferay.petra.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.search.experiences.predict.misspellings.index.MisspellingSet;
import com.liferay.search.experiences.predict.misspellings.web.internal.constants.MisspellingsMVCCommandNames;
import com.liferay.search.experiences.predict.misspellings.web.internal.index.MisspellingSetFields;

import java.util.List;
import java.util.Objects;

import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public class ViewMisspellingSetsManagementToolbarDisplayContext
	extends SearchContainerManagementToolbarDisplayContext {

	public ViewMisspellingSetsManagementToolbarDisplayContext(
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
				dropdownItem.putData("action", "deleteEntries");

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
		PortletURL searchURL = PortletURLBuilder.createRenderURL(
			liferayPortletResponse
		).setMVCRenderCommandName(
			MisspellingsMVCCommandNames.VIEW_MISSPELLING_SETS
		).setParameter(
			"orderByCol", getOrderByCol()
		).setParameter(
			"orderByType", getOrderByCol()
		).build();

		return searchURL.toString();
	}

	@Override
	public List<ViewTypeItem> getViewTypeItems() {
		PortletURL portletURL = PortletURLBuilder.createRenderURL(
			liferayPortletResponse
		).setMVCRenderCommandName(
			MisspellingsMVCCommandNames.VIEW_MISSPELLING_SETS
		).setParameter(
			"orderByCol", getOrderByCol()
		).setParameter(
			"orderByType", getOrderByCol()
		).build();

		if (searchContainer.getDelta() > 0) {
			portletURL.setProperty(
				"delta", String.valueOf(searchContainer.getDelta()));
		}

		if (searchContainer.getCur() > 0) {
			portletURL.setProperty(
				"cur", String.valueOf(searchContainer.getCur()));
		}

		return new ViewTypeItemList(portletURL, _displayStyle) {

			private static final long serialVersionUID = 1L;

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
					Objects.equals(
						getOrderByCol(), MisspellingSetFields.PHRASE));
				dropdownItem.setHref(
					_getCurrentSortingURL(), "orderByCol",
					MisspellingSetFields.PHRASE);
				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "phrase"));
			}
		).add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(
						getOrderByCol(), MisspellingSetFields.CREATED));
				dropdownItem.setHref(
					_getCurrentSortingURL(), "orderByCol",
					MisspellingSetFields.CREATED);
				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "created"));
			}
		).add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(
						getOrderByCol(), MisspellingSetFields.MODIFIED));
				dropdownItem.setHref(
					_getCurrentSortingURL(), "orderByCol",
					MisspellingSetFields.MODIFIED);
				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "modified"));
			}
		).add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(
						getOrderByCol(), MisspellingSetFields.LANGUAGE_ID));
				dropdownItem.setHref(
					_getCurrentSortingURL(), "orderByCol",
					MisspellingSetFields.LANGUAGE_ID);
				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "language"));
			}
		).build();
	}

	private PortletURL _getCurrentSortingURL() {
		PortletURL sortingURL = PortletURLBuilder.create(
			getPortletURL()
		).setMVCRenderCommandName(
			MisspellingsMVCCommandNames.VIEW_MISSPELLING_SETS
		).setParameter(
			SearchContainer.DEFAULT_CUR_PARAM, "0"
		).build();

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