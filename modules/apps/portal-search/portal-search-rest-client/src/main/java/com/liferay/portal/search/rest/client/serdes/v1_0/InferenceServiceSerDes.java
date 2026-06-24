/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.rest.client.serdes.v1_0;

import com.liferay.portal.search.rest.client.dto.v1_0.InferenceService;
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
public class InferenceServiceSerDes {

	public static InferenceService toDTO(String json) {
		InferenceServiceJSONParser inferenceServiceJSONParser =
			new InferenceServiceJSONParser();

		return inferenceServiceJSONParser.parseToDTO(json);
	}

	public static InferenceService[] toDTOs(String json) {
		InferenceServiceJSONParser inferenceServiceJSONParser =
			new InferenceServiceJSONParser();

		return inferenceServiceJSONParser.parseToDTOs(json);
	}

	public static String toJSON(InferenceService inferenceService) {
		if (inferenceService == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (inferenceService.getConfiguration() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"configuration\": ");

			if (inferenceService.getConfiguration() instanceof String) {
				sb.append("\"");
				sb.append((String)inferenceService.getConfiguration());
				sb.append("\"");
			}
			else {
				sb.append(inferenceService.getConfiguration());
			}
		}

		if (inferenceService.getService() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"service\": ");

			sb.append("\"");

			sb.append(_escape(inferenceService.getService()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		InferenceServiceJSONParser inferenceServiceJSONParser =
			new InferenceServiceJSONParser();

		return inferenceServiceJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(InferenceService inferenceService) {
		if (inferenceService == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (inferenceService.getConfiguration() == null) {
			map.put("configuration", null);
		}
		else {
			map.put(
				"configuration",
				String.valueOf(inferenceService.getConfiguration()));
		}

		if (inferenceService.getService() == null) {
			map.put("service", null);
		}
		else {
			map.put("service", String.valueOf(inferenceService.getService()));
		}

		return map;
	}

	public static class InferenceServiceJSONParser
		extends BaseJSONParser<InferenceService> {

		@Override
		protected InferenceService createDTO() {
			return new InferenceService();
		}

		@Override
		protected InferenceService[] createDTOArray(int size) {
			return new InferenceService[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "configuration")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "service")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			InferenceService inferenceService, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "configuration")) {
				if (jsonParserFieldValue != null) {
					inferenceService.setConfiguration(
						(Object)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "service")) {
				if (jsonParserFieldValue != null) {
					inferenceService.setService((String)jsonParserFieldValue);
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
// LIFERAY-REST-BUILDER-HASH:482934923