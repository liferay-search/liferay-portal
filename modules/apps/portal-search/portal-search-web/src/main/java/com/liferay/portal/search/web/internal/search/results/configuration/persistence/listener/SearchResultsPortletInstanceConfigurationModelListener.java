/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.web.internal.search.results.configuration.persistence.listener;

import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListener;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleThreadLocal;
import com.liferay.portal.search.engine.SearchEngineInformation;
import com.liferay.portal.search.web.internal.search.results.configuration.SearchResultsPortletInstanceConfiguration;

import java.util.Dictionary;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Joshua Cords
 */
@Component(
	property = "model.class.name=com.liferay.portal.search.web.internal.search.results.configuration.SearchResultsPortletInstanceConfiguration",
	service = ConfigurationModelListener.class
)
public class SearchResultsPortletInstanceConfigurationModelListener
	implements ConfigurationModelListener {

	@Override
	public void onBeforeSave(String pid, Dictionary<String, Object> properties)
		throws ConfigurationModelListenerException {

		SearchEngineInformation searchEngineInformation =
			_searchEngineInformationSnapshot.get();

		if ((searchEngineInformation == null) ||
			!FeatureFlagManagerUtil.isEnabled(
				CompanyThreadLocal.getCompanyId(), "LPD-64988")) {

			return;
		}

		int indexMaxResultWindow =
			searchEngineInformation.getIndexMaxResultWindow();

		if (GetterUtil.getInteger(properties.get("maximumSearchDepth")) <=
				indexMaxResultWindow) {

			return;
		}

		throw new ConfigurationModelListenerException(
			_language.format(
				LocaleThreadLocal.getThemeDisplayLocale(),
				"the-maximum-search-depth-must-be-less-than-or-equal-to-the-" +
					"max-result-window-x",
				indexMaxResultWindow),
			SearchResultsPortletInstanceConfiguration.class, getClass(),
			properties);
	}

	private static final Snapshot<SearchEngineInformation>
		_searchEngineInformationSnapshot = new Snapshot<>(
			SearchResultsPortletInstanceConfigurationModelListener.class,
			SearchEngineInformation.class, null, true);

	@Reference
	private Language _language;

}