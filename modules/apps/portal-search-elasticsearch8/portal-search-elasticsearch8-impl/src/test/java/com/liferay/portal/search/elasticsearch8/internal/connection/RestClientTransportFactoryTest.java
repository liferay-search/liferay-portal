/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.connection;

import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.BasicHttpContext;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Felipe Lorenz
 */
public class RestClientTransportFactoryTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testCreateConnectionKeepAliveStrategy() {
		_testCreateConnectionKeepAliveStrategy(180000, -1);
		_testCreateConnectionKeepAliveStrategy(180000, 600);
		_testCreateConnectionKeepAliveStrategy(60000, 60);
	}

	private void _testCreateConnectionKeepAliveStrategy(
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

		ConnectionKeepAliveStrategy connectionKeepAliveStrategy =
			ReflectionTestUtil.invoke(
				_restClientTransportFactory,
				"_createConnectionKeepAliveStrategy", new Class<?>[0]);

		Assert.assertEquals(
			expectedKeepAliveDuration,
			connectionKeepAliveStrategy.getKeepAliveDuration(
				httpResponse, new BasicHttpContext(null)));
	}

	private final RestClientTransportFactory _restClientTransportFactory =
		new RestClientTransportFactory.Builder(
			new String[] {"http://" + RandomTestUtil.randomString()}
		).build();

}