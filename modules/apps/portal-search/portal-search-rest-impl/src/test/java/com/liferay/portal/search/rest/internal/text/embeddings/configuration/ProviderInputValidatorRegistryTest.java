/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.rest.internal.text.embeddings.configuration;

import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.rest.text.embeddings.configuration.ProviderInputValidator;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Rodrigo Guedes de Souza
 */
public class ProviderInputValidatorRegistryTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_providerInputValidatorRegistry = new ProviderInputValidatorRegistry();

		ReflectionTestUtil.setFieldValue(
			_providerInputValidatorRegistry, "_serviceTrackerList",
			_serviceTrackerList);

		Mockito.when(
			_serviceTrackerList.iterator()
		).thenAnswer(
			invocation -> Arrays.asList(
				_providerInputValidator
			).iterator()
		);

		Mockito.when(
			_providerInputValidator.getService()
		).thenReturn(
			"openai"
		);
	}

	@Test
	public void testValidateDispatchesToMatchingValidator() {
		Map<String, Object> serviceSettings =
			HashMapBuilder.<String, Object>put(
				"model_id", "unknown"
			).build();

		Map<String, String> fieldErrors = HashMapBuilder.put(
			"model_id", "The model is not supported."
		).build();

		Mockito.when(
			_providerInputValidator.validate(serviceSettings)
		).thenReturn(
			fieldErrors
		);

		Assert.assertSame(
			fieldErrors,
			_providerInputValidatorRegistry.validate(
				"openai", serviceSettings));
	}

	@Test
	public void testValidateServiceSettingsThatAreNotAMapIsTreatedAsEmpty() {
		Mockito.when(
			_providerInputValidator.validate(Collections.emptyMap())
		).thenReturn(
			Collections.emptyMap()
		);

		Map<String, String> fieldErrors =
			_providerInputValidatorRegistry.validate("openai", "not a map");

		Assert.assertTrue(fieldErrors.toString(), fieldErrors.isEmpty());

		Mockito.verify(
			_providerInputValidator
		).validate(
			Collections.emptyMap()
		);
	}

	@Test
	public void testValidateUnknownServiceIsValid() {
		Map<String, String> fieldErrors =
			_providerInputValidatorRegistry.validate(
				"cohere", Collections.emptyMap());

		Assert.assertTrue(fieldErrors.toString(), fieldErrors.isEmpty());

		Mockito.verify(
			_providerInputValidator, Mockito.never()
		).validate(
			Mockito.anyMap()
		);
	}

	private final ProviderInputValidator _providerInputValidator = Mockito.mock(
		ProviderInputValidator.class);
	private ProviderInputValidatorRegistry _providerInputValidatorRegistry;
	private final ServiceTrackerList<ProviderInputValidator>
		_serviceTrackerList = Mockito.mock(ServiceTrackerList.class);

}