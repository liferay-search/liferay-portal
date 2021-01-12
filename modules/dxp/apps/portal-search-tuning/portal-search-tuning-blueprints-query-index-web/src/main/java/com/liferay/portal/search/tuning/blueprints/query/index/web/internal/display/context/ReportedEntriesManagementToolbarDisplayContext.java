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

package com.liferay.portal.search.tuning.blueprints.query.index.web.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemListBuilder;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryStringStatus;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryString;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringFields;

import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public class ReportedEntriesManagementToolbarDisplayContext
	extends QueryStringsEntriesManagementToolbarDisplayContext {

	public ReportedEntriesManagementToolbarDisplayContext(
		HttpServletRequest httpServletRequest,
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		SearchContainer<QueryString> searchContainer, String displayStyle) {

		super(
			httpServletRequest, liferayPortletRequest, liferayPortletResponse,
			searchContainer, displayStyle, QueryStringStatus.REPORTED);
	}

	@Override
	protected List<DropdownItem> getOrderByDropdownItems() {
		return DropdownItemListBuilder.add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(
						getOrderByCol(), QueryStringFields.CONTENT_RAW));
				dropdownItem.setHref(
					getCurrentSortingURL(), "orderByCol",
					QueryStringFields.CONTENT_RAW);
				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, "content"));
			}
		).add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(
						getOrderByCol(), QueryStringFields.HIT_COUNT));
				dropdownItem.setHref(
					getCurrentSortingURL(), "orderByCol",
					QueryStringFields.HIT_COUNT);
				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, "hitcount"));
			}
		).add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(
						getOrderByCol(), QueryStringFields.REPORT_COUNT));
				dropdownItem.setHref(
					getCurrentSortingURL(), "orderByCol",
					QueryStringFields.REPORT_COUNT);
				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, "reportcount"));
			}
		).build();
	}

}