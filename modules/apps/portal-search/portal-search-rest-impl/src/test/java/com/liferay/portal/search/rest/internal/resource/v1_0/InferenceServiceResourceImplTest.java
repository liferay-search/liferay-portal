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
import com.liferay.portal.search.rest.dto.v1_0.InferenceService;
import com.liferay.portal.search.semantic.InferenceServicesResolver;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.portal.vulcan.pagination.Page;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;

import java.util.Arrays;
import java.util.List;
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
public class InferenceServiceResourceImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_inferenceServiceResourceImpl = new InferenceServiceResourceImpl();

		ReflectionTestUtil.setFieldValue(
			InferenceServiceResourceImpl.class,
			"_inferenceServicesResolverSnapshot",
			_inferenceServicesResolverSnapshot);
		ReflectionTestUtil.setFieldValue(
			_inferenceServiceResourceImpl, "_jsonFactory",
			new JSONFactoryImpl());

		Mockito.when(
			_inferenceServicesResolverSnapshot.get()
		).thenReturn(
			_inferenceServicesResolver
		);

		Mockito.when(
			_company.getCompanyId()
		).thenReturn(
			_COMPANY_ID
		);

		_inferenceServiceResourceImpl.contextCompany = _company;

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
	public void testGetInferenceServicesPage() throws Exception {
		Mockito.when(
			_inferenceServicesResolver.resolveInferenceServices()
		).thenReturn(
			Arrays.asList(
				new com.liferay.portal.search.semantic.InferenceService(
					"{\"api_key\": {\"required\": true, \"sensitive\": true}}",
					"openai"),
				new com.liferay.portal.search.semantic.InferenceService(
					null, "hugging_face"))
		);

		Page<InferenceService> inferenceServicesPage =
			_inferenceServiceResourceImpl.getInferenceServicesPage();

		List<InferenceService> inferenceServices =
			(List<InferenceService>)inferenceServicesPage.getItems();

		Assert.assertEquals(
			inferenceServices.toString(), 2, inferenceServices.size());

		InferenceService inferenceService = inferenceServices.get(0);

		Assert.assertEquals("openai", inferenceService.getService());

		Map<String, Object> configuration =
			(Map<String, Object>)inferenceService.getConfiguration();

		Assert.assertTrue(
			configuration.toString(), configuration.containsKey("api_key"));

		inferenceService = inferenceServices.get(1);

		Assert.assertNull(inferenceService.getConfiguration());
		Assert.assertEquals("hugging_face", inferenceService.getService());
	}

	@Test
	public void testGetInferenceServicesPageFeatureFlagDisabled() {
		_setUpFeatureFlagManagerUtil(false);

		try {
			_inferenceServiceResourceImpl.getInferenceServicesPage();

			Assert.fail();
		}
		catch (Exception exception) {
			Assert.assertTrue(exception instanceof NotFoundException);
		}
	}

	@Test
	public void testGetInferenceServicesPageWithoutInferenceServicesResolver()
		throws Exception {

		Mockito.when(
			_inferenceServicesResolverSnapshot.get()
		).thenReturn(
			null
		);

		Page<InferenceService> inferenceServicesPage =
			_inferenceServiceResourceImpl.getInferenceServicesPage();

		List<InferenceService> inferenceServices =
			(List<InferenceService>)inferenceServicesPage.getItems();

		Assert.assertEquals(
			inferenceServices.toString(), 0, inferenceServices.size());
	}

	@Test
	public void testGetInferenceServicesPageWithoutPermission() {
		Mockito.when(
			_permissionChecker.isCompanyAdmin()
		).thenReturn(
			false
		);

		try {
			_inferenceServiceResourceImpl.getInferenceServicesPage();

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

	private static final long _COMPANY_ID = 12345;

	private final Company _company = Mockito.mock(Company.class);
	private final MockedStatic<FeatureFlagManagerUtil>
		_featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
			FeatureFlagManagerUtil.class);
	private InferenceServiceResourceImpl _inferenceServiceResourceImpl;
	private final InferenceServicesResolver _inferenceServicesResolver =
		Mockito.mock(InferenceServicesResolver.class);
	private final Snapshot<InferenceServicesResolver>
		_inferenceServicesResolverSnapshot = Mockito.mock(Snapshot.class);
	private final PermissionChecker _permissionChecker = Mockito.mock(
		PermissionChecker.class);

}