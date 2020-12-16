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
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingSetIndexName;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = MisspellingSetIndexReader.class)
public class MisspellingSetIndexReaderImpl
	implements MisspellingSetIndexReader {

	@Override
	public Optional<MisspellingSet> fetchOptional(
		MisspellingSetIndexName misspellingSetIndexName, String id) {

		return _getDocumentOptional(
			misspellingSetIndexName, id
		).map(
			document -> translate(document, id)
		);
	}

	public long getNextMisspellingSetId(
		MisspellingSetIndexName misspellingSetIndexName) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.addComplexQueryParts(
			Arrays.asList(
				_complexQueryPartBuilderFactory.builder(
				).query(
					_queries.matchAll()
				).occur(
					"filter"
				).build()));

		searchSearchRequest.setFetchSource(true);
		searchSearchRequest.setIndexNames(
			misspellingSetIndexName.getIndexName());
		searchSearchRequest.setPreferLocalCluster(false);
		searchSearchRequest.setSize(1);
		searchSearchRequest.setStart(0);
		searchSearchRequest.addSorts(
			_sorts.field(
				MisspellingSetFields.MISSPELLING_SET_ID, SortOrder.DESC));

		return _getNextMisspellingSetId(
			_searchEngineAdapter.execute(searchSearchRequest));
	}

	@Override
	public boolean isExists(MisspellingSetIndexName misspellingSetIndexName) {
		IndicesExistsIndexRequest indicesExistsIndexRequest =
			new IndicesExistsIndexRequest(
				misspellingSetIndexName.getIndexName());

		indicesExistsIndexRequest.setPreferLocalCluster(false);

		IndicesExistsIndexResponse indicesExistsIndexResponse =
			_searchEngineAdapter.execute(indicesExistsIndexRequest);

		return indicesExistsIndexResponse.isExists();
	}

	@Override
	public List<MisspellingSet> search(
		MisspellingSetIndexName misspellingSetIndexName) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames(
			misspellingSetIndexName.getIndexName());
		searchSearchRequest.setPreferLocalCluster(false);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		return _documentToMisspellingSetTranslator.translateAll(
			searchSearchResponse.getSearchHits());
	}

	@Reference(unbind = "-")
	protected void setSearchEngineAdapter(
		SearchEngineAdapter searchEngineAdapter) {

		_searchEngineAdapter = searchEngineAdapter;
	}

	protected MisspellingSet translate(Document document, String id) {
		return _documentToMisspellingSetTranslator.translate(document, id);
	}

	private Optional<Document> _getDocumentOptional(
		MisspellingSetIndexName misspellingSetIndexName, String id) {

		if (Validator.isNull(id)) {
			return Optional.empty();
		}

		GetDocumentRequest getDocumentRequest = new GetDocumentRequest(
			misspellingSetIndexName.getIndexName(), id);

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

	private long _getNextMisspellingSetId(
		SearchSearchResponse searchSearchResponse) {

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		if (searchHits.getTotalHits() == 0) {
			return 1;
		}

		List<SearchHit> searchHitList = searchHits.getSearchHits();

		SearchHit searchHit = searchHitList.get(0);

		Document document = searchHit.getDocument();

		return document.getLong(MisspellingSetFields.MISSPELLING_SET_ID);
	}

	@Reference
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Reference
	private DocumentToMisspellingSetTranslator
		_documentToMisspellingSetTranslator;

	@Reference
	private Queries _queries;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

	@Reference
	private Sorts _sorts;

}