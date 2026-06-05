/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.index;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.inference.ElasticsearchInferenceClient;
import co.elastic.clients.elasticsearch.inference.GetInferenceRequest;
import co.elastic.clients.elasticsearch.inference.GetInferenceResponse;
import co.elastic.clients.elasticsearch.inference.InferenceEndpointInfo;
import co.elastic.clients.elasticsearch.inference.TaskType;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.IOException;

import java.util.Collections;

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
public class InferenceEndpointValidatorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_inferenceEndpointValidator = new InferenceEndpointValidator();

		ReflectionTestUtil.setFieldValue(
			_inferenceEndpointValidator, "_elasticsearchConnectionManager",
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
	public void testValidate() throws Exception {
		_setUpGetInferenceResponse(TaskType.TextEmbedding);

		_inferenceEndpointValidator.validate(_INFERENCE_ID);

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
	public void testValidateBlankInferenceId() {
		try {
			_inferenceEndpointValidator.validate(StringPool.BLANK);

			Assert.fail();
		}
		catch (IllegalArgumentException illegalArgumentException) {
			Assert.assertEquals(
				"Inference ID is null or empty",
				illegalArgumentException.getMessage());
		}
	}

	@Test
	public void testValidateNoEndpoints() throws Exception {
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
			_inferenceEndpointValidator.validate(_INFERENCE_ID);

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				_NOT_FOUND_MESSAGE, runtimeException.getMessage());
		}
	}

	@Test
	public void testValidateNotFound() throws Exception {
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
			_inferenceEndpointValidator.validate(_INFERENCE_ID);

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
	public void testValidateWrapsElasticsearchException() throws Exception {
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
			_inferenceEndpointValidator.validate(_INFERENCE_ID);

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				"Unable to validate inference endpoint " +
					"\"liferay-active-provider\"",
				runtimeException.getMessage());
			Assert.assertSame(
				elasticsearchException, runtimeException.getCause());
		}
	}

	@Test
	public void testValidateWrapsIOException() throws Exception {
		IOException ioException = new IOException();

		Mockito.when(
			_elasticsearchInferenceClient.get(
				Mockito.any(GetInferenceRequest.class))
		).thenThrow(
			ioException
		);

		try {
			_inferenceEndpointValidator.validate(_INFERENCE_ID);

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				"Unable to validate inference endpoint " +
					"\"liferay-active-provider\"",
				runtimeException.getMessage());
			Assert.assertSame(ioException, runtimeException.getCause());
		}
	}

	@Test
	public void testValidateWrongTaskType() throws Exception {
		_setUpGetInferenceResponse(TaskType.Completion);

		try {
			_inferenceEndpointValidator.validate(_INFERENCE_ID);

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				"Inference endpoint \"liferay-active-provider\" has " +
					"task_type \"completion\", expected \"text_embedding\". " +
						"Recreate it in the Semantic Search admin UI.",
				runtimeException.getMessage());
		}
	}

	private void _setUpGetInferenceResponse(TaskType taskType)
		throws Exception {

		InferenceEndpointInfo inferenceEndpointInfo = Mockito.mock(
			InferenceEndpointInfo.class);

		Mockito.when(
			inferenceEndpointInfo.taskType()
		).thenReturn(
			taskType
		);

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
	}

	private static final String _INFERENCE_ID = "liferay-active-provider";

	private static final String _NOT_FOUND_MESSAGE =
		"Inference endpoint \"liferay-active-provider\" was not found in " +
			"Elasticsearch. Configure it in the Semantic Search admin UI " +
				"first.";

	private final ElasticsearchClient _elasticsearchClient = Mockito.mock(
		ElasticsearchClient.class);
	private final ElasticsearchConnectionManager
		_elasticsearchConnectionManager = Mockito.mock(
			ElasticsearchConnectionManager.class);
	private final ElasticsearchInferenceClient _elasticsearchInferenceClient =
		Mockito.mock(ElasticsearchInferenceClient.class);
	private InferenceEndpointValidator _inferenceEndpointValidator;

}