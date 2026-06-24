/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.semantic;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Resolves the Elasticsearch inference endpoint name used by the
 * Elasticsearch-provided text embedding strategy for the given company.
 *
 * @author Rodrigo Guedes de Souza
 */
@ProviderType
public interface InferenceIdResolver {

	public String resolveInferenceId(long companyId);

}