/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.facet;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.SamplerAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch.core.SearchRequest;

import com.liferay.portal.json.JSONObjectImpl;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.facet.Facet;
import com.liferay.portal.search.internal.facet.FacetImpl;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Joshua Cords
 */
public class FacetTranslatorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testTranslateWithFacetSampleSize() {
		int facetSampleSize = RandomTestUtil.randomInt(1, 10000);

		Map<String, Aggregation> aggregations = _translate(
			true, facetSampleSize);

		Assert.assertEquals(aggregations.toString(), 1, aggregations.size());

		Aggregation aggregation = aggregations.get(
			FacetUtil.SAMPLER_AGGREGATION_NAME);

		Assert.assertTrue(aggregation.isSampler());

		SamplerAggregation samplerAggregation = aggregation.sampler();

		Assert.assertEquals(
			Integer.valueOf(facetSampleSize), samplerAggregation.shardSize());

		Map<String, Aggregation> facetAggregations = aggregation.aggregations();

		Assert.assertEquals(
			facetAggregations.toString(), 2, facetAggregations.size());

		Aggregation facetAggregation = facetAggregations.get(_TAG_FIELD_NAME);

		Assert.assertTrue(facetAggregation.isTerms());

		TermsAggregation termsAggregation = facetAggregation.terms();

		Assert.assertEquals(_TAG_FIELD_NAME, termsAggregation.field());
	}

	@Test
	public void testTranslateWithFacetSampleSizeAndAggregationFiltering() {
		Map<String, Aggregation> aggregations = _translate(
			false, RandomTestUtil.randomInt(1, 10000));

		Assert.assertEquals(aggregations.toString(), 2, aggregations.size());

		Aggregation aggregation = aggregations.get(_CATEGORY_FIELD_NAME);

		Assert.assertTrue(aggregation.isSampler());

		Map<String, Aggregation> samplerAggregations =
			aggregation.aggregations();

		Aggregation facetAggregation = samplerAggregations.get(
			_CATEGORY_FIELD_NAME);

		Assert.assertTrue(facetAggregation.isTerms());

		aggregation = aggregations.get(FacetUtil.SAMPLER_AGGREGATION_NAME);

		Assert.assertTrue(aggregation.isFilter());

		Map<String, Aggregation> filterAggregations =
			aggregation.aggregations();

		aggregation = filterAggregations.get(
			FacetUtil.SAMPLER_AGGREGATION_NAME);

		Assert.assertTrue(aggregation.isSampler());

		samplerAggregations = aggregation.aggregations();

		facetAggregation = samplerAggregations.get(_TAG_FIELD_NAME);

		Assert.assertTrue(facetAggregation.isTerms());
	}

	@Test
	public void testTranslateWithoutFacetSampleSize() {
		Map<String, Aggregation> aggregations = _translate(true, null);

		Assert.assertEquals(aggregations.toString(), 2, aggregations.size());
		Assert.assertNull(aggregations.get(FacetUtil.SAMPLER_AGGREGATION_NAME));

		Aggregation aggregation = aggregations.get(_TAG_FIELD_NAME);

		Assert.assertTrue(aggregation.isTerms());
	}

	private Facet _createFacet(String fieldName, String... selections) {
		Facet facet = new FacetImpl(fieldName, new SearchContext());

		FacetConfiguration facetConfiguration = facet.getFacetConfiguration();

		facetConfiguration.setDataJSONObject(new JSONObjectImpl());

		facet.select(selections);

		return facet;
	}

	private Map<String, Aggregation> _translate(
		boolean basicFacetSelection, Integer facetSampleSize) {

		SearchRequest.Builder searchRequestBuilder =
			new SearchRequest.Builder();

		new FacetTranslator(
		).translate(
			basicFacetSelection, facetSampleSize,
			HashMapBuilder.
				<String, com.liferay.portal.kernel.search.facet.Facet>put(
					_CATEGORY_FIELD_NAME,
					_createFacet(_CATEGORY_FIELD_NAME, "1", "2")
				).put(
					_TAG_FIELD_NAME, _createFacet(_TAG_FIELD_NAME)
				).build(),
			null, searchRequestBuilder
		);

		SearchRequest searchRequest = searchRequestBuilder.build();

		return searchRequest.aggregations();
	}

	private static final String _CATEGORY_FIELD_NAME = "assetCategoryIds";

	private static final String _TAG_FIELD_NAME = "assetTagNames";

}