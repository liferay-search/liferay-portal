/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.semantic;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Locates the Liferay-managed Elasticsearch inference endpoint for a company,
 * used to enforce the single-active-endpoint constraint: at most one
 * Liferay-managed endpoint may exist per company at any time.
 *
 * @author Rodrigo Guedes de Souza
 */
@ProviderType
public interface InferenceEndpointLocator {

	/**
	 * Returns the name of the existing Liferay-managed inference endpoint
	 * whose name starts with the given prefix, or {@code null} when none
	 * exists.
	 */
	public String findInferenceId(String inferenceIdPrefix);

}