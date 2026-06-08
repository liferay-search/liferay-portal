/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.semantic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.inference.ElasticsearchInferenceClient;
import co.elastic.clients.elasticsearch.inference.GetInferenceResponse;
import co.elastic.clients.elasticsearch.inference.InferenceEndpointInfo;

import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.semantic.InferenceEndpointLocator;

import java.io.IOException;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodrigo Guedes de Souza
 */
@Component(service = InferenceEndpointLocator.class)
public class ElasticsearchInferenceEndpointLocator
	implements InferenceEndpointLocator {

	@Override
	public String findInferenceId(String inferenceIdPrefix) {
		if (Validator.isBlank(inferenceIdPrefix)) {
			throw new IllegalArgumentException(
				"Inference ID prefix is null or empty");
		}

		GetInferenceResponse getInferenceResponse = null;

		try {
			ElasticsearchClient elasticsearchClient =
				_elasticsearchConnectionManager.getElasticsearchClient();

			ElasticsearchInferenceClient elasticsearchInferenceClient =
				elasticsearchClient.inference();

			getInferenceResponse = elasticsearchInferenceClient.get();
		}
		catch (ElasticsearchException elasticsearchException) {
			throw new RuntimeException(
				"Unable to get the inference endpoints from Elasticsearch",
				elasticsearchException);
		}
		catch (IOException ioException) {
			throw new RuntimeException(
				"Unable to get the inference endpoints from Elasticsearch",
				ioException);
		}

		List<InferenceEndpointInfo> inferenceEndpointInfos =
			getInferenceResponse.endpoints();

		for (InferenceEndpointInfo inferenceEndpointInfo :
				inferenceEndpointInfos) {

			String inferenceId = inferenceEndpointInfo.inferenceId();

			if ((inferenceId != null) &&
				inferenceId.startsWith(inferenceIdPrefix)) {

				return inferenceId;
			}
		}

		return null;
	}

	@Reference
	private ElasticsearchConnectionManager _elasticsearchConnectionManager;

}