/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.index;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import co.elastic.clients.elasticsearch.indices.PutMappingResponse;
import co.elastic.clients.json.JsonpMapper;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.capabilities.ExternalEmbeddingCapabilityGate;
import com.liferay.portal.search.capabilities.ExternalEmbeddingEligibility;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.elasticsearch8.internal.index.util.SemanticTextMappingsUtil;
import com.liferay.portal.search.elasticsearch8.internal.util.JsonpUtil;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.semantic.InferenceIdResolver;
import com.liferay.portal.search.semantic.SemanticFieldNames;
import com.liferay.portal.search.semantic.SemanticTextEmbeddingIndexMigrationHelper;

import jakarta.json.spi.JsonProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import java.util.Locale;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodrigo Guedes de Souza
 */
@Component(service = SemanticTextEmbeddingIndexMigrationHelper.class)
public class ElasticsearchTextEmbeddingIndexMigrationHelper
	implements SemanticTextEmbeddingIndexMigrationHelper {

	@Override
	public void enableSemanticTextOnExistingIndex(
		long companyId, Set<String> assetTypes, Set<Locale> locales) {

		if ((assetTypes == null) || assetTypes.isEmpty() || (locales == null) ||
			locales.isEmpty()) {

			if (_log.isDebugEnabled()) {
				_log.debug(
					StringBundler.concat(
						"Skipping the semantic_text mappings update for ",
						"company ", companyId,
						" because no asset types or locales were given"));
			}

			return;
		}

		ExternalEmbeddingEligibility externalEmbeddingEligibility =
			_externalEmbeddingCapabilityGate.check();

		if (!externalEmbeddingEligibility.isAvailable()) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					StringBundler.concat(
						"Skipping the semantic_text mappings update for ",
						"company ", companyId, " because the external ",
						"embedding capability is unavailable"));
			}

			return;
		}

		String inferenceId = _inferenceIdResolver.resolveInferenceId(companyId);

		if (Validator.isBlank(inferenceId)) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Unable to add semantic_text mappings for company ",
						companyId,
						" because no inference endpoint name was resolved"));
			}

			return;
		}

		_putMapping(
			_indexNameBuilder.getIndexName(companyId),
			_buildSemanticTextMappings(assetTypes, inferenceId, locales));
	}

	private JSONObject _buildSemanticTextMappings(
		Set<String> assetTypes, String inferenceId, Set<Locale> locales) {

		JSONObject propertiesJSONObject = _jsonFactory.createJSONObject();

		SemanticTextMappingsUtil.putSemanticTextProperties(
			assetTypes, inferenceId, locales, _semanticFieldNames,
			propertiesJSONObject);

		return _jsonFactory.createJSONObject(
		).put(
			"properties", propertiesJSONObject
		);
	}

	private void _putMapping(String indexName, JSONObject mappingsJSONObject) {
		PutMappingResponse putMappingResponse = null;

		try {
			ElasticsearchClient elasticsearchClient =
				_elasticsearchConnectionManager.getElasticsearchClient();

			ElasticsearchIndicesClient elasticsearchIndicesClient =
				elasticsearchClient.indices();

			JsonpMapper jsonpMapper =
				_elasticsearchConnectionManager.getJsonpMapper(null);

			JsonProvider jsonProvider = jsonpMapper.jsonProvider();

			String mappings = String.valueOf(mappingsJSONObject);

			try (InputStream inputStream = new ByteArrayInputStream(
					mappings.getBytes(StandardCharsets.UTF_8))) {

				PutMappingRequest.Builder builder =
					new PutMappingRequest.Builder(
					).index(
						indexName
					).withJson(
						jsonProvider.createParser(inputStream), jsonpMapper
					);

				putMappingResponse = elasticsearchIndicesClient.putMapping(
					builder.build());
			}
		}
		catch (Exception exception) {
			throw new RuntimeException(
				"Unable to add semantic_text mappings to index " + indexName,
				exception);
		}

		if (!putMappingResponse.acknowledged()) {
			throw new RuntimeException(
				StringBundler.concat(
					"Elasticsearch did not acknowledge the semantic_text ",
					"mappings update for index ", indexName));
		}

		JsonpUtil.logInfoResponse(putMappingResponse, _log);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ElasticsearchTextEmbeddingIndexMigrationHelper.class);

	@Reference
	private ElasticsearchConnectionManager _elasticsearchConnectionManager;

	@Reference
	private ExternalEmbeddingCapabilityGate _externalEmbeddingCapabilityGate;

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private InferenceIdResolver _inferenceIdResolver;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private SemanticFieldNames _semanticFieldNames;

}