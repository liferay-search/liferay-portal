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

package com.liferay.portal.search.tuning.blueprints.facets.internal.request.handler;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.StringArrayParameter;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.spi.request.FacetRequestHandler;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=file_extension",
	service = FacetRequestHandler.class
)
public class FileExtensionFacetRequestHandler
	extends BaseFacetRequestHandler implements FacetRequestHandler {

	public Optional<Parameter> getParameterOptional(
		BlueprintsAttributes blueprintsAttributes, Messages messages,
		JSONObject configurationJsonObject) {

		if (!_validateConfiguration(configurationJsonObject, messages)) {
			Optional.empty();
		}

		String parameterName = configurationJsonObject.getString(
			FacetConfigurationKeys.PARAMETER_NAME.getJsonKey());

		Optional<Object> valueOptional =
			blueprintsAttributes.getAttributeOptional(parameterName);

		if (!valueOptional.isPresent()) {
			return Optional.empty();
		}

		String[] valueArray;

		boolean multiValue = configurationJsonObject.getBoolean(
			FacetConfigurationKeys.MULTI_VALUE.getJsonKey(), true);

		if (multiValue) {
			valueArray = GetterUtil.getStringValues(valueOptional.get());
		}
		else {
			valueArray = new String[] {
				GetterUtil.getString(valueOptional.get())
			};
		}

		List<String> values = _getValues(valueArray, configurationJsonObject);

		if (values.isEmpty()) {
			return Optional.empty();
		}

		Stream<String> stream = values.stream();

		Parameter parameter = new StringArrayParameter(
			parameterName, null, stream.toArray(String[]::new));

		return Optional.of(parameter);
	}

	private List<String> _getValues(
		String[] valueArray, JSONObject configurationJsonObject) {

		List<String> values = new ArrayList<>();

		JSONObject handlerParametersJsonObject =
			configurationJsonObject.getJSONObject(
				FacetConfigurationKeys.HANDLER_PARAMETERS.getJsonKey());

		JSONArray valueAggregationsJsonArray =
			handlerParametersJsonObject.getJSONArray(
				FacetConfigurationKeys.VALUE_AGGREGATIONS.getJsonKey());

		for (String requestValue : valueArray) {
			String[] translatedValueArray = null;

			for (int i = 0; i < valueAggregationsJsonArray.length(); i++) {
				try {
					JSONObject jsonObject =
						valueAggregationsJsonArray.getJSONObject(i);

					if (jsonObject.getString(
							FacetConfigurationKeys.VALUE_AGGREGATION_KEY.
								getJsonKey()
						).equals(
							requestValue
						)) {

						translatedValueArray = jsonObject.getString(
							FacetConfigurationKeys.VALUE_AGGREGATION_VALUES.
								getJsonKey()
						).split(
							","
						);

						break;
					}
				}
				catch (Exception exception) {
					_log.error(exception.getMessage(), exception);
				}
			}

			if (translatedValueArray != null) {
				Collections.addAll(values, translatedValueArray);
			}
			else {
				values.add(requestValue);
			}
		}

		return values;
	}

	private boolean _validateConfiguration(
		JSONObject configurationJsonObject, Messages messages) {

		boolean valid = true;

		JSONObject handlerParametersJsonObject =
			configurationJsonObject.getJSONObject(
				FacetConfigurationKeys.HANDLER_PARAMETERS.getJsonKey());

		if (handlerParametersJsonObject == null) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-facet-handler-parameters",
					"Facet handler parameters are not defined", null,
					configurationJsonObject,
					FacetConfigurationKeys.HANDLER_PARAMETERS.getJsonKey(),
					null));

			valid = false;
		}

		if ((handlerParametersJsonObject == null) ||
			!handlerParametersJsonObject.has(
				FacetConfigurationKeys.VALUE_AGGREGATIONS.getJsonKey())) {

			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-facet-handler-aggregations",
					"File extension facet handler's value mappings are not " +
						"defined",
					null, configurationJsonObject,
					FacetConfigurationKeys.VALUE_AGGREGATIONS.getJsonKey(),
					null));

			valid = false;
		}

		return valid;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FileExtensionFacetRequestHandler.class);

}