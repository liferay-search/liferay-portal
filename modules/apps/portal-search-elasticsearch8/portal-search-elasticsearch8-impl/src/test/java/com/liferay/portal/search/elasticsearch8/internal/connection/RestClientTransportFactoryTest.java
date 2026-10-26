/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.connection;

import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.BasicHttpContext;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Felipe Lorenz
 */
public class RestClientTransportFactoryTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testNewRestClientTransport() {
		HttpClientBuilder httpClientBuilder = Mockito.spy(
			HttpClientBuilder.class);

		_httpClientBuilderMockedStatic.when(
			HttpClientBuilder::create
		).thenReturn(
			httpClientBuilder
		);

		_restClientTransportFactory.newRestClientTransport();

		ArgumentCaptor<ConnectionKeepAliveStrategy> argumentCaptor =
			ArgumentCaptor.forClass(ConnectionKeepAliveStrategy.class);

		Mockito.verify(
			httpClientBuilder
		).setKeepAliveStrategy(
			argumentCaptor.capture()
		);

		ConnectionKeepAliveStrategy connectionKeepAliveStrategy =
			argumentCaptor.getValue();

		_assertGetKeepAliveDuration(connectionKeepAliveStrategy, 180000, -1);
		_assertGetKeepAliveDuration(connectionKeepAliveStrategy, 180000, 0);
		_assertGetKeepAliveDuration(connectionKeepAliveStrategy, 180000, 600);
		_assertGetKeepAliveDuration(connectionKeepAliveStrategy, 60000, 60);
	}

	private void _assertGetKeepAliveDuration(
		ConnectionKeepAliveStrategy connectionKeepAliveStrategy,
		long expectedKeepAliveDuration, long keepAliveTimeoutHeaderValue) {

		HttpResponse httpResponse = new BasicHttpResponse(
			new BasicStatusLine(
				HttpVersion.HTTP_1_1, HttpStatus.SC_OK,
				RandomTestUtil.randomString()));

		if (keepAliveTimeoutHeaderValue > -1) {
			httpResponse.setHeader(
				HttpHeaders.KEEP_ALIVE,
				"timeout=" + keepAliveTimeoutHeaderValue);
		}

		Assert.assertEquals(
			expectedKeepAliveDuration,
			connectionKeepAliveStrategy.getKeepAliveDuration(
				httpResponse, new BasicHttpContext(null)));
	}

	private static final MockedStatic<HttpClientBuilder>
		_httpClientBuilderMockedStatic = Mockito.mockStatic(
			HttpClientBuilder.class, Mockito.CALLS_REAL_METHODS);

	private final RestClientTransportFactory _restClientTransportFactory =
		new RestClientTransportFactory.Builder(
			new String[] {"http://" + RandomTestUtil.randomString()}
		).build();

}