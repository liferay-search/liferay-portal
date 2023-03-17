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

package com.liferay.portal.search.elasticsearch7.internal;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BaseIndexSearcher;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.HitsImpl;
import com.liferay.portal.kernel.search.IndexSearcher;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.suggest.QuerySuggester;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.Aggregation;
import com.liferay.portal.search.aggregation.pipeline.PipelineAggregation;
import com.liferay.portal.search.constants.SearchContextAttributes;
import com.liferay.portal.search.elasticsearch7.constants.ElasticsearchSearchContextAttributes;
import com.liferay.portal.search.elasticsearch7.internal.configuration.ElasticsearchConfigurationWrapper;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.BaseSearchRequest;
import com.liferay.portal.search.engine.adapter.search.BaseSearchResponse;
import com.liferay.portal.search.engine.adapter.search.ClosePointInTimeRequest;
import com.liferay.portal.search.engine.adapter.search.CountSearchRequest;
import com.liferay.portal.search.engine.adapter.search.CountSearchResponse;
import com.liferay.portal.search.engine.adapter.search.OpenPointInTimeRequest;
import com.liferay.portal.search.engine.adapter.search.OpenPointInTimeResponse;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.legacy.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.legacy.searcher.SearchResponseBuilderFactory;
import com.liferay.portal.search.pit.PointInTime;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchResponseBuilder;
import com.liferay.portal.search.sort.Sorts;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 * @author Milen Dyankov
 */
@Component(
	property = "search.engine.impl=Elasticsearch", service = IndexSearcher.class
)
public class ElasticsearchIndexSearcher extends BaseIndexSearcher {

	@Override
	public String getQueryString(SearchContext searchContext, Query query) {
		return _searchEngineAdapter.getQueryString(query);
	}

