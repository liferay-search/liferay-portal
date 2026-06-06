/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.semantic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.inference.ElasticsearchInferenceClient;
import co.elastic.clients.elasticsearch.inference.PutRequest;
import co.elastic.clients.elasticsearch.inference.TaskType;
import co.elastic.clients.json.JsonData;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.semantic.InferenceEndpointCreator;

import java.io.IOException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodrigo Guedes de Souza
 */
@Component(service = InferenceEndpointCreator.class)
public class ElasticsearchInferenceEndpointCreator
	implements InferenceEndpointCreator {

	@Override
	public void createInferenceEndpoint(
		String inferenceId, String service, String serviceSettingsJSON) {

		if (Validator.isBlank(inferenceId)) {
			throw new IllegalArgumentException("Inference ID is null or empty");
		}

		if (Validator.isBlank(service)) {
			throw new IllegalArgumentException("Service is null or empty");
		}

		JsonData serviceSettingsJsonData = _toJsonData(serviceSettingsJSON);

		try {
			ElasticsearchClient elasticsearchClient =
				_elasticsearchConnectionManager.getElasticsearchClient();

			ElasticsearchInferenceClient elasticsearchInferenceClient =
				elasticsearchClient.inference();

			elasticsearchInferenceClient.put(
				PutRequest.of(
					putRequest -> putRequest.inferenceConfig(
						inferenceEndpoint -> inferenceEndpoint.service(
							service
						).serviceSettings(
							serviceSettingsJsonData
						)
					).inferenceId(
						inferenceId
					).taskType(
						TaskType.TextEmbedding
					)));
		}
		catch (ElasticsearchException elasticsearchException) {
			throw new RuntimeException(
				StringBundler.concat(
					"Unable to create inference endpoint \"", inferenceId,
					"\": ", elasticsearchException.getMessage()),
				elasticsearchException);
		}
		catch (IOException ioException) {
			throw new RuntimeException(
				StringBundler.concat(
					"Unable to create inference endpoint \"", inferenceId,
					"\". Check the Elasticsearch connection and try again."),
				ioException);
		}
	}

	private JsonData _toJsonData(String serviceSettingsJSON) {
		if (Validator.isBlank(serviceSettingsJSON)) {
			return JsonData.fromJson("{}");
		}

		return JsonData.fromJson(serviceSettingsJSON);
	}

	@Reference
	private ElasticsearchConnectionManager _elasticsearchConnectionManager;

}