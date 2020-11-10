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

package com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.search.index.IndexNameBuilder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = MisspellingSetIndexNameBuilder.class)
public class MisspellingSetIndexNameBuilderImpl
	implements MisspellingSetIndexNameBuilder {

	@Override
	public MisspellingSetIndexName getMisspellingSetIndexName(
		long companyId) {

		return new MisspellingSetIndexNameImpl(
			_indexNameBuilder.getIndexName(companyId) + StringPool.DASH +
				MISSPELLINGS_INDEX_NAME_SUFFIX);
	}

	@Reference(unbind = "-")
	protected void setIndexNameBuilder(IndexNameBuilder indexNameBuilder) {
		_indexNameBuilder = indexNameBuilder;
	}

	protected static final String MISSPELLINGS_INDEX_NAME_SUFFIX =
		"search-tuning-blueprints-misspellings";

	private IndexNameBuilder _indexNameBuilder;

	private static class MisspellingSetIndexNameImpl
		implements MisspellingSetIndexName {

		public MisspellingSetIndexNameImpl(String indexName) {
			_indexName = indexName;
		}

		@Override
		public String getIndexName() {
			return _indexName;
		}

		private final String _indexName;

	}

}