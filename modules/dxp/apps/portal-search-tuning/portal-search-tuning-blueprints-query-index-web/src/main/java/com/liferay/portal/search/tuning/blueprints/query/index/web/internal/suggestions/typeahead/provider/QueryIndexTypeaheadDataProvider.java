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

package com.liferay.portal.search.tuning.blueprints.query.index.web.internal.suggestions.typeahead.provider;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.MultiMatchQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.tuning.blueprints.query.index.configuration.QueryIndexConfiguration;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringFields;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.util.SuggestionsDataProviderHelper;
import com.liferay.portal.search.tuning.blueprints.suggestions.attributes.SuggestionsAttributes;
import com.liferay.portal.search.tuning.blueprints.suggestions.spi.provider.TypeaheadDataProvider;
import com.liferay.portal.search.tuning.blueprints.suggestions.suggestion.Suggestion;

import java.util.ArrayList;
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
	configurationPid = "com.liferay.portal.search.tuning.blueprints.query.index.configuration.QueryIndexConfiguration",
	immediate = true, property = "name=query_index",
	service = TypeaheadDataProvider.class
)
public class QueryIndexTypeaheadDataProvider implements TypeaheadDataProvider {

	@Override
	public List<Suggestion> getSuggestions(
		SuggestionsAttributes suggestionsAttributes) {

		if (!_queryIndexConfiguration.enableTypeaheadDataProvider()) {
			return new ArrayList<>();
		}

		SearchHits searchHits = _suggestionsProviderHelper.search(
			_getQuery(suggestionsAttributes), suggestionsAttributes);

		if (searchHits.getTotalHits() == 0) {
			return new ArrayList<>();
		}

		return _getResults(searchHits.getSearchHits());
	}

	@Override
	public int getWeight() {
		return _queryIndexConfiguration.typeaheadDataProviderWeight();
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_queryIndexConfiguration = ConfigurableUtil.createConfigurable(
			QueryIndexConfiguration.class, properties);
	}

	private void _addSearchClauses(
		BooleanQuery booleanQuery,
		SuggestionsAttributes suggestionsAttributes) {

		MultiMatchQuery multiMatchQuery = _queries.multiMatch(
			suggestionsAttributes.getKeywords(),
			_getFields(suggestionsAttributes));

		multiMatchQuery.setFuzziness("1");
		multiMatchQuery.setPrefixLength(2);
		multiMatchQuery.setType(MultiMatchQuery.Type.BOOL_PREFIX);

		booleanQuery.addMustQueryClauses(multiMatchQuery);
	}

	private Map<String, Float> _getFields(
		SuggestionsAttributes suggestionsAttributes) {

		Map<String, Float> fieldsBoosts = HashMapBuilder.put(
			QueryStringFields.CONTENT, 1.0F
		).put(
			QueryStringFields.CONTENT + "._2gram", 1.0F
		).put(
			QueryStringFields.CONTENT + "._3gram", 1.0F
		).put(
			QueryStringFields.CONTENT + "_snowball", 2.0F
		).build();

		_suggestionsProviderHelper.addLocalizedFields(
			fieldsBoosts, suggestionsAttributes.getLanguageId());

		return fieldsBoosts;
	}

	private Query _getQuery(SuggestionsAttributes suggestionsAttributes) {
		BooleanQuery booleanQuery = _queries.booleanQuery();

		_suggestionsProviderHelper.addCompanyFilterClause(
			booleanQuery, suggestionsAttributes);

		_suggestionsProviderHelper.addGroupFilterClause(
			booleanQuery, suggestionsAttributes);

		_suggestionsProviderHelper.addStatusFilterClause(
			booleanQuery, suggestionsAttributes);

		_suggestionsProviderHelper.addHitCountFilterClause(
			booleanQuery,
			_queryIndexConfiguration.typeAheadProviderHitCountThreshold());

		_addSearchClauses(booleanQuery, suggestionsAttributes);

		_suggestionsProviderHelper.addLanguageBoosterClause(
			booleanQuery, suggestionsAttributes);

		return booleanQuery;
	}

	private List<Suggestion> _getResults(List<SearchHit> searchHits) {
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

	@Reference
	private Queries _queries;

	private volatile QueryIndexConfiguration _queryIndexConfiguration;

	@Reference
	private QueryStringIndexNameBuilder _queryStringIndexNameBuilder;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

	@Reference
	private SuggestionsDataProviderHelper _suggestionsProviderHelper;

}