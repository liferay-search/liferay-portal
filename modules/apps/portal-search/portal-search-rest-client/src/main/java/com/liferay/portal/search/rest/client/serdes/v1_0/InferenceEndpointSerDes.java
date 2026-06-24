/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.rest.client.serdes.v1_0;

import com.liferay.portal.search.rest.client.dto.v1_0.InferenceEndpoint;
import com.liferay.portal.search.rest.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Petteri Karttunen
 * @generated
 */
@Generated("")
public class InferenceEndpointSerDes {

	public static InferenceEndpoint toDTO(String json) {
		InferenceEndpointJSONParser inferenceEndpointJSONParser =
			new InferenceEndpointJSONParser();

		return inferenceEndpointJSONParser.parseToDTO(json);
	}

	public static InferenceEndpoint[] toDTOs(String json) {
		InferenceEndpointJSONParser inferenceEndpointJSONParser =
			new InferenceEndpointJSONParser();

		return inferenceEndpointJSONParser.parseToDTOs(json);
	}

	public static String toJSON(InferenceEndpoint inferenceEndpoint) {
		if (inferenceEndpoint == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (inferenceEndpoint.getErrorMessage() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"errorMessage\": ");

			sb.append("\"");

			sb.append(_escape(inferenceEndpoint.getErrorMessage()));

			sb.append("\"");
		}

		if (inferenceEndpoint.getInferenceId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"inferenceId\": ");

			sb.append("\"");

			sb.append(_escape(inferenceEndpoint.getInferenceId()));

			sb.append("\"");
		}

		if (inferenceEndpoint.getService() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"service\": ");

			sb.append("\"");

			sb.append(_escape(inferenceEndpoint.getService()));

			sb.append("\"");
		}

		if (inferenceEndpoint.getServiceSettings() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"serviceSettings\": ");

			if (inferenceEndpoint.getServiceSettings() instanceof String) {
				sb.append("\"");
				sb.append((String)inferenceEndpoint.getServiceSettings());
				sb.append("\"");
			}
			else {
				sb.append(inferenceEndpoint.getServiceSettings());
			}
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		InferenceEndpointJSONParser inferenceEndpointJSONParser =
			new InferenceEndpointJSONParser();

		return inferenceEndpointJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		InferenceEndpoint inferenceEndpoint) {

		if (inferenceEndpoint == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (inferenceEndpoint.getErrorMessage() == null) {
			map.put("errorMessage", null);
		}
		else {
			map.put(
				"errorMessage",
				String.valueOf(inferenceEndpoint.getErrorMessage()));
		}

		if (inferenceEndpoint.getInferenceId() == null) {
			map.put("inferenceId", null);
		}
		else {
			map.put(
				"inferenceId",
				String.valueOf(inferenceEndpoint.getInferenceId()));
		}

		if (inferenceEndpoint.getService() == null) {
			map.put("service", null);
		}
		else {
			map.put("service", String.valueOf(inferenceEndpoint.getService()));
		}

		if (inferenceEndpoint.getServiceSettings() == null) {
			map.put("serviceSettings", null);
		}
		else {
			map.put(
				"serviceSettings",
				String.valueOf(inferenceEndpoint.getServiceSettings()));
		}

		return map;
	}

	public static class InferenceEndpointJSONParser
		extends BaseJSONParser<InferenceEndpoint> {

		@Override
		protected InferenceEndpoint createDTO() {
			return new InferenceEndpoint();
		}

		@Override
		protected InferenceEndpoint[] createDTOArray(int size) {
			return new InferenceEndpoint[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "errorMessage")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "inferenceId")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "service")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "serviceSettings")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			InferenceEndpoint inferenceEndpoint, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "errorMessage")) {
				if (jsonParserFieldValue != null) {
					inferenceEndpoint.setErrorMessage(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "inferenceId")) {
				if (jsonParserFieldValue != null) {
					inferenceEndpoint.setInferenceId(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "service")) {
				if (jsonParserFieldValue != null) {
					inferenceEndpoint.setService((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "serviceSettings")) {
				if (jsonParserFieldValue != null) {
					inferenceEndpoint.setServiceSettings(
						(Object)jsonParserFieldValue);
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			sb.append(_toJSON(value));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value == null) {
			return "null";
		}

		if (value instanceof Map) {
			return _toJSON((Map)value);
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder("[");

			Object[] values = (Object[])value;

			for (int i = 0; i < values.length; i++) {
				sb.append(_toJSON(values[i]));

				if ((i + 1) < values.length) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}

		if (value instanceof String) {
			return "\"" + _escape(value) + "\"";
		}

		return String.valueOf(value);
	}

}
// LIFERAY-REST-BUILDER-HASH:-1525587531