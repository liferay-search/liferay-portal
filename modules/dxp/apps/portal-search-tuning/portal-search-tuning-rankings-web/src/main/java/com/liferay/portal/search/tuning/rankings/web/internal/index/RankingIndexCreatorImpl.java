/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.portal.search.tuning.rankings.web.internal.index;

import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.index.CreateIndexRequest;
import com.liferay.portal.search.engine.adapter.index.DeleteIndexRequest;
import com.liferay.portal.search.tuning.rankings.web.internal.index.name.RankingIndexName;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Wade Cao
 * @author Adam Brandizzi
 */
@Component(service = RankingIndexCreator.class)
public class RankingIndexCreatorImpl implements RankingIndexCreator {

	@Override
	public void create(RankingIndexName rankingIndexName) {
		String mappings = StringUtil.read(
			getClass(), _INDEX_MAPPINGS_RESOURCE_NAME);

		String source = StringUtil.read(
			getClass(), _INDEX_SETTINGS_RESOURCE_NAME);

		CreateIndexRequest createIndexRequest = new CreateIndexRequest(
			rankingIndexName.getIndexName());

		createIndexRequest.setMappings(mappings);

		createIndexRequest.setSource(source);

		_searchEngineAdapter.execute(createIndexRequest);
	}

	@Override
	public void delete(RankingIndexName rankingIndexName) {
		DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(
			rankingIndexName.getIndexName());

		_searchEngineAdapter.execute(deleteIndexRequest);
	}

	private static final String _INDEX_MAPPINGS_RESOURCE_NAME =
		"/META-INF/search/liferay-search-tuning-rankings-index-mappings.json";

	private static final String _INDEX_SETTINGS_RESOURCE_NAME =
		"/META-INF/search/liferay-search-tuning-rankings-index-settings.json";

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}