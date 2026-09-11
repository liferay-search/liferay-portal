/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.highlight;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchPhraseQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;

/**
 * @author Olivia Yu
 */
public class HighlightQueryFactoryTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testKeepsExactPhraseQuery() {
		Query query = _matchPhrase(null);

		Assert.assertSame(query, HighlightQueryFactory.create(query));
	}

	@Test
	public void testKeepsMustNotQueryClauses() {
		Query query = _match();

		BoolQuery.Builder builder = new BoolQuery.Builder();

		builder.must(_match());
		builder.mustNot(query);

		BoolQuery boolQuery = _createBoolQuery(
			HighlightQueryFactory.create(new Query(builder.build())));

		Assert.assertEquals(List.of(query), boolQuery.mustNot());
	}

	@Test
	public void testKeepsQueryWithoutProximityQuery() {
		Query query = _match();

		Assert.assertSame(query, HighlightQueryFactory.create(query));
	}

	@Test
	public void testRemovesProximityQueryFromBooleanQuery() {
		Query exactPhraseQuery = _matchPhrase(null);
		Query matchQuery = _match();

		BoolQuery.Builder builder = new BoolQuery.Builder();

		builder.must(matchQuery);
		builder.should(_matchPhrase(50), exactPhraseQuery);

		BoolQuery boolQuery = _createBoolQuery(
			HighlightQueryFactory.create(new Query(builder.build())));

		Assert.assertEquals(List.of(matchQuery), boolQuery.must());
		Assert.assertEquals(List.of(exactPhraseQuery), boolQuery.should());
	}

	@Test
	public void testRemovesProximityQueryFromNestedBooleanQuery() {
		Query matchQuery = _match();

		BoolQuery.Builder innerBuilder = new BoolQuery.Builder();

		innerBuilder.must(matchQuery);
		innerBuilder.should(_matchPhrase(50));

		BoolQuery.Builder builder = new BoolQuery.Builder();

		builder.should(new Query(innerBuilder.build()));

		BoolQuery boolQuery = _createBoolQuery(
			HighlightQueryFactory.create(new Query(builder.build())));

		List<Query> queries = boolQuery.should();

		BoolQuery innerBoolQuery = _createBoolQuery(queries.get(0));

		Assert.assertEquals(List.of(matchQuery), innerBoolQuery.must());
		Assert.assertTrue(
			innerBoolQuery.should(
			).toString(),
			innerBoolQuery.should(
			).isEmpty());
	}

	@Test
	public void testRemovesProximityQueryLeavingNothing() {
		BoolQuery.Builder builder = new BoolQuery.Builder();

		builder.should(_matchPhrase(50));

		Assert.assertNull(
			HighlightQueryFactory.create(new Query(builder.build())));
	}

	@Test
	public void testRemovesStandaloneProximityQuery() {
		Assert.assertNull(HighlightQueryFactory.create(_matchPhrase(50)));
	}

	@Test
	public void testReturnsNullForNullQuery() {
		Assert.assertNull(HighlightQueryFactory.create(null));
	}

	private BoolQuery _createBoolQuery(Query query) {
		Assert.assertTrue(String.valueOf(query), query.isBool());

		return query.bool();
	}

	private Query _match() {
		MatchQuery.Builder builder = new MatchQuery.Builder();

		builder.field(_FIELD_NAME);
		builder.query(FieldValue.of(_KEYWORDS));

		return new Query(builder.build());
	}

	private Query _matchPhrase(Integer slop) {
		MatchPhraseQuery.Builder builder = new MatchPhraseQuery.Builder();

		builder.field(_FIELD_NAME);
		builder.query(_KEYWORDS);
		builder.slop(slop);

		return new Query(builder.build());
	}

	private static final String _FIELD_NAME = "content_en_US";

	private static final String _KEYWORDS = "solar galaxy";

}