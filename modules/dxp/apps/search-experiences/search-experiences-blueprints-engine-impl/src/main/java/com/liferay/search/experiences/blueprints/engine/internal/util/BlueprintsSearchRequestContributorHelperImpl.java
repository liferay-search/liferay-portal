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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.exception.BlueprintsEngineException;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataCreator;
import com.liferay.search.experiences.blueprints.engine.util.BlueprintsSearchRequestContributorHelper;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.service.BlueprintService;

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

	public void combine(
			SearchRequestBuilder searchRequestBuilder, long blueprintId,
			BlueprintsAttributes blueprintsAttributes, Messages messages)
		throws BlueprintsEngineException, PortalException {

		Blueprint blueprint = _blueprintService.getBlueprint(blueprintId);

		ParameterData parameterData = _parameterDataCreator.create(
			blueprint, blueprintsAttributes, messages);

		if (!_blueprintsSearchRequestHelper.shouldApplyIndexerClauses(
				blueprint)) {

			searchRequestBuilder.emptySearchEnabled(
				true
			).withSearchContext(
				searchContext -> searchContext.setAttribute(
					"search.full.query.suppress.indexer.provided.clauses",
					Boolean.TRUE)
			);
		}

		searchRequestBuilder.modelIndexerClassNames(
			_blueprintsSearchRequestHelper.getModelIndexerClassNames(
				blueprint, blueprintsAttributes.getCompanyId()));

		_blueprintsSearchRequestHelper.setSource(
			searchRequestBuilder, parameterData, blueprint, messages);

		_blueprintsSearchRequestHelper.executeSearchRequestBodyContributors(
			searchRequestBuilder, parameterData, blueprint, messages);

		_blueprintsSearchRequestHelper.checkEngineErrors(
			blueprint.getBlueprintId(), messages);
	}

	@Reference
	private BlueprintService _blueprintService;

	@Reference
	private BlueprintsSearchRequestHelper _blueprintsSearchRequestHelper;

	@Reference
	private ParameterDataCreator _parameterDataCreator;

}