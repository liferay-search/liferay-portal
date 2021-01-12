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

package com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index;

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
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexName;

import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = QueryStringIndexReader.class)
public class QueryStringIndexReaderImpl implements QueryStringIndexReader {

	@Override
	public Optional<String> fetchIdOptional(
		QueryStringIndexName queryStringIndexName, long companyId, long groupId,
		String keywords) {

		if (Validator.isBlank(keywords)) {
			return Optional.empty();
		}

		Query query = _buildQuery(companyId, groupId, keywords);

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setFetchSource(true);
		searchSearchRequest.setFetchSourceIncludes(new String[] {"_id"});
		searchSearchRequest.setIndexNames(queryStringIndexName.getIndexName());
		searchSearchRequest.setPreferLocalCluster(false);
		searchSearchRequest.setQuery(query);
		searchSearchRequest.setSize(1);
		searchSearchRequest.setStart(0);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		if (searchHits.getTotalHits() == 0) {
			return Optional.empty();
		}

		List<SearchHit> hits = searchHits.getSearchHits();

		SearchHit searchHit = hits.get(0);

		return Optional.of(searchHit.getId());
	}

	@Override
	public Optional<QueryString> fetchQueryStringOptional(
		QueryStringIndexName queryStringIndexName, String id) {

		return _getDocumentOptional(
			queryStringIndexName, id
		).map(
			document -> translate(document, id)
		);
	}

	@Override
	public boolean isIndexExists(QueryStringIndexName queryStringIndexName) {
		IndicesExistsIndexRequest indicesExistsIndexRequest =
			new IndicesExistsIndexRequest(queryStringIndexName.getIndexName());

		indicesExistsIndexRequest.setPreferLocalCluster(false);

		IndicesExistsIndexResponse indicesExistsIndexResponse =
			_searchEngineAdapter.execute(indicesExistsIndexRequest);

		return indicesExistsIndexResponse.isExists();
	}

	@Reference(unbind = "-")
	protected void setSearchEngineAdapter(
		SearchEngineAdapter searchEngineAdapter) {

		_searchEngineAdapter = searchEngineAdapter;
	}

	protected QueryString translate(Document document, String id) {
		return _documentToQueryStringTranslator.translate(document, id);
	}

	private void _addCompanyFilter(BooleanQuery booleanQuery, long companyId) {
		booleanQuery.addFilterQueryClauses(
			_queries.term(QueryStringFields.COMPANY_ID, companyId));
	}

	private void _addGroupFilter(BooleanQuery booleanQuery, long groupId) {
		booleanQuery.addFilterQueryClauses(
			_queries.term(QueryStringFields.GROUP_ID, groupId));
	}

	private void _addSearchClause(BooleanQuery booleanQuery, String keywords) {
		booleanQuery.addMustQueryClauses(
			_queries.term(QueryStringFields.CONTENT_RAW, keywords));
	}

	private Query _buildQuery(long companyId, long groupId, String keywords) {
		BooleanQuery booleanQuery = _queries.booleanQuery();

		_addCompanyFilter(booleanQuery, companyId);

		_addGroupFilter(booleanQuery, groupId);

		_addSearchClause(booleanQuery, keywords);

		return booleanQuery;
	}

	private Optional<Document> _getDocumentOptional(
		QueryStringIndexName queryStringIndexName, String id) {

		if (Validator.isNull(id)) {
			return Optional.empty();
		}

		GetDocumentRequest getDocumentRequest = new GetDocumentRequest(
			queryStringIndexName.getIndexName(), id);

		getDocumentRequest.setFetchSource(true);
		getDocumentRequest.setPreferLocalCluster(false);

		GetDocumentResponse getDocumentResponse = _searchEngineAdapter.execute(
			getDocumentRequest);

		if (getDocumentResponse.isExists()) {
			return Optional.of(getDocumentResponse.getDocument());
		}

		return Optional.empty();
	}

	@Reference
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Reference
	private DocumentToQueryStringTranslator _documentToQueryStringTranslator;

	@Reference
	private Queries _queries;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}