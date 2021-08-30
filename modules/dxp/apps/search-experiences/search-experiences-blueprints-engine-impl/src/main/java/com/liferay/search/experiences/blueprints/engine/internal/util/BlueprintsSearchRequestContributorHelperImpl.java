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
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.internal.searchrequest.BlueprintToSearchRequestTranslator;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataCreator;
import com.liferay.search.experiences.blueprints.engine.util.BlueprintsSearchRequestContributorHelper;
import com.liferay.search.experiences.blueprints.exception.NoSuchBlueprintException;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.service.BlueprintService;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilderFactory;

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

		Blueprint blueprint = _getBlueprint(blueprintId);

		if (blueprint == null) {
			return;
		}

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

	private Blueprint _getBlueprint(long blueprintId) {
		try {
			return _blueprintService.getBlueprint(blueprintId);
		}
		catch (NoSuchBlueprintException noSuchBlueprintException) {
			_log.error(noSuchBlueprintException);

			return null;
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintsSearchRequestContributorHelperImpl.class);

	@Reference
	private BlueprintService _blueprintService;

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