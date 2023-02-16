/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.search.elasticsearch7.internal.search.engine.adapter.search;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchClientResolver;
import com.liferay.portal.search.engine.adapter.search.ClearScrollRequest;
import com.liferay.portal.search.engine.adapter.search.ClearScrollResponse;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Gustavo Lima
 */
@Component(service = ClearScrollRequestExecutor.class)
public class ClearScrollRequestExecutorImpl
	implements ClearScrollRequestExecutor {

	@Override
	public ClearScrollResponse execute(ClearScrollRequest clearScrollRequest) {
		org.elasticsearch.action.search.ClearScrollRequest
			elasticsearchClearScrollRequest =
				new org.elasticsearch.action.search.ClearScrollRequest();

		elasticsearchClearScrollRequest.addScrollId(
			clearScrollRequest.getScrollId());

		org.elasticsearch.action.search.ClearScrollResponse
			clearScrollResponse = _getClearScrollResponse(
				clearScrollRequest, elasticsearchClearScrollRequest);

		if (_log.isInfoEnabled() && clearScrollResponse.isSucceeded()) {
			_log.info(
				"Clear scroll request to scrollId:" +
					clearScrollRequest.getScrollId() + " executed");
		}
		else if (_log.isWarnEnabled() && !clearScrollResponse.isSucceeded()) {
			_log.warn(
				"Clear scroll request to scrollId:" +
					clearScrollRequest.getScrollId() + " failed");
		}

		return new ClearScrollResponse(clearScrollResponse.isSucceeded());
	}

	private org.elasticsearch.action.search.ClearScrollResponse
		_getClearScrollResponse(
			ClearScrollRequest clearScrollRequest,
			org.elasticsearch.action.search.ClearScrollRequest
				elasticsearchClearScrollRequest) {

		RestHighLevelClient restHighLevelClient =
			_elasticsearchClientResolver.getRestHighLevelClient(
				clearScrollRequest.getConnectionId(),
				clearScrollRequest.isPreferLocalCluster());

		try {
			return restHighLevelClient.clearScroll(
				elasticsearchClearScrollRequest, RequestOptions.DEFAULT);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception.getMessage(), exception);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ClearScrollRequestExecutorImpl.class);

	@Reference
	private ElasticsearchClientResolver _elasticsearchClientResolver;

}