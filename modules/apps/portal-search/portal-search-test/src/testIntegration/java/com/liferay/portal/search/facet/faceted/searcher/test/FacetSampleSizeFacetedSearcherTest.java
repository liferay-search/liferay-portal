/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.facet.faceted.searcher.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.facet.Facet;
import com.liferay.portal.search.facet.tag.AssetTagNamesFacetFactory;
import com.liferay.portal.search.facet.type.AssetEntriesFacetFactory;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.legacy.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Joshua Cords
 */
@RunWith(Arquillian.class)
@Sync
public class FacetSampleSizeFacetedSearcherTest
	extends BaseFacetedSearcherTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_group = userSearchFixture.addGroup();
		_keyword = RandomTestUtil.randomString();
		_tag = RandomTestUtil.randomString();

		for (int i = 0; i < _USERS_COUNT; i++) {
			addUser(_group, _keyword, _tag);
		}
	}

	@Test
	public void testFacetSampleSize() throws Exception {
		_assertSampleSizeLimitsFrequency(_USERS_COUNT - 2, _USERS_COUNT - 2);
		_assertSampleSizeLimitsFrequency(_USERS_COUNT, _USERS_COUNT + 1);
		_assertSampleSizeLimitsFrequency(_USERS_COUNT, _USERS_COUNT);
		_assertSampleSizeLimitsFrequency(_USERS_COUNT, null);
	}

	@Test
	public void testFacetSampleSizeWithAggregation() throws Exception {
		SearchContext searchContext = getSearchContext(_keyword);

		Facet facet = _assetTagNamesFacetFactory.newInstance(searchContext);

		SearchRequestBuilder searchRequestBuilder = _getSearchRequestBuilder(
			facet, _USERS_COUNT - 2, searchContext);

		String aggregationName = RandomTestUtil.randomString();

		searchRequestBuilder.addAggregation(
			_aggregations.terms(
				aggregationName, Field.ASSET_TAG_NAMES + ".raw"));

		SearchResponse searchResponse = _searcher.search(
			searchRequestBuilder.build());

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)searchResponse.getAggregationResult(
				aggregationName);

		Bucket bucket = termsAggregationResult.getBucket(_tag);

		Assert.assertEquals(_USERS_COUNT, bucket.getDocCount());

		_assertFrequency(_USERS_COUNT - 2, facet, _tag);
	}

	@Test
	public void testFacetSampleSizeWithEmptySearch() throws Exception {
		SearchContext searchContext = getSearchContext(null);

		Facet facet = _assetTagNamesFacetFactory.newInstance(searchContext);

		SearchRequestBuilder searchRequestBuilder = _getSearchRequestBuilder(
			facet, _USERS_COUNT - 2, searchContext);

		searchRequestBuilder.emptySearchEnabled(
			true
		).modelIndexerClasses(
			User.class
		);

		_searcher.search(searchRequestBuilder.build());

		FacetCollector facetCollector = facet.getFacetCollector();

		TermCollector termCollector = facetCollector.getTermCollector(_tag);

		Assert.assertTrue(termCollector.getFrequency() <= (_USERS_COUNT - 2));
	}

	@Test
	public void testFacetSampleSizeWithFacetSelection() throws Exception {
		for (int i = 0; i < _USERS_COUNT; i++) {
			addUser(
				_group, _keyword,
				_keyword + StringPool.SPACE + RandomTestUtil.randomString(),
				_keyword + StringPool.SPACE + RandomTestUtil.randomString());
		}

		SearchContext searchContext = getSearchContext(_keyword);

		Facet facet = _assetTagNamesFacetFactory.newInstance(searchContext);

		facet.select(RandomTestUtil.randomString());

		SearchRequestBuilder searchRequestBuilder = _getSearchRequestBuilder(
			facet, _USERS_COUNT - 2, searchContext);

		Facet assetEntriesFacet = _assetEntriesFacetFactory.newInstance(
			searchContext);

		searchRequestBuilder.withFacetContext(
			facetContext -> facetContext.addFacet(assetEntriesFacet));

		SearchResponse searchResponse = _searcher.search(
			searchRequestBuilder.build());

		SearchHits searchHits = searchResponse.getSearchHits();

		Assert.assertEquals(0, searchHits.getTotalHits());

		facet.select(_tag);

		searchResponse = _searcher.search(searchRequestBuilder.build());

		searchHits = searchResponse.getSearchHits();

		Assert.assertEquals(_USERS_COUNT, searchHits.getTotalHits());

		_assertFrequency(
			_USERS_COUNT - 2, assetEntriesFacet, User.class.getName());
	}

	@Test
	public void testFacetSampleSizeWithFederatedSearchRequest()
		throws Exception {

		SearchContext federatedSearchContext = getSearchContext(_keyword);

		Facet federatedFacet = _assetTagNamesFacetFactory.newInstance(
			federatedSearchContext);

		SearchRequestBuilder federatedSearchRequestBuilder =
			_getSearchRequestBuilder(
				federatedFacet, _USERS_COUNT - 2, federatedSearchContext);

		federatedSearchRequestBuilder.federatedSearchKey(
			RandomTestUtil.randomString());

		SearchContext searchContext = getSearchContext(_keyword);

		Facet facet = _assetTagNamesFacetFactory.newInstance(searchContext);

		SearchRequestBuilder searchRequestBuilder = _getSearchRequestBuilder(
			facet, null, searchContext);

		searchRequestBuilder.addFederatedSearchRequest(
			federatedSearchRequestBuilder.build());

		_searcher.search(searchRequestBuilder.build());

		_assertFrequency(_USERS_COUNT - 2, federatedFacet, _tag);
		_assertFrequency(_USERS_COUNT, facet, _tag);
	}

	private void _assertFrequency(
		int expectedFrequency, Facet facet, String term) {

		FacetCollector facetCollector = facet.getFacetCollector();

		TermCollector termCollector = facetCollector.getTermCollector(term);

		Assert.assertEquals(expectedFrequency, termCollector.getFrequency());
	}

	private void _assertSampleSizeLimitsFrequency(
			int expectedFrequency, Integer facetSampleSize)
		throws Exception {

		SearchContext searchContext = getSearchContext(_keyword);

		Facet facet = _assetTagNamesFacetFactory.newInstance(searchContext);

		SearchRequestBuilder searchRequestBuilder = _getSearchRequestBuilder(
			facet, facetSampleSize, searchContext);

		SearchResponse searchResponse = _searcher.search(
			searchRequestBuilder.build());

		SearchHits searchHits = searchResponse.getSearchHits();

		Assert.assertEquals(_USERS_COUNT, searchHits.getTotalHits());

		_assertFrequency(expectedFrequency, facet, _tag);
	}

	private SearchRequestBuilder _getSearchRequestBuilder(
		Facet facet, Integer facetSampleSize, SearchContext searchContext) {

		return _searchRequestBuilderFactory.builder(
			searchContext
		).facetSampleSize(
			facetSampleSize
		).withFacetContext(
			facetContext -> facetContext.addFacet(facet)
		);
	}

	private static final int _USERS_COUNT = 5;

	@Inject
	private Aggregations _aggregations;

	@Inject
	private AssetEntriesFacetFactory _assetEntriesFacetFactory;

	@Inject
	private AssetTagNamesFacetFactory _assetTagNamesFacetFactory;

	private Group _group;
	private String _keyword;

	@Inject
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Inject
	private Searcher _searcher;

	private String _tag;

}