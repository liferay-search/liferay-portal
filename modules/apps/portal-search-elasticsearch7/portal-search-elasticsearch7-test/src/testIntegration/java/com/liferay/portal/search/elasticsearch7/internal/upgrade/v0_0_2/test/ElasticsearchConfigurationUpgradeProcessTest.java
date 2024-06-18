/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch7.internal.upgrade.v0_0_2.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.upgrade.UpgradeStep;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.search.elasticsearch7.configuration.ElasticsearchConfiguration;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;

import java.util.Dictionary;
import java.util.Objects;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Petteri Karttunen
 */
@RunWith(Arquillian.class)
public class ElasticsearchConfigurationUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		Configuration configuration = _getConfiguration();

		if (configuration.getProperties() != null) {
			_originalProperties = configuration.getProperties();
		}

		configuration.update(
			HashMapDictionaryBuilder.putAll(
				configuration.getProperties()
			).put(
				"operationMode", "REMOTE"
			).put(
				"productionModeEnabled", Boolean.FALSE
			).build());
	}

	@After
	public void tearDown() throws Exception {
		if (_originalProperties != null) {
			Configuration configuration = _getConfiguration();

			configuration.update(_originalProperties);
		}
	}

	@Test
	public void testUpgrade() throws Exception {
		Configuration configuration = _getConfiguration();

		Dictionary<String, Object> properties = configuration.getProperties();

		Assert.assertEquals(
			"REMOTE", GetterUtil.getString(properties.get("operationMode")));
		Assert.assertEquals(
			GetterUtil.getBoolean(properties.get("productionModeEnabled")),
			Boolean.FALSE);

		UpgradeProcess upgradeProcess = _getUpgradeProcess();

		upgradeProcess.upgrade();

		Configuration upgradedConfiguration = _getConfiguration();

		Dictionary<String, Object> upgradedProperties =
			upgradedConfiguration.getProperties();

		Assert.assertNull(upgradedProperties.get("operationMode"));
		Assert.assertEquals(
			GetterUtil.getBoolean(
				upgradedProperties.get("productionModeEnabled")),
			Boolean.TRUE);
	}

	private Configuration _getConfiguration() throws Exception {
		return _configurationAdmin.getConfiguration(
			ElasticsearchConfiguration.class.getName(), StringPool.QUESTION);
	}

	private UpgradeProcess _getUpgradeProcess() {
		UpgradeProcess[] upgradeProcesses = new UpgradeProcess[1];

		_upgradeStepRegistrator.register(
			(fromSchemaVersionString, toSchemaVersionString, upgradeSteps) -> {
				for (UpgradeStep upgradeStep : upgradeSteps) {
					Class<? extends UpgradeStep> clazz = upgradeStep.getClass();

					if (Objects.equals(clazz.getName(), _CLASS_NAME)) {
						upgradeProcesses[0] = (UpgradeProcess)upgradeStep;

						break;
					}
				}
			});

		return upgradeProcesses[0];
	}

	private static final String _CLASS_NAME =
		"com.liferay.portal.search.elasticsearch7.internal.upgrade.v0_0_2." +
			"ElasticsearchConfigurationUpgradeProcess";

	@Inject(
		filter = "(&(component.name=com.liferay.portal.search.elasticsearch7.internal.upgrade.registry.ElasticsearchUpgradeStepRegistrator))"
	)
	private static UpgradeStepRegistrator _upgradeStepRegistrator;

	@Inject
	private ConfigurationAdmin _configurationAdmin;

	private Dictionary<String, Object> _originalProperties;

}