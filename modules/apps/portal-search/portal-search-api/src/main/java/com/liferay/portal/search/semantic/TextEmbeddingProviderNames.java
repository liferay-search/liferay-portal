/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.semantic;

/**
 * Holds the text embedding provider names that are cross-module contracts.
 * The Elasticsearch-provided (BYO-LLM) provider name is matched literally by
 * the inference endpoint name resolution and by the Semantic Search
 * configuration UI, so every consumer must reference the same constant.
 *
 * @author Rodrigo Guedes de Souza
 */
public class TextEmbeddingProviderNames {

	public static final String ELASTICSEARCH_INFERENCE_ENDPOINT =
		"elasticsearch-inference-endpoint";

}