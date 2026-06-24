/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.semantic;

/**
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