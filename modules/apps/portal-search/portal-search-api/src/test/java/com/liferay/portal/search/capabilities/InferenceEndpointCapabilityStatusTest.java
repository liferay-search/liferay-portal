/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.capabilities;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Rodrigo Guedes de Souza
 */
public class InferenceEndpointCapabilityStatusTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testAvailable() {
		InferenceEndpointCapabilityStatus inferenceEndpointCapabilityStatus =
			InferenceEndpointCapabilityStatus.available();

		Assert.assertTrue(inferenceEndpointCapabilityStatus.isAvailable());
		Assert.assertEquals("", inferenceEndpointCapabilityStatus.getReason());
	}

	@Test
	public void testUnavailable() {
		InferenceEndpointCapabilityStatus inferenceEndpointCapabilityStatus =
			InferenceEndpointCapabilityStatus.unavailable(
				"semantic-search.capability.feature-flag-disabled");

		Assert.assertFalse(inferenceEndpointCapabilityStatus.isAvailable());
		Assert.assertEquals(
			"semantic-search.capability.feature-flag-disabled",
			inferenceEndpointCapabilityStatus.getReason());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnavailableWithEmptyReason() {
		InferenceEndpointCapabilityStatus.unavailable("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnavailableWithNullReason() {
		InferenceEndpointCapabilityStatus.unavailable(null);
	}

}