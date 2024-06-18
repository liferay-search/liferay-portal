/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch7.internal.upgrade.v0_0_2;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.elasticsearch7.configuration.ElasticsearchConfiguration;

import java.util.Dictionary;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Petteri Karttunen
 */
public class ElasticsearchConfigurationUpgradeProcess extends UpgradeProcess {

	public ElasticsearchConfigurationUpgradeProcess(
		ConfigurationAdmin configurationAdmin) {

		_configurationAdmin = configurationAdmin;
	}

	@Override
	protected void doUpgrade() throws Exception {
		Configuration configuration = _configurationAdmin.getConfiguration(
			ElasticsearchConfiguration.class.getName(), StringPool.QUESTION);

		Dictionary<String, Object> properties = configuration.getProperties();

		if (properties == null) {
			return;
		}

		String operationMode = GetterUtil.getString(
			properties.get("operationMode"));

		if (StringUtil.equals(operationMode, "REMOTE")) {
			properties.put("productionModeEnabled", Boolean.TRUE);
		}

		properties.remove("operationMode");

		configuration.update(properties);
	}

	private final ConfigurationAdmin _configurationAdmin;

}