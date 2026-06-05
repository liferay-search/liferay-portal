/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.index;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.SemanticTextProperty;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import co.elastic.clients.elasticsearch.indices.PutMappingResponse;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.search.capabilities.ExternalEmbeddingCapabilityGate;
import com.liferay.portal.search.capabilities.ExternalEmbeddingEligibility;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.semantic.InferenceIdResolver;
import com.liferay.portal.search.semantic.SemanticFieldNames;
import com.liferay.portal.search.semantic.SemanticTextEmbeddingProviderType;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * @author Rodrigo Guedes de Souza
 */
public class ElasticsearchTextEmbeddingIndexMigrationHelperTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_elasticsearchTextEmbeddingIndexMigrationHelper =
			new ElasticsearchTextEmbeddingIndexMigrationHelper();

		ReflectionTestUtil.setFieldValue(
			_elasticsearchTextEmbeddingIndexMigrationHelper,
			"_elasticsearchConnectionManager", _elasticsearchConnectionManager);
		ReflectionTestUtil.setFieldValue(
			_elasticsearchTextEmbeddingIndexMigrationHelper,
			"_externalEmbeddingCapabilityGate",
			_externalEmbeddingCapabilityGate);
		ReflectionTestUtil.setFieldValue(
			_elasticsearchTextEmbeddingIndexMigrationHelper,
			"_indexNameBuilder", _indexNameBuilder);
		ReflectionTestUtil.setFieldValue(
			_elasticsearchTextEmbeddingIndexMigrationHelper,
			"_inferenceIdResolver", _inferenceIdResolver);
		ReflectionTestUtil.setFieldValue(
			_elasticsearchTextEmbeddingIndexMigrationHelper, "_jsonFactory",
			_jsonFactory);
		ReflectionTestUtil.setFieldValue(
			_elasticsearchTextEmbeddingIndexMigrationHelper,
			"_semanticFieldNames", _semanticFieldNames());
	}

	@Test
	public void testEnableSemanticTextOnExistingIndex() throws Exception {
		_setUpElasticsearch();
		_setUpExternalEmbeddingCapabilityGate(true);
		_setUpInferenceIdResolver(_INFERENCE_ID);
		_setUpPutMapping(true);

		_enableSemanticTextOnExistingIndex();

		PutMappingRequest putMappingRequest = _capturePutMappingRequest();

		Assert.assertEquals(
			Collections.singletonList(_INDEX_NAME), putMappingRequest.index());

		Map<String, Property> properties = putMappingRequest.properties();

		Assert.assertEquals(properties.toString(), 1, properties.size());

		Property property = properties.get("journal_article_en_US_semantic");

		Assert.assertTrue(property.isSemanticText());

		SemanticTextProperty semanticTextProperty = property.semanticText();

		Assert.assertEquals(_INFERENCE_ID, semanticTextProperty.inferenceId());
	}

	@Test
	public void testEnableSemanticTextOnExistingIndexEmptyAssetTypes() {
		_elasticsearchTextEmbeddingIndexMigrationHelper.
			enableSemanticTextOnExistingIndex(
				_COMPANY_ID, Collections.emptySet(),
				SetUtil.fromArray(LocaleUtil.US));

		Mockito.verifyNoInteractions(
			_elasticsearchIndicesClient, _externalEmbeddingCapabilityGate);
	}

	@Test
	public void testEnableSemanticTextOnExistingIndexEmptyLocales() {
		_elasticsearchTextEmbeddingIndexMigrationHelper.
			enableSemanticTextOnExistingIndex(
				_COMPANY_ID, SetUtil.fromArray("journal_article"),
				Collections.emptySet());

		Mockito.verifyNoInteractions(
			_elasticsearchIndicesClient, _externalEmbeddingCapabilityGate);
	}

	@Test
	public void testEnableSemanticTextOnExistingIndexExternalEmbeddingCapabilityUnavailable() {
		_setUpExternalEmbeddingCapabilityGate(false);

		_enableSemanticTextOnExistingIndex();

		Mockito.verifyNoInteractions(
			_elasticsearchIndicesClient, _inferenceIdResolver);
	}

	@Test
	public void testEnableSemanticTextOnExistingIndexNoInferenceId() {
		_setUpExternalEmbeddingCapabilityGate(true);
		_setUpInferenceIdResolver(null);

		_enableSemanticTextOnExistingIndex();

		Mockito.verifyNoInteractions(_elasticsearchIndicesClient);
	}

	@Test
	public void testEnableSemanticTextOnExistingIndexNotAcknowledged()
		throws Exception {

		_setUpElasticsearch();
		_setUpExternalEmbeddingCapabilityGate(true);
		_setUpInferenceIdResolver(_INFERENCE_ID);
		_setUpPutMapping(false);

		try {
			_enableSemanticTextOnExistingIndex();

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				"Elasticsearch did not acknowledge the semantic_text " +
					"mappings update for index liferay-42",
				runtimeException.getMessage());
		}
	}

	@Test
	public void testEnableSemanticTextOnExistingIndexNullAssetTypes() {
		_elasticsearchTextEmbeddingIndexMigrationHelper.
			enableSemanticTextOnExistingIndex(
				_COMPANY_ID, null, SetUtil.fromArray(LocaleUtil.US));

		Mockito.verifyNoInteractions(
			_elasticsearchIndicesClient, _externalEmbeddingCapabilityGate);
	}

	@Test
	public void testEnableSemanticTextOnExistingIndexNullLocales() {
		_elasticsearchTextEmbeddingIndexMigrationHelper.
			enableSemanticTextOnExistingIndex(
				_COMPANY_ID, SetUtil.fromArray("journal_article"), null);

		Mockito.verifyNoInteractions(
			_elasticsearchIndicesClient, _externalEmbeddingCapabilityGate);
	}

	@Test
	public void testEnableSemanticTextOnExistingIndexWrapsClientException()
		throws Exception {

		_setUpElasticsearch();
		_setUpExternalEmbeddingCapabilityGate(true);
		_setUpInferenceIdResolver(_INFERENCE_ID);

		RuntimeException runtimeException1 = new RuntimeException("boom");

		Mockito.when(
			_elasticsearchIndicesClient.putMapping(
				Mockito.any(PutMappingRequest.class))
		).thenThrow(
			runtimeException1
		);

		try {
			_enableSemanticTextOnExistingIndex();

			Assert.fail();
		}
		catch (RuntimeException runtimeException2) {
			Assert.assertEquals(
				"Unable to add semantic_text mappings to index liferay-42",
				runtimeException2.getMessage());
			Assert.assertSame(runtimeException1, runtimeException2.getCause());
		}
	}

	private PutMappingRequest _capturePutMappingRequest() throws Exception {
		ArgumentCaptor<PutMappingRequest> argumentCaptor =
			ArgumentCaptor.forClass(PutMappingRequest.class);

		Mockito.verify(
			_elasticsearchIndicesClient
		).putMapping(
			argumentCaptor.capture()
		);

		return argumentCaptor.getValue();
	}

	private void _enableSemanticTextOnExistingIndex() {
		_elasticsearchTextEmbeddingIndexMigrationHelper.
			enableSemanticTextOnExistingIndex(
				_COMPANY_ID, SetUtil.fromArray("journal_article"),
				SetUtil.fromArray(LocaleUtil.US));
	}

	private SemanticFieldNames _semanticFieldNames() {
		SemanticFieldNames semanticFieldNames = Mockito.mock(
			SemanticFieldNames.class);

		Mockito.when(
			semanticFieldNames.fieldName(
				Mockito.any(Locale.class),
				Mockito.eq(
					SemanticTextEmbeddingProviderType.ELASTICSEARCH_PROVIDED),
				Mockito.anyString(), Mockito.anyInt())
		).thenAnswer(
			invocationOnMock -> StringBundler.concat(
				invocationOnMock.getArgument(2, String.class),
				StringPool.UNDERLINE,
				LocaleUtil.toLanguageId(
					invocationOnMock.getArgument(0, Locale.class)),
				"_semantic")
		);

		return semanticFieldNames;
	}

	private void _setUpElasticsearch() {
		Mockito.when(
			_elasticsearchClient.indices()
		).thenReturn(
			_elasticsearchIndicesClient
		);

		Mockito.when(
			_elasticsearchConnectionManager.getElasticsearchClient()
		).thenReturn(
			_elasticsearchClient
		);

		Mockito.when(
			_elasticsearchConnectionManager.getJsonpMapper(null)
		).thenReturn(
			_jsonpMapper
		);

		Mockito.when(
			_indexNameBuilder.getIndexName(_COMPANY_ID)
		).thenReturn(
			_INDEX_NAME
		);
	}

	private void _setUpExternalEmbeddingCapabilityGate(boolean available) {
		ExternalEmbeddingEligibility externalEmbeddingEligibility =
			ExternalEmbeddingEligibility.available();

		if (!available) {
			externalEmbeddingEligibility =
				ExternalEmbeddingEligibility.unavailable(
					"semantic-search.external-embedding-capability." +
						"unsupported-search-engine");
		}

		Mockito.when(
			_externalEmbeddingCapabilityGate.check()
		).thenReturn(
			externalEmbeddingEligibility
		);
	}

	private void _setUpInferenceIdResolver(String inferenceId) {
		Mockito.when(
			_inferenceIdResolver.resolveInferenceId(_COMPANY_ID)
		).thenReturn(
			inferenceId
		);
	}

	private void _setUpPutMapping(boolean acknowledged) throws Exception {
		PutMappingResponse putMappingResponse = Mockito.mock(
			PutMappingResponse.class);

		Mockito.when(
			putMappingResponse.acknowledged()
		).thenReturn(
			acknowledged
		);

		Mockito.when(
			_elasticsearchIndicesClient.putMapping(
				Mockito.any(PutMappingRequest.class))
		).thenReturn(
			putMappingResponse
		);
	}

	private static final long _COMPANY_ID = 42;

	private static final String _INDEX_NAME = "liferay-42";

	private static final String _INFERENCE_ID = "liferay-active-provider";

	private final ElasticsearchClient _elasticsearchClient = Mockito.mock(
		ElasticsearchClient.class);
	private final ElasticsearchConnectionManager
		_elasticsearchConnectionManager = Mockito.mock(
			ElasticsearchConnectionManager.class);
	private final ElasticsearchIndicesClient _elasticsearchIndicesClient =
		Mockito.mock(ElasticsearchIndicesClient.class);
	private ElasticsearchTextEmbeddingIndexMigrationHelper
		_elasticsearchTextEmbeddingIndexMigrationHelper;
	private final ExternalEmbeddingCapabilityGate
		_externalEmbeddingCapabilityGate = Mockito.mock(
			ExternalEmbeddingCapabilityGate.class);
	private final IndexNameBuilder _indexNameBuilder = Mockito.mock(
		IndexNameBuilder.class);
	private final InferenceIdResolver _inferenceIdResolver = Mockito.mock(
		InferenceIdResolver.class);
	private final JSONFactory _jsonFactory = new JSONFactoryImpl();
	private final JsonpMapper _jsonpMapper = new JacksonJsonpMapper();

}