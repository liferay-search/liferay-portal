/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.rest.text.embeddings.configuration;

import java.util.Map;

/**
 * Validates the service settings an admin enters for a BYO-LLM inference
 * service before Liferay issues the {@code PUT _inference} call, so an invalid
 * {@code model_id}, an out-of-range {@code dimensions}, or a missing required
 * field is caught with a clear per-field message instead of an unrecoverable
 * Elasticsearch error.
 *
 * <p>
 * Implementations are OSGi components, one per inference service, so adding a
 * provider does not require changing the form or the registry.
 * </p>
 *
 * @author Rodrigo Guedes de Souza
 */
public interface ProviderInputValidator {

	/**
	 * Returns the Elasticsearch inference service this validator handles
	 * (e.g., {@code openai}, {@code hugging_face}, {@code googlevertexai}).
	 */
	public String getService();

	/**
	 * Validates the given service settings, returning a map from field name to
	 * an error message for each invalid field. An empty map means the settings
	 * are valid.
	 */
	public Map<String, String> validate(Map<String, Object> serviceSettings);

}