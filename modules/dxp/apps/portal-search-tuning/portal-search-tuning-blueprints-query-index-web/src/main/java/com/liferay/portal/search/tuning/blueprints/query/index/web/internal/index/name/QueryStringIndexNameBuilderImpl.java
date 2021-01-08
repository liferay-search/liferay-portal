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

package com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.name;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexName;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexNameBuilder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = QueryStringIndexNameBuilder.class)
public class QueryStringIndexNameBuilderImpl
	implements QueryStringIndexNameBuilder {

	@Override
	public QueryStringIndexName getQueryStringIndexName(long companyId) {
		return new QueryStringIndexNameImpl(
			_indexNameBuilder.getIndexName(companyId) + StringPool.DASH +
				QUERY_STRING_INDEX_NAME_SUFFIX);
	}

	@Reference(unbind = "-")
	protected void setIndexNameBuilder(IndexNameBuilder indexNameBuilder) {
		_indexNameBuilder = indexNameBuilder;
	}

	protected static final String QUERY_STRING_INDEX_NAME_SUFFIX =
		"search-tuning-blueprints-query-strings";

	private IndexNameBuilder _indexNameBuilder;

	private static class QueryStringIndexNameImpl
		implements QueryStringIndexName {

		public QueryStringIndexNameImpl(String indexName) {
			_indexName = indexName;
		}

		@Override
		public String getIndexName() {
			return _indexName;
		}

		private final String _indexName;

	}

}