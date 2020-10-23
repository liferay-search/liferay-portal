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

package com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.GetDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.GetDocumentResponse;
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexRequest;
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexResponse;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingsDefinitionIndexName;

import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = MisspellingsDefinitionIndexReader.class)
public class MisspellingsDefinitionIndexReaderImpl
	implements MisspellingsDefinitionIndexReader {

	@Override
	public Optional<MisspellingsDefinition> fetchOptional(
		MisspellingsDefinitionIndexName misspellingsDefinitionIndexName,
		String uid) {

		return _getDocumentOptional(
			misspellingsDefinitionIndexName, uid
		).map(
			document -> translate(document)
		);
	}

	@Override
	public boolean isExists(
		MisspellingsDefinitionIndexName misspellingsDefinitionIndexName) {

		IndicesExistsIndexRequest indicesExistsIndexRequest =
			new IndicesExistsIndexRequest(
				misspellingsDefinitionIndexName.getIndexName());

		indicesExistsIndexRequest.setPreferLocalCluster(false);

		IndicesExistsIndexResponse indicesExistsIndexResponse =
			_searchEngineAdapter.execute(indicesExistsIndexRequest);

		return indicesExistsIndexResponse.isExists();
	}

	@Override
	public List<MisspellingsDefinition> search(
		MisspellingsDefinitionIndexName misspellingsDefinitionIndexName) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames(
			misspellingsDefinitionIndexName.getIndexName());
		searchSearchRequest.setPreferLocalCluster(false);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		return _documentToMisspellingsDefinitionTranslator.translateAll(
			searchSearchResponse.getSearchHits());
	}

	@Reference(unbind = "-")
	protected void setSearchEngineAdapter(
		SearchEngineAdapter searchEngineAdapter) {

		_searchEngineAdapter = searchEngineAdapter;
	}

	protected MisspellingsDefinition translate(Document document) {
		return _documentToMisspellingsDefinitionTranslator.translate(document);
	}

	private Optional<Document> _getDocumentOptional(
		MisspellingsDefinitionIndexName misspellingsDefinitionIndexName,
		String uid) {

		if (Validator.isNull(uid)) {
			return Optional.empty();
		}

		GetDocumentRequest getDocumentRequest = new GetDocumentRequest(
			misspellingsDefinitionIndexName.getIndexName(), uid);

		getDocumentRequest.setFetchSource(true);
		getDocumentRequest.setFetchSourceInclude(StringPool.STAR);
		getDocumentRequest.setPreferLocalCluster(false);

		GetDocumentResponse getDocumentResponse = _searchEngineAdapter.execute(
			getDocumentRequest);

		if (getDocumentResponse.isExists()) {
			return Optional.of(getDocumentResponse.getDocument());
		}

		return Optional.empty();
	}

	@Reference
	private DocumentToMisspellingsDefinitionTranslator
		_documentToMisspellingsDefinitionTranslator;

	@Reference
	private Queries _queries;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}