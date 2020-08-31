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

package com.liferay.portal.search.tuning.blueprints.ipstack.internal.dataprovider;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.engine.cache.JsonDataProviderCache;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.dataprovider.GeoLocationDataProvider;
import com.liferay.portal.search.tuning.blueprints.ipstack.internal.configuration.IPStackConfiguration;

import java.io.IOException;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	configurationPid = "com.liferay.portal.search.tuning.blueprints.ipstack.internal.configuration.IPStackConfiguration",
	immediate = true, property = "name=ipstack",
	service = GeoLocationDataProvider.class
)
public class IPStackDataProvider implements GeoLocationDataProvider {

	@Override
	public JSONObject getGeoLocationData(
		SearchParameterData searchParameterData, String ipAddress) {

		return getIpStackDataJsonObject(searchParameterData, ipAddress);
	}

	@Override
	public GeoLocationPoint getGeoLocationPoint(
		SearchParameterData searchParameterData, String ipAddress) {

		JSONObject jsonObject = getIpStackDataJsonObject(
			searchParameterData, ipAddress);

		if (jsonObject != null) {
			double latitude = jsonObject.getDouble("latitude");
			double longitude = jsonObject.getDouble("longitude");

			return new GeoLocationPoint(latitude, longitude);
		}

		return null;
	}

	protected JSONObject getIpStackDataJsonObject(
		SearchParameterData searchParameterData, String ipAddress) {

		try {
			return getIpStackDataJsonObject(ipAddress);
		}
		catch (CheckpointFailedException checkpointFailedException) {
			for (Message message : checkpointFailedException.getMessages()) {
				searchParameterData.addMessage(message);
			}

			return null;
		}
	}

	protected JSONObject getIpStackDataJsonObject(String ipAddress1) {
		if (!_ipStackConfiguration.isEnabled()) {
			return null;
		}

		String ipAddress2 = getIpAddress(ipAddress1);

		JSONObject ipStackDataJsonObject1 = _jsonDataProviderCache.get(
			ipAddress2);

		if (ipStackDataJsonObject1 != null) {
			return ipStackDataJsonObject1;
		}

		JSONObject ipStackDataJsonObject2 = _getIpStackData(ipAddress2);

		_jsonDataProviderCache.put(
			ipAddress2, ipStackDataJsonObject2,
			_ipStackConfiguration.cacheTimeout());

		if (_log.isDebugEnabled()) {
			_log.debug("IPStack data: " + ipStackDataJsonObject2);
		}

		return ipStackDataJsonObject2;
	}

	protected String getIpAddress(String ipAddress) {
		String testIPAddress = StringUtil.trim(
			_ipStackConfiguration.testIpAddress());

		if (!Validator.isBlank(testIPAddress)) {
			if (_log.isDebugEnabled()) {
				_log.debug("Using test IP address " + testIPAddress);
			}

			return testIPAddress;
		}

		return ipAddress;
	}

	public boolean isEnabled() {
		return _ipStackConfiguration.isEnabled();
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_ipStackConfiguration = ConfigurableUtil.createConfigurable(
			IPStackConfiguration.class, properties);
	}

	protected String getIpStackResponse(String ipAddress) throws IOException {
		if (ipAddress.equals("91.233.116.229")) {
			return getIpStackResponse("Helsinki", "60.1699", "24.9384");
		}

		if (ipAddress.equals("104.172.41.95")) {
			return getIpStackResponse("Brea", "33.9165", "-117.9003");
		}

		String rawData = _http.URLtoString(getURL(ipAddress));

		if (rawData == null) {
			throw new CheckpointFailedException(
				new Message(
					Severity.ERROR, "ipstack",
					"ipstack.error.empty-response"));
		}

		return rawData;
	}

	protected String getIpStackResponse(
		String city, String latitude, String longitude) {

		return StringBundler.concat(
			"{ \"city\": \"", city, "\", \"latitude\": \"", latitude,
			"\", \"longitude\": \"", longitude, "\" }");
	}

	protected String getURL(String ipAddress1) {
		List<Message> messages = new ArrayList<>();

		String apiKey = _checkApiKey(_ipStackConfiguration.apiKey(), messages);
		String apiURL = _checkApiURL(_ipStackConfiguration.apiURL(), messages);
		String ipAddress2 = _checkIpAddress(ipAddress1, messages);

		if (ListUtil.isNotEmpty(messages)) {
			throw new CheckpointFailedException(messages);
		}

		return _buildURL(apiKey, apiURL, ipAddress2);
	}

