/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.semantic;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.configuration.SemanticSearchConfiguration;
import com.liferay.portal.search.configuration.SemanticSearchConfigurationProvider;
import com.liferay.portal.search.rest.dto.v1_0.EmbeddingProviderConfiguration;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Rodrigo Guedes de Souza
 */
public class InferenceIdResolverImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_inferenceIdResolverImpl = new InferenceIdResolverImpl();

		ReflectionTestUtil.setFieldValue(
			_inferenceIdResolverImpl, "_semanticSearchConfigurationProvider",
			_semanticSearchConfigurationProvider);

		Mockito.when(
			_semanticSearchConfigurationProvider.getCompanyConfiguration(
				Mockito.anyLong())
		).thenReturn(
			_semanticSearchConfiguration
		);
	}

	@Test
	public void testResolveInferenceIdReturnsComposedNameForElasticsearchProvider() {
		_setProviderConfigurationJSONs(
			_elasticsearchProviderJSON("hugging_face"));

		Assert.assertEquals(
			"liferay-42-inference-hugging_face",
			_inferenceIdResolverImpl.resolveInferenceId(_COMPANY_ID));

		Mockito.verify(
			_semanticSearchConfigurationProvider
		).getCompanyConfiguration(
			_COMPANY_ID
		);
	}

	@Test
	public void testResolveInferenceIdReturnsFirstElasticsearchProvider() {
		_setProviderConfigurationJSONs(
			_elasticsearchProviderJSON("openai"),
			_elasticsearchProviderJSON("hugging_face"));

		Assert.assertEquals(
			"liferay-42-inference-openai",
			_inferenceIdResolverImpl.resolveInferenceId(_COMPANY_ID));
	}

	@Test
	public void testResolveInferenceIdReturnsNullWhenAllJSONsAreMalformed() {
		_setProviderConfigurationJSONs("", "not-valid-json");

		Assert.assertNull(
			_inferenceIdResolverImpl.resolveInferenceId(_COMPANY_ID));
	}

	@Test
	public void testResolveInferenceIdReturnsNullWhenAttributesAreNotAMap() {
		_setProviderConfigurationJSONs(
			_toJSON("not-a-map", _ELASTICSEARCH_PROVIDER_NAME));

		Assert.assertNull(
			_inferenceIdResolverImpl.resolveInferenceId(_COMPANY_ID));
	}

	@Test
	public void testResolveInferenceIdReturnsNullWhenElasticsearchProviderHasNoAttributes() {
		_setProviderConfigurationJSONs(
			_toJSON(null, _ELASTICSEARCH_PROVIDER_NAME));

		Assert.assertNull(
			_inferenceIdResolverImpl.resolveInferenceId(_COMPANY_ID));
	}

	@Test
	public void testResolveInferenceIdReturnsNullWhenElasticsearchProviderHasNoService() {
		_setProviderConfigurationJSONs(
			_toJSON(
				HashMapBuilder.<String, Object>put(
					"model_id", "text-embedding-3-large"
				).build(),
				_ELASTICSEARCH_PROVIDER_NAME));

		Assert.assertNull(
			_inferenceIdResolverImpl.resolveInferenceId(_COMPANY_ID));
	}

	@Test
	public void testResolveInferenceIdReturnsNullWhenJSONsAreNull() {
		Mockito.when(
			_semanticSearchConfiguration.
				textEmbeddingProviderConfigurationJSONs()
		).thenReturn(
			null
		);

		Assert.assertNull(
			_inferenceIdResolverImpl.resolveInferenceId(_COMPANY_ID));
	}

	@Test
	public void testResolveInferenceIdReturnsNullWhenNoConfigurations() {
		_setProviderConfigurationJSONs();

		Assert.assertNull(
			_inferenceIdResolverImpl.resolveInferenceId(_COMPANY_ID));
	}

	@Test
	public void testResolveInferenceIdReturnsNullWhenOnlyLiferayProvided() {
		_setProviderConfigurationJSONs(_toJSON(null, "OpenAI"));

		Assert.assertNull(
			_inferenceIdResolverImpl.resolveInferenceId(_COMPANY_ID));
	}

	@Test
	public void testResolveInferenceIdReturnsNullWhenProviderNameCaseDiffers() {
		_setProviderConfigurationJSONs(
			_toJSON(
				HashMapBuilder.<String, Object>put(
					"service", "openai"
				).build(),
				"elasticsearch inference endpoint"));

		Assert.assertNull(
			_inferenceIdResolverImpl.resolveInferenceId(_COMPANY_ID));
	}

	@Test
	public void testResolveInferenceIdReturnsNullWhenServiceIsNotAString() {
		_setProviderConfigurationJSONs(
			_toJSON(
				HashMapBuilder.<String, Object>put(
					"service", 123
				).build(),
				_ELASTICSEARCH_PROVIDER_NAME));

		Assert.assertNull(
			_inferenceIdResolverImpl.resolveInferenceId(_COMPANY_ID));
	}

	@Test
	public void testResolveInferenceIdSkipsBlankAndMalformedJSONs() {
		_setProviderConfigurationJSONs(
			"", "not-valid-json", _elasticsearchProviderJSON("openai"));

		Assert.assertEquals(
			"liferay-42-inference-openai",
			_inferenceIdResolverImpl.resolveInferenceId(_COMPANY_ID));
	}

	@Test
	public void testResolveInferenceIdSkipsJSONWithNullProviderName() {
		EmbeddingProviderConfiguration embeddingProviderConfiguration =
			new EmbeddingProviderConfiguration();

		embeddingProviderConfiguration.setLanguageIds(new String[] {"en_US"});

		_setProviderConfigurationJSONs(
			embeddingProviderConfiguration.toString(),
			_elasticsearchProviderJSON("openai"));

		Assert.assertEquals(
			"liferay-42-inference-openai",
			_inferenceIdResolverImpl.resolveInferenceId(_COMPANY_ID));
	}

	private String _elasticsearchProviderJSON(String service) {
		return _toJSON(
			HashMapBuilder.<String, Object>put(
				"service", service
			).build(),
			_ELASTICSEARCH_PROVIDER_NAME);
	}

	private void _setProviderConfigurationJSONs(String... jsons) {
		Mockito.when(
			_semanticSearchConfiguration.
				textEmbeddingProviderConfigurationJSONs()
		).thenReturn(
			jsons
		);
	}

	private String _toJSON(Object attributes, String providerName) {
		EmbeddingProviderConfiguration embeddingProviderConfiguration =
			new EmbeddingProviderConfiguration();

		embeddingProviderConfiguration.setAttributes(attributes);
		embeddingProviderConfiguration.setProviderName(providerName);

		return embeddingProviderConfiguration.toString();
	}

	private static final long _COMPANY_ID = 42;

	private static final String _ELASTICSEARCH_PROVIDER_NAME =
		"Elasticsearch Inference Endpoint";

	private InferenceIdResolverImpl _inferenceIdResolverImpl;
	private final SemanticSearchConfiguration _semanticSearchConfiguration =
		Mockito.mock(SemanticSearchConfiguration.class);
	private final SemanticSearchConfigurationProvider
		_semanticSearchConfigurationProvider = Mockito.mock(
			SemanticSearchConfigurationProvider.class);

}