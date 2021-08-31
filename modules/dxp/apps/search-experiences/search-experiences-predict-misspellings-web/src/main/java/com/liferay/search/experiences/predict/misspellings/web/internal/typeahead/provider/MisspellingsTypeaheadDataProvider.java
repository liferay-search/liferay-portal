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

package com.liferay.search.experiences.predict.misspellings.web.internal.typeahead.provider;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.MatchQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.search.experiences.predict.misspellings.index.name.MisspellingsIndexName;
import com.liferay.search.experiences.predict.misspellings.index.name.MisspellingsIndexNameBuilder;
import com.liferay.search.experiences.predict.misspellings.web.internal.index.MisspellingSetFields;
import com.liferay.search.experiences.predict.misspellings.web.internal.util.MisspellingsQueryHelper;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributes;
import com.liferay.search.experiences.predict.suggestions.constants.SuggestionConstants;
import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;
import com.liferay.search.experiences.predict.suggestions.spi.provider.TypeaheadDataProvider;
import com.liferay.search.experiences.predict.suggestions.suggestion.SuggestionResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "data.provider.key=misspellings",
	service = TypeaheadDataProvider.class
)
public class MisspellingsTypeaheadDataProvider
	implements TypeaheadDataProvider {

	@Override
	public List<SuggestionResponse<String>> getSuggestions(
		SuggestionAttributes suggestionAttributes) {

		DataProviderSettings dataProviderSettings = _getDataProviderSettings(
			suggestionAttributes);

		SearchSearchResponse searchSearchResponse = _search(
			suggestionAttributes, dataProviderSettings);

		return _getSuggestions(searchSearchResponse);
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_key = GetterUtil.getString(properties.get("data.provider.key"));
	}

	private void _addGroupFilterClause(
		BooleanQuery booleanQuery, SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		long[] sourceGroupIds = _getSourceGroupIds(dataProviderSettings);

		if (sourceGroupIds.length != 0) {
			_misspellingsQueryHelper.addGroupFilterClause(
				booleanQuery, sourceGroupIds);
		}
		else {
			_misspellingsQueryHelper.addGroupFilterClause(
				booleanQuery, suggestionAttributes.getGroupId());
		}
	}

	private void _addLanguageFilterClause(
		BooleanQuery booleanQuery, SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		String[] languageIds = _getLanguageIds(dataProviderSettings);

		if (languageIds.length != 0) {
			_misspellingsQueryHelper.addLanguageFilterClause(
				booleanQuery, languageIds);
		}
		else {
			Locale locale = suggestionAttributes.getLocale();

			_misspellingsQueryHelper.addLanguageFilterClause(
				booleanQuery, locale.toString());
		}
	}

	private void _addSearchClauses(
		BooleanQuery booleanQuery, SuggestionAttributes suggestionAttributes) {

		MatchQuery matchQuery = _queries.match(
			MisspellingSetFields.MISSPELLINGS,
			suggestionAttributes.getKeywords());

		matchQuery.setFuzziness(0F);

		booleanQuery.addMustQueryClauses(matchQuery);
	}

	private void _addSuggestion(
		List<SuggestionResponse<String>> suggestions, SearchHit searchHit) {

		Document document = searchHit.getDocument();

		suggestions.add(
			new SuggestionResponse<String>(
				document.getString(MisspellingSetFields.PHRASE),
				searchHit.getScore()));
	}

	private DataProviderSettings _getDataProviderSettings(
		SuggestionAttributes suggestionAttributes) {

		return suggestionAttributes.getDataProviderSettings(_key);
	}

	private String[] _getLanguageIds(
		DataProviderSettings dataProviderSettings) {

		if (dataProviderSettings == null) {
			return new String[0];
		}

		return GetterUtil.getStringValues(
			dataProviderSettings.getAttribute(
				SuggestionConstants.LANGUAGE_IDS));
	}

	private Query _getQuery(
		SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		_addGroupFilterClause(
			booleanQuery, suggestionAttributes, dataProviderSettings);

		_addLanguageFilterClause(
			booleanQuery, suggestionAttributes, dataProviderSettings);

		_addSearchClauses(booleanQuery, suggestionAttributes);

		return booleanQuery;
	}

	private long[] _getSourceGroupIds(
		DataProviderSettings dataProviderSettings) {

		if (dataProviderSettings == null) {
			return new long[0];
		}

		return GetterUtil.getLongValues(
			dataProviderSettings.getAttribute(
				SuggestionConstants.SOURCE_GROUP_IDS));
	}

	private List<SuggestionResponse<String>> _getSuggestions(
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

	private SearchSearchResponse _search(
		SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		MisspellingsIndexName misspellingsIndexName =
			_misspellingsIndexNameBuilder.getMisspellingsIndexName(
				suggestionAttributes.getCompanyId());

		searchSearchRequest.setFetchSource(true);
		searchSearchRequest.setIndexNames(misspellingsIndexName.getIndexName());
		searchSearchRequest.setPreferLocalCluster(false);
		searchSearchRequest.setQuery(
			_getQuery(suggestionAttributes, dataProviderSettings));
		searchSearchRequest.setSize(suggestionAttributes.getSize());
		searchSearchRequest.setStart(0);

		return _searchEngineAdapter.execute(searchSearchRequest);
	}

	private volatile String _key;

	@Reference
	private MisspellingsIndexNameBuilder _misspellingsIndexNameBuilder;

	@Reference
	private MisspellingsQueryHelper _misspellingsQueryHelper;

	@Reference
	private Queries _queries;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}