	private String _buildURL(String apiKey, String apiURL, String ipAddress) {
		return StringBundler.concat(
			apiURL, "/", ipAddress, "?access_key=", apiKey);
	}

	private String _checkApiKey(String apiKey, List<Message> messages) {
		if (Validator.isBlank(apiKey)) {
			messages.add(
				new Message(
					Severity.ERROR, "ipstack", "ipstack.error.api-key-empty"));

			return null;
		}

		return apiKey;
	}

	private String _checkApiURL(String apiURL, List<Message> messages) {
		if (Validator.isBlank(apiURL)) {
			messages.add(
				new Message(
					Severity.ERROR, "ipstack", "ipstack.error.api-url-empty"));

			return null;
		}

		return apiURL;
	}

	private String _checkIpAddress(
		String ipAddress, List<Message> messages) {

		ipAddress = StringUtil.trim(ipAddress);

		if (Validator.isBlank(ipAddress)) {
			messages.add(
				new Message(
					Severity.ERROR, "ipstack",
					"ipstack.error.empty-ip-address"));

			return null;
		}

		Inet4Address inet4Address;

		try {
			inet4Address = (Inet4Address)InetAddress.getByName(ipAddress);
		}
		catch (UnknownHostException unknownHostException) {
			messages.add(
				new Message(
					Severity.ERROR, "ipstack", "ipstack.error.invalid-ip",
					unknownHostException.getMessage(), unknownHostException,
					null, null, null));

			_log.error(unknownHostException.getMessage(), unknownHostException);

			return null;
		}
		catch (SecurityException securityException) {
			messages.add(
				new Message(
					Severity.ERROR, "ipstack",
					"ipstack.error.security-exception",
					securityException.getMessage(), securityException, null,
					null, null));

			_log.error(securityException.getMessage(), securityException);

			return null;
		}

		if (inet4Address.isSiteLocalAddress() ||
				inet4Address.isAnyLocalAddress() ||
				inet4Address.isLinkLocalAddress() ||
				inet4Address.isLoopbackAddress() ||
				inet4Address.isMulticastAddress()) {

			messages.add(
				new Message(
					Severity.ERROR, "ipstack",
					"ipstack.error.unable-to-provide-geolocation-data-for-private-ip-address",
					null, null, ipAddress));

			return null;
		}

		return ipAddress;
	}

	private JSONObject _getIpStackData(String ipAddress) {
		try {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
				getIpStackResponse(ipAddress));

			if ((jsonObject == null) || (jsonObject.get("latitude") == null) ||
				(jsonObject.get("longitude") == null)) {

				throw new CheckpointFailedException(
					new Message(
						Severity.ERROR, "ipstack",
						"ipstack.error.invalid-response-data", jsonObject, null,
						null));
			}

			return jsonObject;
		}
		catch (IOException ioException) {
			_log.error(ioException.getMessage(), ioException);

			throw new CheckpointFailedException(
				new Message(
					Severity.ERROR, "ipstack", "ipstack.error.network-error",
					ioException.getMessage(), ioException, null, null, null));
		}
		catch (JSONException jsonException) {
			_log.error(jsonException.getMessage(), jsonException);

			throw new CheckpointFailedException(
				new Message(
					Severity.ERROR, "ipstack",
					"openweathermap.error.invalid-ipstack-response-format",
					jsonException.getMessage(), jsonException, null, null,
					null));
		}
	}

	private static class CheckpointFailedException extends RuntimeException {

		private final List<Message> _messages;

		public List<Message> getMessages() {
			return _messages;
		}

		public CheckpointFailedException(List<Message> messages) {
			_messages = messages;
		}

		public CheckpointFailedException(Message... messages) {
			_messages = Arrays.asList(messages);
		}

	}

	private static final Log _log = LogFactoryUtil.getLog(
		IPStackDataProvider.class);

	@Reference
	private Http _http;

	private volatile IPStackConfiguration _ipStackConfiguration;

	@Reference
	private JsonDataProviderCache _jsonDataProviderCache;

}