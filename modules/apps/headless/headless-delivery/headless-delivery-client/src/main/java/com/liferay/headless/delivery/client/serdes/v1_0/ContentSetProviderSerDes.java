/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.client.serdes.v1_0;

import com.liferay.headless.delivery.client.dto.v1_0.ContentSetProvider;
import com.liferay.headless.delivery.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class ContentSetProviderSerDes {

	public static ContentSetProvider toDTO(String json) {
		ContentSetProviderJSONParser contentSetProviderJSONParser =
			new ContentSetProviderJSONParser();

		return contentSetProviderJSONParser.parseToDTO(json);
	}

	public static ContentSetProvider[] toDTOs(String json) {
		ContentSetProviderJSONParser contentSetProviderJSONParser =
			new ContentSetProviderJSONParser();

		return contentSetProviderJSONParser.parseToDTOs(json);
	}

	public static String toJSON(ContentSetProvider contentSetProvider) {
		if (contentSetProvider == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (contentSetProvider.getItemSubtype() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"itemSubtype\": ");

			sb.append("\"");

			sb.append(_escape(contentSetProvider.getItemSubtype()));

			sb.append("\"");
		}

		if (contentSetProvider.getItemType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"itemType\": ");

			sb.append("\"");

			sb.append(_escape(contentSetProvider.getItemType()));

			sb.append("\"");
		}

		if (contentSetProvider.getKey() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"key\": ");

			sb.append("\"");

			sb.append(_escape(contentSetProvider.getKey()));

			sb.append("\"");
		}

		if (contentSetProvider.getTitle() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"title\": ");

			sb.append("\"");

			sb.append(_escape(contentSetProvider.getTitle()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		ContentSetProviderJSONParser contentSetProviderJSONParser =
			new ContentSetProviderJSONParser();

		return contentSetProviderJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		ContentSetProvider contentSetProvider) {

		if (contentSetProvider == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (contentSetProvider.getItemSubtype() == null) {
			map.put("itemSubtype", null);
		}
		else {
			map.put(
				"itemSubtype",
				String.valueOf(contentSetProvider.getItemSubtype()));
		}

		if (contentSetProvider.getItemType() == null) {
			map.put("itemType", null);
		}
		else {
			map.put(
				"itemType", String.valueOf(contentSetProvider.getItemType()));
		}

		if (contentSetProvider.getKey() == null) {
			map.put("key", null);
		}
		else {
			map.put("key", String.valueOf(contentSetProvider.getKey()));
		}

		if (contentSetProvider.getTitle() == null) {
			map.put("title", null);
		}
		else {
			map.put("title", String.valueOf(contentSetProvider.getTitle()));
		}

		return map;
	}

	public static class ContentSetProviderJSONParser
		extends BaseJSONParser<ContentSetProvider> {

		@Override
		protected ContentSetProvider createDTO() {
			return new ContentSetProvider();
		}

		@Override
		protected ContentSetProvider[] createDTOArray(int size) {
			return new ContentSetProvider[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "itemSubtype")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "itemType")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "key")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "title")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			ContentSetProvider contentSetProvider, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "itemSubtype")) {
				if (jsonParserFieldValue != null) {
					contentSetProvider.setItemSubtype(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "itemType")) {
				if (jsonParserFieldValue != null) {
					contentSetProvider.setItemType(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "key")) {
				if (jsonParserFieldValue != null) {
					contentSetProvider.setKey((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "title")) {
				if (jsonParserFieldValue != null) {
					contentSetProvider.setTitle((String)jsonParserFieldValue);
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
// LIFERAY-REST-BUILDER-HASH:91378154