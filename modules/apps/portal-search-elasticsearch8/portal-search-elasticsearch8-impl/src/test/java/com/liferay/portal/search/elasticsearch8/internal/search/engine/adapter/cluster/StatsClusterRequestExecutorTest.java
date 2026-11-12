/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.search.engine.adapter.cluster;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonpDeserializer;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.JsonEndpoint;

import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchClientResolver;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchConnectionFixture;
import com.liferay.portal.search.elasticsearch8.internal.util.ResourceUtil;
import com.liferay.portal.search.engine.adapter.cluster.ClusterHealthStatus;
import com.liferay.portal.search.engine.adapter.cluster.StatsClusterRequest;
import com.liferay.portal.search.engine.adapter.cluster.StatsClusterResponse;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonParser;

import java.io.StringReader;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Dylan Rebelak
 */
public class StatsClusterRequestExecutorTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		ElasticsearchConnectionFixture elasticsearchConnectionFixture =
			ElasticsearchConnectionFixture.builder(
			).clusterName(
				StatsClusterRequestExecutorTest.class.getSimpleName()
			).build();

		elasticsearchConnectionFixture.createNode();

		_elasticsearchConnectionFixture = elasticsearchConnectionFixture;
	}

	@After
	public void tearDown() throws Exception {
		_elasticsearchConnectionFixture.destroyNode();
	}

	@Test
	public void testClusterRequestExecution() {
		StatsClusterRequest statsClusterRequest = new StatsClusterRequest(
			new String[] {_NODE_ID});

		StatsClusterRequestExecutor statsClusterRequestExecutor =
			new StatsClusterRequestExecutor(_elasticsearchConnectionFixture);

		StatsClusterResponse statsClusterResponse =
			statsClusterRequestExecutor.execute(statsClusterRequest);

		Assert.assertNotNull(statsClusterResponse);
		Assert.assertNotEquals(
			0, statsClusterResponse.getAvailableSpaceInBytes());
		Assert.assertNotNull(statsClusterResponse.getClusterHealthStatus());
		Assert.assertNotEquals(0, statsClusterResponse.getUsedSpaceInBytes());
	}

	@Test
	public void testExecuteWithoutOpenFileDescriptors() throws Exception {
		ElasticsearchClientResolver elasticsearchClientResolver = Mockito.mock(
			ElasticsearchClientResolver.class);

		ElasticsearchTransport elasticsearchTransport = Mockito.mock(
			ElasticsearchTransport.class);
		JacksonJsonpMapper jacksonJsonpMapper = new JacksonJsonpMapper();

		Mockito.when(
			elasticsearchTransport.jsonpMapper()
		).thenReturn(
			jacksonJsonpMapper
		);

		Mockito.when(
			elasticsearchTransport.performRequest(
				Mockito.any(), Mockito.any(), Mockito.any())
		).then(
			invocation -> {
				String json = ResourceUtil.getResourceAsString(
					getClass(),
					"StatsClusterRequestExecutorTest-clusterStats.json");

				JsonProvider jsonProvider = jacksonJsonpMapper.jsonProvider();

				try (JsonParser jsonParser = jsonProvider.createParser(
						new StringReader(json))) {

					JsonEndpoint<?, ?, ?> jsonEndpoint = invocation.getArgument(
						1);

					JsonpDeserializer<?> jsonpDeserializer =
						jsonEndpoint.responseDeserializer();

					return jsonpDeserializer.deserialize(
						jsonParser, jacksonJsonpMapper);
				}
			}
		);

		Mockito.when(
			elasticsearchClientResolver.getElasticsearchClient(
				Mockito.any(), Mockito.anyBoolean())
		).thenReturn(
			new ElasticsearchClient(elasticsearchTransport)
		);

		StatsClusterRequestExecutor statsClusterRequestExecutor =
			new StatsClusterRequestExecutor(elasticsearchClientResolver);

		StatsClusterResponse statsClusterResponse =
			statsClusterRequestExecutor.execute(
				new StatsClusterRequest(new String[0]));

		Assert.assertEquals(
			192573853696L, statsClusterResponse.getAvailableSpaceInBytes());
		Assert.assertEquals(
			ClusterHealthStatus.GREEN,
			statsClusterResponse.getClusterHealthStatus());
		Assert.assertEquals(
			802036301824L, statsClusterResponse.getUsedSpaceInBytes());
	}

	private static final String _NODE_ID = "liferay_sidecar";

	private ElasticsearchConnectionFixture _elasticsearchConnectionFixture;

}