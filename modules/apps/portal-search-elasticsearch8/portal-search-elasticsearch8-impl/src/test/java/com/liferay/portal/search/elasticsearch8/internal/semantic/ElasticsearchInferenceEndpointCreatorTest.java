/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.semantic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.inference.ElasticsearchInferenceClient;
import co.elastic.clients.elasticsearch.inference.InferenceEndpoint;
import co.elastic.clients.elasticsearch.inference.PutRequest;
import co.elastic.clients.elasticsearch.inference.TaskType;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.IOException;

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
public class ElasticsearchInferenceEndpointCreatorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_elasticsearchInferenceEndpointCreator =
			new ElasticsearchInferenceEndpointCreator();

		ReflectionTestUtil.setFieldValue(
			_elasticsearchInferenceEndpointCreator,
			"_elasticsearchConnectionManager", _elasticsearchConnectionManager);

		Mockito.when(
			_elasticsearchClient.inference()
		).thenReturn(
			_elasticsearchInferenceClient
		);

		Mockito.when(
			_elasticsearchConnectionManager.getElasticsearchClient()
		).thenReturn(
			_elasticsearchClient
		);
	}

	@Test
	public void testCreateInferenceEndpoint() throws Exception {
		_elasticsearchInferenceEndpointCreator.createInferenceEndpoint(
			_INFERENCE_ID, "openai",
			"{\"api_key\": \"secret\", \"model_id\": " +
				"\"text-embedding-3-large\"}");

		ArgumentCaptor<PutRequest> argumentCaptor = ArgumentCaptor.forClass(
			PutRequest.class);

		Mockito.verify(
			_elasticsearchInferenceClient
		).put(
			argumentCaptor.capture()
		);

		PutRequest putRequest = argumentCaptor.getValue();

		Assert.assertEquals(_INFERENCE_ID, putRequest.inferenceId());
		Assert.assertEquals(TaskType.TextEmbedding, putRequest.taskType());

		InferenceEndpoint inferenceEndpoint = putRequest.inferenceConfig();

		Assert.assertEquals("openai", inferenceEndpoint.service());
		Assert.assertNotNull(inferenceEndpoint.serviceSettings());
	}

	@Test
	public void testCreateInferenceEndpointBlankInferenceId() {
		try {
			_elasticsearchInferenceEndpointCreator.createInferenceEndpoint(
				StringPool.BLANK, "openai", "{}");

			Assert.fail();
		}
		catch (IllegalArgumentException illegalArgumentException) {
			Assert.assertEquals(
				"Inference ID is null or empty",
				illegalArgumentException.getMessage());
		}
	}

	@Test
	public void testCreateInferenceEndpointBlankService() {
		try {
			_elasticsearchInferenceEndpointCreator.createInferenceEndpoint(
				_INFERENCE_ID, StringPool.BLANK, "{}");

			Assert.fail();
		}
		catch (IllegalArgumentException illegalArgumentException) {
			Assert.assertEquals(
				"Service is null or empty",
				illegalArgumentException.getMessage());
		}
	}

	@Test
	public void testCreateInferenceEndpointWithoutServiceSettings()
		throws Exception {

		_elasticsearchInferenceEndpointCreator.createInferenceEndpoint(
			_INFERENCE_ID, "openai", null);

		ArgumentCaptor<PutRequest> argumentCaptor = ArgumentCaptor.forClass(
			PutRequest.class);

		Mockito.verify(
			_elasticsearchInferenceClient
		).put(
			argumentCaptor.capture()
		);

		PutRequest putRequest = argumentCaptor.getValue();

		InferenceEndpoint inferenceEndpoint = putRequest.inferenceConfig();

		Assert.assertNotNull(inferenceEndpoint.serviceSettings());
	}

	@Test
	public void testCreateInferenceEndpointWrapsElasticsearchException()
		throws Exception {

		String message =
			"[es/inference.put] failed: [status_exception] Invalid API key";

		ElasticsearchException elasticsearchException = Mockito.mock(
			ElasticsearchException.class);

		Mockito.when(
			elasticsearchException.getMessage()
		).thenReturn(
			message
		);

		Mockito.when(
			_elasticsearchInferenceClient.put(Mockito.any(PutRequest.class))
		).thenThrow(
			elasticsearchException
		);

		try {
			_elasticsearchInferenceEndpointCreator.createInferenceEndpoint(
				_INFERENCE_ID, "openai", "{}");

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				"Unable to create inference endpoint " +
					"\"liferay-12345-inference-openai\": " + message,
				runtimeException.getMessage());
			Assert.assertSame(
				elasticsearchException, runtimeException.getCause());
		}
	}

	@Test
	public void testCreateInferenceEndpointWrapsIOException() throws Exception {
		IOException ioException = new IOException();

		Mockito.when(
			_elasticsearchInferenceClient.put(Mockito.any(PutRequest.class))
		).thenThrow(
			ioException
		);

		try {
			_elasticsearchInferenceEndpointCreator.createInferenceEndpoint(
				_INFERENCE_ID, "openai", "{}");

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				"Unable to create inference endpoint " +
					"\"liferay-12345-inference-openai\". Check the " +
						"Elasticsearch connection and try again.",
				runtimeException.getMessage());
			Assert.assertSame(ioException, runtimeException.getCause());
		}
	}

	private static final String _INFERENCE_ID =
		"liferay-12345-inference-openai";

	private final ElasticsearchClient _elasticsearchClient = Mockito.mock(
		ElasticsearchClient.class);
	private final ElasticsearchConnectionManager
		_elasticsearchConnectionManager = Mockito.mock(
			ElasticsearchConnectionManager.class);
	private final ElasticsearchInferenceClient _elasticsearchInferenceClient =
		Mockito.mock(ElasticsearchInferenceClient.class);
	private ElasticsearchInferenceEndpointCreator
		_elasticsearchInferenceEndpointCreator;

}