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

package com.liferay.portal.search.tuning.blueprints.query.index.web.internal.request;

import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchContextFactory;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.sort.Sort;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexName;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryStringStatus;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryString;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringFields;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public class SearchQueryStringRequest {

	public SearchQueryStringRequest(
		QueryStringIndexName queryStringIndexName,
		HttpServletRequest httpServletRequest, Queries queries,
		SearchContainer<QueryString> searchContainer,
		SearchEngineAdapter searchEngineAdapter, Sorts sorts,
		QueryStringStatus status) {

		_queryStringIndexName = queryStringIndexName;
		_httpServletRequest = httpServletRequest;
		_queries = queries;
		_searchContainer = searchContainer;
		_searchEngineAdapter = searchEngineAdapter;
		_sorts = sorts;
		_status = status;

		_searchContext = SearchContextFactory.getInstance(httpServletRequest);
	}

	public SearchQueryStringResponse search() {
		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setFetchSource(true);
		searchSearchRequest.setIndexNames(_queryStringIndexName.getIndexName());
		searchSearchRequest.setPreferLocalCluster(false);
		searchSearchRequest.setQuery(_getQuery());
		searchSearchRequest.setSize(_searchContainer.getDelta());
		searchSearchRequest.setSorts(_getSorts());
		searchSearchRequest.setStart(_searchContainer.getStart());

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		SearchQueryStringResponse searchQueryStringResponse =
			new SearchQueryStringResponse();

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		searchQueryStringResponse.setSearchHits(searchHits);
		searchQueryStringResponse.setTotalHits((int)searchHits.getTotalHits());

		return searchQueryStringResponse;
	}

	private Query _getQuery() {
		String keywords = _searchContext.getKeywords();

		BooleanQuery booleanQuery = _queries.booleanQuery();

		if (!Validator.isBlank(keywords)) {
			booleanQuery.addMustQueryClauses(
				_queries.match(QueryStringFields.CONTENT, keywords));
		}
		else {
			booleanQuery.addMustQueryClauses(_queries.matchAll());
		}

		if (_status != null) {
			booleanQuery.addFilterQueryClauses(
				_queries.term(QueryStringFields.STATUS, _status.name()));
		}

		return booleanQuery;
	}

	private Collection<Sort> _getSorts() {
		String orderByCol = ParamUtil.getString(
			_httpServletRequest, "orderByCol", QueryStringFields.CREATED);
		String orderByType = ParamUtil.getString(
			_httpServletRequest, "orderByType", "desc");

		SortOrder sortOrder = SortOrder.ASC;

		if (Objects.equals(orderByType, "desc")) {
			sortOrder = SortOrder.DESC;
		}

		return Arrays.asList(_sorts.field(orderByCol, sortOrder));
	}

	private final HttpServletRequest _httpServletRequest;
	private final Queries _queries;
	private final QueryStringIndexName _queryStringIndexName;
	private final SearchContainer<QueryString> _searchContainer;
	private final SearchContext _searchContext;
	private final SearchEngineAdapter _searchEngineAdapter;
	private final Sorts _sorts;
	private final QueryStringStatus _status;

}