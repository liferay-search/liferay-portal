/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.semantic;

import co.elastic.clients.transport.rest_client.RestClientTransport;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchConnection;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.semantic.InferenceService;
import com.liferay.portal.search.semantic.InferenceServicesResolver;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EntityUtils;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Resolves the inference services that support the {@code text_embedding}
 * task type via the {@code GET _inference/_services/text_embedding} API. The
 * call goes through the low-level REST client because the bundled
 * Elasticsearch Java API client does not type this endpoint yet, and the raw
 * field schemas are passed through to the consumers anyway.
 *
 * @author Rodrigo Guedes de Souza
 */
@Component(service = InferenceServicesResolver.class)
public class ElasticsearchInferenceServicesResolver
	implements InferenceServicesResolver {

	@Override
	public List<InferenceService> resolveInferenceServices() {
		String responseJSON = null;

		try {
			ElasticsearchConnection elasticsearchConnection =
				_elasticsearchConnectionManager.getElasticsearchConnection();

			RestClientTransport restClientTransport =
				elasticsearchConnection.getRestClientTransport();

			RestClient restClient = restClientTransport.restClient();

			Response response = restClient.performRequest(
				new Request("GET", "/_inference/_services/text_embedding"));

			responseJSON = EntityUtils.toString(response.getEntity());
		}
		catch (Exception exception) {
			throw new RuntimeException(
				"Unable to get the inference services from Elasticsearch",
				exception);
		}

		return _toInferenceServices(responseJSON);
	}

	private List<InferenceService> _toInferenceServices(String responseJSON) {
		List<InferenceService> inferenceServices = new ArrayList<>();

		try {
			JSONArray jsonArray = _jsonFactory.createJSONArray(responseJSON);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);

				if (jsonObject == null) {
					continue;
				}

				String service = jsonObject.getString("service", null);

				if (Validator.isBlank(service)) {
					continue;
				}

				String configurationJSON = null;

				JSONObject configurationsJSONObject = jsonObject.getJSONObject(
					"configurations");

				if (configurationsJSONObject != null) {
					configurationJSON = configurationsJSONObject.toString();
				}

				inferenceServices.add(
					new InferenceService(configurationJSON, service));
			}
		}
		catch (JSONException jsonException) {
			throw new RuntimeException(
				"Unable to parse the inference services response",
				jsonException);
		}

		return inferenceServices;
	}

	@Reference
	private ElasticsearchConnectionManager _elasticsearchConnectionManager;

	@Reference
	private JSONFactory _jsonFactory;

}