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

package com.liferay.portal.search.tuning.blueprints.query.index.web.internal.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.query.RangeTermQuery;
import com.liferay.portal.search.query.TermQuery;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexName;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryStringStatus;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringFields;
import com.liferay.portal.search.tuning.blueprints.suggestions.attributes.SuggestionsAttributes;
import com.liferay.portal.search.tuning.blueprints.suggestions.suggestion.Suggestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SuggestionsDataProviderHelper.class)
public class SuggestionsDataProviderHelper {

	public void addCompanyFilterClause(
		BooleanQuery booleanQuery,
		SuggestionsAttributes suggestionsAttributes) {

		booleanQuery.addFilterQueryClauses(
			_queries.term(
				QueryStringFields.COMPANY_ID,
				suggestionsAttributes.getCompanyId()));
	}

	public void addGroupFilterClause(
		BooleanQuery booleanQuery,
		SuggestionsAttributes suggestionsAttributes) {

		booleanQuery.addFilterQueryClauses(
			_queries.term(
				QueryStringFields.GROUP_ID,
				suggestionsAttributes.getGroupId()));
	}

	public void addHitCountFilterClause(
		BooleanQuery booleanQuery, int threshold) {

		RangeTermQuery rangeTermQuery = _queries.rangeTerm(
			QueryStringFields.HIT_COUNT, true, true);

		rangeTermQuery.setLowerBound(threshold);
		rangeTermQuery.setUpperBound(Long.MAX_VALUE);

		booleanQuery.addFilterQueryClauses(rangeTermQuery);
	}

	public void addLanguageBoosterClause(
		BooleanQuery booleanQuery,
		SuggestionsAttributes suggestionsAttributes) {

		TermQuery termQuery = _queries.term(
			QueryStringFields.LANGUAGE_ID,
			suggestionsAttributes.getLanguageId());

		termQuery.setBoost(2.0F);

		booleanQuery.addShouldQueryClauses(termQuery);
	}

	public void addLocalizedFields(
		Map<String, Float> fieldsBoosts, String languageId) {

		if (QueryIndexUtil.isAnalyzedLanguage(languageId)) {
			StringBundler sb = new StringBundler(3);

			sb.append(QueryStringFields.CONTENT);
			sb.append("_");
			sb.append(languageId);

			fieldsBoosts.put(sb.toString(), 1.0F);
		}
	}

	public void addStatusFilterClause(
		BooleanQuery booleanQuery,
		SuggestionsAttributes suggestionsAttributes) {

		BooleanQuery statusQuery = _queries.booleanQuery();

		statusQuery.addShouldQueryClauses(
			_queries.term(
				QueryStringFields.STATUS, QueryStringStatus.ACTIVE.name()));

		statusQuery.addShouldQueryClauses(
			_queries.term(
				QueryStringFields.STATUS, QueryStringStatus.REPORTED.name()));

		booleanQuery.addFilterQueryClauses(statusQuery);
	}

	public List<Suggestion> getSuggestions(List<SearchHit> searchHits) {
		List<Suggestion> suggestions = new ArrayList<>();

		Stream<SearchHit> stream = searchHits.stream();

		stream.forEach(
			searchHit -> {
				Document document = searchHit.getDocument();

				suggestions.add(
					new Suggestion(
						document.getString(QueryStringFields.CONTENT),
						searchHit.getScore()));
			});

		return suggestions;
	}

	public SearchHits search(
		Query query, SuggestionsAttributes suggestionsAttributes) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		QueryStringIndexName queryStringIndexName =
			_queryStringIndexNameBuilder.getQueryStringIndexName(
				suggestionsAttributes.getCompanyId());

		searchSearchRequest.setFetchSource(true);
		searchSearchRequest.setIndexNames(queryStringIndexName.getIndexName());
		searchSearchRequest.setPreferLocalCluster(false);
		searchSearchRequest.setQuery(query);
		searchSearchRequest.setSize(suggestionsAttributes.getSize());
		searchSearchRequest.setStart(0);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		return searchSearchResponse.getSearchHits();
	}

	@Reference
	private Queries _queries;

	@Reference
	private QueryStringIndexNameBuilder _queryStringIndexNameBuilder;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}