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

package com.liferay.portal.search.tuning.blueprints.engine.internal.query.postprocessor;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.KeywordIndexingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.SpellCheckingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.internal.searchrequest.SearchRequestContextBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.spi.query.QueryPostProcessor;
import com.liferay.portal.search.tuning.blueprints.engine.suggester.Suggester;
import com.liferay.portal.search.tuning.blueprints.engine.util.SearchClientHelper;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * TODO:https://issues.liferay.com/browse/LPS-118888
 *
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = QueryPostProcessor.class)
public class SpellCheckingPostProcessor implements QueryPostProcessor {

	@Override
	public boolean process(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse) {

		Optional<JSONObject> spellCheckingConfigurationJsonObjectOptional =
			_blueprintHelper.getSpellCheckingConfigurationOptional(
				searchRequestContext.getBlueprint());

		if (!spellCheckingConfigurationJsonObjectOptional.isPresent()) {
			return true;
		}

		JSONObject spellCheckingConfigurationJsonObject =
			spellCheckingConfigurationJsonObjectOptional.get();

		boolean enabled = spellCheckingConfigurationJsonObject.getBoolean(
			KeywordIndexingConfigurationKeys.ENABLED.getJsonKey());

		if (!enabled) {
			return true;
		}

		SearchHits searchHits = searchResponse.getSearchHits();

		int hitsThreshold = spellCheckingConfigurationJsonObject.getInt(
			SpellCheckingConfigurationKeys.HITS_THRESHOLD.getJsonKey(), 1);

		if (searchHits.getTotalHits() >= hitsThreshold) {
			return true;
		}

		List<String> suggestions = _spellChecker.getSuggestions(
			searchRequestContext);

		if (suggestions.contains(searchRequestContext.getKeywords())) {
			suggestions.remove(searchRequestContext.getKeywords());
		}

		if (_log.isDebugEnabled()) {
			_log.debug("Query suggestions size is " + suggestions.size());
		}

		if (!suggestions.isEmpty()) {
			if (_log.isDebugEnabled()) {
				_log.debug("Suggestions found");
			}

			/*
			 * TODO
			 */
			if (false) {
				SearchRequestBuilder builder =
					_searchRequestBuilderFactory.builder();

				SearchRequestContextBuilder searchRequestContextBuilder =
					new SearchRequestContextBuilder(builder);

				searchRequestContextBuilder.rawKeywords(
					searchRequestContext.getRawKeywords());

				 if (_log.isDebugEnabled()) {
				 _log.debug("Using querySuggestions[0] for alternative search.");
				 }

				 builder.queryString(suggestions.get(0));

				 // Remove the new keywords from query suggestions.
				 suggestions.remove(0);

				 SearchSearchResponse newResponse = _executeNewSearch(
					 searchRequestContext);

				 // Copy new values to the response.
				 if (newResponse.getAggregationResultsMap() != null) {
					 for (Entry<String, AggregationResult> entry :
						 newResponse.getAggregationResultsMap().entrySet()) {

						 searchResponse.addAggregationResult(entry.getValue());
					 }
				 }

				 searchResponse.setSearchHits(newResponse.getSearchHits());
				 searchResponse.getHits().copy(newResponse.getHits());
			}
		}

		searchResponse.getHits(
		).setQuerySuggestions(
			suggestions.toArray(new String[0])
		);

		return true;
	}

	private SearchSearchResponse _executeNewSearch(
			SearchRequestContext searchRequestContext) {

		return _searchClientHelper.getSearchResponse(
			searchRequestContext,
			_searchClientHelper.getSearchRequestData(searchRequestContext));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SpellCheckingPostProcessor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference(target = "(type=spellcheck)")
	private Suggester _spellChecker;

	@Reference
	private SearchClientHelper _searchClientHelper;;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

}