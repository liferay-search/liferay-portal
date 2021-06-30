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

package com.liferay.search.experiences.predict.keyword.index.web.internal.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
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
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.search.experiences.predict.keyword.index.constants.KeywordEntryStatus;
import com.liferay.search.experiences.predict.keyword.index.index.name.KeywordIndexName;
import com.liferay.search.experiences.predict.keyword.index.index.name.KeywordIndexNameBuilder;
import com.liferay.search.experiences.predict.keyword.index.web.internal.index.KeywordEntryFields;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributes;
import com.liferay.search.experiences.predict.suggestions.constants.SuggestionConstants;
import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;
import com.liferay.search.experiences.predict.suggestions.suggestion.SuggestionResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SuggestionDataProviderHelper.class)
public class SuggestionDataProviderHelper {

	public void addGroupFilterClause(
		BooleanQuery booleanQuery, SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		long[] groupIds;

		if (dataProviderSettings == null) {
			groupIds = new long[0];
		}
		else {
			groupIds = GetterUtil.getLongValues(
				dataProviderSettings.getAttribute(
					SuggestionConstants.SOURCE_GROUP_IDS));
		}

		if (groupIds.length != 0) {
			TermsQuery termsQuery = _queries.terms(KeywordEntryFields.GROUP_ID);

			termsQuery.addValues(ArrayUtil.toStringArray(groupIds));

			booleanQuery.addFilterQueryClauses(termsQuery);
		}
		else {
			booleanQuery.addFilterQueryClauses(
				_queries.term(
					KeywordEntryFields.GROUP_ID,
					suggestionAttributes.getGroupId()));
		}
	}

	public void addHitCountFilterClause(
		BooleanQuery booleanQuery, int threshold) {

		RangeTermQuery rangeTermQuery = _queries.rangeTerm(
			KeywordEntryFields.HIT_COUNT, true, true);

		rangeTermQuery.setLowerBound(threshold);
		rangeTermQuery.setUpperBound(Long.MAX_VALUE);

		booleanQuery.addFilterQueryClauses(rangeTermQuery);
	}

	public void addLanguageBoosterClause(
		BooleanQuery booleanQuery, SuggestionAttributes suggestionAttributes) {

		TermQuery termQuery = _queries.term(
			KeywordEntryFields.LANGUAGE_ID,
			_language.getLanguageId(suggestionAttributes.getLocale()));

		termQuery.setBoost(2.0F);

		booleanQuery.addShouldQueryClauses(termQuery);
	}

	public void addLocalizedFields(
		Map<String, Float> fieldsBoosts, Locale locale) {

		String languageId = _language.getLanguageId(locale);

		if (KeywordIndexUtil.isAnalyzedLanguage(languageId)) {
			StringBundler sb = new StringBundler(3);

			sb.append(KeywordEntryFields.CONTENT);
			sb.append("_");
			sb.append(languageId);

			fieldsBoosts.put(sb.toString(), 1.0F);
		}
	}

	public void addStatusFilterClause(
		BooleanQuery booleanQuery, SuggestionAttributes suggestionAttributes) {

		BooleanQuery statusQuery = _queries.booleanQuery();

		statusQuery.addShouldQueryClauses(
			_queries.term(
				KeywordEntryFields.STATUS, KeywordEntryStatus.ACTIVE.name()));

		statusQuery.addShouldQueryClauses(
			_queries.term(
				KeywordEntryFields.STATUS, KeywordEntryStatus.REPORTED.name()));

		booleanQuery.addFilterQueryClauses(statusQuery);
	}

	public List<SuggestionResponse<String>> getSuggestions(
		SearchSearchResponse searchSearchResponse) {

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		List<SuggestionResponse<String>> suggestions = new ArrayList<>();

		if (searchHits.getTotalHits() == 0) {
			return suggestions;
		}

		List<SearchHit> hits = searchHits.getSearchHits();

		hits.forEach(searchHit -> _addSuggestion(suggestions, searchHit));

		return suggestions;
	}

	public SearchSearchResponse search(
		SuggestionAttributes suggestionAttributes, Query query) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		KeywordIndexName keywordIndexName =
			_keywordIndexNameBuilder.getKeywordIndexName(
				suggestionAttributes.getCompanyId());

		searchSearchRequest.setFetchSource(true);
		searchSearchRequest.setIndexNames(keywordIndexName.getIndexName());
		searchSearchRequest.setPreferLocalCluster(false);
		searchSearchRequest.setQuery(query);
		searchSearchRequest.setSize(suggestionAttributes.getSize());
		searchSearchRequest.setStart(0);

		return _searchEngineAdapter.execute(searchSearchRequest);
	}

	private void _addSuggestion(
		List<SuggestionResponse<String>> suggestions, SearchHit searchHit) {

		Document document = searchHit.getDocument();

		suggestions.add(
			new SuggestionResponse<String>(
				document.getString(KeywordEntryFields.CONTENT),
				searchHit.getScore()));
	}

	@Reference
	private KeywordIndexNameBuilder _keywordIndexNameBuilder;

	@Reference
	private Language _language;

	@Reference
	private Queries _queries;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}