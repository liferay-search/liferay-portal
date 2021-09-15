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

package com.liferay.search.experiences.blueprints.engine.internal.searchrequest;

import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.definition.BlueprintDefinition;
import com.liferay.search.experiences.blueprints.definition.BlueprintDefinitionFactory;
import com.liferay.search.experiences.blueprints.definition.ClauseContributorsDefinition;
import com.liferay.search.experiences.blueprints.definition.FrameworkDefinition;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.constants.SearchContextAttributeKeys;
import com.liferay.search.experiences.blueprints.engine.internal.attributes.util.BlueprintsAttributeValuesHelper;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author André de Oliveira
 */
@Component(service = BlueprintToSearchRequestTranslator.class)
public class BlueprintToSearchRequestTranslator {

	public void translate(
		Blueprint blueprint, SearchRequestBuilder searchRequestBuilder,
		ParameterData parameterData,
		BlueprintsAttributes blueprintsAttributes) {

		applyBlueprintDefinition(
			blueprint, blueprintsAttributes, parameterData,
			searchRequestBuilder);
	}

	protected void applyApplyIndexerClauses(
		FrameworkDefinition frameworkDefinition, ParameterData parameterData,
		SearchRequestBuilder searchRequestBuilder) {

		Optional<Boolean> optional =
			frameworkDefinition.getApplyIndexerClausesOptional();

		optional.ifPresent(
			applyIndexerClauses -> {
				searchRequestBuilder.withSearchContext(
					searchContext -> searchContext.setAttribute(
						"search.full.query.suppress.indexer.provided.clauses",
						!applyIndexerClauses));

				if (applyIndexerClauses) {
					searchRequestBuilder.queryString(
						parameterData.getKeywords());
				}
				else {
					searchRequestBuilder.emptySearchEnabled(true);
				}
			});
	}

	protected void applyBlueprintDefinition(
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		ParameterData parameterData,
		SearchRequestBuilder searchRequestBuilder) {

		BlueprintDefinition blueprintDefinition =
			_blueprintDefinitionFactory.getBlueprintDefinition(blueprint);

		applyFrameworkDefinition(
			blueprintsAttributes, blueprintDefinition.getFrameworkDefinition(),
			parameterData, searchRequestBuilder);
	}

	protected void applyClauseContributorsDefinition(
		ClauseContributorsDefinition clauseContributorsDefinition,
		SearchRequestBuilder searchRequestBuilder) {

		searchRequestBuilder.withSearchContext(
			searchContext -> searchContext.setAttribute(
				"search.full.query.clause.contributors.excludes",
				StringUtil.merge(clauseContributorsDefinition.getExcludes()))
		).withSearchContext(
			searchContext -> searchContext.setAttribute(
				"search.full.query.clause.contributors.includes",
				StringUtil.merge(clauseContributorsDefinition.getIncludes()))
		);
	}

	protected void applyFederatedSearchKey(
		BlueprintsAttributes blueprintsAttributes,
		SearchRequestBuilder searchRequestBuilder) {

		Optional<String> federatedSearchKeyOptional =
			_blueprintsAttributeValuesHelper.getStringOptional(
				blueprintsAttributes,
				SearchContextAttributeKeys.FEDERATED_SEARCH_KEY);

		if (federatedSearchKeyOptional.isPresent()) {
			searchRequestBuilder.federatedSearchKey(
				federatedSearchKeyOptional.get());
		}
	}

	protected void applyFrameworkDefinition(
		BlueprintsAttributes blueprintsAttributes,
		FrameworkDefinition frameworkDefinition, ParameterData parameterData,
		SearchRequestBuilder searchRequestBuilder) {

		applyApplyIndexerClauses(
			frameworkDefinition, parameterData, searchRequestBuilder);

		Optional<ClauseContributorsDefinition> optional =
			frameworkDefinition.getClauseContributorsDefinitionOptional();

		optional.ifPresent(
			clauseContributorsDefinition -> applyClauseContributorsDefinition(
				clauseContributorsDefinition, searchRequestBuilder));

		searchRequestBuilder.modelIndexerClassNames(
			frameworkDefinition.getSearchableAssetTypes());

		applyFederatedSearchKey(blueprintsAttributes, searchRequestBuilder);
	}

	@Reference
	private BlueprintDefinitionFactory _blueprintDefinitionFactory;

	@Reference
	private BlueprintsAttributeValuesHelper _blueprintsAttributeValuesHelper;

}