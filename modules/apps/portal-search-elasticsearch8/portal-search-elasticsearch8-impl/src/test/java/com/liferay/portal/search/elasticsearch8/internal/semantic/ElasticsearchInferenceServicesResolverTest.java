/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.semantic;

import co.elastic.clients.transport.rest_client.RestClientTransport;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchConnection;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.semantic.InferenceService;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.util.List;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * @author Rodrigo Guedes de Souza
 */
public class ElasticsearchInferenceServicesResolverTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_elasticsearchInferenceServicesResolver =
			new ElasticsearchInferenceServicesResolver();

		ReflectionTestUtil.setFieldValue(
			_elasticsearchInferenceServicesResolver,
			"_elasticsearchConnectionManager", _elasticsearchConnectionManager);
		ReflectionTestUtil.setFieldValue(
			_elasticsearchInferenceServicesResolver, "_jsonFactory",
			new JSONFactoryImpl());

		Mockito.when(
			_elasticsearchConnection.getRestClientTransport()
		).thenReturn(
			_restClientTransport
		);

		Mockito.when(
			_elasticsearchConnectionManager.getElasticsearchConnection()
		).thenReturn(
			_elasticsearchConnection
		);

		Mockito.when(
			_restClientTransport.restClient()
		).thenReturn(
			_restClient
		);
	}

	@Test
	public void testResolveInferenceServices() throws Exception {
		_setUpRestClient(
			StringBundler.concat(
				"[{\"service\": \"openai\", \"name\": \"OpenAI\", ",
				"\"configurations\": {\"api_key\": {\"required\": true, ",
				"\"sensitive\": true, \"type\": \"str\"}}}, {\"service\": ",
				"\"hugging_face\"}]"));

		List<InferenceService> inferenceServices =
			_elasticsearchInferenceServicesResolver.resolveInferenceServices();

		Assert.assertEquals(
			inferenceServices.toString(), 2, inferenceServices.size());

		InferenceService inferenceService = inferenceServices.get(0);

		Assert.assertEquals("openai", inferenceService.getService());
		Assert.assertTrue(
			inferenceService.getConfigurationJSON(),
			inferenceService.getConfigurationJSON(
			).contains(
				"api_key"
			));

		inferenceService = inferenceServices.get(1);

		Assert.assertEquals("hugging_face", inferenceService.getService());
		Assert.assertNull(inferenceService.getConfigurationJSON());

		ArgumentCaptor<Request> argumentCaptor = ArgumentCaptor.forClass(
			Request.class);

		Mockito.verify(
			_restClient
		).performRequest(
			argumentCaptor.capture()
		);

		Request request = argumentCaptor.getValue();

		Assert.assertEquals(
			"/_inference/_services/text_embedding", request.getEndpoint());
		Assert.assertEquals("GET", request.getMethod());
	}

	@Test
	public void testResolveInferenceServicesSkipsEntriesWithoutService()
		throws Exception {

		_setUpRestClient("[{\"name\": \"OpenAI\"}, {\"service\": \"\"}]");

		List<InferenceService> inferenceServices =
			_elasticsearchInferenceServicesResolver.resolveInferenceServices();

		Assert.assertEquals(
			inferenceServices.toString(), 0, inferenceServices.size());
	}

	@Test
	public void testResolveInferenceServicesWrapsInvalidJSON()
		throws Exception {

		_setUpRestClient("not a json array");

		try {
			_elasticsearchInferenceServicesResolver.resolveInferenceServices();

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				"Unable to parse the inference services response",
				runtimeException.getMessage());
		}
	}

	@Test
	public void testResolveInferenceServicesWrapsIOException()
		throws Exception {

		IOException ioException = new IOException();

		Mockito.when(
			_restClient.performRequest(Mockito.any(Request.class))
		).thenThrow(
			ioException
		);

		try {
			_elasticsearchInferenceServicesResolver.resolveInferenceServices();

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				"Unable to get the inference services from Elasticsearch",
				runtimeException.getMessage());
			Assert.assertSame(ioException, runtimeException.getCause());
		}
	}

	private void _setUpRestClient(String responseJSON) throws Exception {
		Response response = Mockito.mock(Response.class);

		Mockito.when(
			response.getEntity()
		).thenReturn(
			new StringEntity(
				responseJSON,
				ContentType.APPLICATION_JSON.withCharset(
					StandardCharsets.UTF_8))
		);

		Mockito.when(
			_restClient.performRequest(Mockito.any(Request.class))
		).thenReturn(
			response
		);
	}

	private final ElasticsearchConnection _elasticsearchConnection =
		Mockito.mock(ElasticsearchConnection.class);
	private final ElasticsearchConnectionManager
		_elasticsearchConnectionManager = Mockito.mock(
			ElasticsearchConnectionManager.class);
	private ElasticsearchInferenceServicesResolver
		_elasticsearchInferenceServicesResolver;
	private final RestClient _restClient = Mockito.mock(RestClient.class);
	private final RestClientTransport _restClientTransport = Mockito.mock(
		RestClientTransport.class);

}