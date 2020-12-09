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

package com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.keywords;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.portal.search.tuning.blueprints.engine.spi.keywords.KeywordsProcessor;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.MisspellingSetFields;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingSetIndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, property = "name=misspellings",
service = KeywordsProcessor.class)
public class MisspellingsKeywordsProcessor implements KeywordsProcessor {

	@Override
	public String process(
		String keywords, Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		 Messages messages) {

		if (_allowMisspellings(blueprintsAttributes)) {
			return keywords;
		}

		SearchHits searchHits = _search(keywords, blueprintsAttributes.getCompanyId());
		
		if (searchHits.getTotalHits() > 0) {
			return _doSpellCheck(keywords, searchHits.getSearchHits());
		}
				
		return keywords;
	}

	private boolean _allowMisspellings(
		BlueprintsAttributes blueprintsAttributes) {

		Optional<Object> optional = blueprintsAttributes.getAttributeOptional(
			ReservedParameterNames.ALLOW_MISSPELLING.getKey());

		if (!optional.isPresent()) {
			return false;
		}

		return GetterUtil.getBoolean(optional.get());
	}
	
	private String _doSpellCheck(String keywords, List<SearchHit> searchHits) {
		
		keywords = keywords.toLowerCase();
		
		for (SearchHit searchHit : searchHits) {
			
			Document document = searchHit.getDocument();
			
			List<String> misspellings = document.getStrings(MisspellingSetFields.MISSPELLINGS);
			
			String phrase =  document.getString(MisspellingSetFields.PHRASE);

			for (String misspelling : misspellings) {
				keywords =  StringUtil.replace(keywords, misspelling, phrase);
			}
		}
		
		return keywords;
	}	

	private String[] _getKeywordsArray(String keywords) {

		String regex = "[\\ ,.;\\\\-]+";

		return keywords.split(regex);
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
	
	private SearchHits _search(String keywords, long companyId) {
		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setFetchSource(true);
		searchSearchRequest.setIndexNames(
				_misspellingSetIndexNameBuilder.getMisspellingSetIndexName(companyId).getIndexName());
		searchSearchRequest.setPreferLocalCluster(false);
		searchSearchRequest.setQuery(_getQuery(keywords));
		searchSearchRequest.setSize(1);
		searchSearchRequest.setStart(0);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		return searchSearchResponse.getSearchHits();
	}
	
	@Reference
	private MisspellingSetIndexNameBuilder
		_misspellingSetIndexNameBuilder;

	@Reference
	private Queries _queries;
	
	@Reference
	private SearchEngineAdapter _searchEngineAdapter;
}