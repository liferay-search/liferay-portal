/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch7.internal.index.configuration.contributor;

import com.liferay.portal.search.elasticsearch7.internal.index.constants.LiferayTypeMappingsConstants;
import com.liferay.portal.search.elasticsearch7.internal.util.ResourceUtil;
import com.liferay.portal.search.spi.index.configuration.contributor.CompanyIndexConfigurationContributor;
import com.liferay.portal.search.spi.index.configuration.contributor.helper.MappingsHelper;
import com.liferay.portal.search.spi.index.configuration.contributor.helper.SettingsHelper;

import org.osgi.service.component.annotations.Component;

/**
 * @author Bryan Engler
 */
@Component(service = CompanyIndexConfigurationContributor.class)
public class DefaultMappingsAndSettingsCompanyIndexConfigurationContributor
	implements CompanyIndexConfigurationContributor {

	@Override
	public void contributeMappings(MappingsHelper mappingsHelper) {
		String mappings = ResourceUtil.getResourceAsString(
			getClass(),
			LiferayTypeMappingsConstants.
				LIFERAY_DOCUMENT_TYPE_MAPPING_FILE_NAME);

		mappingsHelper.putMappings(mappings);
	}

	@Override
	public void contributeSettings(SettingsHelper settingsHelper) {
		settingsHelper.put("index.default_pipeline", "timestamp");
	}

}