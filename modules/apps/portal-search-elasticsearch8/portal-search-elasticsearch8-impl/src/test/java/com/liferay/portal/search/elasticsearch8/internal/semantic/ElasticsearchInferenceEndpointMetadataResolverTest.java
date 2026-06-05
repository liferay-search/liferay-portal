/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.semantic;

import co.elastic.clients.elasticsearch.inference.InferenceEndpointInfo;
import co.elastic.clients.json.JsonData;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.semantic.InferenceEndpointMetadata;
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
public class ElasticsearchInferenceEndpointMetadataResolverTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_elasticsearchInferenceEndpointMetadataResolver =
			new ElasticsearchInferenceEndpointMetadataResolver();

		ReflectionTestUtil.setFieldValue(
			_elasticsearchInferenceEndpointMetadataResolver,
			"_inferenceEndpointInfoFetcher", _inferenceEndpointInfoFetcher);
	}

	@Test
	public void testResolveInferenceEndpointMetadata() {
		_setUpInferenceEndpointInfoFetcher(
			StringBundler.concat(
				"{\"dimensions\": 3072, \"model_id\": ",
				"\"text-embedding-3-large\", \"similarity\": ",
				"\"dot_product\"}"));

		InferenceEndpointMetadata inferenceEndpointMetadata =
			_elasticsearchInferenceEndpointMetadataResolver.
				resolveInferenceEndpointMetadata(_INFERENCE_ID);

		Assert.assertEquals(3072, inferenceEndpointMetadata.getDimensions());
		Assert.assertEquals(
			"text-embedding-3-large", inferenceEndpointMetadata.getModelId());
		Assert.assertEquals("openai", inferenceEndpointMetadata.getService());

		Mockito.verify(
			_inferenceEndpointInfoFetcher
		).fetchInferenceEndpointInfos(
			_INFERENCE_ID
		);
	}

	@Test
	public void testResolveInferenceEndpointMetadataWithoutServiceSettings() {
		_setUpInferenceEndpointInfoFetcher("{}");

		InferenceEndpointMetadata inferenceEndpointMetadata =
			_elasticsearchInferenceEndpointMetadataResolver.
				resolveInferenceEndpointMetadata(_INFERENCE_ID);

		Assert.assertEquals(0, inferenceEndpointMetadata.getDimensions());
		Assert.assertNull(inferenceEndpointMetadata.getModelId());
		Assert.assertEquals("openai", inferenceEndpointMetadata.getService());
	}

	private void _setUpInferenceEndpointInfoFetcher(
		String serviceSettingsJSON) {

		InferenceEndpointInfo inferenceEndpointInfo = Mockito.mock(
			InferenceEndpointInfo.class);

		Mockito.when(
			inferenceEndpointInfo.service()
		).thenReturn(
			"openai"
		);

		Mockito.when(
			inferenceEndpointInfo.serviceSettings()
		).thenReturn(
			JsonData.fromJson(serviceSettingsJSON)
		);

		Mockito.when(
			_inferenceEndpointInfoFetcher.fetchInferenceEndpointInfos(
				_INFERENCE_ID)
		).thenReturn(
			Collections.singletonList(inferenceEndpointInfo)
		);
	}

	private static final String _INFERENCE_ID = "liferay-active-provider";

	private ElasticsearchInferenceEndpointMetadataResolver
		_elasticsearchInferenceEndpointMetadataResolver;
	private final InferenceEndpointInfoFetcher _inferenceEndpointInfoFetcher =
		Mockito.mock(InferenceEndpointInfoFetcher.class);

}