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

package com.liferay.portal.search.tuning.blueprints.openweathermap.internal.parameter;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.DoubleParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.IntegerParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterDataBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterDefinition;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.StringParameter;
import com.liferay.portal.search.tuning.blueprints.engine.spi.dataprovider.GeoLocationDataProvider;
import com.liferay.portal.search.tuning.blueprints.engine.spi.parameter.ParameterContributor;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.openweathermap.internal.dataprovider.OpenWeatherMapDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=openweathermap",
	service = ParameterContributor.class
)
public class OpenWeatherMapParameterContributor
	implements ParameterContributor {

	@Override
	public void contribute(
		ParameterDataBuilder parameterDataBuilder, Blueprint blueprint,
		BlueprintsAttributes blueprintsAttributes, Messages messages) {

		String ipAddress = _getIpAddress(blueprintsAttributes);

		if (Validator.isBlank(ipAddress)) {
			messages.addMessage(
				new Message(
					Severity.INFO, "ipstack",
					"core.error.ip-parameter-not-found",
					"IP address wat not found in parameter data"));

			return;
		}

		_contribute(parameterDataBuilder, messages, ipAddress);
	}

	@Override
	public List<ParameterDefinition> getParameterDefinitions() {
		List<ParameterDefinition> parameterDefinitions = new ArrayList<>();

		parameterDefinitions.add(
			new ParameterDefinition(
				"${openweathermap.weather_id}",
				IntegerParameter.class.getName(),
				"parameter.openweathermap.weather-id"));

		parameterDefinitions.add(
			new ParameterDefinition(
				"${openweathermap.weather_name}",
				StringParameter.class.getName(),
				"parameter.openweathermap.weather-name"));

		parameterDefinitions.add(
			new ParameterDefinition(
				"${openweathermap.temperature}",
				DoubleParameter.class.getName(),
				"parameter.openweathermap.temperature"));

		return parameterDefinitions;
	}

	private void _contribute(
		ParameterDataBuilder parameterDataBuilder, Messages messages,
		String ipAddress) {

		GeoLocationPoint geoLocationPoint =
			_geoLocationDataProvider.getGeoLocationPoint(messages, ipAddress);

		if (geoLocationPoint == null) {
			return;
		}

		JSONObject weatherDataJSONObject =
			_openWeatherMapDataProvider.getWeatherDataJSONObject(
				messages, geoLocationPoint);

		if (weatherDataJSONObject == null) {
			return;
		}

		JSONArray weatherJsonArray = weatherDataJSONObject.getJSONArray(
			"weather");

		JSONObject weatherJsonObject = weatherJsonArray.getJSONObject(0);

		if (weatherJsonObject == null) {
			return;
		}

		parameterDataBuilder.addParameter(
			new IntegerParameter(
				"openweathermap.weather_id", "${openweathermap.weather_id}",
				weatherJsonObject.getInt("id")));

		parameterDataBuilder.addParameter(
			new StringParameter(
				"openweathermap.weather_name", "${openweathermap.weather_name}",
				weatherJsonObject.getString("main")));

		parameterDataBuilder.addParameter(
			new DoubleParameter(
				"openweathermap.temperature", "${openweathermap.temperature}",
				weatherJsonObject.getDouble("temp")));
	}

	private String _getIpAddress(BlueprintsAttributes blueprintsAttributes) {
		Optional<Object> valueOptional =
			blueprintsAttributes.getAttributeOptional(
				ReservedParameterNames.IP_ADDRESS.getKey());

		return GetterUtil.getString(valueOptional.orElse(null));
	}

	@Reference
	private GeoLocationDataProvider _geoLocationDataProvider;

	@Reference
	private OpenWeatherMapDataProvider _openWeatherMapDataProvider;

}