	@Override
	public Hits search(SearchContext searchContext, Query query) {
		StopWatch stopWatch = new StopWatch();

		stopWatch.start();

		try {
			int end = searchContext.getEnd();
			int start = searchContext.getStart();

			SearchRequest searchRequest = _getSearchRequest(searchContext);

			Integer from = searchRequest.getFrom();
			Integer size = searchRequest.getSize();

			if ((from == null) && (size != null)) {
				end = size;
				start = 0;
			}
			else if ((from != null) && (size != null)) {
				end = from + size;
				start = from;
			}

			if (start == QueryUtil.ALL_POS) {
				start = 0;
			}
			else if (start < 0) {
				throw new IllegalArgumentException("Invalid start " + start);
			}

			if (end == QueryUtil.ALL_POS) {
				end = GetterUtil.getInteger(
					_props.get(PropsKeys.INDEX_SEARCH_LIMIT));
			}
			else if (end < 0) {
				throw new IllegalArgumentException("Invalid end " + end);
			}

			SearchSearchRequest searchSearchRequest = createSearchSearchRequest(
				searchRequest, searchContext, query);

			PointInTime pointInTime = null;

			SearchSearchResponse searchSearchResponse = null;//why did we get rid of _getSearchResponseBuilder(), presumably because we only need to generated it once

			int maxWindow = 10000;//should be configurable from system settings

			if (end > maxWindow) {//we should order the searches from earliest results to last - isn't this necessary anyways for compiling hits and dealing with searchAfter?
				int maxWindowPages = end / maxWindow;//is this going to round up always? Yes, so 20020 / 10000 = 2 maxWindowPages
				int searchAfterStart = 9999;//maxWindow - 1
				end = end % maxWindow; //gets the remainder, 20020 % 10000 = 20

				pointInTime = _createPointInTime(searchContext, searchRequest);
				searchSearchRequest.setPointInTime(pointInTime);//we need to use PiT in our SearchSearchRequest

				size = 1; //we're jumping to the last result - 10000. and then continuing to search after that. This causes problems for accuracy though - some of which we have already though
				start = start % maxWindow; //do we need to update the same start/end, so if start is 20, start stays 20
//if searching for the last page, could be just reverse the sorts??
				for (int i = 0; i < maxWindowPages; i++) {//for each windowPage
					setStartAndSize( //don't think the method is worth it, just bring the code up
						searchSearchRequest, searchAfterStart, size);//why is our first search start 9999 and size 1? - shouldn't it be start = start and size = maxWindow

					searchSearchResponse = _searchEngineAdapter.execute(
						searchSearchRequest);//we should only save a searchSearchResponse on the last search - except we need the last hit

					_searchAfter(searchSearchRequest, searchSearchResponse);

					size = maxWindow;
					searchAfterStart = 0;//and each following search start = 0? - we need to grab all results, see https://github.com/elastic/elasticsearch/issues/28068#issuecomment-375660535
				}//maybe we could reduce the document size by modifying stored fields?
			}

			if (start != 0) {//what if start was 20, it grabs 0-19. But why are we searching results here and then later? I guess we want to search everything between the large window and the last window
				size = start - 1;

				setStartAndSize(searchSearchRequest, 0, size);

				searchSearchResponse = _searchEngineAdapter.execute(//won't this overwrite the previous searchSearchResponse? - yes
					searchSearchRequest);

				_searchAfter(searchSearchRequest, searchSearchResponse);
			}

			size = end - start;//in the case of 20 to 200020, start is still 20, end is now 20,size is now 0? This is wrong

			setStartAndSize(searchSearchRequest, 0, size);

			searchSearchResponse = _searchEngineAdapter.execute(//this looks like it wants to be the last search, searching for documents between the start and maxWindow
				searchSearchRequest);

			_populateResponse(searchSearchResponse, searchContext);

			if (pointInTime != null) {
				_closePointInTime(pointInTime);
			}

			Hits hits = searchSearchResponse.getHits();//are we getting ALL the hits in the correct order? - we're just getting the last hits

			hits.setStart(stopWatch.getStartTime());

			return hits;
		}
		catch (RuntimeException runtimeException) {
			if (!handle(runtimeException)) {
				if (_elasticsearchConfigurationWrapper.logExceptionsOnly()) {
					_log.error(runtimeException);
				}
				else {
					throw runtimeException;
				}
			}

			searchContext.setAttribute(
				"search.exception.message",
				_getExceptionMessage(runtimeException));

			return new HitsImpl();
		}
		finally {
			if (_log.isInfoEnabled()) {
				stopWatch.stop();

				_log.info(
					StringBundler.concat(
						"Searching took ", stopWatch.getTime(), " ms"));
			}
		}
	}

	@Override
	public long searchCount(SearchContext searchContext, Query query) {
		StopWatch stopWatch = new StopWatch();

		stopWatch.start();

		try {
			CountSearchRequest countSearchRequest = _createCountSearchRequest(
				searchContext, query);

			CountSearchResponse countSearchResponse =
				_searchEngineAdapter.execute(countSearchRequest);

			if (_log.isInfoEnabled()) {
				_log.info(
					StringBundler.concat(
						"The search engine processed ",
						countSearchResponse.getSearchRequestString(), " in ",
						countSearchResponse.getExecutionTime(), " ms"));
			}

			_populateResponse(
				countSearchResponse, _getSearchResponseBuilder(searchContext));

			return countSearchResponse.getCount();
		}
		catch (RuntimeException runtimeException) {
			if (!handle(runtimeException)) {
				if (_elasticsearchConfigurationWrapper.logExceptionsOnly()) {
					_log.error(runtimeException);
				}
				else {
					throw runtimeException;
				}
			}

			return 0;
		}
		finally {
			if (_log.isInfoEnabled()) {
				stopWatch.stop();

				_log.info(
					StringBundler.concat(
						"Searching took ", stopWatch.getTime(), " ms"));
			}
		}
	}

