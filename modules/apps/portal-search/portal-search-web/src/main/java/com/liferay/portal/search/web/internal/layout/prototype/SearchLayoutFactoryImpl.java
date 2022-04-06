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

package com.liferay.portal.search.web.internal.layout.prototype;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutTypePortlet;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.language.LanguageResources;
import com.liferay.portal.search.web.layout.prototype.SearchLayoutPrototypeCustomizer;

import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Adam Brandizzi
 * @author André de Oliveira
 * @author Lino Alves
 */
@Component(immediate = true, service = SearchLayoutFactory.class)
public class SearchLayoutFactoryImpl implements SearchLayoutFactory {

	@Override
	public void createSearchLayout(Group group) {
		if (!_shouldCreateSearchLayout(group)) {
			return;
		}

		_createSearchLayout(group);
	}

	protected void customize(Layout layout) throws Exception {
		if (searchLayoutPrototypeCustomizer != null) {
			searchLayoutPrototypeCustomizer.customize(layout);
		}
		else {
			_defaultSearchLayoutPrototypeCustomizer.customize(layout);
		}
	}

	protected String getLayoutTemplateId() {
		if (searchLayoutPrototypeCustomizer != null) {
			return searchLayoutPrototypeCustomizer.getLayoutTemplateId();
		}

		return _defaultSearchLayoutPrototypeCustomizer.getLayoutTemplateId();
	}

	@Reference
	protected GroupLocalService groupLocalService;

	@Reference
	protected LayoutLocalService layoutLocalService;

	@Reference(
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	protected volatile SearchLayoutPrototypeCustomizer
		searchLayoutPrototypeCustomizer;

	private void _createSearchLayout(Group group) {
		try {
			Layout layout = layoutLocalService.addLayout(
				group.getCreatorUserId(), group.getGroupId(), false,
				LayoutConstants.DEFAULT_PARENT_LAYOUT_ID,
				_getSearchTitleLocalizationMap(), null,
				_getSearchDescriptionLocalizationMap(), null, null,
				LayoutConstants.TYPE_PORTLET, StringPool.BLANK, true,
				_getFriendlyURLMap(), new ServiceContext());

			LayoutTypePortlet layoutTypePortlet =
				(LayoutTypePortlet)layout.getLayoutType();

			layoutTypePortlet.setLayoutTemplateId(
				0, getLayoutTemplateId(), false);

			layoutLocalService.updateLayout(
				layout.getGroupId(), layout.isPrivateLayout(),
				layout.getLayoutId(), layout.getTypeSettings());

			customize(layout);

			UnicodeProperties unicodeProperties =
				group.getTypeSettingsProperties();

			unicodeProperties.put("searchLayoutCreated", "true");

			group.setTypeSettingsProperties(unicodeProperties);

			groupLocalService.updateGroup(group);

			if (_log.isInfoEnabled()) {
				_log.info("Search Page created");
			}
		}
		catch (RuntimeException runtimeException) {
			throw runtimeException;
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
	}

	private Map<Locale, String> _getFriendlyURLMap() {
		return LocalizationUtil.getLocalizationMap("/search");
	}

	private Map<Locale, String> _getLocalizationMap(String key) {
		return ResourceBundleUtil.getLocalizationMap(
			LanguageResources.PORTAL_RESOURCE_BUNDLE_LOADER, key);
	}

	private Map<Locale, String> _getSearchDescriptionLocalizationMap() {
		return _getLocalizationMap("layout-prototype-search-description");
	}

	private Map<Locale, String> _getSearchTitleLocalizationMap() {
		return _getLocalizationMap("layout-prototype-search-title");
	}

	private boolean _hasSearchLayout(Group group) {
		Layout layout = layoutLocalService.fetchLayoutByFriendlyURL(
			group.getGroupId(), false, "/search");

		if (layout != null) {
			return true;
		}

		return false;
	}

	private boolean _shouldCreateSearchLayout(Group group) {
		if (_hasSearchLayout(group)) {
			return false;
		}

		UnicodeProperties unicodeProperties = group.getTypeSettingsProperties();

		if (unicodeProperties.get("searchLayoutCreated") != null) {
			return false;
		}

		return true;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SearchLayoutFactoryImpl.class);

	private final SearchLayoutPrototypeCustomizer
		_defaultSearchLayoutPrototypeCustomizer =
			new DefaultSearchLayoutPrototypeCustomizer();

}