/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.spi.index.configuration.contributor;

import com.liferay.portal.search.spi.index.configuration.contributor.helper.IndexSettingsHelper;
import com.liferay.portal.search.spi.index.configuration.contributor.helper.MappingsHelper;

/**
 * @author Adam Brandizzi
 */
public interface IndexConfigurationContributor {

	public void contributeMappings(MappingsHelper mappingsHelper);

	public void contributeSettings(IndexSettingsHelper indexSettingsHelper);

}