	protected SearchSearchRequest createSearchSearchRequest(
		SearchRequest searchRequest, SearchContext searchContext, Query query) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		_prepare(searchSearchRequest, searchRequest, query, searchContext);

		QueryConfig queryConfig = searchContext.getQueryConfig();

		searchSearchRequest.setAlternateUidFieldName(
			queryConfig.getAlternateUidFieldName());

		searchSearchRequest.setBasicFacetSelection(
			searchRequest.isBasicFacetSelection());

		searchSearchRequest.putAllFacets(searchContext.getFacets());

		searchSearchRequest.setFetchSource(searchRequest.getFetchSource());
		searchSearchRequest.setFetchSourceExcludes(
			searchRequest.getFetchSourceExcludes());
		searchSearchRequest.setFetchSourceIncludes(
			searchRequest.getFetchSourceIncludes());
		searchSearchRequest.setGroupBy(searchContext.getGroupBy());
		searchSearchRequest.setGroupByRequests(
			searchRequest.getGroupByRequests());
		searchSearchRequest.setHighlightEnabled(
			queryConfig.isHighlightEnabled());

		searchSearchRequest.setHighlightFieldNames(
			queryConfig.getHighlightFieldNames());
		searchSearchRequest.setHighlightFragmentSize(
			queryConfig.getHighlightFragmentSize());
		searchSearchRequest.setHighlightSnippetSize(
			queryConfig.getHighlightSnippetSize());
		searchSearchRequest.setLocale(queryConfig.getLocale());
		searchSearchRequest.setHighlightRequireFieldMatch(
			queryConfig.isHighlightRequireFieldMatch());
		searchSearchRequest.setLuceneSyntax(
			GetterUtil.getBoolean(
				searchContext.getAttribute(
					SearchContextAttributes.ATTRIBUTE_KEY_LUCENE_SYNTAX)));

		String preference = (String)searchContext.getAttribute(
			ElasticsearchSearchContextAttributes.
				ATTRIBUTE_KEY_SEARCH_REQUEST_PREFERENCE);

		if (!Validator.isBlank(preference)) {
			searchSearchRequest.setPreference(preference);
		}

		searchSearchRequest.setScoreEnabled(queryConfig.isScoreEnabled());
		searchSearchRequest.setSelectedFieldNames(
			queryConfig.getSelectedFieldNames());

		Sort[] sortsKernel = searchContext.getSorts();
		List<com.liferay.portal.search.sort.Sort> sortsSearch =
			searchRequest.getSorts();

		searchSearchRequest.setSorts(sortsKernel);//no reason to add these if they're null or empty right? looks like this could be an else with the below if
		searchSearchRequest.setSorts(sortsSearch);

		if ((sortsKernel == null) && sortsSearch.isEmpty()) {
			searchSearchRequest.addSorts(_sorts.field("_scores"));
			searchSearchRequest.addSorts(_sorts.field("modified_sortable"));//this tiebreaker shouldn't be necessary
		}//	https://www.elastic.co/guide/en/elasticsearch/reference/current/paginate-search-results.html	All PIT search requests add an implicit sort tiebreaker field called _shard_doc, which can also be provided explicitly. If you cannot use a PIT, we recommend that you include a tiebreaker field in your sort. This tiebreaker field should contain a unique value for each document. If you don’t include a tiebreaker field, your paged results could miss or duplicate hits.

		searchSearchRequest.setStats(searchContext.getStats());

