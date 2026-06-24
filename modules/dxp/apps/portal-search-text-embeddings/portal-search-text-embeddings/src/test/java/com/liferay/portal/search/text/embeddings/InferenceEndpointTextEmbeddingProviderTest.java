/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.text.embeddings;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.search.rest.dto.v1_0.EmbeddingProviderConfiguration;
import com.liferay.portal.search.semantic.InferenceEndpointMetadata;
import com.liferay.portal.search.semantic.InferenceEndpointMetadataResolver;
import com.liferay.portal.search.semantic.InferenceIdResolver;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Rodrigo Guedes de Souza
 */
public class InferenceEndpointTextEmbeddingProviderTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_inferenceEndpointTextEmbeddingProvider =
			new InferenceEndpointTextEmbeddingProvider();

		ReflectionTestUtil.setFieldValue(
			_inferenceEndpointTextEmbeddingProvider,
			"_inferenceEndpointMetadataResolver",
			_inferenceEndpointMetadataResolver);
		ReflectionTestUtil.setFieldValue(
			_inferenceEndpointTextEmbeddingProvider, "_inferenceIdResolver",
			_inferenceIdResolver);
	}

	@Test
	public void testGetEmbedding() {
		EmbeddingProviderConfiguration embeddingProviderConfiguration =
			new EmbeddingProviderConfiguration();

		embeddingProviderConfiguration.setProviderName(
			"elasticsearch-inference-endpoint");

		try {
			_inferenceEndpointTextEmbeddingProvider.getEmbedding(
				embeddingProviderConfiguration, "Hello world");

			Assert.fail();
		}
		catch (UnsupportedOperationException unsupportedOperationException) {
			Assert.assertEquals(
				"Embeddings are computed server-side by Elasticsearch via " +
					"semantic_text fields",
				unsupportedOperationException.getMessage());
		}
	}

	@Test
	public void testGetEndpointMetadata() {
		Mockito.when(
			_inferenceIdResolver.resolveInferenceId(_COMPANY_ID)
		).thenReturn(
			_INFERENCE_ID
		);

		InferenceEndpointMetadata inferenceEndpointMetadata =
			new InferenceEndpointMetadata(
				3072, "text-embedding-3-large", "openai");

		Mockito.when(
			_inferenceEndpointMetadataResolver.resolveInferenceEndpointMetadata(
				_INFERENCE_ID)
		).thenReturn(
			inferenceEndpointMetadata
		);

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(_COMPANY_ID)) {

			Assert.assertSame(
				inferenceEndpointMetadata,
				_inferenceEndpointTextEmbeddingProvider.getEndpointMetadata());
		}

		Mockito.verify(
			_inferenceEndpointMetadataResolver
		).resolveInferenceEndpointMetadata(
			_INFERENCE_ID
		);
	}

	@Test
	public void testGetEndpointMetadataWithoutResolvedInferenceId() {
		Mockito.when(
			_inferenceIdResolver.resolveInferenceId(_COMPANY_ID)
		).thenReturn(
			null
		);

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(_COMPANY_ID)) {

			Assert.assertNull(
				_inferenceEndpointTextEmbeddingProvider.getEndpointMetadata());
		}

		Mockito.verifyNoInteractions(_inferenceEndpointMetadataResolver);
	}

	@Test
	public void testGetProviderName() {
		Assert.assertEquals(
			"elasticsearch-inference-endpoint",
			_inferenceEndpointTextEmbeddingProvider.getProviderName());
	}

	@Test
	public void testNoExternalHTTPClient() {
		Class<?> clazz = InferenceEndpointTextEmbeddingProvider.class;

		for (Field field : clazz.getDeclaredFields()) {
			Assert.assertNotEquals(Http.class, field.getType());
		}
	}

	private static final long _COMPANY_ID = 12345;

	private static final String _INFERENCE_ID = "liferay-active-provider";

	private final InferenceEndpointMetadataResolver
		_inferenceEndpointMetadataResolver = Mockito.mock(
			InferenceEndpointMetadataResolver.class);
	private InferenceEndpointTextEmbeddingProvider
		_inferenceEndpointTextEmbeddingProvider;
	private final InferenceIdResolver _inferenceIdResolver = Mockito.mock(
		InferenceIdResolver.class);

}