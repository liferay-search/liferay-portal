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
import com.liferay.search.experiences.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.search.experiences.blueprints.engine.internal.attributes.util.BlueprintsAttributesHelper;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataBuilder;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDefinition;
import com.liferay.search.experiences.blueprints.engine.parameter.StringArrayParameter;
import com.liferay.search.experiences.blueprints.engine.spi.parameter.ParameterContributor;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.model.Blueprint;

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
		BlueprintsAttributes blueprintsAttributes, Messages messages) {

		_setExcludedSearchRequestBodyContributors(
			parameterDataBuilder, blueprintsAttributes);
	}

	@Override
	public String getCategoryNameKey() {
		return "system";
	}

	@Override
	public List<ParameterDefinition> getParameterDefinitions() {
		return new ArrayList<>();
	}

	private void _setExcludedSearchRequestBodyContributors(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes) {

		Optional<String[]> excludedSearchRequestBodyContributorsOptional =
			_blueprintsAttributesHelper.getStringArrayOptional(
				blueprintsAttributes,
				ReservedParameterNames.
					EXCLUDED_SEARCH_REQUEST_BODY_CONTRIBUTORS.getKey());

		if (!excludedSearchRequestBodyContributorsOptional.isPresent()) {
			return;
		}

		parameterDataBuilder.addParameter(
			new StringArrayParameter(
				ReservedParameterNames.
					EXCLUDED_SEARCH_REQUEST_BODY_CONTRIBUTORS.getKey(),
				null,
				GetterUtil.getStringValues(
					excludedSearchRequestBodyContributorsOptional.get())));
	}

	@Reference
	private BlueprintsAttributesHelper _blueprintsAttributesHelper;

}