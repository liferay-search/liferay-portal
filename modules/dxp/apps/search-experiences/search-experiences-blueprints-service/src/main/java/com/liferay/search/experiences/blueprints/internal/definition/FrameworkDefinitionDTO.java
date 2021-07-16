/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.blueprints.internal.definition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author André de Oliveira
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FrameworkDefinitionDTO {

	@JsonProperty("apply_indexer_clauses")
	protected boolean applyIndexerClauses;

	@JsonProperty("clause_contributors")
	protected ClauseContributorsDefinitionDTO clauseContributorsDefinitionDTO;

	@JsonProperty("searchable_asset_types")
	protected String[] searchableAssetTypes;

}