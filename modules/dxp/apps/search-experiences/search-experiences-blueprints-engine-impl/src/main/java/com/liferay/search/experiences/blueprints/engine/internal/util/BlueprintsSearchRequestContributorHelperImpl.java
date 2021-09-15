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

package com.liferay.search.experiences.blueprints.engine.internal.util;

import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.BlueprintLookup;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.internal.searchrequest.BlueprintToSearchRequestTranslator;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataCreator;
import com.liferay.search.experiences.blueprints.engine.util.BlueprintsSearchRequestContributorHelper;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilderFactory;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, service = BlueprintsSearchRequestContributorHelper.class
)
public class BlueprintsSearchRequestContributorHelperImpl
	implements BlueprintsSearchRequestContributorHelper {

	@Override
	public void combine(
		SearchRequestBuilder searchRequestBuilder, long blueprintId,
		BlueprintsAttributes blueprintsAttributes) {

		Optional<Blueprint> optional = _blueprintLookup.getBlueprintOptional(
			blueprintId);

		optional.ifPresent(
			blueprint -> _combine(
				searchRequestBuilder, blueprint, blueprintsAttributes));
	}

	private void _combine(
		SearchRequestBuilder searchRequestBuilder, Blueprint blueprint,
		BlueprintsAttributes blueprintsAttributes) {

		ProblemsHolderBuilder problemsHolderBuilder =
			_problemsHolderBuilderFactory.builder();

		ParameterData parameterData = _parameterDataCreator.create(
			blueprint, blueprintsAttributes, problemsHolderBuilder);

		_blueprintToSearchRequestTranslator.translate(
			blueprint, searchRequestBuilder, parameterData,
			blueprintsAttributes);

		_blueprintsSearchRequestHelper.setFieldRetrieval(
			searchRequestBuilder, parameterData, blueprint,
			problemsHolderBuilder);

		_blueprintsSearchRequestHelper.setPreview(
			searchRequestBuilder, parameterData, blueprint);

		_blueprintsSearchRequestHelper.executeSearchRequestBodyContributors(
			searchRequestBuilder, parameterData, blueprint,
			problemsHolderBuilder);

		_blueprintsSearchRequestHelper.checkEngineErrors(
			blueprint.getBlueprintId(), problemsHolderBuilder.build());
	}

	@Reference
	private BlueprintLookup _blueprintLookup;

	@Reference
	private BlueprintsSearchRequestHelper _blueprintsSearchRequestHelper;

	@Reference
	private BlueprintToSearchRequestTranslator
		_blueprintToSearchRequestTranslator;

	@Reference
	private ParameterDataCreator _parameterDataCreator;

	@Reference
	private ProblemsHolderBuilderFactory _problemsHolderBuilderFactory;

}