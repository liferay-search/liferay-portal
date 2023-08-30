/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch7.internal.index;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch7.internal.helper.SearchLogHelperUtil;
import com.liferay.portal.search.elasticsearch7.internal.index.constants.IndexSettingsConstants;
import com.liferay.portal.search.elasticsearch7.internal.index.constants.LiferayTypeMappingsConstants;
import com.liferay.portal.search.elasticsearch7.internal.settings.SettingsBuilder;
import com.liferay.portal.search.elasticsearch7.internal.util.ResourceUtil;
import com.liferay.portal.search.spi.index.configuration.contributor.helper.MappingsHelper;

import java.io.IOException;

import java.util.LinkedHashMap;
import java.util.Map;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.xcontent.XContentType;

/**
 * @author André de Oliveira
 */
public class LiferayDocumentTypeFactory implements MappingsHelper {

	public LiferayDocumentTypeFactory(
		String indexName, IndicesClient indicesClient,
		JSONFactory jsonFactory) {

		_indexName = indexName;
		_indicesClient = indicesClient;
		_jsonFactory = jsonFactory;
	}

	public void loadDefaultAnalyzers(SettingsBuilder settingsBuilder) {
		String defaultAnalyzers = ResourceUtil.getResourceAsString(
			getClass(),
			IndexSettingsConstants.INDEX_SETTINGS_ANALYSIS_FILE_NAME);

		settingsBuilder.loadFromSource(defaultAnalyzers);
	}

	@Override
	public void putMappings(String source) {
		PutMappingRequest putMappingRequest = new PutMappingRequest(_indexName);

		putMappingRequest.source(
			_getMappingsToPutWithMergedDynamicTemplates(
				_getCurrentMappings(_indexName), source),
			XContentType.JSON);

		try {
			ActionResponse actionResponse = _indicesClient.putMapping(
				putMappingRequest, RequestOptions.DEFAULT);

			SearchLogHelperUtil.logActionResponse(_log, actionResponse);
		}
		catch (Exception exception) {
			_log.error(
				"Unable to put mappings for index " + _indexName, exception);
		}
	}

	public void setMappings(CreateIndexRequest createIndexRequest) {
		setMappings(createIndexRequest, null);
	}

	public void setMappings(
		CreateIndexRequest createIndexRequest, String overrideMappings) {

		createIndexRequest.mapping(
			"_doc", _getMappings(overrideMappings), XContentType.JSON);
	}

	protected JSONObject createJSONObject(String mappings) {
		try {
			return _jsonFactory.createJSONObject(mappings);
		}
		catch (JSONException jsonException) {
			throw new RuntimeException(jsonException);
		}
	}

	private String _getCurrentMappings(String indexName) {
		GetMappingsRequest getMappingsRequest = new GetMappingsRequest();

		getMappingsRequest.indices(indexName);

		GetMappingsResponse getMappingsResponse = null;

		try {
			getMappingsResponse = _indicesClient.getMapping(
				getMappingsRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}

		Map<String, MappingMetadata> mappings = getMappingsResponse.mappings();

		MappingMetadata mappingMetadata = mappings.get(indexName);

		CompressedXContent compressedXContent = mappingMetadata.source();

		return compressedXContent.toString();
	}

	private String _getMappings(String overrideMappings) {
		if (Validator.isNotNull(overrideMappings)) {
			JSONObject jsonObject = _removeLegacyDocumentType(overrideMappings);

			return jsonObject.toString();
		}

		String defaultMappings = ResourceUtil.getResourceAsString(
			getClass(),
			LiferayTypeMappingsConstants.
				LIFERAY_DOCUMENT_TYPE_MAPPING_FILE_NAME);
		String defaultMappingTemplate = ResourceUtil.getResourceAsString(
			getClass(),
			LiferayTypeMappingsConstants.
				LIFERAY_DOCUMENT_TYPE_MAPPING_DEFAULT_TEMPLATE_FILE_NAME);

		return _getMappingsToPutWithMergedDynamicTemplates(
			defaultMappingTemplate, defaultMappings);
	}

	private String _getMappingsToPutWithMergedDynamicTemplates(
		String currentMappings, String mappingsToPut) {

		JSONObject currentMappingsJSONObject = _removeLegacyDocumentType(
			currentMappings);
		JSONObject mappingsToPutJSONObject = _removeLegacyDocumentType(
			mappingsToPut);

		mappingsToPutJSONObject.put(
			"dynamic_templates",
			_mergeDynamicTemplates(
				mappingsToPutJSONObject.getJSONArray("dynamic_templates"),
				currentMappingsJSONObject.getJSONArray("dynamic_templates")));

		return mappingsToPutJSONObject.toString();
	}

	private JSONArray _mergeDynamicTemplates(
		JSONArray dynamicTemplatesToPutJSONArray,
		JSONArray currentDynamicTemplatesJSONArray) {

		if (dynamicTemplatesToPutJSONArray == null) {
			return currentDynamicTemplatesJSONArray;
		}

		LinkedHashMap<String, JSONObject> linkedHashMap = new LinkedHashMap<>();

		_putAll(linkedHashMap, dynamicTemplatesToPutJSONArray);

		_putAll(linkedHashMap, currentDynamicTemplatesJSONArray);

		JSONArray mergedDynamicTemplatesJSONArray =
			_jsonFactory.createJSONArray();

		JSONObject defaultTemplateJSONObject = null;

		for (Map.Entry<String, JSONObject> entry : linkedHashMap.entrySet()) {
			String key = entry.getKey();

			if (key.equals("template_")) {
				defaultTemplateJSONObject = entry.getValue();
			}
			else {
				mergedDynamicTemplatesJSONArray.put(entry.getValue());
			}
		}

		if (defaultTemplateJSONObject != null) {
			mergedDynamicTemplatesJSONArray.put(defaultTemplateJSONObject);
		}

		return mergedDynamicTemplatesJSONArray;
	}

	private void _putAll(Map<String, JSONObject> map, JSONArray jsonArray) {
		if (jsonArray == null) {
			return;
		}

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);

			JSONArray namesJSONArray = jsonObject.names();

			String name = (String)namesJSONArray.get(0);

			map.put(name, jsonObject);
		}
	}

	private JSONObject _removeLegacyDocumentType(String source) {
		JSONObject jsonObject = createJSONObject(source);

		if (jsonObject.has(
				LiferayTypeMappingsConstants.LEGACY_LIFERAY_DOCUMENT_TYPE)) {

			return jsonObject.getJSONObject(
				LiferayTypeMappingsConstants.LEGACY_LIFERAY_DOCUMENT_TYPE);
		}

		return jsonObject;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LiferayDocumentTypeFactory.class);

	private final String _indexName;
	private final IndicesClient _indicesClient;
	private final JSONFactory _jsonFactory;

}