/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.web.internal.upgrade.registry.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.Release;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;

import java.util.Collection;
import java.util.Objects;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.component.runtime.dto.ReferenceDTO;

/**
 * @author Brian Wulbern
 */
@RunWith(Arquillian.class)
public class SearchWebUpgradeStepRegistratorTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	@TestInfo("LPD-112500")
	public void testRegister() throws Exception {
		BundleContext bundleContext = SystemBundleUtil.getBundleContext();

		Collection<ServiceReference<UpgradeStepRegistrator>> serviceReferences =
			bundleContext.getServiceReferences(
				UpgradeStepRegistrator.class,
				"(component.name=" + _CLASS_NAME + ")");

		Assert.assertEquals(
			serviceReferences.toString(), 1, serviceReferences.size());

		for (ServiceReference<UpgradeStepRegistrator> serviceReference :
				serviceReferences) {

			Assert.assertTrue(
				_hasReference(
					_serviceComponentRuntime.getComponentDescriptionDTO(
						serviceReference.getBundle(), _CLASS_NAME),
					Release.class.getName(),
					"(&(release.bundle.symbolic.name=com.liferay.dynamic." +
						"data.mapping.service)(release.schema.version>=" +
							"0.0.2))"));
		}
	}

	private boolean _hasReference(
		ComponentDescriptionDTO componentDescriptionDTO, String interfaceName,
		String target) {

		for (ReferenceDTO referenceDTO : componentDescriptionDTO.references) {
			if (Objects.equals(referenceDTO.interfaceName, interfaceName) &&
				Objects.equals(referenceDTO.target, target)) {

				return true;
			}
		}

		return false;
	}

	private static final String _CLASS_NAME =
		"com.liferay.portal.search.web.internal.upgrade.registry." +
			"SearchWebUpgradeStepRegistrator";

	@Inject
	private ServiceComponentRuntime _serviceComponentRuntime;

}