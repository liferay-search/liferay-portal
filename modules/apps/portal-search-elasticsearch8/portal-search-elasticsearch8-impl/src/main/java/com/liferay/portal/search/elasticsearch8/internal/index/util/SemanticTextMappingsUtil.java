/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.index.util;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.search.semantic.SemanticFieldNames;
import com.liferay.portal.search.semantic.SemanticTextEmbeddingProviderType;

import java.util.Locale;
import java.util.Set;

/**
 * @author Rodrigo Guedes de Souza
 */
public class SemanticTextMappingsUtil {

	public static void putSemanticTextProperties(
		Set<String> assetTypes, String inferenceId, Set<Locale> locales,
		SemanticFieldNames semanticFieldNames,
		JSONObject propertiesJSONObject) {

		for (String assetType : assetTypes) {
			for (Locale locale : locales) {
				String fieldName = semanticFieldNames.fieldName(
					locale,
					SemanticTextEmbeddingProviderType.ELASTICSEARCH_PROVIDED,
					assetType, 0);

				propertiesJSONObject.put(
					fieldName,
					JSONUtil.put(
						"inference_id", inferenceId
					).put(
						"type", "semantic_text"
					));
			}
		}
	}

}