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

package com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.processor;

import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.filter.ComplexQueryPart;
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.portal.search.tuning.blueprints.misspellings.processor.MisspellingsProcessor;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.MisspellingSetFields;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingSetIndexNameBuilder;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = MisspellingsProcessor.class)
public class MisspellingsProcessorImpl implements MisspellingsProcessor {

	@Override
	public String process(
		long companyId, String[] misspellingSetIds, String[] languageIds,
		String keywords) {

		SearchHits searchHits = _search(
			companyId, misspellingSetIds, languageIds, keywords);

		if (searchHits.getTotalHits() > 0) {
			return _spellCheck(keywords, searchHits.getSearchHits());
		}

		return keywords;
	}

	private List<ComplexQueryPart> _getFilterClauses(
		String[] misspellingSetIds, String[] languageIds) {

		List<ComplexQueryPart> complexQueryParts = new ArrayList<>();

		complexQueryParts.add(
			_complexQueryPartBuilderFactory.builder(
			).query(
				_getIdsQuery(misspellingSetIds)
			).occur(
				"filter"
			).build());

		complexQueryParts.add(
			_complexQueryPartBuilderFactory.builder(
			).query(
				_getLanguageQuery(languageIds)
			).occur(
				"filter"
			).build());

		return complexQueryParts;
	}

	private Query _getIdsQuery(Object[] misspellingSetIds) {
		TermsQuery termsQuery = _queries.terms(MisspellingSetFields.UID);

		termsQuery.addValues(misspellingSetIds);

		return termsQuery;
	}

	private String[] _getKeywordsArray(String keywords) {
		String regex = "[\\ ,.;\\\\-]+";

		return keywords.split(regex);
	}

	private Query _getLanguageQuery(Object[] languageIds) {
		TermsQuery termsQuery = _queries.terms(
			MisspellingSetFields.LANGUAGE_ID);

		termsQuery.addValues(languageIds);

		return termsQuery;
	}

	private Query _getQuery(String keywords) {
		String[] keywordsArray = _getKeywordsArray(keywords);

		BooleanQuery query = _queries.booleanQuery();

		for (String keyword : keywordsArray) {
			query.addShouldQueryClauses(
				_queries.term(MisspellingSetFields.MISSPELLINGS, keyword));
		}

		return query;
	}

	private SearchHits _search(
		long companyId, String[] misspellingSetIds, String[] languageIds,
		String keywords) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.addComplexQueryParts(
			_getFilterClauses(misspellingSetIds, languageIds));
		searchSearchRequest.setFetchSource(true);
		searchSearchRequest.setIndexNames(
			_misspellingSetIndexNameBuilder.getMisspellingSetIndexName(
				companyId
			).getIndexName());
		searchSearchRequest.setPreferLocalCluster(false);
		searchSearchRequest.setQuery(_getQuery(keywords));
		searchSearchRequest.setSize(1);
		searchSearchRequest.setStart(0);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		return searchSearchResponse.getSearchHits();
	}

	private String _spellCheck(String keywords, List<SearchHit> searchHits) {
		keywords = StringUtil.toLowerCase(keywords);

		for (SearchHit searchHit : searchHits) {
			Document document = searchHit.getDocument();

			List<String> misspellings = document.getStrings(
				MisspellingSetFields.MISSPELLINGS);

			String phrase = document.getString(MisspellingSetFields.PHRASE);

			for (String misspelling : misspellings) {
				if (keywords.contains(misspelling)) {
					keywords = StringUtil.replace(
						keywords, misspelling, phrase);

					break;
				}
			}
		}

		return keywords;
	}

	@Reference
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Reference
	private MisspellingSetIndexNameBuilder _misspellingSetIndexNameBuilder;

	@Reference
	private Queries _queries;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}