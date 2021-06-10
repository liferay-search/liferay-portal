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

package com.liferay.search.experiences.blueprints.facets.internal.request.handler;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.StringArrayParameter;
import com.liferay.search.experiences.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.search.experiences.blueprints.facets.internal.util.FacetConfigurationUtil;
import com.liferay.search.experiences.blueprints.facets.spi.request.FacetRequestHandler;
import com.liferay.search.experiences.blueprints.message.Messages;

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
	immediate = true, property = "name=term_map",
	service = FacetRequestHandler.class
)
public class TermMapFacetRequestHandler
	extends BaseFacetRequestHandler implements FacetRequestHandler {

	public Optional<Parameter> getParameterOptional(
		BlueprintsAttributes blueprintsAttributes, Messages messages,
		JSONObject jsonObject) {

		String parameterName = FacetConfigurationUtil.getParameterName(
			jsonObject);

		Optional<Object> valueOptional =
			blueprintsAttributes.getAttributeOptional(parameterName);

		if (!valueOptional.isPresent()) {
			return Optional.empty();
		}

		String[] valueArray;

		if (isMultiValue(jsonObject)) {
			valueArray = GetterUtil.getStringValues(valueOptional.get());
		}
		else {
			valueArray = new String[] {
				GetterUtil.getString(valueOptional.get())
			};
		}

		List<String> values = _getValues(valueArray, jsonObject);

		if (values.isEmpty()) {
			return Optional.empty();
		}

		Stream<String> stream = values.stream();

		Parameter parameter = new StringArrayParameter(
			parameterName, null, stream.toArray(String[]::new));

		return Optional.of(parameter);
	}

	private List<String> _getValues(
		String[] valueArray, JSONObject jsonObject) {

		List<String> values = new ArrayList<>();

		JSONObject parametersJSONObject = jsonObject.getJSONObject(
			FacetConfigurationKeys.PARAMETERS.getJsonKey());

		JSONArray mapJSONArray = parametersJSONObject.getJSONArray("map");

		for (String requestValue : valueArray) {
			JSONArray translatedValuesJSONArray = null;

			for (int i = 0; i < mapJSONArray.length(); i++) {
				JSONObject itemJSONObject = mapJSONArray.getJSONObject(i);

				if ((itemJSONObject == null) || !itemJSONObject.has("key") ||
					!itemJSONObject.has("values")) {

					continue;
				}

				if (requestValue.equals(itemJSONObject.getString("key"))) {
					translatedValuesJSONArray = itemJSONObject.getJSONArray(
						"values");

					break;
				}
			}

			if ((translatedValuesJSONArray != null) &&
				(translatedValuesJSONArray.length() > 0)) {

				Collections.addAll(
					values, JSONUtil.toStringArray(translatedValuesJSONArray));
			}
			else {
				values.add(requestValue);
			}
		}

		return values;
	}

}