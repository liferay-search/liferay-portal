/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.semantic;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Resolves the metadata of an Elasticsearch inference endpoint via the {@code
 * GET _inference/<id>} API.
 *
 * @author Rodrigo Guedes de Souza
 */
@ProviderType
public interface InferenceEndpointMetadataResolver {

	public InferenceEndpointMetadata resolveInferenceEndpointMetadata(
		String inferenceId);

}