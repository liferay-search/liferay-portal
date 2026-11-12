/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.search.engine.adapter.cluster;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonpDeserializer;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.endpoints.SimpleEndpoint;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchClientResolver;
import com.liferay.portal.search.engine.adapter.cluster.StatsClusterRequest;
import com.liferay.portal.search.engine.adapter.cluster.StatsClusterResponse;

import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import java.util.Collections;

/**
 * @author Dylan Rebelak
 */
public class StatsClusterRequestExecutor {

	public StatsClusterRequestExecutor(
		ElasticsearchClientResolver elasticsearchClientResolver) {

		_elasticsearchClientResolver = elasticsearchClientResolver;
	}

	public StatsClusterResponse execute(
		StatsClusterRequest statsClusterRequest) {

		try {
			JsonObject jsonObject = _getClusterStatsJsonObject(
				statsClusterRequest);

			JsonObject nodesJsonObject = jsonObject.getJsonObject("nodes");

			JsonObject fsJsonObject = nodesJsonObject.getJsonObject("fs");

			long availableInBytes = _getLong(
				fsJsonObject, "available_in_bytes");
			long totalInBytes = _getLong(fsJsonObject, "total_in_bytes");

			return new StatsClusterResponse(
				availableInBytes,
				ClusterHealthStatusTranslatorUtil.translate(
					jsonObject.getString("status")),
				jsonObject.toString(), totalInBytes - availableInBytes);
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
	}

	private JsonObject _getClusterStatsJsonObject(
			StatsClusterRequest statsClusterRequest)
		throws Exception {

		ElasticsearchClient elasticsearchClient =
			_elasticsearchClientResolver.getElasticsearchClient(
				statsClusterRequest.getConnectionId(),
				statsClusterRequest.isPreferLocalCluster());

		ElasticsearchTransport elasticsearchTransport =
			elasticsearchClient._transport();

		JsonValue jsonValue = elasticsearchTransport.performRequest(
			statsClusterRequest,
			new SimpleEndpoint<StatsClusterRequest, JsonValue>(
				"es/cluster.stats", clusterStatsRequest -> "GET",
				clusterStatsRequest -> _getPath(clusterStatsRequest),
				clusterStatsRequest -> Collections.emptyMap(),
				clusterStatsRequest -> Collections.emptyMap(),
				clusterStatsRequest -> Collections.emptyMap(), false,
				JsonpDeserializer.jsonValueDeserializer()),
			null);

		return jsonValue.asJsonObject();
	}

	private long _getLong(JsonObject jsonObject, String key) {
		JsonNumber jsonNumber = jsonObject.getJsonNumber(key);

		return jsonNumber.longValue();
	}

	private String _getPath(StatsClusterRequest statsClusterRequest) {
		if (ArrayUtil.isEmpty(statsClusterRequest.getNodeIds())) {
			return "/_cluster/stats";
		}

		String nodeIds = StringUtil.merge(
			statsClusterRequest.getNodeIds(), StringPool.COMMA);

		return "/_cluster/stats/nodes/" + nodeIds;
	}

	private final ElasticsearchClientResolver _elasticsearchClientResolver;

}