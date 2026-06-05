/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.semantic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.inference.ElasticsearchInferenceClient;
import co.elastic.clients.elasticsearch.inference.GetInferenceRequest;
import co.elastic.clients.elasticsearch.inference.GetInferenceResponse;
import co.elastic.clients.elasticsearch.inference.InferenceEndpointInfo;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.IOException;

import java.util.Collections;
import java.util.List;

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
public class InferenceEndpointInfoFetcherTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_inferenceEndpointInfoFetcher = new InferenceEndpointInfoFetcher();

		ReflectionTestUtil.setFieldValue(
			_inferenceEndpointInfoFetcher, "_elasticsearchConnectionManager",
			_elasticsearchConnectionManager);

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
	public void testFetchInferenceEndpointInfos() throws Exception {
		InferenceEndpointInfo inferenceEndpointInfo = Mockito.mock(
			InferenceEndpointInfo.class);

		GetInferenceResponse getInferenceResponse = Mockito.mock(
			GetInferenceResponse.class);

		Mockito.when(
			getInferenceResponse.endpoints()
		).thenReturn(
			Collections.singletonList(inferenceEndpointInfo)
		);

		Mockito.when(
			_elasticsearchInferenceClient.get(
				Mockito.any(GetInferenceRequest.class))
		).thenReturn(
			getInferenceResponse
		);

		List<InferenceEndpointInfo> inferenceEndpointInfos =
			_inferenceEndpointInfoFetcher.fetchInferenceEndpointInfos(
				_INFERENCE_ID);

		Assert.assertEquals(
			inferenceEndpointInfos.toString(), 1,
			inferenceEndpointInfos.size());
		Assert.assertSame(inferenceEndpointInfo, inferenceEndpointInfos.get(0));

		ArgumentCaptor<GetInferenceRequest> argumentCaptor =
			ArgumentCaptor.forClass(GetInferenceRequest.class);

		Mockito.verify(
			_elasticsearchInferenceClient
		).get(
			argumentCaptor.capture()
		);

		GetInferenceRequest getInferenceRequest = argumentCaptor.getValue();

		Assert.assertEquals(_INFERENCE_ID, getInferenceRequest.inferenceId());
	}

	@Test
	public void testFetchInferenceEndpointInfosBlankInferenceId() {
		try {
			_inferenceEndpointInfoFetcher.fetchInferenceEndpointInfos(
				StringPool.BLANK);

			Assert.fail();
		}
		catch (IllegalArgumentException illegalArgumentException) {
			Assert.assertEquals(
				"Inference ID is null or empty",
				illegalArgumentException.getMessage());
		}
	}

	@Test
	public void testFetchInferenceEndpointInfosNoEndpoints() throws Exception {
		GetInferenceResponse getInferenceResponse = Mockito.mock(
			GetInferenceResponse.class);

		Mockito.when(
			getInferenceResponse.endpoints()
		).thenReturn(
			Collections.emptyList()
		);

		Mockito.when(
			_elasticsearchInferenceClient.get(
				Mockito.any(GetInferenceRequest.class))
		).thenReturn(
			getInferenceResponse
		);

		try {
			_inferenceEndpointInfoFetcher.fetchInferenceEndpointInfos(
				_INFERENCE_ID);

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				_NOT_FOUND_MESSAGE, runtimeException.getMessage());
		}
	}

	@Test
	public void testFetchInferenceEndpointInfosNotFound() throws Exception {
		ElasticsearchException elasticsearchException = Mockito.mock(
			ElasticsearchException.class);

		Mockito.when(
			elasticsearchException.status()
		).thenReturn(
			404
		);

		Mockito.when(
			_elasticsearchInferenceClient.get(
				Mockito.any(GetInferenceRequest.class))
		).thenThrow(
			elasticsearchException
		);

		try {
			_inferenceEndpointInfoFetcher.fetchInferenceEndpointInfos(
				_INFERENCE_ID);

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				_NOT_FOUND_MESSAGE, runtimeException.getMessage());
			Assert.assertSame(
				elasticsearchException, runtimeException.getCause());
		}
	}

	@Test
	public void testFetchInferenceEndpointInfosWrapsElasticsearchException()
		throws Exception {

		ElasticsearchException elasticsearchException = Mockito.mock(
			ElasticsearchException.class);

		Mockito.when(
			elasticsearchException.status()
		).thenReturn(
			500
		);

		Mockito.when(
			_elasticsearchInferenceClient.get(
				Mockito.any(GetInferenceRequest.class))
		).thenThrow(
			elasticsearchException
		);

		try {
			_inferenceEndpointInfoFetcher.fetchInferenceEndpointInfos(
				_INFERENCE_ID);

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				_UNAVAILABLE_MESSAGE, runtimeException.getMessage());
			Assert.assertSame(
				elasticsearchException, runtimeException.getCause());
		}
	}

	@Test
	public void testFetchInferenceEndpointInfosWrapsIOException()
		throws Exception {

		IOException ioException = new IOException();

		Mockito.when(
			_elasticsearchInferenceClient.get(
				Mockito.any(GetInferenceRequest.class))
		).thenThrow(
			ioException
		);

		try {
			_inferenceEndpointInfoFetcher.fetchInferenceEndpointInfos(
				_INFERENCE_ID);

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				_UNAVAILABLE_MESSAGE, runtimeException.getMessage());
			Assert.assertSame(ioException, runtimeException.getCause());
		}
	}

	private static final String _INFERENCE_ID = "liferay-active-provider";

	private static final String _NOT_FOUND_MESSAGE =
		"Inference endpoint \"liferay-active-provider\" was not found in " +
			"Elasticsearch. Configure it in the Semantic Search admin UI " +
				"first.";

	private static final String _UNAVAILABLE_MESSAGE =
		"Unable to get inference endpoint \"liferay-active-provider\"";

	private final ElasticsearchClient _elasticsearchClient = Mockito.mock(
		ElasticsearchClient.class);
	private final ElasticsearchConnectionManager
		_elasticsearchConnectionManager = Mockito.mock(
			ElasticsearchConnectionManager.class);
	private final ElasticsearchInferenceClient _elasticsearchInferenceClient =
		Mockito.mock(ElasticsearchInferenceClient.class);
	private InferenceEndpointInfoFetcher _inferenceEndpointInfoFetcher;

}