/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.semantic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.inference.ElasticsearchInferenceClient;
import co.elastic.clients.elasticsearch.inference.TextEmbeddingInferenceResult;
import co.elastic.clients.elasticsearch.inference.TextEmbeddingRequest;
import co.elastic.clients.elasticsearch.inference.TextEmbeddingResponse;
import co.elastic.clients.elasticsearch.inference.TextEmbeddingResult;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.IOException;

import java.util.Arrays;
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
public class ElasticsearchInferenceEndpointTesterTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_elasticsearchInferenceEndpointTester =
			new ElasticsearchInferenceEndpointTester();

		ReflectionTestUtil.setFieldValue(
			_elasticsearchInferenceEndpointTester,
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
	public void testTest() throws Exception {
		_setUpTextEmbeddingResponse(
			Arrays.asList(0.123F, -0.456F, 0.789F), true);

		Assert.assertEquals(
			3,
			_elasticsearchInferenceEndpointTester.test(_INFERENCE_ID, _INPUT));

		ArgumentCaptor<TextEmbeddingRequest> argumentCaptor =
			ArgumentCaptor.forClass(TextEmbeddingRequest.class);

		Mockito.verify(
			_elasticsearchInferenceClient
		).textEmbedding(
			argumentCaptor.capture()
		);

		TextEmbeddingRequest textEmbeddingRequest = argumentCaptor.getValue();

		Assert.assertEquals(_INFERENCE_ID, textEmbeddingRequest.inferenceId());
		Assert.assertEquals(
			Collections.singletonList(_INPUT), textEmbeddingRequest.input());
	}

	@Test
	public void testTestBlankInferenceId() {
		try {
			_elasticsearchInferenceEndpointTester.test(
				StringPool.BLANK, _INPUT);

			Assert.fail();
		}
		catch (IllegalArgumentException illegalArgumentException) {
			Assert.assertEquals(
				"Inference ID is null or empty",
				illegalArgumentException.getMessage());
		}
	}

	@Test
	public void testTestNoEmbeddings() throws Exception {
		_setUpTextEmbeddingResponse(null, true);

		try {
			_elasticsearchInferenceEndpointTester.test(_INFERENCE_ID, _INPUT);

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				"Inference endpoint \"liferay-active-provider\" returned no " +
					"embeddings",
				runtimeException.getMessage());
		}
	}

	@Test
	public void testTestNotFound() throws Exception {
		ElasticsearchException elasticsearchException = Mockito.mock(
			ElasticsearchException.class);

		Mockito.when(
			elasticsearchException.status()
		).thenReturn(
			404
		);

		Mockito.when(
			_elasticsearchInferenceClient.textEmbedding(
				Mockito.any(TextEmbeddingRequest.class))
		).thenThrow(
			elasticsearchException
		);

		try {
			_elasticsearchInferenceEndpointTester.test(_INFERENCE_ID, _INPUT);

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				"Inference endpoint \"liferay-active-provider\" was not " +
					"found in Elasticsearch. Configure it in the Semantic " +
						"Search admin UI first.",
				runtimeException.getMessage());
			Assert.assertSame(
				elasticsearchException, runtimeException.getCause());
		}
	}

	@Test
	public void testTestUnexpectedResultKind() throws Exception {
		TextEmbeddingInferenceResult textEmbeddingInferenceResult =
			Mockito.mock(TextEmbeddingInferenceResult.class);

		Mockito.when(
			textEmbeddingInferenceResult._kind()
		).thenReturn(
			TextEmbeddingInferenceResult.Kind.TextEmbeddingBits
		);

		Mockito.when(
			textEmbeddingInferenceResult.isTextEmbedding()
		).thenReturn(
			false
		);

		TextEmbeddingResponse textEmbeddingResponse = Mockito.mock(
			TextEmbeddingResponse.class);

		Mockito.when(
			textEmbeddingResponse.valueBody()
		).thenReturn(
			textEmbeddingInferenceResult
		);

		Mockito.when(
			_elasticsearchInferenceClient.textEmbedding(
				Mockito.any(TextEmbeddingRequest.class))
		).thenReturn(
			textEmbeddingResponse
		);

		try {
			_elasticsearchInferenceEndpointTester.test(_INFERENCE_ID, _INPUT);

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				"Inference endpoint \"liferay-active-provider\" returned an " +
					"unexpected result kind \"text_embedding_bits\"",
				runtimeException.getMessage());
		}
	}

	@Test
	public void testTestWrapsElasticsearchException() throws Exception {
		String message =
			"[es/inference.text_embedding] failed: [status_exception] " +
				"Invalid API key";

		ElasticsearchException elasticsearchException = Mockito.mock(
			ElasticsearchException.class);

		Mockito.when(
			elasticsearchException.getMessage()
		).thenReturn(
			message
		);

		Mockito.when(
			elasticsearchException.status()
		).thenReturn(
			401
		);

		Mockito.when(
			_elasticsearchInferenceClient.textEmbedding(
				Mockito.any(TextEmbeddingRequest.class))
		).thenThrow(
			elasticsearchException
		);

		try {
			_elasticsearchInferenceEndpointTester.test(_INFERENCE_ID, _INPUT);

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				"Unable to test inference endpoint " +
					"\"liferay-active-provider\": " + message,
				runtimeException.getMessage());
			Assert.assertSame(
				elasticsearchException, runtimeException.getCause());
		}
	}

	@Test
	public void testTestWrapsIOException() throws Exception {
		IOException ioException = new IOException();

		Mockito.when(
			_elasticsearchInferenceClient.textEmbedding(
				Mockito.any(TextEmbeddingRequest.class))
		).thenThrow(
			ioException
		);

		try {
			_elasticsearchInferenceEndpointTester.test(_INFERENCE_ID, _INPUT);

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				"Unable to test inference endpoint " +
					"\"liferay-active-provider\". Check the Elasticsearch " +
						"connection and try again.",
				runtimeException.getMessage());
			Assert.assertSame(ioException, runtimeException.getCause());
		}
	}

	private void _setUpTextEmbeddingResponse(
			List<Float> embedding, boolean textEmbedding)
		throws Exception {

		TextEmbeddingInferenceResult textEmbeddingInferenceResult =
			Mockito.mock(TextEmbeddingInferenceResult.class);

		Mockito.when(
			textEmbeddingInferenceResult.isTextEmbedding()
		).thenReturn(
			textEmbedding
		);

		if (embedding == null) {
			Mockito.when(
				textEmbeddingInferenceResult.textEmbedding()
			).thenReturn(
				Collections.emptyList()
			);
		}
		else {
			TextEmbeddingResult textEmbeddingResult = Mockito.mock(
				TextEmbeddingResult.class);

			Mockito.when(
				textEmbeddingResult.embedding()
			).thenReturn(
				embedding
			);

			Mockito.when(
				textEmbeddingInferenceResult.textEmbedding()
			).thenReturn(
				Collections.singletonList(textEmbeddingResult)
			);
		}

		TextEmbeddingResponse textEmbeddingResponse = Mockito.mock(
			TextEmbeddingResponse.class);

		Mockito.when(
			textEmbeddingResponse.valueBody()
		).thenReturn(
			textEmbeddingInferenceResult
		);

		Mockito.when(
			_elasticsearchInferenceClient.textEmbedding(
				Mockito.any(TextEmbeddingRequest.class))
		).thenReturn(
			textEmbeddingResponse
		);
	}

	private static final String _INFERENCE_ID = "liferay-active-provider";

	private static final String _INPUT = "Liferay Semantic Search test";

	private final ElasticsearchClient _elasticsearchClient = Mockito.mock(
		ElasticsearchClient.class);
	private final ElasticsearchConnectionManager
		_elasticsearchConnectionManager = Mockito.mock(
			ElasticsearchConnectionManager.class);
	private final ElasticsearchInferenceClient _elasticsearchInferenceClient =
		Mockito.mock(ElasticsearchInferenceClient.class);
	private ElasticsearchInferenceEndpointTester
		_elasticsearchInferenceEndpointTester;

}