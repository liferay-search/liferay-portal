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
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.MatchQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.tuning.blueprints.misspellings.processor.MisspellingsProcessor;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.MisspellingSetFields;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingSetIndexNameBuilder;

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
		long companyId, long groupId, String languageId, String keywords) {

		SearchHits searchHits = _search(
			companyId, groupId, languageId, keywords);

		if (searchHits.getTotalHits() > 0) {
			return _spellCheck(keywords, searchHits.getSearchHits());
		}

		return keywords;
	}

	private void _addCompanyFilterClause(
		BooleanQuery booleanQuery, long companyId) {

		booleanQuery.addFilterQueryClauses(
			_queries.term(MisspellingSetFields.COMPANY_ID, companyId));
	}

	private void _addGroupFilterClause(
		BooleanQuery booleanQuery, long groupId) {

		BooleanQuery groupQuery = _queries.booleanQuery();

		groupQuery.addShouldQueryClauses(
			_queries.term(MisspellingSetFields.GROUP_ID, groupId));

		groupQuery.addShouldQueryClauses(
			_queries.term(MisspellingSetFields.GROUP_ID, "*"));

		booleanQuery.addFilterQueryClauses(groupQuery);
	}

	private void _addLanguageFilterClause(
		BooleanQuery booleanQuery, String languageId) {

		BooleanQuery languageQuery = _queries.booleanQuery();

		languageQuery.addShouldQueryClauses(
			_queries.term(MisspellingSetFields.LANGUAGE_ID, languageId));

		languageQuery.addShouldQueryClauses(
			_queries.term(MisspellingSetFields.LANGUAGE_ID, "*"));

		booleanQuery.addFilterQueryClauses(languageQuery);
	}

	private void _addSearchClauses(BooleanQuery booleanQuery, String keywords) {
		MatchQuery matchQuery = _queries.match(
			MisspellingSetFields.MISSPELLINGS, keywords);

		booleanQuery.addMustQueryClauses(matchQuery);
	}

	private Query _getQuery(
		long companyId, long groupId, String languageId, String keywords) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		_addCompanyFilterClause(booleanQuery, companyId);

		_addGroupFilterClause(booleanQuery, groupId);

		if (!Validator.isBlank(languageId)) {
			_addLanguageFilterClause(booleanQuery, languageId);
		}

		_addSearchClauses(booleanQuery, keywords);

		return booleanQuery;
	}

	private SearchHits _search(
		long companyId, long groupId, String languageId, String keywords) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setFetchSource(true);
		searchSearchRequest.setIndexNames(
			_misspellingSetIndexNameBuilder.getMisspellingSetIndexName(
				companyId
			).getIndexName());
		searchSearchRequest.setPreferLocalCluster(false);
		searchSearchRequest.setQuery(
			_getQuery(companyId, groupId, languageId, keywords));
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