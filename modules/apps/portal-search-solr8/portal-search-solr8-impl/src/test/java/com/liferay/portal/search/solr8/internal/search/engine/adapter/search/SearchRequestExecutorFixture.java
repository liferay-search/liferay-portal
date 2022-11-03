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

package com.liferay.portal.search.solr8.internal.search.engine.adapter.search;

import com.liferay.portal.kernel.search.query.QueryTranslator;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.engine.adapter.search.SearchRequestExecutor;
import com.liferay.portal.search.internal.groupby.GroupByResponseFactoryImpl;
import com.liferay.portal.search.internal.hits.SearchHitBuilderFactoryImpl;
import com.liferay.portal.search.internal.hits.SearchHitsBuilderFactoryImpl;
import com.liferay.portal.search.internal.legacy.document.DocumentBuilderFactoryImpl;
import com.liferay.portal.search.internal.legacy.groupby.GroupByRequestFactoryImpl;
import com.liferay.portal.search.internal.legacy.stats.StatsRequestBuilderFactoryImpl;
import com.liferay.portal.search.internal.legacy.stats.StatsResultsTranslatorImpl;
import com.liferay.portal.search.internal.stats.StatsResponseBuilderFactoryImpl;
import com.liferay.portal.search.solr8.internal.connection.SolrClientManager;
import com.liferay.portal.search.solr8.internal.facet.FacetProcessor;
import com.liferay.portal.search.solr8.internal.filter.BooleanFilterTranslatorImpl;
import com.liferay.portal.search.solr8.internal.filter.DateRangeFilterTranslatorImpl;
import com.liferay.portal.search.solr8.internal.filter.DateRangeTermFilterTranslatorImpl;
import com.liferay.portal.search.solr8.internal.filter.ExistsFilterTranslatorImpl;
import com.liferay.portal.search.solr8.internal.filter.GeoBoundingBoxFilterTranslatorImpl;
import com.liferay.portal.search.solr8.internal.filter.GeoDistanceFilterTranslatorImpl;
import com.liferay.portal.search.solr8.internal.filter.GeoDistanceRangeFilterTranslatorImpl;
import com.liferay.portal.search.solr8.internal.filter.GeoPolygonFilterTranslatorImpl;
import com.liferay.portal.search.solr8.internal.filter.MissingFilterTranslatorImpl;
import com.liferay.portal.search.solr8.internal.filter.PrefixFilterTranslatorImpl;
import com.liferay.portal.search.solr8.internal.filter.QueryFilterTranslatorImpl;
import com.liferay.portal.search.solr8.internal.filter.RangeTermFilterTranslatorImpl;
import com.liferay.portal.search.solr8.internal.filter.SolrFilterTranslator;
import com.liferay.portal.search.solr8.internal.filter.TermFilterTranslatorImpl;
import com.liferay.portal.search.solr8.internal.filter.TermsFilterTranslatorImpl;
import com.liferay.portal.search.solr8.internal.groupby.DefaultGroupByTranslator;
import com.liferay.portal.search.solr8.internal.search.response.DefaultSearchSearchResponseAssemblerHelperImpl;
import com.liferay.portal.search.solr8.internal.search.response.SearchSearchResponseAssemblerHelper;
import com.liferay.portal.search.solr8.internal.sort.SolrSortFieldTranslator;
import com.liferay.portal.search.solr8.internal.stats.DefaultStatsTranslator;
import com.liferay.portal.search.solr8.internal.stats.StatsTranslator;

import org.apache.solr.client.solrj.SolrQuery;

/**
 * @author Bryan Engler
 */
public class SearchRequestExecutorFixture {

	public SearchRequestExecutor getSearchRequestExecutor() {
		return _searchRequestExecutor;
	}

	public void setUp() {
		_searchRequestExecutor = createSearchRequestExecutor(
			_solrClientManager, _facetProcessor, _queryTranslator);
	}

	protected static BaseSearchResponseAssembler
		createBaseSearchResponseAssembler() {

		BaseSearchResponseAssemblerImpl baseSearchResponseAssembler =
			new BaseSearchResponseAssemblerImpl();

		ReflectionTestUtil.setFieldValue(baseSearchResponseAssembler, "_statsTranslator", createStatsTranslator());

		return baseSearchResponseAssembler;
	}

	protected static BaseSolrQueryAssembler createBaseSolrQueryAssembler(
		FacetProcessor<SolrQuery> facetProcessor,
		QueryTranslator<String> queryTranslator) {

		BaseSolrQueryAssemblerImpl baseSolrQueryAssembler =
			new BaseSolrQueryAssemblerImpl();

		ReflectionTestUtil.setFieldValue(baseSolrQueryAssembler, "_queryTranslator", queryTranslator);
		ReflectionTestUtil.setFieldValue(baseSolrQueryAssembler, "_statsTranslator", createStatsTranslator());
		ReflectionTestUtil.setFieldValue(baseSolrQueryAssembler, "_filterTranslator", createSolrFilterTranslator());
		ReflectionTestUtil.setFieldValue(baseSolrQueryAssembler, "_facetProcessor", facetProcessor);

		return baseSolrQueryAssembler;
	}

