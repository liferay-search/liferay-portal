/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.search.admin.web.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.util.MultiselectItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.MultiselectItemListBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.NavigationItemList;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.search.index.IndexInformation;
import com.liferay.portal.search.index.IndexNameBuilder;

import java.util.List;
import java.util.Objects;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author Adam Brandizzi
 * @author Tibor Lipusz
 */
public class SearchAdminDisplayBuilder {

	public SearchAdminDisplayBuilder(
		CompanyLocalService companyLocalService,
		IndexNameBuilder indexNameBuilder, Language language, Portal portal,
		RenderRequest renderRequest, RenderResponse renderResponse) {

		_companyLocalService = companyLocalService;
		_indexNameBuilder = indexNameBuilder;
		_language = language;
		_portal = portal;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;
	}

	public SearchAdminDisplayContext build() {
		SearchAdminDisplayContext searchAdminDisplayContext =
			new SearchAdminDisplayContext();

		NavigationItemList navigationItemList = new NavigationItemList();
		String selectedTab = getSelectedTab();

		addNavigationItemList(navigationItemList, "connections", selectedTab);

		addNavigationItemList(navigationItemList, "index-actions", selectedTab);

		if (isIndexInformationAvailable()) {
			addNavigationItemList(
				navigationItemList, "field-mappings", selectedTab);
		}

		searchAdminDisplayContext.setVirtualInstanceMultiselectItems(
			buildVirtualInstanceMultiselectItems());
		searchAdminDisplayContext.setNavigationItemList(navigationItemList);
		searchAdminDisplayContext.setSelectedTab(selectedTab);

		return searchAdminDisplayContext;
	}

	public void setIndexInformation(IndexInformation indexInformation) {
		_indexInformation = indexInformation;
	}

	protected void addNavigationItemList(
		NavigationItemList navigationItemList, String label,
		String selectedTab) {

		navigationItemList.add(
			navigationItem -> {
				navigationItem.setActive(selectedTab.equals(label));
				navigationItem.setHref(
					_renderResponse.createRenderURL(), "tabs1", label);
				navigationItem.setLabel(
					_language.get(
						_portal.getHttpServletRequest(_renderRequest), label));
			});
	}

	protected List<MultiselectItem> buildVirtualInstanceMultiselectItems() {
		List<Company> companies = _companyLocalService.getCompanies();

		List<MultiselectItem> virtualInstanceMultiselectItems =
			MultiselectItemListBuilder.add(
				multiselectItem -> {
					StringBundler sb = new StringBundler(5);

					sb.append(
						_language.get(
							_portal.getHttpServletRequest(_renderRequest),
							"system"));
					sb.append(StringPool.SPACE);
					sb.append(StringPool.OPEN_PARENTHESIS);
					sb.append(
						_indexNameBuilder.getIndexName(
							CompanyConstants.SYSTEM));
					sb.append(StringPool.CLOSE_PARENTHESIS);

					multiselectItem.setLabel(sb.toString());

					multiselectItem.setValue(
						String.valueOf(CompanyConstants.SYSTEM));
				}
			).build();

		companies.forEach(
			company -> {
				MultiselectItem multiselectItem = new MultiselectItem();

				StringBundler sb = new StringBundler(5);

				sb.append(company.getWebId());
				sb.append(StringPool.SPACE);
				sb.append(StringPool.OPEN_PARENTHESIS);
				sb.append(
					_indexNameBuilder.getIndexName(company.getCompanyId()));
				sb.append(StringPool.CLOSE_PARENTHESIS);

				multiselectItem.setLabel(sb.toString());

				multiselectItem.setValue(
					String.valueOf(company.getCompanyId()));

				virtualInstanceMultiselectItems.add(multiselectItem);
			});

		return virtualInstanceMultiselectItems;
	}

	protected String getSelectedTab() {
		String selectedTab = ParamUtil.getString(
			_renderRequest, "tabs1", "connections");

		if (!Objects.equals(selectedTab, "field-mappings") &&
			!Objects.equals(selectedTab, "index-actions") &&
			!Objects.equals(selectedTab, "connections")) {

			return "connections";
		}

		if (Objects.equals(selectedTab, "field-mappings") &&
			!isIndexInformationAvailable()) {

			return "connections";
		}

		return selectedTab;
	}

	protected boolean isIndexInformationAvailable() {
		if (_indexInformation != null) {
			return true;
		}

		return false;
	}

	private final CompanyLocalService _companyLocalService;
	private IndexInformation _indexInformation;
	private final IndexNameBuilder _indexNameBuilder;
	private final Language _language;
	private final Portal _portal;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;

}