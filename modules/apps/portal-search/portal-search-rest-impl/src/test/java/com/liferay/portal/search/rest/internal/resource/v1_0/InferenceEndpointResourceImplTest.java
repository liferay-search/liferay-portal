/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.rest.internal.resource.v1_0;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.rest.dto.v1_0.InferenceEndpoint;
import com.liferay.portal.search.rest.internal.text.embeddings.configuration.ProviderInputValidatorRegistry;
import com.liferay.portal.search.semantic.InferenceEndpointCreator;
import com.liferay.portal.search.semantic.InferenceEndpointLocator;
import com.liferay.portal.search.semantic.InferenceIdResolver;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import java.util.Collections;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Rodrigo Guedes de Souza
 */
public class InferenceEndpointResourceImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_inferenceEndpointResourceImpl = new InferenceEndpointResourceImpl();

		ReflectionTestUtil.setFieldValue(
			InferenceEndpointResourceImpl.class,
			"_inferenceEndpointCreatorSnapshot",
			_inferenceEndpointCreatorSnapshot);
		ReflectionTestUtil.setFieldValue(
			InferenceEndpointResourceImpl.class,
			"_inferenceEndpointLocatorSnapshot",
			_inferenceEndpointLocatorSnapshot);
		ReflectionTestUtil.setFieldValue(
			_inferenceEndpointResourceImpl, "_inferenceIdResolver",
			_inferenceIdResolver);
		ReflectionTestUtil.setFieldValue(
			_inferenceEndpointResourceImpl, "_jsonFactory",
			new JSONFactoryImpl());
		ReflectionTestUtil.setFieldValue(
			_inferenceEndpointResourceImpl, "_providerInputValidatorRegistry",
			_providerInputValidatorRegistry);

		Mockito.when(
			_inferenceEndpointCreatorSnapshot.get()
		).thenReturn(
			_inferenceEndpointCreator
		);

		Mockito.when(
			_inferenceEndpointLocatorSnapshot.get()
		).thenReturn(
			_inferenceEndpointLocator
		);

		Mockito.when(
			_providerInputValidatorRegistry.validate(
				Mockito.anyString(), Mockito.anyMap())
		).thenReturn(
			Collections.emptyMap()
		);

		Mockito.when(
			_inferenceIdResolver.composeInferenceId(_COMPANY_ID, "openai")
		).thenReturn(
			_INFERENCE_ID
		);

		Mockito.when(
			_company.getCompanyId()
		).thenReturn(
			_COMPANY_ID
		);

		_inferenceEndpointResourceImpl.contextCompany = _company;

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
	public void testPostInferenceEndpoint() throws Exception {
		InferenceEndpoint inferenceEndpoint =
			_inferenceEndpointResourceImpl.postInferenceEndpoint(
				_toInferenceEndpoint("openai"));

		Assert.assertNull(inferenceEndpoint.getErrorMessage());
		Assert.assertEquals(_INFERENCE_ID, inferenceEndpoint.getInferenceId());
		Assert.assertEquals("openai", inferenceEndpoint.getService());
		Assert.assertNull(inferenceEndpoint.getServiceSettings());

		ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(
			String.class);

		Mockito.verify(
			_inferenceEndpointCreator
		).createInferenceEndpoint(
			Mockito.eq(_INFERENCE_ID), Mockito.eq("openai"),
			argumentCaptor.capture()
		);

		String serviceSettingsJSON = argumentCaptor.getValue();

		Assert.assertTrue(
			serviceSettingsJSON, serviceSettingsJSON.contains("api_key"));
	}

	@Test
	public void testPostInferenceEndpointBlankService() {
		try {
			_inferenceEndpointResourceImpl.postInferenceEndpoint(
				_toInferenceEndpoint(null));

			Assert.fail();
		}
		catch (Exception exception) {
			Assert.assertTrue(exception instanceof BadRequestException);
		}
	}

	@Test
	public void testPostInferenceEndpointFeatureFlagDisabled() {
		_setUpFeatureFlagManagerUtil(false);

		try {
			_inferenceEndpointResourceImpl.postInferenceEndpoint(
				_toInferenceEndpoint("openai"));

			Assert.fail();
		}
		catch (Exception exception) {
			Assert.assertTrue(exception instanceof NotFoundException);
		}
	}

	@Test
	public void testPostInferenceEndpointWhenEndpointAlreadyExists()
		throws Exception {

		Mockito.when(
			_inferenceIdResolver.composeInferenceIdPrefix(_COMPANY_ID)
		).thenReturn(
			"liferay-12345-inference-"
		);

		Mockito.when(
			_inferenceEndpointLocator.findInferenceId(
				"liferay-12345-inference-")
		).thenReturn(
			"liferay-12345-inference-hugging_face"
		);

		try {
			_inferenceEndpointResourceImpl.postInferenceEndpoint(
				_toInferenceEndpoint("openai"));

			Assert.fail();
		}
		catch (ClientErrorException clientErrorException) {
			Response response = clientErrorException.getResponse();

			Assert.assertEquals(
				Response.Status.CONFLICT.getStatusCode(), response.getStatus());
		}

		Mockito.verifyNoInteractions(_inferenceEndpointCreator);
	}

	@Test
	public void testPostInferenceEndpointWithoutInferenceEndpointCreator()
		throws Exception {

		Mockito.when(
			_inferenceEndpointCreatorSnapshot.get()
		).thenReturn(
			null
		);

		InferenceEndpoint inferenceEndpoint =
			_inferenceEndpointResourceImpl.postInferenceEndpoint(
				_toInferenceEndpoint("openai"));

		Assert.assertEquals(
			"Inference endpoints are only supported when the search engine " +
				"is Elasticsearch.",
			inferenceEndpoint.getErrorMessage());
		Assert.assertNull(inferenceEndpoint.getInferenceId());

		Mockito.verifyNoInteractions(_inferenceEndpointCreator);
	}

	@Test
	public void testPostInferenceEndpointWithoutPermission() {
		Mockito.when(
			_permissionChecker.isCompanyAdmin()
		).thenReturn(
			false
		);

		try {
			_inferenceEndpointResourceImpl.postInferenceEndpoint(
				_toInferenceEndpoint("openai"));

			Assert.fail();
		}
		catch (Exception exception) {
			Assert.assertTrue(exception instanceof NotAuthorizedException);
		}
	}

	@Test
	public void testPostInferenceEndpointWrapsCreatorException()
		throws Exception {

		String message =
			"Unable to create inference endpoint " +
				"\"liferay-12345-inference-openai\"";

		Mockito.doThrow(
			new RuntimeException(message)
		).when(
			_inferenceEndpointCreator
		).createInferenceEndpoint(
			Mockito.anyString(), Mockito.anyString(), Mockito.anyString()
		);

		InferenceEndpoint inferenceEndpoint =
			_inferenceEndpointResourceImpl.postInferenceEndpoint(
				_toInferenceEndpoint("openai"));

		Assert.assertEquals(message, inferenceEndpoint.getErrorMessage());
		Assert.assertEquals(_INFERENCE_ID, inferenceEndpoint.getInferenceId());
		Assert.assertNull(inferenceEndpoint.getServiceSettings());
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
				"api_key", "secret"
			).put(
				"model_id", "text-embedding-3-large"
			).build());

		return inferenceEndpoint;
	}

	private static final long _COMPANY_ID = 12345;

	private static final String _INFERENCE_ID =
		"liferay-12345-inference-openai";

	private final Company _company = Mockito.mock(Company.class);
	private final MockedStatic<FeatureFlagManagerUtil>
		_featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
			FeatureFlagManagerUtil.class);
	private final InferenceEndpointCreator _inferenceEndpointCreator =
		Mockito.mock(InferenceEndpointCreator.class);
	private final Snapshot<InferenceEndpointCreator>
		_inferenceEndpointCreatorSnapshot = Mockito.mock(Snapshot.class);
	private final InferenceEndpointLocator _inferenceEndpointLocator =
		Mockito.mock(InferenceEndpointLocator.class);
	private final Snapshot<InferenceEndpointLocator>
		_inferenceEndpointLocatorSnapshot = Mockito.mock(Snapshot.class);
	private InferenceEndpointResourceImpl _inferenceEndpointResourceImpl;
	private final InferenceIdResolver _inferenceIdResolver = Mockito.mock(
		InferenceIdResolver.class);
	private final PermissionChecker _permissionChecker = Mockito.mock(
		PermissionChecker.class);
	private final ProviderInputValidatorRegistry
		_providerInputValidatorRegistry = Mockito.mock(
			ProviderInputValidatorRegistry.class);

}