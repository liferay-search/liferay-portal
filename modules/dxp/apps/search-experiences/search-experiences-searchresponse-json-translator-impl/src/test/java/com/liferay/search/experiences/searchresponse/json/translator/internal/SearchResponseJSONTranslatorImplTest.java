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

package com.liferay.search.experiences.searchresponse.json.translator.internal;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.AggregationResults;
import com.liferay.portal.search.internal.aggregation.AggregationResultsImpl;
import com.liferay.portal.search.internal.legacy.searcher.SearchResponseBuilderFactoryImpl;
import com.liferay.portal.search.legacy.searcher.SearchResponseBuilderFactory;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.search.experiences.searchresponse.json.translator.SearchResponseJSONTranslator;
import com.liferay.search.experiences.searchresponse.json.translator.internal.aggregations.AggregationJSONTranslatorsHolder;
import com.liferay.search.experiences.searchresponse.json.translator.internal.aggregations.AggregationsJSONTranslationContributor;
import com.liferay.search.experiences.searchresponse.json.translator.internal.hits.HitsJSONTranslationContributor;
import com.liferay.search.experiences.searchresponse.json.translator.internal.paging.PagingJSONTranslationContributor;
import com.liferay.search.experiences.searchresponse.json.translator.spi.contributor.JSONTranslationContributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author André de Oliveira
 */
public class SearchResponseJSONTranslatorImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testAllElements() {
		SearchResponseJSONTranslator searchResponseJSONTranslator =
			createSearchResponseJSONTranslator(
				createAggregationsJSONTranslationContributor(),
				createHitsJSONTranslationContributor(),
				createPagingJSONTranslationContributor());

		AggregationResult aggregationResult = aggregationResults.avg("foo", 42);

		Assert.assertEquals(
			"{\"hits\":[],\"pagination\":{},\"aggregations\":" +
				"{\"foo\":{\"name\":\"foo\",\"value\":42}}}",
			searchResponseJSONTranslator.translate(
				searchResponseBuilderFactory.builder(
					new SearchContext()
				).aggregationResultsMap(
					HashMapBuilder.put(
						"foo", aggregationResult
					).build()
				).build(),
				null, null, null, null, null));
	}

	@Test
	public void testEmpty() {
		SearchResponseJSONTranslator searchResponseJSONTranslator =
			createSearchResponseJSONTranslator(
				createAggregationsJSONTranslationContributor(),
				createHitsJSONTranslationContributor(),
				createPagingJSONTranslationContributor());

		Assert.assertEquals(
			"{\"hits\":[],\"pagination\":{}}",
			searchResponseJSONTranslator.translate(
				searchResponseBuilderFactory.builder(
					new SearchContext()
				).build(),
				null, null, null, null, null));
	}

	@Test
	public void testException() {
		JSONTranslationContributor jsonTranslationContributor = Mockito.mock(
			JSONTranslationContributor.class);

		RuntimeException runtimeException = new NullPointerException();

		Mockito.doThrow(
			runtimeException
		).when(
			jsonTranslationContributor
		).contribute(
			Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
			Mockito.any(), Mockito.any()
		);

		SearchResponseJSONTranslator searchResponseJSONTranslator =
			createSearchResponseJSONTranslator(jsonTranslationContributor);

		List<Exception> exceptions = new ArrayList<>();

		String jsonString = searchResponseJSONTranslator.translate(
			searchResponseBuilderFactory.builder(
				new SearchContext()
			).build(),
			null, null, null, exceptions::add, null);

		Assert.assertEquals("{}", jsonString);

		Assert.assertEquals(runtimeException, exceptions.get(0));
	}

	protected JSONTranslationContributor
		createAggregationsJSONTranslationContributor() {

		AggregationsJSONTranslationContributor
			aggregationsJSONTranslationContributor =
				new AggregationsJSONTranslationContributor();

		aggregationsJSONTranslationContributor.
			setAggregationJSONTranslatorsHolder(
				Mockito.mock(AggregationJSONTranslatorsHolder.class));

		aggregationsJSONTranslationContributor.setJSONFactory(
			JSONFactoryUtil.getJSONFactory());

		return aggregationsJSONTranslationContributor;
	}

	protected JSONTranslationContributor
		createHitsJSONTranslationContributor() {

		HitsJSONTranslationContributor hitsJSONTranslationContributor =
			new HitsJSONTranslationContributor();

		hitsJSONTranslationContributor.setJSONFactory(
			JSONFactoryUtil.getJSONFactory());

		return hitsJSONTranslationContributor;
	}

	protected JSONTranslationContributor
		createPagingJSONTranslationContributor() {

		PagingJSONTranslationContributor pagingJSONTranslationContributor =
			new PagingJSONTranslationContributor();

		pagingJSONTranslationContributor.setJSONFactory(
			JSONFactoryUtil.getJSONFactory());

		return pagingJSONTranslationContributor;
	}

	protected SearchResponseJSONTranslator createSearchResponseJSONTranslator(
		JSONTranslationContributor... jsonTranslationContributors) {

		SearchResponseJSONTranslatorImpl searchResponseJSONTranslatorImpl =
			new SearchResponseJSONTranslatorImpl();

		searchResponseJSONTranslatorImpl.setJSONFactory(
			JSONFactoryUtil.getJSONFactory());

		List<JSONTranslationContributor> list = Arrays.asList(
			jsonTranslationContributors);

		searchResponseJSONTranslatorImpl.setJSONTranslationContributors(list);

		return searchResponseJSONTranslatorImpl;
	}

	protected AggregationResults aggregationResults =
		new AggregationResultsImpl();
	protected SearchResponseBuilderFactory searchResponseBuilderFactory =
		new SearchResponseBuilderFactoryImpl();

}