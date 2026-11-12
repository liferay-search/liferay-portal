/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.search.response;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.InnerHitsResult;
import co.elastic.clients.elasticsearch.core.search.ResponseBody;
import co.elastic.clients.json.JsonData;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.SimpleFacet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.search.elasticsearch8.internal.facet.FacetUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.portal.util.LocalizationImpl;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Prathima Shreenath
 */
public class SearchResponseTranslatorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		LocalizationUtil localizationUtil = new LocalizationUtil();

		localizationUtil.setLocalization(new LocalizationImpl());
	}

	@Test
	public void testAddSnippetsConcatenatesDistinctInnerHitFragments() {
		String fieldName = RandomTestUtil.randomString();

		Document document = _addSnippets(
			_createHit(
				fieldName, Collections.singletonList("alpha"),
				_createInnerHitsResult(
					_createHit(
						fieldName, Collections.singletonList("gamma"), null))));

		Assert.assertEquals(
			"alpha...gamma", document.get("snippet_" + fieldName));
	}

	@Test
	public void testAddSnippetsDoesNotDuplicateBaseAndLocalizedField() {
		Hit.Builder<JsonData> builder = new Hit.Builder<>();

		String fieldName = RandomTestUtil.randomString();
		List<String> fragments = Collections.singletonList(
			"<liferay-hl>alpha</liferay-hl> beta");

		builder.highlight(fieldName, fragments);

		String localizedFieldName = Field.getLocalizedName(
			LocaleUtil.US, fieldName);

		builder.highlight(localizedFieldName, fragments);

		builder.index("0");

		Document document = _addSnippets(builder.build());

		Assert.assertEquals(
			"<liferay-hl>alpha</liferay-hl> beta",
			document.get("snippet_" + localizedFieldName));
	}

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

	private Document _addSnippets(Hit<JsonData> hit) {
		Document document = new DocumentImpl();

		ReflectionTestUtil.invoke(
			new SearchResponseTranslator(), "_addSnippets",
			new Class<?>[] {Document.class, Hit.class, Locale.class}, document,
			hit, LocaleUtil.US);

		return document;
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
				filterAggregate -> filterAggregate.docCount(
					3
				).aggregations(
					name, subaggregate
				)));
	}

	private Hit<JsonData> _createHit(
		String fieldName, List<String> fragments,
		InnerHitsResult innerHitsResult) {

		Hit.Builder<JsonData> builder = new Hit.Builder<>();

		builder.highlight(fieldName, fragments);
		builder.index("0");

		if (innerHitsResult != null) {
			builder.innerHits(RandomTestUtil.randomString(), innerHitsResult);
		}

		return builder.build();
	}

	private InnerHitsResult _createInnerHitsResult(Hit<JsonData> hit) {
		HitsMetadata.Builder<JsonData> hitsMetadataBuilder =
			new HitsMetadata.Builder<>();

		hitsMetadataBuilder.hits(Collections.singletonList(hit));

		InnerHitsResult.Builder builder = new InnerHitsResult.Builder();

		builder.hits(hitsMetadataBuilder.build());

		return builder.build();
	}

	private Aggregate _createSamplerAggregate() {
		return Aggregate.of(
			aggregate -> aggregate.sampler(
				samplerAggregate -> samplerAggregate.docCount(
					3
				).aggregations(
					_FIELD_NAME, _createTermsAggregate()
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
								stringTermsBucket -> stringTermsBucket.key(
									FieldValue.of(_TERM)
								).docCount(
									3
								)))))));
	}

	private void _updateFacetCollectors(
		Facet facet, Integer facetSampleSize,
		SearchResponse<JsonData> searchResponse) {

		ReflectionTestUtil.invoke(
			new SearchResponseTranslator(), "_updateFacetCollectors",
			new Class<?>[] {Integer.class, Map.class, ResponseBody.class},
			facetSampleSize,
			HashMapBuilder.put(
				_FIELD_NAME, facet
			).build(),
			searchResponse);
	}

	private static final String _FIELD_NAME = "assetTagNames";

	private static final String _TERM = "alpha";

}