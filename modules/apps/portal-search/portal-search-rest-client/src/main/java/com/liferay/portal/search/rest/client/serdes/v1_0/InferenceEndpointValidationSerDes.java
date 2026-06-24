/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.rest.client.serdes.v1_0;

import com.liferay.portal.search.rest.client.dto.v1_0.InferenceEndpointValidation;
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
public class InferenceEndpointValidationSerDes {

	public static InferenceEndpointValidation toDTO(String json) {
		InferenceEndpointValidationJSONParser
			inferenceEndpointValidationJSONParser =
				new InferenceEndpointValidationJSONParser();

		return inferenceEndpointValidationJSONParser.parseToDTO(json);
	}

	public static InferenceEndpointValidation[] toDTOs(String json) {
		InferenceEndpointValidationJSONParser
			inferenceEndpointValidationJSONParser =
				new InferenceEndpointValidationJSONParser();

		return inferenceEndpointValidationJSONParser.parseToDTOs(json);
	}

	public static String toJSON(
		InferenceEndpointValidation inferenceEndpointValidation) {

		if (inferenceEndpointValidation == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (inferenceEndpointValidation.getFieldErrors() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"fieldErrors\": ");

			if (inferenceEndpointValidation.getFieldErrors() instanceof
					String) {

				sb.append("\"");
				sb.append((String)inferenceEndpointValidation.getFieldErrors());
				sb.append("\"");
			}
			else {
				sb.append(inferenceEndpointValidation.getFieldErrors());
			}
		}

		if (inferenceEndpointValidation.getValid() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"valid\": ");

			sb.append(inferenceEndpointValidation.getValid());
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		InferenceEndpointValidationJSONParser
			inferenceEndpointValidationJSONParser =
				new InferenceEndpointValidationJSONParser();

		return inferenceEndpointValidationJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		InferenceEndpointValidation inferenceEndpointValidation) {

		if (inferenceEndpointValidation == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (inferenceEndpointValidation.getFieldErrors() == null) {
			map.put("fieldErrors", null);
		}
		else {
			map.put(
				"fieldErrors",
				String.valueOf(inferenceEndpointValidation.getFieldErrors()));
		}

		if (inferenceEndpointValidation.getValid() == null) {
			map.put("valid", null);
		}
		else {
			map.put(
				"valid",
				String.valueOf(inferenceEndpointValidation.getValid()));
		}

		return map;
	}

	public static class InferenceEndpointValidationJSONParser
		extends BaseJSONParser<InferenceEndpointValidation> {

		@Override
		protected InferenceEndpointValidation createDTO() {
			return new InferenceEndpointValidation();
		}

		@Override
		protected InferenceEndpointValidation[] createDTOArray(int size) {
			return new InferenceEndpointValidation[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "fieldErrors")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "valid")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			InferenceEndpointValidation inferenceEndpointValidation,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "fieldErrors")) {
				if (jsonParserFieldValue != null) {
					inferenceEndpointValidation.setFieldErrors(
						(Object)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "valid")) {
				if (jsonParserFieldValue != null) {
					inferenceEndpointValidation.setValid(
						(Boolean)jsonParserFieldValue);
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
// LIFERAY-REST-BUILDER-HASH:112322570