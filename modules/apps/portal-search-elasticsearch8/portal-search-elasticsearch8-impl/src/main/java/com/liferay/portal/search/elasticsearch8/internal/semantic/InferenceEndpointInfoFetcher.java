/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.semantic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.inference.ElasticsearchInferenceClient;
import co.elastic.clients.elasticsearch.inference.GetInferenceRequest;
import co.elastic.clients.elasticsearch.inference.GetInferenceResponse;
import co.elastic.clients.elasticsearch.inference.InferenceEndpointInfo;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchConnectionManager;

import java.io.IOException;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodrigo Guedes de Souza
 */
@Component(service = InferenceEndpointInfoFetcher.class)
public class InferenceEndpointInfoFetcher {

	public List<InferenceEndpointInfo> fetchInferenceEndpointInfos(
		String inferenceId) {

		if (Validator.isBlank(inferenceId)) {
			throw new IllegalArgumentException("Inference ID is null or empty");
		}

		GetInferenceResponse getInferenceResponse = null;

		try {
			ElasticsearchClient elasticsearchClient =
				_elasticsearchConnectionManager.getElasticsearchClient();

			ElasticsearchInferenceClient elasticsearchInferenceClient =
				elasticsearchClient.inference();

			getInferenceResponse = elasticsearchInferenceClient.get(
				GetInferenceRequest.of(
					getInferenceRequest -> getInferenceRequest.inferenceId(
						inferenceId)));
		}
		catch (ElasticsearchException elasticsearchException) {
			if (elasticsearchException.status() == 404) {
				throw new RuntimeException(
					_getNotFoundMessage(inferenceId), elasticsearchException);
			}

			throw new RuntimeException(
				_getUnavailableMessage(inferenceId), elasticsearchException);
		}
		catch (IOException ioException) {
			throw new RuntimeException(
				_getUnavailableMessage(inferenceId), ioException);
		}

		List<InferenceEndpointInfo> inferenceEndpointInfos =
			getInferenceResponse.endpoints();

		if (inferenceEndpointInfos.isEmpty()) {
			throw new RuntimeException(_getNotFoundMessage(inferenceId));
		}

		return inferenceEndpointInfos;
	}

	private String _getNotFoundMessage(String inferenceId) {
		return StringBundler.concat(
			"Inference endpoint \"", inferenceId, "\" was not found in ",
			"Elasticsearch. Configure it in the Semantic Search admin UI ",
			"first.");
	}

	private String _getUnavailableMessage(String inferenceId) {
		return StringBundler.concat(
			"Unable to get inference endpoint \"", inferenceId, "\"");
	}

	@Reference
	private ElasticsearchConnectionManager _elasticsearchConnectionManager;

}