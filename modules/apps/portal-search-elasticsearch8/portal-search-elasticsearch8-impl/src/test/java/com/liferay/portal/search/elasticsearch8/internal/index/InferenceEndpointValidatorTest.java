/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.index;

import co.elastic.clients.elasticsearch.inference.InferenceEndpointInfo;
import co.elastic.clients.elasticsearch.inference.TaskType;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.elasticsearch8.internal.semantic.InferenceEndpointInfoFetcher;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

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
			_inferenceEndpointValidator, "_inferenceEndpointInfoFetcher",
			_inferenceEndpointInfoFetcher);
	}

	@Test
	public void testValidate() {
		_setUpInferenceEndpointInfoFetcher(TaskType.TextEmbedding);

		_inferenceEndpointValidator.validate(_INFERENCE_ID);

		Mockito.verify(
			_inferenceEndpointInfoFetcher
		).fetchInferenceEndpointInfos(
			_INFERENCE_ID
		);
	}

	@Test
	public void testValidatePropagatesFetchException() {
		RuntimeException runtimeException1 = new RuntimeException();

		Mockito.when(
			_inferenceEndpointInfoFetcher.fetchInferenceEndpointInfos(
				_INFERENCE_ID)
		).thenThrow(
			runtimeException1
		);

		try {
			_inferenceEndpointValidator.validate(_INFERENCE_ID);

			Assert.fail();
		}
		catch (RuntimeException runtimeException2) {
			Assert.assertSame(runtimeException1, runtimeException2);
		}
	}

	@Test
	public void testValidateWrongTaskType() {
		_setUpInferenceEndpointInfoFetcher(TaskType.Completion);

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

	private void _setUpInferenceEndpointInfoFetcher(TaskType taskType) {
		InferenceEndpointInfo inferenceEndpointInfo = Mockito.mock(
			InferenceEndpointInfo.class);

		Mockito.when(
			inferenceEndpointInfo.taskType()
		).thenReturn(
			taskType
		);

		Mockito.when(
			_inferenceEndpointInfoFetcher.fetchInferenceEndpointInfos(
				_INFERENCE_ID)
		).thenReturn(
			Collections.singletonList(inferenceEndpointInfo)
		);
	}

	private static final String _INFERENCE_ID = "liferay-active-provider";

	private final InferenceEndpointInfoFetcher _inferenceEndpointInfoFetcher =
		Mockito.mock(InferenceEndpointInfoFetcher.class);
	private InferenceEndpointValidator _inferenceEndpointValidator;

}