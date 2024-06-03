/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.index.configuration.contributor;

import com.liferay.portal.search.opensearch2.internal.index.constants.MappingsConstants;
import com.liferay.portal.search.opensearch2.internal.util.ResourceUtil;
import com.liferay.portal.search.spi.index.configuration.contributor.IndexConfigurationContributor;
import com.liferay.portal.search.spi.index.configuration.contributor.helper.IndexSettingsHelper;
import com.liferay.portal.search.spi.index.configuration.contributor.helper.TypeMappingsHelper;

import org.osgi.service.component.annotations.Component;

/**
 * @author Gustavo Lima
 */
@Component(service = IndexConfigurationContributor.class)
public class DefaultMappingsAndSettingsIndexConfigurationContributor
	implements IndexConfigurationContributor {

	@Override
	public void contributeMappings(TypeMappingsHelper typeMappingsHelper) {
		String mappings = ResourceUtil.getResourceAsString(
			getClass(), MappingsConstants.LIFERAY_MAPPING_FILE_NAME);

		typeMappingsHelper.putTypeMappings(mappings);
	}

	@Override
	public void contributeSettings(IndexSettingsHelper indexSettingsHelper) {
		indexSettingsHelper.put("index.default_pipeline", "timestamp");
	}

}