/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.connection;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.search.opensearch2.configuration.OpenSearchConnectionConfiguration;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Olivia Yu
 */
public class OpenSearchConnectionsHolderImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@AfterClass
	public static void tearDownClass() {
		_configurableUtilMockedStatic.close();
	}

	@Before
	public void setUp() {
		_openSearchConnectionsHolderImpl.http = _http;

		ReflectionTestUtil.setFieldValue(
			_openSearchConnectionsHolderImpl, "_secretResolver",
			_secretResolver);

		_configurableUtilMockedStatic.when(
			() -> ConfigurableUtil.createConfigurable(
				Mockito.eq(OpenSearchConnectionConfiguration.class),
				Mockito.anyMap())
		).thenReturn(
			_openSearchConnectionConfiguration
		);
	}

	@Test
	public void testActivate() {
		String connectionId = RandomTestUtil.randomString();

		Mockito.when(
			_openSearchConnectionConfiguration.connectionId()
		).thenReturn(
			connectionId
		);

		Mockito.when(
			_openSearchConnectionConfiguration.networkHostAddresses()
		).thenReturn(
			RandomTestUtil.randomStrings(10)
		);

		String password = RandomTestUtil.randomString();

		Mockito.when(
			_openSearchConnectionConfiguration.password()
		).thenReturn(
			password
		);

		String proxyHost = RandomTestUtil.randomString();

		Mockito.when(
			_openSearchConnectionConfiguration.proxyHost()
		).thenReturn(
			proxyHost
		);

		String proxyPassword = RandomTestUtil.randomString();

		Mockito.when(
			_openSearchConnectionConfiguration.proxyPassword()
		).thenReturn(
			proxyPassword
		);

		int proxyPort = RandomTestUtil.randomInt();

		Mockito.when(
			_openSearchConnectionConfiguration.proxyPort()
		).thenReturn(
			proxyPort
		);

		String proxyUserName = RandomTestUtil.randomString();

		Mockito.when(
			_openSearchConnectionConfiguration.proxyUserName()
		).thenReturn(
			proxyUserName
		);

		String truststorePassword = RandomTestUtil.randomString();

		Mockito.when(
			_openSearchConnectionConfiguration.truststorePassword()
		).thenReturn(
			truststorePassword
		);

		String resolvedPassword = _mockResolve(password);
		String resolvedProxyPassword = _mockResolve(proxyPassword);
		String resolvedTruststorePassword = _mockResolve(truststorePassword);

		_openSearchConnectionsHolderImpl.activate(new HashMap<>());

		OpenSearchConnection openSearchConnection =
			_openSearchConnectionsHolderImpl.getOpenSearchConnection(
				connectionId);

		Assert.assertEquals(
			resolvedPassword,
			ReflectionTestUtil.getFieldValue(
				openSearchConnection, "_password"));
		Assert.assertEquals(
			resolvedTruststorePassword,
			ReflectionTestUtil.getFieldValue(
				openSearchConnection, "_truststorePassword"));

		ProxyConfig proxyConfig = ReflectionTestUtil.getFieldValue(
			openSearchConnection, "_proxyConfig");

		Assert.assertEquals(proxyHost, proxyConfig.getHost());
		Assert.assertEquals(resolvedProxyPassword, proxyConfig.getPassword());
		Assert.assertEquals(proxyPort, proxyConfig.getPort());
		Assert.assertEquals(proxyUserName, proxyConfig.getUserName());
	}

	private String _mockResolve(String value) {
		String resolvedValue = RandomTestUtil.randomString();

		Mockito.when(
			_secretResolver.resolve(CompanyConstants.SYSTEM, value)
		).thenReturn(
			resolvedValue
		);

		return resolvedValue;
	}

	private static final MockedStatic<ConfigurableUtil>
		_configurableUtilMockedStatic = Mockito.mockStatic(
			ConfigurableUtil.class);

	private final Http _http = Mockito.mock(Http.class);
	private final OpenSearchConnectionConfiguration
		_openSearchConnectionConfiguration = Mockito.mock(
			OpenSearchConnectionConfiguration.class);
	private final OpenSearchConnectionsHolderImpl
		_openSearchConnectionsHolderImpl =
			new OpenSearchConnectionsHolderImpl();
	private final SecretResolver _secretResolver = Mockito.mock(
		SecretResolver.class);

}