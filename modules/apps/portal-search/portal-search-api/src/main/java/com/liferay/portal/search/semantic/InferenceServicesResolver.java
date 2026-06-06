/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.semantic;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Resolves the inference services that Elasticsearch supports for the {@code
 * text_embedding} task type via the {@code GET _inference/_services} API.
 *
 * @author Rodrigo Guedes de Souza
 */
@ProviderType
public interface InferenceServicesResolver {

	public List<InferenceService> resolveInferenceServices();

}