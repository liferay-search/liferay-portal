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

package com.liferay.search.experiences.blueprints.ipstack.internal.dataprovider;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.search.experiences.blueprints.engine.cache.JSONDataProviderCache;
import com.liferay.search.experiences.blueprints.engine.spi.dataprovider.GeoLocationDataProvider;
import com.liferay.search.experiences.blueprints.ipstack.internal.configuration.IPStackConfiguration;
import com.liferay.search.experiences.problems.ProblemBuilders;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.io.IOException;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	configurationPid = "com.liferay.search.experiences.blueprints.ipstack.internal.configuration.IPStackConfiguration",
	immediate = true, property = "name=ipstack",
	service = GeoLocationDataProvider.class
)
public class IPStackDataProvider implements GeoLocationDataProvider {

	@Override
	public Optional<JSONObject> getGeoLocationData(
		String ipAddress, ProblemsHolderBuilder problemsHolderBuilder) {

		return Optional.ofNullable(
			getIpStackDataJSONObject(ipAddress, problemsHolderBuilder));
	}

	@Override
	public Optional<GeoLocationPoint> getGeoLocationPoint(
		String ipAddress, ProblemsHolderBuilder problemsHolderBuilder) {

		JSONObject jsonObject = getIpStackDataJSONObject(
			ipAddress, problemsHolderBuilder);

		if (jsonObject != null) {
			double latitude = jsonObject.getDouble("latitude");
			double longitude = jsonObject.getDouble("longitude");

			return Optional.of(new GeoLocationPoint(latitude, longitude));
		}

		return Optional.empty();
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

	protected String getIpAddress(String ipAddress) {
		String testIPAddress = StringUtil.trim(
			_ipStackConfiguration.testIpAddress());

		if (!Validator.isBlank(testIPAddress)) {
			if (_log.isDebugEnabled()) {
				_log.debug("Using IPStack test IP address " + testIPAddress);
			}

			return testIPAddress;
		}

		return ipAddress;
	}

	protected JSONObject getIpStackDataJSONObject(
		String ipAddress1, ProblemsHolderBuilder problemsHolderBuilder) {

		String ipAddress2 = getIpAddress(ipAddress1);

		if (!_ipStackConfiguration.isEnabled() ||
			!_validateConfiguration(problemsHolderBuilder) ||
			!_validateIPAddress(ipAddress2, problemsHolderBuilder)) {

			return null;
		}

		JSONObject ipStackDataJsonObject1 = _getCachedIPStackDataJSONObject(
			ipAddress2);

		if (ipStackDataJsonObject1 != null) {
			return ipStackDataJsonObject1;
		}

		JSONObject ipStackDataJsonObject2 = _fetchIPStackDataJSONObject(
			ipAddress2, problemsHolderBuilder);

		if (!_validateJSONResponseData(
				ipStackDataJsonObject2, problemsHolderBuilder)) {

			return null;
		}

		_jsonDataProviderCache.put(
			ipAddress2, ipStackDataJsonObject2,
			_ipStackConfiguration.cacheTimeout());

		if (_log.isDebugEnabled()) {
			_log.debug("IPStack data: " + ipStackDataJsonObject2);
		}

		return ipStackDataJsonObject2;
	}

	@Reference(unbind = "=")
	protected void setMessagesHelper(ProblemBuilders problemBuilders) {
		_problemBuilders = problemBuilders;
	}

	private String _buildURL(String ipAddress) {
		StringBundler sb = new StringBundler(5);

		String apiURL = _ipStackConfiguration.apiURL();

		sb.append(apiURL);

		if (!apiURL.endsWith("/")) {
			sb.append("/");
		}

		sb.append(ipAddress);
		sb.append("?access_key=");
		sb.append(_ipStackConfiguration.apiKey());

		return sb.toString();
	}

	private JSONObject _createIPStackDataJSONObject(
		String rawData, ProblemsHolderBuilder problemsHolderBuilder) {

		if (Validator.isBlank(rawData)) {
			problemsHolderBuilder.addProblem(
				_problemBuilders.error(
					getClass().getName(), "ipstack.error.response-was-empty",
					null, null, null,
					new Throwable(
						"IPStack response was empty. Source URL [ " +
							_ipStackConfiguration.apiURL() + " ]")
				).build());

			return null;
		}

		try {
			return _jsonFactory.createJSONObject(rawData);
		}
		catch (JSONException jsonException) {
			problemsHolderBuilder.addProblem(
				_problemBuilders.error(
					getClass().getName(),
					"ipstack.error.response-format-was-invalid", null, null,
					null, jsonException
				).build());
		}

		return null;
	}

	private JSONObject _fetchIPStackDataJSONObject(
		String ipAddress, ProblemsHolderBuilder problemsHolderBuilder) {

		String url = _buildURL(ipAddress);

		try {
			return _createIPStackDataJSONObject(
				_http.URLtoString(url), problemsHolderBuilder);
		}
		catch (IOException ioException) {
			problemsHolderBuilder.addProblem(
				_problemBuilders.error(
					getClass().getName(), "ipstack.error.network-error", null,
					null, ipAddress, ioException
				).build());
		}

		return null;
	}

	private JSONObject _getCachedIPStackDataJSONObject(String ipAddress) {
		return _jsonDataProviderCache.getJSONObject(ipAddress);
	}

	private boolean _validateConfiguration(
		ProblemsHolderBuilder problemsHolderBuilder) {

		boolean valid = true;

		if (Validator.isBlank(_ipStackConfiguration.apiKey())) {
			problemsHolderBuilder.addProblem(
				_problemBuilders.error(
					getClass().getName(),
					"ipstack.error.api-key-must-be-configured", null, null,
					null, new Throwable("IPStack API key must be configured")
				).build());

			valid = false;
		}

		if (Validator.isBlank(_ipStackConfiguration.apiURL())) {
			problemsHolderBuilder.addProblem(
				_problemBuilders.error(
					getClass().getName(),
					"ipstack.error.api-url-must-be-configured", null, null,
					null, new Throwable("IPStack API URL must be configured")
				).build());

			valid = false;
		}

		return valid;
	}

	private boolean _validateIPAddress(
		String ipAddress, ProblemsHolderBuilder problemsHolderBuilder) {

		ipAddress = StringUtil.trim(ipAddress);

		if (Validator.isBlank(ipAddress)) {
			problemsHolderBuilder.addProblem(
				_problemBuilders.error(
					getClass().getName(), "ipstack.error.empty-ip-address",
					null, null, null, new Throwable("IP address empty")
				).build());

			return false;
		}

		Inet4Address address;

		try {
			address = (Inet4Address)InetAddress.getByName(ipAddress);
		}
		catch (UnknownHostException unknownHostException) {
			problemsHolderBuilder.addProblem(
				_problemBuilders.error(
					getClass().getName(), "ipstack.error.invalid-ip", null,
					null, ipAddress, unknownHostException
				).build());

			return false;
		}
		catch (SecurityException securityException) {
			problemsHolderBuilder.addProblem(
				_problemBuilders.error(
					getClass().getName(), "ipstack.error.security-exception",
					null, null, ipAddress, securityException
				).build());

			return false;
		}

		if (address.isSiteLocalAddress() || address.isAnyLocalAddress() ||
			address.isLinkLocalAddress() || address.isLoopbackAddress() ||
			address.isMulticastAddress()) {

			problemsHolderBuilder.addProblem(
				_problemBuilders.error(
					getClass().getName(),
					"ipstack.error.geolocation-data-unavailable-for-ip-address",
					null, null, ipAddress,
					new Throwable(
						"Geolocation data unavailable for IP address [ " +
							ipAddress + " ]")
				).build());

			return false;
		}

		return true;
	}

	private boolean _validateJSONResponseData(
		JSONObject jsonObject, ProblemsHolderBuilder problemsHolderBuilder) {

		if ((jsonObject == null) || !jsonObject.has("latitude") ||
			!jsonObject.has("longitude")) {

			problemsHolderBuilder.addProblem(
				_problemBuilders.error(
					getClass().getName(),
					"ipstack.error.response-data-was-invalid", jsonObject, null,
					null,
					new Throwable(
						"IPStack response data was invalid [ " + jsonObject +
							" ]")
				).build());

			return false;
		}

		return true;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		IPStackDataProvider.class);

	@Reference
	private Http _http;

	private volatile IPStackConfiguration _ipStackConfiguration;

	@Reference
	private JSONDataProviderCache _jsonDataProviderCache;

	@Reference
	private JSONFactory _jsonFactory;

	private ProblemBuilders _problemBuilders;

}