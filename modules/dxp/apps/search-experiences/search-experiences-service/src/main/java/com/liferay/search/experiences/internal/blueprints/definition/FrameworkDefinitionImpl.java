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

package com.liferay.search.experiences.internal.blueprints.definition;

import java.util.Optional;

/**
 * @author André de Oliveira
 */
public class FrameworkDefinitionImpl implements FrameworkDefinition {

	public FrameworkDefinitionImpl(
		FrameworkDefinitionDTO frameworkDefinitionDTO) {

		_frameworkDefinitionDTO = _getFrameworkDefinitionBean(
			frameworkDefinitionDTO);
	}

	@Override
	public Optional<Boolean> getApplyIndexerClausesOptional() {
		return Optional.ofNullable(_frameworkDefinitionDTO.applyIndexerClauses);
	}

	@Override
	public Optional<ClauseContributorsDefinition>
		getClauseContributorsDefinitionOptional() {

		return Optional.ofNullable(
			_frameworkDefinitionDTO.clauseContributorsDefinitionDTO
		).map(
			ClauseContributorsDefinitionImpl::new
		);
	}

	@Override
	public String[] getSearchableAssetTypes() {
		if (_frameworkDefinitionDTO.searchableAssetTypes != null) {
			return _frameworkDefinitionDTO.searchableAssetTypes;
		}

		return new String[0];
	}

	private FrameworkDefinitionDTO _getFrameworkDefinitionBean(
		FrameworkDefinitionDTO frameworkDefinitionDTO) {

		if (frameworkDefinitionDTO != null) {
			return frameworkDefinitionDTO;
		}

		return new FrameworkDefinitionDTO() {
			{
				applyIndexerClauses = true;
				searchableAssetTypes = new String[] {
					"com.liferay.journal.model.JournalArticle",
					"com.liferay.document.library.kernel.model.DLFileEntry"
				};
			}
		};
	}

	private final FrameworkDefinitionDTO _frameworkDefinitionDTO;

}