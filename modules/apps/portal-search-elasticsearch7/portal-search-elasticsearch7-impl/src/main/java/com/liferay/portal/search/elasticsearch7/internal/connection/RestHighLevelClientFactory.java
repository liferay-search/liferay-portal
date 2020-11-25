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
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.SystemProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch7.internal.util.ClassLoaderUtil;

import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.security.KeyStore;

import java.util.stream.Stream;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * @author André de Oliveira
 */
public class RestHighLevelClientFactory {

	public static Builder builder() {
		return new Builder();
	}

	public RestHighLevelClient newRestHighLevelClient() {
		RestClientBuilder restClientBuilder = RestClient.builder(
			getHttpHosts()
		).setHttpClientConfigCallback(
			this::customizeHttpClient
		).setRequestConfigCallback(
			this::customizeRequestConfig
		);

		return ClassLoaderUtil.getWithContextClassLoader(
			() -> new RestHighLevelClient(restClientBuilder), getClass());
	}

	public static class Builder {

		public Builder authenticationEnabled(boolean authenticationEnabled) {
			_restHighLevelClientFactory._authenticationEnabled =
				authenticationEnabled;

			return this;
		}

		public RestHighLevelClientFactory build() {
			return new RestHighLevelClientFactory(_restHighLevelClientFactory);
		}

		public Builder httpSSLEnabled(boolean httpSSLEnabled) {
			_restHighLevelClientFactory._httpSSLEnabled = httpSSLEnabled;

			return this;
		}

		public Builder networkHostAddresses(String[] networkHostAddresses) {
			_restHighLevelClientFactory._networkHostAddresses =
				networkHostAddresses;

			return this;
		}

		public Builder password(String password) {
			_restHighLevelClientFactory._password = password;

			return this;
		}

		public Builder proxyHost(String proxyHost) {
			_restHighLevelClientFactory._proxyHost = proxyHost;

			return this;
		}

		public Builder proxyPassword(String proxyPassword) {
			_restHighLevelClientFactory._proxyPassword = proxyPassword;

			return this;
		}

		public Builder proxyPort(int proxyPort) {
			_restHighLevelClientFactory._proxyPort = proxyPort;

			return this;
		}

		public Builder proxyUserName(String proxyUserName) {
			_restHighLevelClientFactory._proxyUserName = proxyUserName;

			return this;
		}

		public Builder truststorePassword(String truststorePassword) {
			_restHighLevelClientFactory._truststorePassword =
				truststorePassword;

			return this;
		}

		public Builder truststorePath(String truststorePath) {
			_restHighLevelClientFactory._truststorePath = truststorePath;

			return this;
		}

		public Builder truststoreType(String truststoreType) {
			_restHighLevelClientFactory._truststoreType = truststoreType;

			return this;
		}

		public Builder userName(String userName) {
			_restHighLevelClientFactory._userName = userName;

			return this;
		}

		private final RestHighLevelClientFactory _restHighLevelClientFactory =
			new RestHighLevelClientFactory();

	}

	protected CredentialsProvider createCredentialsProvider() {
		CredentialsProvider credentialsProvider =
			new BasicCredentialsProvider();

		if (_applyProxyCredentials()) {
			credentialsProvider.setCredentials(
				new AuthScope(_proxyHost, _proxyPort),
				new UsernamePasswordCredentials(
					_proxyUserName, _proxyPassword));
		}

		credentialsProvider.setCredentials(
			AuthScope.ANY,
			new UsernamePasswordCredentials(_userName, _password));

		return credentialsProvider;
	}

	protected SSLContext createSSLContext() {
		try {
			Path path = Paths.get(_truststorePath);

			InputStream inputStream = Files.newInputStream(path);

			KeyStore keyStore = KeyStore.getInstance(_truststoreType);

			keyStore.load(inputStream, _truststorePassword.toCharArray());

			SSLContextBuilder sslContextBuilder = SSLContexts.custom();

			sslContextBuilder.loadKeyMaterial(
				keyStore, _truststorePassword.toCharArray());
			sslContextBuilder.loadTrustMaterial(keyStore, null);

			return sslContextBuilder.build();
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	protected HttpAsyncClientBuilder customizeHttpClient(
		HttpAsyncClientBuilder httpAsyncClientBuilder) {

		if (_authenticationEnabled) {
			httpAsyncClientBuilder.setDefaultCredentialsProvider(
				createCredentialsProvider());
		}

		if (_httpSSLEnabled) {
			httpAsyncClientBuilder.setSSLContext(createSSLContext());
		}

		if (_applyProxyConfig()) {
			httpAsyncClientBuilder.setProxy(
				new HttpHost(_getProxyHost(), _getProxyPort(), "http"));
		}

		return httpAsyncClientBuilder;
	}

	protected RequestConfig.Builder customizeRequestConfig(
		RequestConfig.Builder requestConfigBuilder) {

		return requestConfigBuilder.setSocketTimeout(120000);
	}

	protected HttpHost[] getHttpHosts() {
		return Stream.of(
			_networkHostAddresses
		).map(
			HttpHost::create
		).toArray(
			HttpHost[]::new
		);
	}

	private RestHighLevelClientFactory() {
	}

	private RestHighLevelClientFactory(
		RestHighLevelClientFactory restHighLevelClientFactory) {

		_authenticationEnabled =
			restHighLevelClientFactory._authenticationEnabled;
		_httpSSLEnabled = restHighLevelClientFactory._httpSSLEnabled;
		_networkHostAddresses =
			restHighLevelClientFactory._networkHostAddresses;
		_password = restHighLevelClientFactory._password;
		_truststorePassword = restHighLevelClientFactory._truststorePassword;
		_truststorePath = restHighLevelClientFactory._truststorePath;
		_truststoreType = restHighLevelClientFactory._truststoreType;
		_proxyHost = restHighLevelClientFactory._proxyHost;
		_proxyPassword = restHighLevelClientFactory._proxyPassword;
		_proxyPort = restHighLevelClientFactory._proxyPort;
		_proxyUserName = restHighLevelClientFactory._proxyUserName;
		_userName = restHighLevelClientFactory._userName;
	}

	private boolean _applyProxyConfig() {
		if (Validator.isNotNull(_proxyHost)) {
			return true;
		}

		if (!HttpUtil.hasProxyConfig()) {
			return false;
		}

		return Stream.of(
			_networkHostAddresses
		).allMatch(
			host -> !HttpUtil.isNonProxyHost(host)
		);
	}

	private boolean _applyProxyCredentials() {
		if (Validator.isNotNull(_proxyHost) &&
			Validator.isNotNull(_proxyPassword) && (_proxyPort > 0) &&
			Validator.isNotNull(_proxyUserName)) {

			return true;
		}

		return false;
	}

	private String _getProxyHost() {
		if (Validator.isNotNull(_proxyHost)) {
			return _proxyHost;
		}

		return SystemProperties.get("http.proxyHost");
	}

	private int _getProxyPort() {
		if (_proxyPort > 0) {
			return _proxyPort;
		}

		return GetterUtil.getInteger(SystemProperties.get("http.proxyPort"));
	}

	private boolean _authenticationEnabled;
	private boolean _httpSSLEnabled;
	private String[] _networkHostAddresses;
	private String _password;
	private String _proxyHost;
	private String _proxyPassword;
	private int _proxyPort;
	private String _proxyUserName;
	private String _truststorePassword;
	private String _truststorePath;
	private String _truststoreType;
	private String _userName;

}