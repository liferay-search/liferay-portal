/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.rest.internal.text.embeddings.configuration;

import java.util.Map;

/**
 * @author Rodrigo Guedes de Souza
 */
public interface ProviderInputValidatorRegistry {

	public Map<String, String> validate(String service, Object serviceSettings);

}