		return searchSearchRequest;
	}

	@Override
	protected QuerySuggester getQuerySuggester() {
		return _querySuggester;
	}

	protected boolean handle(Exception exception) {
		Throwable throwable = exception.getCause();

		if (throwable == null) {
			return false;
		}

		String message = throwable.getMessage();

		if (message == null) {
			return false;
		}

		if (message.contains(
				"Text fields are not optimised for operations that require " +
					"per-document field data")) {

			_log.error(
				"Unable to aggregate facet on a nonkeyword field", exception);

			return true;
		}

		return false;
	}

	protected void setIndexNames(
		BaseSearchRequest baseSearchRequest, SearchRequest searchRequest,
		SearchContext searchContext) {

		baseSearchRequest.setIndexNames(
			_getIndexes(searchRequest, searchContext));
	}

	protected void setQuery(
		BaseSearchRequest baseSearchRequest, SearchRequest searchRequest) {

		baseSearchRequest.setQuery(searchRequest.getQuery());
	}

	protected void setStartAndSize(
		SearchSearchRequest searchSearchRequest, int start, Integer size) {

		searchSearchRequest.setStart(start);
		searchSearchRequest.setSize(size);
	}

	private void _closePointInTime(PointInTime pointInTime) {
		ClosePointInTimeRequest closePointInTimeRequest =
			new ClosePointInTimeRequest(pointInTime.getPitId());

		_searchEngineAdapter.execute(closePointInTimeRequest);
	}

	private CountSearchRequest _createCountSearchRequest(
		SearchContext searchContext, Query query) {

		CountSearchRequest countSearchRequest = new CountSearchRequest();

		_prepare(
			countSearchRequest, _getSearchRequest(searchContext), query,
			searchContext);

		return countSearchRequest;
	}

	private PointInTime _createPointInTime(
		SearchContext searchContext, SearchRequest searchRequest) {

		OpenPointInTimeRequest openPointInTimeRequest =
			new OpenPointInTimeRequest();

		openPointInTimeRequest.setIndices(
			_getIndexes(searchRequest, searchContext));
		openPointInTimeRequest.setKeepAliveMinutes(1);

		OpenPointInTimeResponse openPointInTimeResponse =
			_searchEngineAdapter.execute(openPointInTimeRequest);

		PointInTime pointInTime = new PointInTime(
			openPointInTimeResponse.pitId());

		pointInTime.setKeepAlive("1m");//this should be configurable

		return pointInTime;
	}

	private String _getExceptionMessage(RuntimeException runtimeException) {
		String message = runtimeException.toString();

		for (Throwable throwable : runtimeException.getSuppressed()) {
			message = message.concat("\nSuppressed: " + throwable.getMessage());
		}

		return message;
	}

	private String[] _getIndexes(
		SearchRequest searchRequest, SearchContext searchContext) {

		List<String> indexes = searchRequest.getIndexes();

		if (!indexes.isEmpty()) {
			return indexes.toArray(new String[0]);
		}

		String indexName = _indexNameBuilder.getIndexName(
			searchContext.getCompanyId());

		return new String[] {indexName};
	}

	private SearchRequest _getSearchRequest(SearchContext searchContext) {
		SearchRequestBuilder searchRequestBuilder = _getSearchRequestBuilder(
			searchContext);

		return searchRequestBuilder.build();
	}

	private SearchRequestBuilder _getSearchRequestBuilder(
		SearchContext searchContext) {

		return _searchRequestBuilderFactory.builder(searchContext);
	}

	private SearchResponseBuilder _getSearchResponseBuilder(
		SearchContext searchContext) {

		return _searchResponseBuilderFactory.builder(searchContext);
	}

	private void _populateResponse(
		BaseSearchResponse baseSearchResponse,
		SearchResponseBuilder searchResponseBuilder) {

		searchResponseBuilder.aggregationResultsMap(
			baseSearchResponse.getAggregationResultsMap()
		).count(
			baseSearchResponse.getCount()
		).requestString(
			baseSearchResponse.getSearchRequestString()
		).responseString(
			baseSearchResponse.getSearchResponseString()
		).statsResponseMap(
			baseSearchResponse.getStatsResponseMap()
		).searchTimeValue(
			baseSearchResponse.getSearchTimeValue()
		);
	}

	private void _populateResponse(
		SearchSearchResponse searchSearchResponse,
		SearchContext searchContext) {

		SearchResponseBuilder searchResponseBuilder = _getSearchResponseBuilder(
			searchContext);

		_populateResponse(
			(BaseSearchResponse)searchSearchResponse, searchResponseBuilder);

		searchResponseBuilder.groupByResponses(
			searchSearchResponse.getGroupByResponses());
	}

	private void _prepare(
		BaseSearchRequest baseSearchRequest, SearchRequest searchRequest,
		Query query, SearchContext searchContext) {

		baseSearchRequest.addComplexQueryParts(
			searchRequest.getComplexQueryParts());
		baseSearchRequest.setExplain(searchRequest.isExplain());
		baseSearchRequest.setHighlight(searchRequest.getHighlight());
		baseSearchRequest.setIncludeResponseString(
			searchRequest.isIncludeResponseString());
		baseSearchRequest.setPostFilterQuery(
			searchRequest.getPostFilterQuery());
		baseSearchRequest.addPostFilterComplexQueryParts(
			searchRequest.getPostFilterComplexQueryParts());
		baseSearchRequest.setRescores(searchRequest.getRescores());
		baseSearchRequest.setStatsRequests(searchRequest.getStatsRequests());
		baseSearchRequest.setTrackTotalHits(
			_elasticsearchConfigurationWrapper.trackTotalHits());

		_setAggregations(baseSearchRequest, searchRequest);
		_setConnectionId(baseSearchRequest, searchRequest);
		setIndexNames(baseSearchRequest, searchRequest, searchContext);
		_setLegacyQuery(baseSearchRequest, query);
		_setLegacyPostFilter(baseSearchRequest, query);
		_setPipelineAggregations(baseSearchRequest, searchRequest);
		setQuery(baseSearchRequest, searchRequest);
	}

	private void _searchAfter(//rename - _searchAfterStoreLastResult
		SearchSearchRequest searchSearchRequest,
		SearchSearchResponse searchSearchResponse) {

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		List<SearchHit> searchHitList = searchHits.getSearchHits();

		SearchHit lastSearchHit = searchHitList.get(searchHitList.size() - 1);

		searchSearchRequest.setSearchAfter(lastSearchHit.getSortValues());
	}

	private void _setAggregations(
		BaseSearchRequest baseSearchRequest, SearchRequest searchRequest) {

		Map<String, Aggregation> map = searchRequest.getAggregationsMap();

		for (Aggregation aggregation : map.values()) {
			baseSearchRequest.addAggregation(aggregation);
		}
	}

	private void _setConnectionId(
		BaseSearchRequest baseSearchRequest, SearchRequest searchRequest) {

		baseSearchRequest.setConnectionId(searchRequest.getConnectionId());
	}

	private void _setLegacyPostFilter(
		BaseSearchRequest baseSearchRequest, Query query) {

		if (query != null) {
			baseSearchRequest.setPostFilter(query.getPostFilter());
		}
	}

	private void _setLegacyQuery(
		BaseSearchRequest baseSearchRequest, Query query) {

		baseSearchRequest.setQuery(query);
	}

	private void _setPipelineAggregations(
		BaseSearchRequest baseSearchRequest, SearchRequest searchRequest) {

		Map<String, PipelineAggregation> map =
			searchRequest.getPipelineAggregationsMap();

		for (PipelineAggregation aggregation : map.values()) {
			baseSearchRequest.addPipelineAggregation(aggregation);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ElasticsearchIndexSearcher.class);

	@Reference
	private volatile ElasticsearchConfigurationWrapper
		_elasticsearchConfigurationWrapper;

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private Props _props;

	@Reference(target = "(search.engine.impl=Elasticsearch)")
	private QuerySuggester _querySuggester;

	@Reference(target = "(search.engine.impl=Elasticsearch)")
	private SearchEngineAdapter _searchEngineAdapter;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Reference
	private SearchResponseBuilderFactory _searchResponseBuilderFactory;

	@Reference
	private Sorts _sorts;

}