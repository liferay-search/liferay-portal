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

package com.liferay.portal.search.tuning.blueprints.facets.internal.handler.request;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.StringArrayParameter;
import com.liferay.portal.search.tuning.blueprints.engine.spi.facet.FacetRequestHandler;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

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

	@Override
	public Optional<Parameter> getParameter(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData,
		JSONObject configurationJsonObject) {

		if (!_validateConfiguration(
				searchParameterData, configurationJsonObject)) {

			Optional.empty();
		}

		JSONObject handlerParametersJsonObject =
			configurationJsonObject.getJSONObject(
				FacetConfigurationKeys.HANDLER_PARAMETERS.getJsonKey());

		String parameterName = configurationJsonObject.getString(
			FacetConfigurationKeys.PARAMETER_NAME.getJsonKey());

		boolean multiValue = configurationJsonObject.getBoolean(
			FacetConfigurationKeys.MULTI_VALUE.getJsonKey(), true);

		String[] valueArray;

		if (multiValue) {
			valueArray = ParamUtil.getStringValues(
				httpServletRequest, parameterName);
		}
		else {
			valueArray = new String[] {
				ParamUtil.getString(httpServletRequest, parameterName)
			};
		}

		if ((valueArray == null) || (valueArray.length == 0)) {
			return Optional.empty();
		}

		JSONArray valueAggregationsJsonArray =
			handlerParametersJsonObject.getJSONArray(
				FacetConfigurationKeys.VALUE_AGGREGATIONS.getJsonKey());

		List<String> values = new ArrayList<>();

		for (String requestValue : valueArray) {
			String[] translatedValueArray = null;

			for (int i = 0; i < valueAggregationsJsonArray.length(); i++) {
				try {
					JSONObject valueAggregationJson =
						valueAggregationsJsonArray.getJSONObject(i);

					if (valueAggregationJson.getString(
							FacetConfigurationKeys.VALUE_AGGREGATION_KEY.
								getJsonKey()
						).equals(
							requestValue
						)) {

						translatedValueArray = valueAggregationJson.getString(
							FacetConfigurationKeys.VALUE_AGGREGATION_VALUES.
								getJsonKey()
						).split(
							","
						);

						break;
					}
				}
				catch (Exception e) {
					_log.error(e.getMessage(), e);
				}
			}

			if (translatedValueArray != null) {
				Collections.addAll(values, translatedValueArray);
			}
			else {
				values.add(requestValue);
			}
		}

		if (values.isEmpty()) {
			return Optional.empty();
		}

		Stream<String> stream = values.stream();

		Parameter parameter = new StringArrayParameter(
			parameterName, null, stream.toArray(String[]::new));

		return Optional.of(parameter);
	}

	private boolean _validateConfiguration(
		SearchParameterData searchParameterData,
		JSONObject configurationJsonObject) {

		boolean valid = true;

		JSONObject handlerParametersJsonObject =
			configurationJsonObject.getJSONObject(
				FacetConfigurationKeys.HANDLER_PARAMETERS.getJsonKey());

		if (Validator.isNull(handlerParametersJsonObject)) {
			searchParameterData.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-facet-handler-parameters", null, null,
					configurationJsonObject,
					FacetConfigurationKeys.HANDLER_PARAMETERS.getJsonKey(),
					null));

			return false;
		}

		if (!handlerParametersJsonObject.has(
				FacetConfigurationKeys.VALUE_AGGREGATIONS.getJsonKey())) {

			searchParameterData.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-facet-handler-aggregations", null,
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