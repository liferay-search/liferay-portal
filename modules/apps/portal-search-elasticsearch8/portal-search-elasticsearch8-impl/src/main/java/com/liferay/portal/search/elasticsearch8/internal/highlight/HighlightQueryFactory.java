/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.highlight;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhraseQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import com.liferay.portal.kernel.util.ListUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Olivia Yu
 */
public class HighlightQueryFactory {

	public static Query create(Query query) {
		if (query == null) {
			return null;
		}

		return _removeProximityQueries(query);
	}

	private static boolean _addQueryClauses(
		Consumer<List<Query>> consumer, List<Query> queries) {

		List<Query> newQueries = _removeProximityQueries(queries);

		if (ListUtil.isEmpty(newQueries)) {
			return false;
		}

		consumer.accept(newQueries);

		return true;
	}

	private static List<Query> _removeProximityQueries(List<Query> queries) {
		List<Query> newQueries = new ArrayList<>(queries.size());

		for (Query query : queries) {
			Query newQuery = _removeProximityQueries(query);

			if (newQuery != null) {
				newQueries.add(newQuery);
			}
		}

		return newQueries;
	}

	private static Query _removeProximityQueries(Query query) {
		if (query.isMatchPhrase()) {
			MatchPhraseQuery matchPhraseQuery = query.matchPhrase();

			if (matchPhraseQuery.slop() != null) {
				return null;
			}

			return query;
		}

		if (!query.isBool()) {
			return query;
		}

		BoolQuery boolQuery = query.bool();

		BoolQuery.Builder builder = new BoolQuery.Builder();

		boolean hasClauses = _addQueryClauses(
			builder::filter, boolQuery.filter());

		hasClauses |= _addQueryClauses(builder::must, boolQuery.must());
		hasClauses |= _addQueryClauses(builder::should, boolQuery.should());

		if (!hasClauses) {
			return null;
		}

		if (ListUtil.isNotEmpty(boolQuery.mustNot())) {
			builder.mustNot(boolQuery.mustNot());
		}

		return new Query(builder.build());
	}

}