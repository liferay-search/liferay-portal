/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.rest.internal.resource.v1_0;

import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.rest.dto.v1_0.InferenceEndpoint;
import com.liferay.portal.search.rest.dto.v1_0.InferenceEndpointValidation;
import com.liferay.portal.search.rest.internal.text.embeddings.configuration.ProviderInputValidatorRegistry;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;

import java.util.Collections;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Rodrigo Guedes de Souza
 */
public class InferenceEndpointValidationResourceImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_inferenceEndpointValidationResourceImpl =
			new InferenceEndpointValidationResourceImpl();

		ReflectionTestUtil.setFieldValue(
			_inferenceEndpointValidationResourceImpl,
			"_providerInputValidatorRegistry", _providerInputValidatorRegistry);

		Mockito.when(
			_company.getCompanyId()
		).thenReturn(
			_COMPANY_ID
		);

		_inferenceEndpointValidationResourceImpl.contextCompany = _company;

		Mockito.when(
			_permissionChecker.isCompanyAdmin()
		).thenReturn(
			true
		);

		PermissionThreadLocal.setPermissionChecker(_permissionChecker);

		_setUpFeatureFlagManagerUtil(true);
	}

	@After
	public void tearDown() {
		_featureFlagManagerUtilMockedStatic.close();

		PermissionThreadLocal.setPermissionChecker(null);
	}

	@Test
	public void testPostInferenceEndpointValidateBlankService() {
		try {
			_inferenceEndpointValidationResourceImpl.
				postInferenceEndpointValidate(_toInferenceEndpoint(null));

			Assert.fail();
		}
		catch (Exception exception) {
			Assert.assertTrue(exception instanceof BadRequestException);
		}
	}

	@Test
	public void testPostInferenceEndpointValidateFeatureFlagDisabled() {
		_setUpFeatureFlagManagerUtil(false);

		try {
			_inferenceEndpointValidationResourceImpl.
				postInferenceEndpointValidate(_toInferenceEndpoint("openai"));

			Assert.fail();
		}
		catch (Exception exception) {
			Assert.assertTrue(exception instanceof NotFoundException);
		}
	}

	@Test
	public void testPostInferenceEndpointValidateInvalid() throws Exception {
		Map<String, String> fieldErrors = HashMapBuilder.put(
			"model_id", "The model is not supported."
		).build();

		Mockito.when(
			_providerInputValidatorRegistry.validate(
				Mockito.eq("openai"), Mockito.anyMap())
		).thenReturn(
			fieldErrors
		);

		InferenceEndpointValidation inferenceEndpointValidation =
			_inferenceEndpointValidationResourceImpl.
				postInferenceEndpointValidate(_toInferenceEndpoint("openai"));

		Assert.assertEquals(
			Boolean.FALSE, inferenceEndpointValidation.getValid());

		Map<String, String> resultFieldErrors =
			(Map<String, String>)inferenceEndpointValidation.getFieldErrors();

		Assert.assertEquals(
			"The model is not supported.", resultFieldErrors.get("model_id"));
	}

	@Test
	public void testPostInferenceEndpointValidateValid() throws Exception {
		Mockito.when(
			_providerInputValidatorRegistry.validate(
				Mockito.eq("openai"), Mockito.anyMap())
		).thenReturn(
			Collections.emptyMap()
		);

		InferenceEndpointValidation inferenceEndpointValidation =
			_inferenceEndpointValidationResourceImpl.
				postInferenceEndpointValidate(_toInferenceEndpoint("openai"));

		Assert.assertEquals(
			Boolean.TRUE, inferenceEndpointValidation.getValid());
	}

	@Test
	public void testPostInferenceEndpointValidateWithoutPermission() {
		Mockito.when(
			_permissionChecker.isCompanyAdmin()
		).thenReturn(
			false
		);

		try {
			_inferenceEndpointValidationResourceImpl.
				postInferenceEndpointValidate(_toInferenceEndpoint("openai"));

			Assert.fail();
		}
		catch (Exception exception) {
			Assert.assertTrue(exception instanceof NotAuthorizedException);
		}
	}

	private void _setUpFeatureFlagManagerUtil(boolean enabled) {
		_featureFlagManagerUtilMockedStatic.when(
			() -> FeatureFlagManagerUtil.isEnabled(_COMPANY_ID, "LPD-11319")
		).thenReturn(
			enabled
		);
	}

	private InferenceEndpoint _toInferenceEndpoint(String service) {
		InferenceEndpoint inferenceEndpoint = new InferenceEndpoint();

		inferenceEndpoint.setService(service);
		inferenceEndpoint.setServiceSettings(
			HashMapBuilder.<String, Object>put(
				"model_id", "text-embedding-9-ultra"
			).build());

		return inferenceEndpoint;
	}

	private static final long _COMPANY_ID = 12345;

	private final Company _company = Mockito.mock(Company.class);
	private final MockedStatic<FeatureFlagManagerUtil>
		_featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
			FeatureFlagManagerUtil.class);
	private InferenceEndpointValidationResourceImpl
		_inferenceEndpointValidationResourceImpl;
	private final PermissionChecker _permissionChecker = Mockito.mock(
		PermissionChecker.class);
	private final ProviderInputValidatorRegistry
		_providerInputValidatorRegistry = Mockito.mock(
			ProviderInputValidatorRegistry.class);

}