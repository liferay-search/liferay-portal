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

package com.liferay.search.experiences.blueprints.admin.web.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenuBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemListBuilder;
import com.liferay.petra.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.search.experiences.blueprints.admin.web.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.search.experiences.blueprints.admin.web.internal.constants.BlueprintsAdminWebKeys;
import com.liferay.search.experiences.blueprints.admin.web.internal.security.permission.resource.BlueprintsAdminPermission;
import com.liferay.search.experiences.blueprints.constants.BlueprintsActionKeys;
import com.liferay.search.experiences.blueprints.constants.ElementTypes;
import com.liferay.search.experiences.blueprints.model.Element;

import java.util.List;

import javax.portlet.PortletURL;

/**
 * @author Petteri Karttunen
 */
public class ViewElementsManagementToolbarDisplayContext
	extends ViewEntriesManagementToolbarDisplayContext {

	public ViewElementsManagementToolbarDisplayContext(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		SearchContainer<Element> searchContainer, String displayStyle) {

		super(
			liferayPortletRequest.getHttpServletRequest(),
			liferayPortletRequest, liferayPortletResponse, searchContainer,
			displayStyle);
	}

	@Override
	public List<DropdownItem> getActionDropdownItems() {
		return DropdownItemListBuilder.add(
			dropdownItem -> {
				dropdownItem.putData("action", "hideEntries");

				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, "hide"));

				dropdownItem.setQuickAction(true);
			}
		).add(
			dropdownItem -> {
				dropdownItem.putData("action", "showEntries");

				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, "show"));

				dropdownItem.setQuickAction(true);
			}
		).add(
			dropdownItem -> {
				dropdownItem.putData("action", "deleteEntries");

				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, "delete"));

				dropdownItem.setQuickAction(true);
			}
		).build();
	}

	@Override
	public CreationMenu getCreationMenu() {
		if (!BlueprintsAdminPermission.contains(
				themeDisplay.getPermissionChecker(),
				themeDisplay.getScopeGroupId(),
				BlueprintsActionKeys.ADD_ELEMENT)) {

			return null;
		}

		return CreationMenuBuilder.addDropdownItem(
			dropdownItem -> {
				dropdownItem.putData("action", "addElement");
				dropdownItem.putData(
					"defaultLocale",
					LocaleUtil.toLanguageId(LocaleUtil.getDefault()));

				dropdownItem.putData(
					"editElementURL",
					createActionURL(
						BlueprintsAdminMVCCommandNames.EDIT_ELEMENT,
						Constants.ADD));

				dropdownItem.putData(
					"type", String.valueOf(ElementTypes.QUERY_ELEMENT));
				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, "add-element"));
			}
		).build();
	}

	@Override
	public List<DropdownItem> getFilterDropdownItems() {
		return DropdownItemListBuilder.addGroup(
			dropdownGroupItem -> {
				dropdownGroupItem.setDropdownItems(
					_getFilterVisibilityDropdownItems());
				dropdownGroupItem.setLabel(
					LanguageUtil.get(
						httpServletRequest, "filter-by-visibility"));
			}
		).addGroup(
			dropdownGroupItem -> {
				dropdownGroupItem.setDropdownItems(getOrderByDropdownItems());
				dropdownGroupItem.setLabel(
					LanguageUtil.get(httpServletRequest, "order-by"));
			}
		).build();
	}

	@Override
	public Boolean isDisabled() {
		return false;
	}

	private PortletURL _getFilterURL(Boolean hidden) {
		return PortletURLBuilder.create(
			getPortletURL()
		).setParameter(
			BlueprintsAdminWebKeys.HIDDEN, hidden
		).build();
	}

	private List<DropdownItem> _getFilterVisibilityDropdownItems() {
		return DropdownItemListBuilder.add(
			dropdownItem -> {
				dropdownItem.setActive(!_getHidden());
				dropdownItem.setHref(_getFilterURL(Boolean.FALSE));
				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, "visible"));
			}
		).add(
			dropdownItem -> {
				dropdownItem.setActive(_getHidden());
				dropdownItem.setHref(_getFilterURL(Boolean.TRUE));
				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, "hidden"));
			}
		).build();
	}

	private Boolean _getHidden() {
		return ParamUtil.getBoolean(
			liferayPortletRequest, BlueprintsAdminWebKeys.HIDDEN,
			Boolean.FALSE);
	}

}