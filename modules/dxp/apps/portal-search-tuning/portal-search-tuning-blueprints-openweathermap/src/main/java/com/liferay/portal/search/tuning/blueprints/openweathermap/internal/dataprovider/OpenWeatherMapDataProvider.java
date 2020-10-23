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

package com.liferay.portal.search.tuning.blueprints.openweathermap.internal.dataprovider;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.engine.cache.JsonDataProviderCache;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;
import com.liferay.portal.search.tuning.blueprints.openweathermap.internal.configuration.OpenWeatherMapConfiguration;

import java.io.IOException;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * OpenWeatherMap data provider.
 *
 * See sample data at https://samples.openweathermap.org/data/2.5/weather?lat=35&lon=139&appid=b6907d289e10d714a6e88b30761fae22
 *
 * @author Petteri Karttunen
 */
@Component(
	configurationPid = "com.liferay.portal.search.tuning.blueprints.openweathermap.internal.configuration.OpenWeatherMapConfiguration",
	immediate = true, service = OpenWeatherMapDataProvider.class
)
public class OpenWeatherMapDataProvider {

	public JSONObject getWeatherDataJSONObject(
		Messages messages, GeoLocationPoint geoLocationPoint) {

		if (_openWeatherMapConfiguration.isEnabled()) {
			return null;
		}

		if (!_validateConfiguration(messages)) {
			return null;
		}

		String cacheKey = _getCacheKey(geoLocationPoint);

		JSONObject weatherDataJsonObject = _jsonDataProviderCache.get(cacheKey);

		if (weatherDataJsonObject != null) {
			return weatherDataJsonObject;
		}

		weatherDataJsonObject = _getWeatherDataJSONObject(
			geoLocationPoint, messages);

		if (!_validateData(weatherDataJsonObject, messages)) {
			return null;
		}

		_jsonDataProviderCache.put(
			cacheKey, weatherDataJsonObject,
			_openWeatherMapConfiguration.cacheTimeout());

		if (_log.isDebugEnabled()) {
			_log.debug("OpenWeatherMap data: " + weatherDataJsonObject);
		}

		return weatherDataJsonObject;
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

	private String _buildURL(
		String apiKey, String apiURL, GeoLocationPoint geoLocationPoint) {

		StringBundler sb = new StringBundler(7);

		sb.append(apiURL);
		sb.append("?lat=");
		sb.append(String.valueOf(geoLocationPoint.getLatitude()));
		sb.append("&lon=");
		sb.append(String.valueOf(geoLocationPoint.getLongitude()));
		sb.append("&units=metric&format=json&APPID=");
		sb.append(apiKey);

		return sb.toString();
	}

	private String _getCacheKey(GeoLocationPoint geoLocationPoint) {
		return String.valueOf(geoLocationPoint.getLatitude()) + "-" +
			String.valueOf(geoLocationPoint.getLongitude());
	}

	private JSONObject _getWeatherDataJSONObject(
		GeoLocationPoint geoLocationPoint, Messages messages) {

		String apiKey = _openWeatherMapConfiguration.apiKey();
		String apiURL = _openWeatherMapConfiguration.apiURL();

		String url = _buildURL(apiKey, apiURL, geoLocationPoint);

		JSONObject weatherDataJsonObject = null;

		try {
			String rawData = _http.URLtoString(url);

			if (rawData == null) {
				messages.addMessage(
					new Message(
						Severity.ERROR, "openweathermap",
						"openweathermap.error.esponse-empty",
						"OpenWeatherMap response empty"));

				return null;
			}

			weatherDataJsonObject = JSONFactoryUtil.createJSONObject(rawData);
		}
		catch (IOException ioException) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "openweathermap",
					"openweathermap.error.network-error",
					ioException.getMessage(), ioException, null, null, null));
			_log.error(ioException.getMessage(), ioException);
		}
		catch (JSONException jsonException) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "openweathermap",
					"openweathermap.error.invalid-ipstack-response-format",
					jsonException.getMessage(), jsonException, null, null,
					null));
			_log.error(jsonException.getMessage(), jsonException);
		}

		return weatherDataJsonObject;
	}

	private boolean _validateConfiguration(Messages messages) {
		String apiKey = _openWeatherMapConfiguration.apiKey();
		String apiURL = _openWeatherMapConfiguration.apiURL();

		boolean valid = true;

		if (Validator.isBlank(apiKey)) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "openweathermap",
					"openweathermap.error.api-key-empty",
					"OpenWeatherMap API key is not defined"));
			valid = false;
		}

		if (Validator.isBlank(apiURL)) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "openweathermap",
					"openweathermap.error.api-url-empty",
					"OpenWeatherMap API URL is not defined"));

			valid = false;
		}

		return valid;
	}

	private boolean _validateData(JSONObject jsonObject, Messages messages) {
		if ((jsonObject == null) || (jsonObject.get("weather") == null)) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "openweathermap",
					"openweathermap.error.invalid-response-data",
					"Invalid OpenWeatherMap response data", null, null, null,
					jsonObject.toString()));

			return false;
		}

		return true;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		OpenWeatherMapDataProvider.class);

	@Reference
	private Http _http;

	@Reference
	private JsonDataProviderCache _jsonDataProviderCache;

	private volatile OpenWeatherMapConfiguration _openWeatherMapConfiguration;

}