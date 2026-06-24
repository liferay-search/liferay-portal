/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.semantic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.inference.ElasticsearchInferenceClient;
import co.elastic.clients.elasticsearch.inference.TextEmbeddingInferenceResult;
import co.elastic.clients.elasticsearch.inference.TextEmbeddingRequest;
import co.elastic.clients.elasticsearch.inference.TextEmbeddingResponse;
import co.elastic.clients.elasticsearch.inference.TextEmbeddingResult;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.semantic.InferenceEndpointTester;

import java.io.IOException;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodrigo Guedes de Souza
 */
@Component(service = InferenceEndpointTester.class)
public class ElasticsearchInferenceEndpointTester
	implements InferenceEndpointTester {

	@Override
	public int test(String inferenceId, String input) {
		if (Validator.isBlank(inferenceId)) {
			throw new IllegalArgumentException("Inference ID is null or empty");
		}

		TextEmbeddingResponse textEmbeddingResponse = null;

		try {
			ElasticsearchClient elasticsearchClient =
				_elasticsearchConnectionManager.getElasticsearchClient();

			ElasticsearchInferenceClient elasticsearchInferenceClient =
				elasticsearchClient.inference();

			textEmbeddingResponse = elasticsearchInferenceClient.textEmbedding(
				TextEmbeddingRequest.of(
					textEmbeddingRequest -> textEmbeddingRequest.inferenceId(
						inferenceId
					).input(
						input
					)));
		}
		catch (ElasticsearchException elasticsearchException) {
			if (elasticsearchException.status() == 404) {
				throw new RuntimeException(
					StringBundler.concat(
						"Inference endpoint \"", inferenceId, "\" was not ",
						"found in Elasticsearch. Configure it in the Semantic ",
						"Search admin UI first."),
					elasticsearchException);
			}

			throw new RuntimeException(
				StringBundler.concat(
					"Unable to test inference endpoint \"", inferenceId, "\": ",
					elasticsearchException.getMessage()),
				elasticsearchException);
		}
		catch (IOException ioException) {
			throw new RuntimeException(
				StringBundler.concat(
					"Unable to test inference endpoint \"", inferenceId,
					"\". Check the Elasticsearch connection and try again."),
				ioException);
		}

		TextEmbeddingInferenceResult textEmbeddingInferenceResult =
			textEmbeddingResponse.valueBody();

		if (!textEmbeddingInferenceResult.isTextEmbedding()) {
			TextEmbeddingInferenceResult.Kind kind =
				textEmbeddingInferenceResult._kind();

			throw new RuntimeException(
				StringBundler.concat(
					"Inference endpoint \"", inferenceId, "\" returned an ",
					"unexpected result kind \"", kind.jsonValue(), "\""));
		}

		List<TextEmbeddingResult> textEmbeddingResults =
			textEmbeddingInferenceResult.textEmbedding();

		if (textEmbeddingResults.isEmpty()) {
			throw new RuntimeException(
				StringBundler.concat(
					"Inference endpoint \"", inferenceId,
					"\" returned no embeddings"));
		}

		TextEmbeddingResult textEmbeddingResult = textEmbeddingResults.get(0);

		List<Float> embedding = textEmbeddingResult.embedding();

		return embedding.size();
	}

	@Reference
	private ElasticsearchConnectionManager _elasticsearchConnectionManager;

}