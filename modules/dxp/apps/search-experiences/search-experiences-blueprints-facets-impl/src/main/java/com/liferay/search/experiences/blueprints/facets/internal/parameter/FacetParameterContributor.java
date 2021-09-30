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

package com.liferay.search.experiences.blueprints.facets.internal.parameter;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataBuilder;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDefinition;
import com.liferay.search.experiences.blueprints.engine.spi.parameter.ParameterContributor;
import com.liferay.search.experiences.blueprints.facets.constants.FacetsBlueprintKeys;
import com.liferay.search.experiences.blueprints.facets.internal.request.handler.FacetRequestHandlerFactory;
import com.liferay.search.experiences.blueprints.facets.internal.util.BlueprintJSONUtil;
import com.liferay.search.experiences.blueprints.facets.internal.util.FacetConfigurationUtil;
import com.liferay.search.experiences.blueprints.facets.spi.request.FacetRequestHandler;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemBuilders;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=facets",
	service = ParameterContributor.class
)
public class FacetParameterContributor implements ParameterContributor {

	@Override
	public void contribute(
		ParameterDataBuilder parameterDataBuilder, Blueprint blueprint,
		BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<JSONObject> optional =
			_blueprintHelper.getJSONObjectConfigurationOptional(
				blueprint,
				"JSONObject/" + FacetsBlueprintKeys.CONFIGURATION_SECTION);

		if (!optional.isPresent()) {
			return;
		}

		_processFacets(
			optional.get(), parameterDataBuilder, blueprintsAttributes,
			problemsHolderBuilder);
	}

	@Override
	public String getCategoryNameKey() {
		return "facets";
	}

	@Override
	public List<ParameterDefinition> getParameterDefinitions() {
		return new ArrayList<>();
	}

	private void _addFacetParameter(
		JSONObject jsonObject, ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<String> optional1 = BlueprintJSONUtil.getFirstKeyOptional(
			jsonObject);

		if (!optional1.isPresent()) {
			return;
		}

		String type = optional1.get();

		JSONObject typeJSONObject = jsonObject.getJSONObject(type);

		if (!FacetConfigurationUtil.isEnabled(typeJSONObject)) {
			return;
		}

		_addParameter(
			type, typeJSONObject, parameterDataBuilder, blueprintsAttributes,
			problemsHolderBuilder);
	}

	private void _addParameter(
		String type, JSONObject jsonObject,
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		try {
			FacetRequestHandler facetRequestHandler =
				_facetRequestHandlerFactory.getHandler(type);

			Optional<Parameter> optional =
				facetRequestHandler.getParameterOptional(
					blueprintsAttributes, problemsHolderBuilder, jsonObject);

			if (optional.isPresent()) {
				parameterDataBuilder.addParameter(optional.get());
			}
		}
		catch (IllegalArgumentException illegalArgumentException) {
			_log.error(illegalArgumentException);

			ProblemBuilder problemBuilder = _problemBuilders.error(
				getClass().getName(), "core.error.invalid-configuration-value",
				jsonObject, null, type, illegalArgumentException);

			problemsHolderBuilder.addProblem(problemBuilder.build());
		}
	}

	private void _processFacets(
		JSONObject jsonObject, ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Set<String> keySet = jsonObject.keySet();

		keySet.forEach(
			facetName -> _addFacetParameter(
				jsonObject.getJSONObject(facetName), parameterDataBuilder,
				blueprintsAttributes, problemsHolderBuilder));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FacetParameterContributor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference(target = "(type=internal)")
	private FacetRequestHandlerFactory _facetRequestHandlerFactory;

	@Reference
	private ProblemBuilders _problemBuilders;

}