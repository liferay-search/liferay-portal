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

package com.liferay.search.experiences.blueprints.engine.internal.parameter.contributor;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.internal.attributes.util.BlueprintsAttributeValuesHelper;
import com.liferay.search.experiences.blueprints.engine.parameter.BooleanParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataBuilder;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDefinition;
import com.liferay.search.experiences.blueprints.engine.parameter.StringArrayParameter;
import com.liferay.search.experiences.blueprints.engine.spi.parameter.ParameterContributor;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=system",
	service = ParameterContributor.class
)
public class SystemParameterContributor implements ParameterContributor {

	@Override
	public void contribute(
		ParameterDataBuilder parameterDataBuilder, Blueprint blueprint,
		BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		_addExcludedSearchRequestBodyContributors(
			parameterDataBuilder, blueprintsAttributes);

		_addExplainParameter(parameterDataBuilder, blueprintsAttributes);

		_addIncludeResponseStringParameter(
			parameterDataBuilder, blueprintsAttributes);

		_addPreviewParameter(parameterDataBuilder, blueprintsAttributes);
	}

	@Override
	public String getCategoryNameKey() {
		return "system";
	}

	@Override
	public List<ParameterDefinition> getParameterDefinitions() {
		return new ArrayList<>();
	}

	private void _addExcludedSearchRequestBodyContributors(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes) {

		Optional<String[]> excludedSearchRequestBodyContributorsOptional =
			_blueprintsAttributeValuesHelper.getStringArrayOptional(
				blueprintsAttributes,
				"excluded_search_request_body_contributors");

		if (!excludedSearchRequestBodyContributorsOptional.isPresent()) {
			return;
		}

		parameterDataBuilder.addParameter(
			new StringArrayParameter(
				"excluded_search_request_body_contributors", null,
				GetterUtil.getStringValues(
					excludedSearchRequestBodyContributorsOptional.get())));
	}

	private void _addExplainParameter(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes) {

		Optional<Boolean> optional =
			_blueprintsAttributeValuesHelper.getBooleanOptional(
				blueprintsAttributes, "explain");

		if (optional.isPresent()) {
			parameterDataBuilder.addParameter(
				new BooleanParameter("explain", "${explain}", optional.get()));
		}
	}

	private void _addIncludeResponseStringParameter(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes) {

		Optional<Boolean> optional =
			_blueprintsAttributeValuesHelper.getBooleanOptional(
				blueprintsAttributes, "include_response_string");

		if (optional.isPresent()) {
			parameterDataBuilder.addParameter(
				new BooleanParameter(
					"include_response_string", "${include_response_string}",
					optional.get()));
		}
	}

	private void _addPreviewParameter(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes) {

		Optional<Boolean> optional =
			_blueprintsAttributeValuesHelper.getBooleanOptional(
				blueprintsAttributes, "preview");

		if (optional.isPresent()) {
			parameterDataBuilder.addParameter(
				new BooleanParameter("preview", null, optional.get()));
		}
	}

	@Reference
	private BlueprintsAttributeValuesHelper _blueprintsAttributeValuesHelper;

}