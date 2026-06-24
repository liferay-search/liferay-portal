/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.semantic;

import co.elastic.clients.elasticsearch.inference.InferenceEndpointInfo;
import co.elastic.clients.json.JsonData;

import com.liferay.portal.search.semantic.InferenceEndpointMetadata;
import com.liferay.portal.search.semantic.InferenceEndpointMetadataResolver;

import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodrigo Guedes de Souza
 */
@Component(service = InferenceEndpointMetadataResolver.class)
public class ElasticsearchInferenceEndpointMetadataResolver
	implements InferenceEndpointMetadataResolver {

	@Override
	public InferenceEndpointMetadata resolveInferenceEndpointMetadata(
		String inferenceId) {

		List<InferenceEndpointInfo> inferenceEndpointInfos =
			_inferenceEndpointInfoFetcher.fetchInferenceEndpointInfos(
				inferenceId);

		InferenceEndpointInfo inferenceEndpointInfo =
			inferenceEndpointInfos.get(0);

		JsonObject serviceSettingsJsonObject = _getServiceSettingsJsonObject(
			inferenceEndpointInfo.serviceSettings());

		return new InferenceEndpointMetadata(
			_getDimensions(serviceSettingsJsonObject),
			_getModelId(serviceSettingsJsonObject),
			inferenceEndpointInfo.service());
	}

	private int _getDimensions(JsonObject serviceSettingsJsonObject) {
		if ((serviceSettingsJsonObject == null) ||
			!serviceSettingsJsonObject.containsKey("dimensions")) {

			return 0;
		}

		JsonNumber jsonNumber = serviceSettingsJsonObject.getJsonNumber(
			"dimensions");

		return jsonNumber.intValue();
	}

	private String _getModelId(JsonObject serviceSettingsJsonObject) {
		if (serviceSettingsJsonObject == null) {
			return null;
		}

		return serviceSettingsJsonObject.getString("model_id", null);
	}

	private JsonObject _getServiceSettingsJsonObject(JsonData jsonData) {
		if (jsonData == null) {
			return null;
		}

		JsonValue jsonValue = jsonData.toJson();

		if (jsonValue.getValueType() != JsonValue.ValueType.OBJECT) {
			return null;
		}

		return jsonValue.asJsonObject();
	}

	@Reference
	private InferenceEndpointInfoFetcher _inferenceEndpointInfoFetcher;

}