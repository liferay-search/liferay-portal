/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.search.elasticsearch7.internal.connection;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.SystemProperties;
import com.liferay.portal.kernel.util.Validator;

import java.util.stream.Stream;

/**
 * @author Adam Brandizzi
 */
public class ProxyConfig {

	public static ProxyConfigBuilder builder(Http http) {
		return new ProxyConfigBuilder(http);
	}

	public String getProxyHost() {
		return _proxyHost;
	}

	public String getProxyPassword() {
		return _proxyPassword;
	}

	public int getProxyPort() {
		return _proxyPort;
	}

	public String getProxyUserName() {
		return _proxyUserName;
	}

	public boolean shouldApplyProxyConfig() {
		return _shouldApplyProxyConfig;
	}

	public boolean shouldApplyProxyCredentials() {
		return _shouldApplyProxyCredentials;
	}

	public static class ProxyConfigBuilder {

		public ProxyConfigBuilder(Http http) {
			_http = http;
		}

		public ProxyConfig build() {
			_proxyConfig._proxyHost = getProxyHost();
			_proxyConfig._proxyPort = getProxyPort();
			_proxyConfig._shouldApplyProxyConfig = shouldApplyProxyConfig();
			_proxyConfig._shouldApplyProxyCredentials =
				shouldApplyProxyCredentials();

			return new ProxyConfig(_proxyConfig);
		}

		public ProxyConfigBuilder networkAddresses(
			String[] networkHostAddresses) {

			_networkHostAddresses = networkHostAddresses;

			return this;
		}

		public ProxyConfigBuilder proxyHost(String proxyHost) {
			_proxyConfig._proxyHost = proxyHost;

			return this;
		}

		public ProxyConfigBuilder proxyPassword(String proxyPassword) {
			_proxyConfig._proxyPassword = proxyPassword;

			return this;
		}

		public ProxyConfigBuilder proxyPort(int proxyPort) {
			_proxyConfig._proxyPort = proxyPort;

			return this;
		}

		public ProxyConfigBuilder proxyUserName(String proxyUserName) {
			_proxyConfig._proxyUserName = proxyUserName;

			return this;
		}

		protected String getProxyHost() {
			if (!Validator.isBlank(_proxyConfig._proxyHost)) {
				return _proxyConfig._proxyHost;
			}

			return SystemProperties.get("http.proxyHost");
		}

		protected int getProxyPort() {
			if (_proxyConfig._proxyPort > 0) {
				return _proxyConfig._proxyPort;
			}

			return GetterUtil.getInteger(
				SystemProperties.get("http.proxyPort"));
		}

		protected boolean shouldApplyProxyConfig() {
			if (!Validator.isBlank(_proxyConfig._proxyHost)) {
				return true;
			}

			if (!_http.hasProxyConfig()) {
				return false;
			}

			return Stream.of(
				_networkHostAddresses
			).allMatch(
				host -> !_http.isNonProxyHost(host)
			);
		}

		protected boolean shouldApplyProxyCredentials() {
			if (!Validator.isBlank(_proxyConfig._proxyHost) &&
				!Validator.isBlank(_proxyConfig._proxyPassword) &&
				(_proxyConfig._proxyPort > 0) &&
				!Validator.isBlank(_proxyConfig._proxyUserName)) {

				return true;
			}

			return false;
		}

		private final Http _http;
		private String[] _networkHostAddresses;
		private ProxyConfig _proxyConfig = new ProxyConfig();

	}

	private ProxyConfig() {
	}

	private ProxyConfig(ProxyConfig proxyConfig) {
		_shouldApplyProxyConfig = proxyConfig._shouldApplyProxyConfig;
		_shouldApplyProxyCredentials = proxyConfig._shouldApplyProxyCredentials;
		_proxyHost = proxyConfig._proxyHost;
		_proxyPassword = proxyConfig._proxyPassword;
		_proxyPort = proxyConfig._proxyPort;
		_proxyUserName = proxyConfig._proxyUserName;
	}

	private String _proxyHost;
	private String _proxyPassword;
	private int _proxyPort;
	private String _proxyUserName;
	private boolean _shouldApplyProxyConfig;
	private boolean _shouldApplyProxyCredentials;

}