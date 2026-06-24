/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.semantic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.inference.ElasticsearchInferenceClient;
import co.elastic.clients.elasticsearch.inference.GetInferenceResponse;
import co.elastic.clients.elasticsearch.inference.InferenceEndpointInfo;

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

import org.mockito.Mockito;

/**
 * @author Rodrigo Guedes de Souza
 */
public class ElasticsearchInferenceEndpointLocatorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_elasticsearchInferenceEndpointLocator =
			new ElasticsearchInferenceEndpointLocator();

		ReflectionTestUtil.setFieldValue(
			_elasticsearchInferenceEndpointLocator,
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
	public void testFindInferenceIdBlankPrefix() {
		try {
			_elasticsearchInferenceEndpointLocator.findInferenceId(
				StringPool.BLANK);

			Assert.fail();
		}
		catch (IllegalArgumentException illegalArgumentException) {
			Assert.assertEquals(
				"Inference ID prefix is null or empty",
				illegalArgumentException.getMessage());
		}
	}

	@Test
	public void testFindInferenceIdMatchesPrefix() throws Exception {
		_setUpGetInferenceResponse(
			".elser-2-elasticsearch", "liferay-12345-inference-openai");

		Assert.assertEquals(
			"liferay-12345-inference-openai",
			_elasticsearchInferenceEndpointLocator.findInferenceId(
				"liferay-12345-inference-"));
	}

	@Test
	public void testFindInferenceIdNoMatchForOtherCompany() throws Exception {
		_setUpGetInferenceResponse(
			".elser-2-elasticsearch", "liferay-99999-inference-openai");

		Assert.assertNull(
			_elasticsearchInferenceEndpointLocator.findInferenceId(
				"liferay-12345-inference-"));
	}

	@Test
	public void testFindInferenceIdNoneExist() throws Exception {
		GetInferenceResponse getInferenceResponse = Mockito.mock(
			GetInferenceResponse.class);

		Mockito.when(
			getInferenceResponse.endpoints()
		).thenReturn(
			Collections.emptyList()
		);

		Mockito.when(
			_elasticsearchInferenceClient.get()
		).thenReturn(
			getInferenceResponse
		);

		Assert.assertNull(
			_elasticsearchInferenceEndpointLocator.findInferenceId(
				"liferay-12345-inference-"));
	}

	@Test
	public void testFindInferenceIdWrapsIOException() throws Exception {
		Mockito.when(
			_elasticsearchInferenceClient.get()
		).thenThrow(
			new IOException()
		);

		try {
			_elasticsearchInferenceEndpointLocator.findInferenceId(
				"liferay-12345-inference-");

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Assert.assertEquals(
				"Unable to get the inference endpoints from Elasticsearch",
				runtimeException.getMessage());
		}
	}

	private void _setUpGetInferenceResponse(String... inferenceIds)
		throws Exception {

		List<InferenceEndpointInfo> inferenceEndpointInfos = Arrays.asList(
			_toInferenceEndpointInfo(inferenceIds[0]),
			_toInferenceEndpointInfo(inferenceIds[1]));

		GetInferenceResponse getInferenceResponse = Mockito.mock(
			GetInferenceResponse.class);

		Mockito.when(
			getInferenceResponse.endpoints()
		).thenReturn(
			inferenceEndpointInfos
		);

		Mockito.when(
			_elasticsearchInferenceClient.get()
		).thenReturn(
			getInferenceResponse
		);
	}

	private InferenceEndpointInfo _toInferenceEndpointInfo(String inferenceId) {
		InferenceEndpointInfo inferenceEndpointInfo = Mockito.mock(
			InferenceEndpointInfo.class);

		Mockito.when(
			inferenceEndpointInfo.inferenceId()
		).thenReturn(
			inferenceId
		);

		return inferenceEndpointInfo;
	}

	private final ElasticsearchClient _elasticsearchClient = Mockito.mock(
		ElasticsearchClient.class);
	private final ElasticsearchConnectionManager
		_elasticsearchConnectionManager = Mockito.mock(
			ElasticsearchConnectionManager.class);
	private final ElasticsearchInferenceClient _elasticsearchInferenceClient =
		Mockito.mock(ElasticsearchInferenceClient.class);
	private ElasticsearchInferenceEndpointLocator
		_elasticsearchInferenceEndpointLocator;

}