	protected static CountSearchRequestExecutor
		createCountSearchRequestExecutor(
			SolrClientManager solrClientManager,
			FacetProcessor<SolrQuery> facetProcessor,
			QueryTranslator<String> queryTranslator) {

		CountSearchRequestExecutorImpl countSearchRequestExecutor =
			new CountSearchRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(countSearchRequestExecutor, "_baseSearchResponseAssembler", createBaseSearchResponseAssembler());
		ReflectionTestUtil.setFieldValue(countSearchRequestExecutor, "_baseSolrQueryAssembler", createBaseSolrQueryAssembler(facetProcessor, queryTranslator));
		ReflectionTestUtil.setFieldValue(countSearchRequestExecutor, "_solrClientManager", solrClientManager);

		return countSearchRequestExecutor;
	}

	protected static SearchRequestExecutor createSearchRequestExecutor(
		SolrClientManager solrClientManager,
		FacetProcessor<SolrQuery> facetProcessor,
		QueryTranslator<String> queryTranslator) {

		SolrSearchRequestExecutor solrSearchRequestExecutor =
			new SolrSearchRequestExecutor();

		ReflectionTestUtil.setFieldValue(solrSearchRequestExecutor, "_countSearchRequestExecutor", createCountSearchRequestExecutor(
				solrClientManager, facetProcessor,
				queryTranslator));
		ReflectionTestUtil.setFieldValue(solrSearchRequestExecutor, "_multisearchSearchRequestExecutor", new MultisearchSearchRequestExecutorImpl());
		ReflectionTestUtil.setFieldValue(solrSearchRequestExecutor, "_searchSearchRequestExecutor", createSearchSearchRequestExecutor(
				solrClientManager, facetProcessor,
				queryTranslator));

		return solrSearchRequestExecutor;
	}

	protected static SearchSearchRequestExecutor
		createSearchSearchRequestExecutor(
			SolrClientManager solrClientManager,
			FacetProcessor<SolrQuery> facetProcessor,
			QueryTranslator<String> queryTranslator) {

		SearchSearchRequestExecutorImpl searchSearchRequestExecutor =
			new SearchSearchRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(searchSearchRequestExecutor, "_searchSearchResponseAssembler", createSearchSearchResponseAssembler());
		ReflectionTestUtil.setFieldValue(searchSearchRequestExecutor, "_searchSolrQueryAssembler", createSearchSolrQueryAssembler(
				facetProcessor, queryTranslator));
		ReflectionTestUtil.setFieldValue(searchSearchRequestExecutor, "_solrClientManager", solrClientManager);

		return searchSearchRequestExecutor;
	}

	protected static SearchSearchResponseAssembler
		createSearchSearchResponseAssembler() {

		SearchSearchResponseAssemblerImpl searchSearchResponseAssembler =
			new SearchSearchResponseAssemblerImpl();

		ReflectionTestUtil.setFieldValue(searchSearchResponseAssembler, "_baseSearchResponseAssembler", createBaseSearchResponseAssembler());
		ReflectionTestUtil.setFieldValue(searchSearchResponseAssembler, "_searchSearchResponseAssemblerHelper", createSearchSearchResponseAssemblerHelper());

		return searchSearchResponseAssembler;
	}

	protected static SearchSearchResponseAssemblerHelper
		createSearchSearchResponseAssemblerHelper() {

		DefaultSearchSearchResponseAssemblerHelperImpl
			defaultSearchSearchResponseAssemblerHelper =
			new DefaultSearchSearchResponseAssemblerHelperImpl();

		ReflectionTestUtil.setFieldValue(defaultSearchSearchResponseAssemblerHelper, "_documentBuilderFactory", new DocumentBuilderFactoryImpl());
		ReflectionTestUtil.setFieldValue(defaultSearchSearchResponseAssemblerHelper, "_groupByResponseFactory", new GroupByResponseFactoryImpl());
		ReflectionTestUtil.setFieldValue(defaultSearchSearchResponseAssemblerHelper, "_searchHitBuilderFactory", new SearchHitBuilderFactoryImpl());
		ReflectionTestUtil.setFieldValue(defaultSearchSearchResponseAssemblerHelper, "_searchHitsBuilderFactory", new SearchHitsBuilderFactoryImpl());
		ReflectionTestUtil.setFieldValue(defaultSearchSearchResponseAssemblerHelper, "_statsTranslator", createStatsTranslator());
		ReflectionTestUtil.setFieldValue(defaultSearchSearchResponseAssemblerHelper, "_statsResultsTranslator", new StatsResultsTranslatorImpl());

		return defaultSearchSearchResponseAssemblerHelper;
	}

