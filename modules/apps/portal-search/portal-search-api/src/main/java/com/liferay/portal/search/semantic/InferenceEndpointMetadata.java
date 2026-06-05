/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.semantic;

/**
 * Immutable metadata of an Elasticsearch inference endpoint, as returned by
 * the {@code GET _inference/<id>} API.
 *
 * <p>
 * {@link #getModelId()} returns {@code null} and {@link #getDimensions()}
 * returns {@code 0} when the endpoint's service settings do not declare the
 * corresponding value.
 * </p>
 *
 * @author Rodrigo Guedes de Souza
 */
public final class InferenceEndpointMetadata {

	public InferenceEndpointMetadata(
		int dimensions, String modelId, String service) {

		_dimensions = dimensions;
		_modelId = modelId;
		_service = service;
	}

	public int getDimensions() {
		return _dimensions;
	}

	public String getModelId() {
		return _modelId;
	}

	public String getService() {
		return _service;
	}

	private final int _dimensions;
	private final String _modelId;
	private final String _service;

}