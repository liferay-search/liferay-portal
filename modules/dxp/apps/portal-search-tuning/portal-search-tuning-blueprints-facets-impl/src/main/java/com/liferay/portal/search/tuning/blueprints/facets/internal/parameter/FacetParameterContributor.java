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

package com.liferay.portal.search.tuning.blueprints.facets.internal.parameter;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterDataBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterDefinition;
import com.liferay.portal.search.tuning.blueprints.engine.spi.parameter.ParameterContributor;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetsBlueprintContributorKeys;
import com.liferay.portal.search.tuning.blueprints.facets.internal.request.handler.FacetRequestHandlerFactory;
import com.liferay.portal.search.tuning.blueprints.facets.spi.request.FacetRequestHandler;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
		BlueprintsAttributes blueprintsAttributes, Messages messages) {

		Optional<JSONArray> configurationJsonArrayOptional =
			_blueprintHelper.getJSONArrayConfigurationOptional(
				blueprint,
				"JSONArray/" +
					FacetsBlueprintContributorKeys.CONFIGURATION_SECTION);

		if (!configurationJsonArrayOptional.isPresent()) {
			return;
		}

		JSONArray configurationJsonArray = configurationJsonArrayOptional.get();

		for (int i = 0; i < configurationJsonArray.length(); i++) {
			JSONObject configurationJsonObject =
				configurationJsonArray.getJSONObject(i);

			boolean enabled = configurationJsonObject.getBoolean(
				FacetConfigurationKeys.ENABLED.getJsonKey(), true);

			if (!enabled) {
				continue;
			}

			if (_validateFacetConfiguration(
					messages, configurationJsonObject)) {

				_parseFacetParameter(
					parameterDataBuilder, blueprintsAttributes, messages,
					configurationJsonObject);
			}
		}
	}

	@Override
	public List<ParameterDefinition> getParameterDefinitions() {
		List<ParameterDefinition> parameterDefinitions = new ArrayList<>();

		return parameterDefinitions;
	}

	private void _parseFacetParameter(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes, Messages messages,
		JSONObject configurationJsonObject) {

		String handler = configurationJsonObject.getString(
			FacetConfigurationKeys.HANDLER.getJsonKey(), "default");

		try {
			FacetRequestHandler facetRequestHandler =
				_facetRequestHandlerFactory.getHandler(handler);

			Optional<Parameter> parameter =
				facetRequestHandler.getParameterOptional(
					blueprintsAttributes, messages, configurationJsonObject);

			if (parameter.isPresent()) {
				parameterDataBuilder.addParameter(parameter.get());
			}
		}
		catch (IllegalArgumentException illegalArgumentException) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.unknown-facet-handler",
					illegalArgumentException.getMessage(),
					illegalArgumentException, configurationJsonObject,
					FacetConfigurationKeys.HANDLER.getJsonKey(), handler));
			_log.error(
				illegalArgumentException.getMessage(),
				illegalArgumentException);
		}
	}

	private boolean _validateFacetConfiguration(
		Messages messages, JSONObject configurationJsonObject) {

		boolean valid = true;

		if (configurationJsonObject.isNull(
				FacetConfigurationKeys.PARAMETER_NAME.getJsonKey())) {

			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-parameter-name",
					"Facet parameter name is not defined", null,
					configurationJsonObject,
					FacetConfigurationKeys.PARAMETER_NAME.getJsonKey(), null));
			valid = false;
		}

		return valid;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FacetParameterContributor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private FacetRequestHandlerFactory _facetRequestHandlerFactory;

}