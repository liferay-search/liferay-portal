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

package com.liferay.portal.search.elasticsearch7.internal.index;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.elasticsearch7.internal.configuration.ElasticsearchConfigurationObserver;
import com.liferay.portal.search.elasticsearch7.internal.configuration.ElasticsearchConfigurationWrapper;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.index.UpdateIndexSettingsIndexRequest;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Gustavo Lima
 */
@Component(service = {})
public class IndexMaxResultWindowUpdate
	implements ElasticsearchConfigurationObserver {

	@Override
	public int compareTo(
		ElasticsearchConfigurationObserver elasticsearchConfigurationObserver) {

		return elasticsearchConfigurationWrapper.compare(
			this, elasticsearchConfigurationObserver);
	}

	@Override
	public int getPriority() {
		return 5;
	}

	@Override
	public void onElasticsearchConfigurationUpdate() {
		_updateMaxResultWindow(
			elasticsearchConfigurationWrapper.indexMaxResultWindow());
	}

	@Activate
	protected void activate() {
		elasticsearchConfigurationWrapper.register(this);

		_updateMaxResultWindow(
			elasticsearchConfigurationWrapper.indexMaxResultWindow());
	}

	@Deactivate
	protected void deactivate() {
		elasticsearchConfigurationWrapper.unregister(this);
	}

	@Reference
	protected volatile ElasticsearchConfigurationWrapper
		elasticsearchConfigurationWrapper;

	private void _updateMaxResultWindow(int maxResultWindow) {
		String[] indexNames = {"liferay-20096"};

		for (String indexName : indexNames) {
			UpdateIndexSettingsIndexRequest updateIndexSettingsIndexRequest =
				new UpdateIndexSettingsIndexRequest(indexName);

			updateIndexSettingsIndexRequest.setSettings(
				"{\"index.max_result_window\": " + maxResultWindow + "}");

			_searchEngineAdapter.execute(updateIndexSettingsIndexRequest);
		}

		if (_log.isInfoEnabled()) {
			_log.info("Updated index.max_result_window to " + maxResultWindow);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		IndexMaxResultWindowUpdate.class);

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}