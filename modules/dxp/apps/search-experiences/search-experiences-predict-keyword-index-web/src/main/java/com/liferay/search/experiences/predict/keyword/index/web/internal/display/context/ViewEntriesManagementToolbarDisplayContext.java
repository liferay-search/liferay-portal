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

package com.liferay.search.experiences.predict.keyword.index.web.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.SearchContainerManagementToolbarDisplayContext;
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
import com.liferay.search.experiences.predict.keyword.index.constants.KeywordEntryStatus;
import com.liferay.search.experiences.predict.keyword.index.index.KeywordEntry;
import com.liferay.search.experiences.predict.keyword.index.web.internal.constants.KeywordIndexMVCCommandNames;
import com.liferay.search.experiences.predict.keyword.index.web.internal.index.KeywordEntryFields;

import java.util.List;
import java.util.Objects;

import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public abstract class ViewEntriesManagementToolbarDisplayContext
	extends SearchContainerManagementToolbarDisplayContext {

	public ViewEntriesManagementToolbarDisplayContext(
		HttpServletRequest httpServletRequest,
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		SearchContainer<KeywordEntry> searchContainer, String displayStyle,
		KeywordEntryStatus keywordEntryStatus) {

		super(
			httpServletRequest, liferayPortletRequest, liferayPortletResponse,
			searchContainer);

		this.httpServletRequest = httpServletRequest;
		this.displayStyle = displayStyle;
		this.keywordEntryStatus = keywordEntryStatus;
	}

	@Override
	public List<DropdownItem> getActionDropdownItems() {
		return DropdownItemListBuilder.add(
			dropdownItem -> {
				dropdownItem.putData("action", "deleteEntries");

				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, "delete"));

				dropdownItem.setQuickAction(true);
			}
		).build();
	}

	@Override
	public String getClearResultsURL() {
		return getSearchActionURL();
	}

	@Override
	public String getDefaultEventHandler() {
		return "KEYWORD_ENTRIES_MANAGEMENT_TOOLBAR_DEFAULT_EVENT_HANDLER";
	}

	@Override
	public String getSearchActionURL() {
		PortletURL searchURL = PortletURLBuilder.createRenderURL(
			liferayPortletResponse
		).setMVCRenderCommandName(
			KeywordIndexMVCCommandNames.VIEW_KEYWORD_ENTRIES
		).setParameter(
			"orderByCol", getOrderByCol()
		).setParameter(
			"orderByType", getOrderByType()
		).setParameter(
			"tabs", getTab()
		).build();

		return searchURL.toString();
	}

	@Override
	public List<ViewTypeItem> getViewTypeItems() {
		PortletURL portletURL = PortletURLBuilder.createRenderURL(
			liferayPortletResponse
		).setMVCRenderCommandName(
			KeywordIndexMVCCommandNames.VIEW_KEYWORD_ENTRIES
		).setParameter(
			"orderByCol", getOrderByCol()
		).setParameter(
			"orderByType", getOrderByType()
		).setParameter(
			"tabs", getTab()
		).build();

		if (searchContainer.getDelta() > 0) {
			portletURL.setProperty(
				"delta", String.valueOf(searchContainer.getDelta()));
		}

		if (searchContainer.getCur() > 0) {
			portletURL.setProperty(
				"cur", String.valueOf(searchContainer.getCur()));
		}

		return new ViewTypeItemList(portletURL, displayStyle) {

			private static final long serialVersionUID = 1L;

			{
				addListViewTypeItem();

				addTableViewTypeItem();
			}
		};
	}

	protected PortletURL getCurrentSortingURL() {
		PortletURL sortingURL = PortletURLBuilder.create(
			getPortletURL()
		).setMVCRenderCommandName(
			KeywordIndexMVCCommandNames.VIEW_KEYWORD_ENTRIES
		).setParameter(
			SearchContainer.DEFAULT_CUR_PARAM, "0"
		).setParameter(
			"tabs", getTab()
		).build();

		String keywords = ParamUtil.getString(
			liferayPortletRequest, "keywords");

		if (Validator.isNotNull(keywords)) {
			sortingURL.setProperty("keywords", keywords);
		}

		return sortingURL;
	}

	@Override
	protected List<DropdownItem> getOrderByDropdownItems() {
		return DropdownItemListBuilder.add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(
						getOrderByCol(), KeywordEntryFields.CONTENT_RAW));
				dropdownItem.setHref(
					getCurrentSortingURL(), "orderByCol",
					KeywordEntryFields.CONTENT_RAW);
				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, "content"));
			}
		).add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(
						getOrderByCol(), KeywordEntryFields.HIT_COUNT));
				dropdownItem.setHref(
					getCurrentSortingURL(), "orderByCol",
					KeywordEntryFields.HIT_COUNT);
				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, "hitcount"));
			}
		).build();
	}

	protected String getTab() {
		return keywordEntryStatus.name();
	}

	protected final String displayStyle;
	protected final HttpServletRequest httpServletRequest;
	protected final KeywordEntryStatus keywordEntryStatus;

}