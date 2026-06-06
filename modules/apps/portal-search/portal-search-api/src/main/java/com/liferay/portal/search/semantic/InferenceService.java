/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.semantic;

/**
 * Immutable description of an inference service that Elasticsearch supports,
 * as returned by the {@code GET _inference/_services} API.
 *
 * <p>
 * {@link #getConfigurationJSON()} returns the raw JSON of the service's field
 * schema (one entry per configurable field, with its label, type, and
 * required/sensitive markers), or {@code null} when Elasticsearch does not
 * expose a schema for the service — consumers fall back to a JSON
 * passthrough in that case.
 * </p>
 *
 * @author Rodrigo Guedes de Souza
 */
public final class InferenceService {

	public InferenceService(String configurationJSON, String service) {
		_configurationJSON = configurationJSON;
		_service = service;
	}

	public String getConfigurationJSON() {
		return _configurationJSON;
	}

	public String getService() {
		return _service;
	}

	private final String _configurationJSON;
	private final String _service;

}