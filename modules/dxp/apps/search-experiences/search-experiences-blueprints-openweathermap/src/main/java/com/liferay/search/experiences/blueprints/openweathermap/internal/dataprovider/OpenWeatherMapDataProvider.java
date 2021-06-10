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

package com.liferay.search.experiences.blueprints.openweathermap.internal.dataprovider;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.search.experiences.blueprints.engine.cache.JSONDataProviderCache;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.openweathermap.internal.configuration.OpenWeatherMapConfiguration;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;

import java.io.IOException;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * OpenWeatherMap data provider
 *
 * See sample data at https://samples.openweathermap.org/data/2.5/weather?lat=35&lon=139&appid=b6907d289e10d714a6e88b30761fae22
 *
 * @author Petteri Karttunen
 */
@Component(
	configurationPid = "com.liferay.search.experiences.blueprints.openweathermap.internal.configuration.OpenWeatherMapConfiguration",
	immediate = true, service = OpenWeatherMapDataProvider.class
)
public class OpenWeatherMapDataProvider {

	public JSONObject getWeatherDataJSONObject(
		Messages messages, GeoLocationPoint geoLocationPoint) {

		if (!_openWeatherMapConfiguration.isEnabled() ||
			!_validateConfiguration(messages)) {

			return null;
		}

		String cacheKey = _getCacheKey(geoLocationPoint);

		JSONObject weatherDataJsonObject1 = _getCachedWeatherJSONObject(
			cacheKey);

		if (weatherDataJsonObject1 != null) {
			return weatherDataJsonObject1;
		}

		JSONObject weatherDataJsonObject2 = _fetchOpenWeatherMapDataJSONObject(
			geoLocationPoint, messages);

		if (!_validateJSONResponseData(weatherDataJsonObject2, messages)) {
			return null;
		}

		_jsonDataProviderCache.put(
			cacheKey, weatherDataJsonObject2,
			_openWeatherMapConfiguration.cacheTimeout());

		if (_log.isDebugEnabled()) {
			_log.debug("OpenWeatherMap data: " + weatherDataJsonObject2);
		}

		return weatherDataJsonObject2;
	}

	public boolean isEnabled() {
		return _openWeatherMapConfiguration.isEnabled();
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_openWeatherMapConfiguration = ConfigurableUtil.createConfigurable(
			OpenWeatherMapConfiguration.class, properties);
	}

	private String _buildURL(GeoLocationPoint geoLocationPoint) {
		StringBundler sb = new StringBundler(7);

		sb.append(_openWeatherMapConfiguration.apiURL());
		sb.append("?lat=");
		sb.append(String.valueOf(geoLocationPoint.getLatitude()));
		sb.append("&lon=");
		sb.append(String.valueOf(geoLocationPoint.getLongitude()));
		sb.append("&units=metric&format=json&APPID=");
		sb.append(_openWeatherMapConfiguration.apiKey());

		return sb.toString();
	}

	private JSONObject _createOpenWeatherMapDataJSONObject(
		String rawData, Messages messages) {

		if (Validator.isBlank(rawData)) {
			MessagesUtil.error(
				messages, getClass().getName(),
				new Throwable("OpenWeatherMap response was empty"), rawData,
				null, null, "openweathermap.error.response-was-empty");

			return null;
		}

		try {
			return _jsonFactory.createJSONObject(rawData);
		}
		catch (JSONException jsonException) {
			MessagesUtil.error(
				messages, getClass().getName(), jsonException, rawData, null,
				null, "openweathermap.error.invalid-response-format");
		}

		return null;
	}

	private JSONObject _fetchOpenWeatherMapDataJSONObject(
		GeoLocationPoint geoLocationPoint, Messages messages) {

		String url = _buildURL(geoLocationPoint);

		try {
			return _createOpenWeatherMapDataJSONObject(
				_http.URLtoString(url), messages);
		}
		catch (IOException ioException) {
			MessagesUtil.error(
				messages, getClass().getName(), ioException,
				geoLocationPoint.toString(), null, null,
				"openweathermap.error.invalid-response-format");
		}

		return null;
	}

	private JSONObject _getCachedWeatherJSONObject(String cacheKey) {
		return _jsonDataProviderCache.getJSONObject(cacheKey);
	}

	private String _getCacheKey(GeoLocationPoint geoLocationPoint) {
		return String.valueOf(geoLocationPoint.getLatitude()) + "-" +
			String.valueOf(geoLocationPoint.getLongitude());
	}

	private boolean _validateConfiguration(Messages messages) {
		boolean valid = true;

		if (Validator.isBlank(_openWeatherMapConfiguration.apiKey())) {
			MessagesUtil.error(
				messages, getClass().getName(),
				new Throwable("OpenWeatherMap API key must be configured"),
				null, null, null,
				"openweathermap.error.api-key-must-be-configured");

			valid = false;
		}

		if (Validator.isBlank(_openWeatherMapConfiguration.apiURL())) {
			MessagesUtil.error(
				messages, getClass().getName(),
				new Throwable("OpenWeatherMap API URL must be configured"),
				null, null, null,
				"openweathermap.error.api-url-must-be-configured");

			valid = false;
		}

		return valid;
	}

	private boolean _validateJSONResponseData(
		JSONObject jsonObject, Messages messages) {

		if ((jsonObject == null) || !jsonObject.has("weather")) {
			MessagesUtil.error(
				messages, getClass().getName(),
				new Throwable("OpenWeatherMap response data was invalid"),
				jsonObject, null, null,
				"openweathermap.error.response-data-was-invalid");

			return false;
		}

		return true;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		OpenWeatherMapDataProvider.class);

	@Reference
	private Http _http;

	@Reference
	private JSONDataProviderCache _jsonDataProviderCache;

	@Reference
	private JSONFactory _jsonFactory;

	private volatile OpenWeatherMapConfiguration _openWeatherMapConfiguration;

}