	protected static SearchSolrQueryAssembler createSearchSolrQueryAssembler(
		FacetProcessor<SolrQuery> facetProcessor,
		QueryTranslator<String> queryTranslator) {

		SearchSolrQueryAssemblerImpl searchSolrQueryAssembler =
			new SearchSolrQueryAssemblerImpl();

		ReflectionTestUtil.setFieldValue(searchSolrQueryAssembler, "_baseSolrQueryAssembler", createBaseSolrQueryAssembler(
				facetProcessor, queryTranslator));
		ReflectionTestUtil.setFieldValue(searchSolrQueryAssembler, "_groupByRequestFactory", new GroupByRequestFactoryImpl());
		ReflectionTestUtil.setFieldValue(searchSolrQueryAssembler, "_groupByTranslator", new DefaultGroupByTranslator());
		ReflectionTestUtil.setFieldValue(searchSolrQueryAssembler, "_sortFieldTranslator", new SolrSortFieldTranslator());
		ReflectionTestUtil.setFieldValue(searchSolrQueryAssembler, "_statsRequestBuilderFactory", new StatsRequestBuilderFactoryImpl());
		ReflectionTestUtil.setFieldValue(searchSolrQueryAssembler, "_statsTranslator", createStatsTranslator());

		return searchSolrQueryAssembler;
	}

	protected static SolrFilterTranslator createSolrFilterTranslator() {
		SolrFilterTranslator solrFilterTranslator = new SolrFilterTranslator();

		ReflectionTestUtil.setFieldValue(solrFilterTranslator, "_rangeTermFilterTranslator", new RangeTermFilterTranslatorImpl());
		ReflectionTestUtil.setFieldValue(solrFilterTranslator, "_booleanQueryTranslator", new BooleanFilterTranslatorImpl());
		ReflectionTestUtil.setFieldValue(solrFilterTranslator, "_dateRangeTermFilterTranslator", new DateRangeTermFilterTranslatorImpl());
		ReflectionTestUtil.setFieldValue(solrFilterTranslator, "_existsFilterTranslator", new ExistsFilterTranslatorImpl());
		ReflectionTestUtil.setFieldValue(solrFilterTranslator, "_geoBoundingBoxFilterTranslator", new GeoBoundingBoxFilterTranslatorImpl());
		ReflectionTestUtil.setFieldValue(solrFilterTranslator, "_geoDistanceFilterTranslator", new GeoDistanceFilterTranslatorImpl());
		ReflectionTestUtil.setFieldValue(solrFilterTranslator, "_geoDistanceRangeFilterTranslator", new GeoDistanceRangeFilterTranslatorImpl());
		ReflectionTestUtil.setFieldValue(solrFilterTranslator, "_geoPolygonFilterTranslator", new GeoPolygonFilterTranslatorImpl());
		ReflectionTestUtil.setFieldValue(solrFilterTranslator, "_missingFilterTranslator", new MissingFilterTranslatorImpl());
		ReflectionTestUtil.setFieldValue(solrFilterTranslator, "_prefixFilterTranslator", new PrefixFilterTranslatorImpl());
		ReflectionTestUtil.setFieldValue(solrFilterTranslator, "_queryFilterTranslator", new QueryFilterTranslatorImpl());
		ReflectionTestUtil.setFieldValue(solrFilterTranslator, "_termFilterTranslator", new TermFilterTranslatorImpl());
		ReflectionTestUtil.setFieldValue(solrFilterTranslator, "_termsFilterTranslator", new TermsFilterTranslatorImpl());

		return solrFilterTranslator;
	}

	protected static StatsTranslator createStatsTranslator() {
		return new DefaultStatsTranslator() {
			{
				setStatsResponseBuilderFactory(
					new StatsResponseBuilderFactoryImpl());
			}
		};
	}

	protected void setFacetProcessor(FacetProcessor<SolrQuery> facetProcessor) {
		_facetProcessor = facetProcessor;
	}

	protected void setQueryTranslator(QueryTranslator<String> queryTranslator) {
		_queryTranslator = queryTranslator;
	}

	protected void setSolrClientManager(SolrClientManager solrClientManager) {
		_solrClientManager = solrClientManager;
	}

	private FacetProcessor<SolrQuery> _facetProcessor;
	private QueryTranslator<String> _queryTranslator;
	private SearchRequestExecutor _searchRequestExecutor;
	private SolrClientManager _solrClientManager;

}