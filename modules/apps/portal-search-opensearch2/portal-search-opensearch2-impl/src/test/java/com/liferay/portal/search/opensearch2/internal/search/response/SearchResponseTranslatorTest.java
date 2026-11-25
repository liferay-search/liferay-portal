/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.search.response;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.SimpleFacet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.opensearch2.internal.facet.FacetUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.HitsMetadata;

/**
 * @author Joshua Cords
 */
public class SearchResponseTranslatorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testUpdateFacetCollectorsWithFacetSampleSize() {
		Facet facet = _createFacet();

		_updateFacetCollectors(
			facet, RandomTestUtil.randomInt(1, 10000),
			_createSearchResponse(
				HashMapBuilder.put(
					FacetUtil.SAMPLER_AGGREGATION_NAME,
					_createSamplerAggregate()
				).build()));

		_assertTermCollector(facet);
	}

	@Test
	public void testUpdateFacetCollectorsWithFacetSampleSizeAndAggregationFiltering() {
		Facet facet = _createFacet();

		_updateFacetCollectors(
			facet, RandomTestUtil.randomInt(1, 10000),
			_createSearchResponse(
				HashMapBuilder.put(
					FacetUtil.SAMPLER_AGGREGATION_NAME,
					_createFilterAggregate(
						FacetUtil.SAMPLER_AGGREGATION_NAME,
						_createSamplerAggregate())
				).build()));

		_assertTermCollector(facet);
	}

	@Test
	public void testUpdateFacetCollectorsWithFacetSampleSizeAndFacetSelection() {
		Facet facet = _createFacet();

		_updateFacetCollectors(
			facet, RandomTestUtil.randomInt(1, 10000),
			_createSearchResponse(
				HashMapBuilder.put(
					_FIELD_NAME,
					_createFilterAggregate(
						_FIELD_NAME, _createSamplerAggregate())
				).build()));

		_assertTermCollector(facet);
	}

	@Test
	public void testUpdateFacetCollectorsWithoutFacetSampleSize() {
		Facet facet = _createFacet();

		_updateFacetCollectors(
			facet, null,
			_createSearchResponse(
				HashMapBuilder.put(
					_FIELD_NAME, _createTermsAggregate()
				).build()));

		_assertTermCollector(facet);
	}

	private void _assertTermCollector(Facet facet) {
		FacetCollector facetCollector = facet.getFacetCollector();

		TermCollector termCollector = facetCollector.getTermCollector(_TERM);

		Assert.assertEquals(3, termCollector.getFrequency());
	}

	private Facet _createFacet() {
		Facet facet = new SimpleFacet(new SearchContext());

		facet.setFieldName(_FIELD_NAME);

		return facet;
	}

	private Aggregate _createFilterAggregate(
		String name, Aggregate subaggregate) {

		return Aggregate.of(
			aggregate -> aggregate.filter(
				filterAggregate -> filterAggregate.aggregations(
					name, subaggregate
				).docCount(
					3
				)));
	}

	private Aggregate _createSamplerAggregate() {
		return Aggregate.of(
			aggregate -> aggregate.sampler(
				samplerAggregate -> samplerAggregate.aggregations(
					_FIELD_NAME, _createTermsAggregate()
				).docCount(
					3
				)));
	}

	private SearchResponse<JsonData> _createSearchResponse(
		Map<String, Aggregate> aggregations) {

		SearchResponse.Builder<JsonData> builder =
			new SearchResponse.Builder<>();

		builder.aggregations(aggregations);

		HitsMetadata.Builder<JsonData> hitsMetadataBuilder =
			new HitsMetadata.Builder<>();

		hitsMetadataBuilder.hits(Collections.emptyList());

		builder.hits(hitsMetadataBuilder.build());

		builder.shards(
			shardStatistics -> shardStatistics.failed(
				0
			).successful(
				1
			).total(
				1
			));
		builder.timedOut(false);
		builder.took(1);

		return builder.build();
	}

	private Aggregate _createTermsAggregate() {
		return Aggregate.of(
			aggregate -> aggregate.sterms(
				stringTermsAggregate -> stringTermsAggregate.buckets(
					buckets -> buckets.array(
						Collections.singletonList(
							StringTermsBucket.of(
								stringTermsBucket -> stringTermsBucket.docCount(
									3
								).key(
									_TERM
								))))
				).docCountErrorUpperBound(
					0L
				).sumOtherDocCount(
					0L
				)));
	}

	private void _updateFacetCollectors(
		Facet facet, Integer facetSampleSize,
		SearchResponse<JsonData> searchResponse) {

		ReflectionTestUtil.invoke(
			new SearchResponseTranslator(), "_updateFacetCollectors",
			new Class<?>[] {Map.class, Integer.class, SearchResponse.class},
			HashMapBuilder.put(
				_FIELD_NAME, facet
			).build(),
			facetSampleSize, searchResponse);
	}

	private static final String _FIELD_NAME = "assetTagNames";

	private static final String _TERM = "alpha";

}