/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.semantic;

import java.util.Locale;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Rodrigo Guedes
 */
@ProviderType
public interface SemanticFieldNames {

	public String fieldName(
		Locale locale,
		SemanticTextEmbeddingProviderType semanticTextEmbeddingProviderType,
		String assetType, int dimensions);

}