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

package com.liferay.search.experiences.predict.synonyms.suggestions.typeahead.provider;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.MultiMatchQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.tuning.synonyms.index.name.SynonymSetIndexName;
import com.liferay.portal.search.tuning.synonyms.index.name.SynonymSetIndexNameBuilder;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributes;
import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;
import com.liferay.search.experiences.predict.suggestions.spi.provider.TypeaheadDataProvider;
import com.liferay.search.experiences.predict.suggestions.suggestion.Suggestion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "data.provider.key=synonyms",
	service = TypeaheadDataProvider.class
)
public class SynonymsTypeaheadDataProvider implements TypeaheadDataProvider {

	public List<Suggestion<String>> getSuggestions(
		SuggestionAttributes suggestionAttributes) {

		DataProviderSettings dataProviderSettings = _getDataProviderSettings(
			suggestionAttributes);

		SearchSearchResponse searchSearchResponse = _search(
			suggestionAttributes, dataProviderSettings);

		return _getSuggestions(
			searchSearchResponse, suggestionAttributes.getKeywords());
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_key = GetterUtil.getString(properties.get("data.provider.key"));
	}

	private void _addSearchClauses(
		BooleanQuery booleanQuery, SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		MultiMatchQuery multiMatchQuery = _queries.multiMatch(
			suggestionAttributes.getKeywords(), SetUtil.fromString("synonyms"));

		multiMatchQuery.setFuzziness(_getFuzziness(dataProviderSettings));
		multiMatchQuery.setPrefixLength(_getPrefixLength(dataProviderSettings));
		multiMatchQuery.setType(MultiMatchQuery.Type.BOOL_PREFIX);

		booleanQuery.addMustQueryClauses(multiMatchQuery);
	}

	private void _addSuggestion(
		List<Suggestion<String>> suggestions, SearchHit searchHit,
		String keywords) {

		Document document = searchHit.getDocument();

		String values = document.getString("synonyms");

		String[] arr = values.split(",");

		Stream<String> stream = Arrays.stream(arr);

		String s = StringUtil.toLowerCase(keywords);

		stream.filter(
			synonym -> !synonym.startsWith(s)
		).forEach(
			synonym -> suggestions.add(
				new Suggestion<String>(synonym, searchHit.getScore()))
		);
	}

	private DataProviderSettings _getDataProviderSettings(
		SuggestionAttributes suggestionAttributes) {

		return suggestionAttributes.getDataProviderSettings(_key);
	}

	private String _getFuzziness(DataProviderSettings dataProviderSettings) {
		if (dataProviderSettings == null) {
			return _DEFAULT_FUZZINESS;
		}

		return GetterUtil.getString(
			dataProviderSettings.getAttribute("fuzziness"), _DEFAULT_FUZZINESS);
	}

	private int _getPrefixLength(DataProviderSettings dataProviderSettings) {
		if (dataProviderSettings == null) {
			return _DEFAULT_PREFIX_LENGTH;
		}

		return GetterUtil.getInteger(
			dataProviderSettings.getAttribute("prefixLength"),
			_DEFAULT_PREFIX_LENGTH);
	}

	private Query _getQuery(
		SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		_addSearchClauses(
			booleanQuery, suggestionAttributes, dataProviderSettings);

		return booleanQuery;
	}

	private List<Suggestion<String>> _getSuggestions(
		SearchSearchResponse searchSearchResponse, String keywords) {

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		List<Suggestion<String>> suggestions = new ArrayList<>();

		if (searchHits.getTotalHits() == 0) {
			return suggestions;
		}

		List<SearchHit> hits = searchHits.getSearchHits();

		hits.forEach(
			searchHit -> _addSuggestion(suggestions, searchHit, keywords));

		return suggestions;
	}

	private SearchSearchResponse _search(
		SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		SynonymSetIndexName synonymSetIndexName =
			_synonymSetIndexNameBuilder.getSynonymSetIndexName(
				suggestionAttributes.getCompanyId());

		searchSearchRequest.setFetchSource(true);
		searchSearchRequest.setIndexNames(synonymSetIndexName.getIndexName());
		searchSearchRequest.setPreferLocalCluster(false);
		searchSearchRequest.setQuery(
			_getQuery(suggestionAttributes, dataProviderSettings));
		searchSearchRequest.setSize(suggestionAttributes.getSize());
		searchSearchRequest.setStart(0);

		return _searchEngineAdapter.execute(searchSearchRequest);
	}

	private static final String _DEFAULT_FUZZINESS = "1";

	private static final int _DEFAULT_PREFIX_LENGTH = 2;

	private volatile String _key;

	@Reference
	private Queries _queries;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

	@Reference
	private SynonymSetIndexNameBuilder _synonymSetIndexNameBuilder;

}