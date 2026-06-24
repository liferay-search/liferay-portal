/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.semantic;

import java.util.Locale;
import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Adds the Elasticsearch-provided {@code semantic_text} fields to a company's
 * existing search index through an additive mapping update.
 *
 * <p>
 * The update only adds the new fields and never re-embeds existing documents —
 * backfilling is the reembedding queue's responsibility. The given asset types
 * and locales must match the ones used at index creation time so migrated
 * indexes carry the same semantic fields as freshly created ones. Null or
 * empty asset types or locales, an unavailable external embedding capability,
 * and an unresolvable inference endpoint name all make the operation a no-op;
 * a failed or unacknowledged mapping update aborts with a {@code
 * RuntimeException}.
 * </p>
 *
 * @author Rodrigo Guedes de Souza
 */
@ProviderType
public interface SemanticTextEmbeddingIndexMigrationHelper {

	public void enableSemanticTextOnExistingIndex(
		long companyId, Set<String> assetTypes, Set<Locale> locales);

}