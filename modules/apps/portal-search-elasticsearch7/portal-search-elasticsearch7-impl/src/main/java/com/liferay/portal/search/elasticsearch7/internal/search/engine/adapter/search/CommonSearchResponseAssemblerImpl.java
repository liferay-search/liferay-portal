/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.search.elasticsearch7.internal.search.engine.adapter.search;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.elasticsearch7.internal.stats.StatsTranslator;
import com.liferay.portal.search.engine.adapter.search.BaseSearchRequest;
import com.liferay.portal.search.engine.adapter.search.BaseSearchResponse;
import com.liferay.portal.search.stats.StatsRequest;

import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.FuzzyQuery;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.search.MatchQueryParser;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.profile.SearchProfileShardResult;
import org.elasticsearch.search.profile.query.QueryProfileShardResult;
import org.elasticsearch.xcontent.ToXContent;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.elasticsearch.xcontent.XContentType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(service = CommonSearchResponseAssembler.class)
public class CommonSearchResponseAssemblerImpl
	implements CommonSearchResponseAssembler {

	@Override
	public void assemble(
		BaseSearchResponse baseSearchResponse,
		BaseSearchRequest baseSearchRequest, SearchResponse searchResponse,
		String searchRequestString) {

		_setExecutionProfile(baseSearchResponse, searchResponse);
		_setExecutionTime(baseSearchResponse, searchResponse);
		_setSearchRequestString(baseSearchResponse, searchRequestString);
		setSearchResponseString(
			baseSearchResponse, baseSearchRequest, searchResponse);
		_setTerminatedEarly(baseSearchResponse, searchResponse);
		_setTimedOut(baseSearchResponse, searchResponse);

		_updateStatsResponses(
			searchResponse.getAggregations(), baseSearchResponse,
			baseSearchRequest.getStatsRequests());
	}

	protected void setSearchResponseString(
		BaseSearchResponse baseSearchResponse,
		BaseSearchRequest baseSearchRequest, SearchResponse searchResponse) {

		if (baseSearchRequest.isIncludeResponseString()) {
			baseSearchResponse.setSearchResponseString(
				searchResponse.toString());
		}
	}

	protected static final String ADJUST_PURE_NEGATIVE_STRING =
		",\"adjust_pure_negative\":true";

	protected static final String AUTO_GENERATE_SYNONYMS_PHRASE_QUERY_STRING =
		",\"auto_generate_synonyms_phrase_query\":true";

	protected static final String BOOST_STRING = ",\"boost\":1.0";

	protected static final String FUZZY_TRANSPOSITIONS_STRING =
		",\"fuzzy_transpositions\":" + FuzzyQuery.defaultTranspositions;

	protected static final String LENIENT_STRING =
		",\"lenient\":" + MatchQueryParser.DEFAULT_LENIENCY;

	protected static final String MAX_EXPANSIONS_STRING =
		",\"max_expansions\":" + FuzzyQuery.defaultMaxExpansions;

	protected static final String OPERATOR_STRING = ",\"operator\":\"OR\"";

	protected static final String PREFIX_LENGTH_STRING =
		",\"prefix_length\":" + FuzzyQuery.defaultPrefixLength;

	protected static final String SLOP_STRING =
		",\"slop\":" + MatchQueryParser.DEFAULT_PHRASE_SLOP;

	protected static final String ZERO_TERMS_QUERY_STRING =
		",\"zero_terms_query\":\"" + MatchQueryParser.DEFAULT_ZERO_TERMS_QUERY +
			"\"";

	private String _getSearchProfileShardResultString(
			SearchProfileShardResult searchProfileShardResult)
		throws IOException {

		XContentBuilder xContentBuilder = XContentFactory.contentBuilder(
			XContentType.JSON);

		List<QueryProfileShardResult> queryProfileShardResults =
			searchProfileShardResult.getQueryProfileResults();

		queryProfileShardResults.forEach(
			queryProfileShardResult -> {
				try {
					xContentBuilder.startObject();

					queryProfileShardResult.toXContent(
						xContentBuilder, ToXContent.EMPTY_PARAMS);

					xContentBuilder.endObject();
				}
				catch (IOException ioException) {
					if (_log.isDebugEnabled()) {
						_log.debug(ioException);
					}
				}
			});

		return Strings.toString(xContentBuilder);
	}

	private void _setExecutionProfile(
		BaseSearchResponse baseSearchResponse, SearchResponse searchResponse) {

		Map<String, SearchProfileShardResult> searchProfileShardResults =
			searchResponse.getProfileResults();

		if (MapUtil.isEmpty(searchProfileShardResults)) {
			return;
		}

		Map<String, String> executionProfile = new HashMap<>();

		searchProfileShardResults.forEach(
			(shardKey, searchProfileShardResult) -> {
				try {
					executionProfile.put(
						shardKey,
						_getSearchProfileShardResultString(
							searchProfileShardResult));
				}
				catch (IOException ioException) {
					if (_log.isInfoEnabled()) {
						_log.info(ioException);
					}
				}
			});

		baseSearchResponse.setExecutionProfile(executionProfile);
	}

	private void _setExecutionTime(
		BaseSearchResponse baseSearchResponse, SearchResponse searchResponse) {

		TimeValue tookTimeValue = searchResponse.getTook();

		baseSearchResponse.setExecutionTime(tookTimeValue.getMillis());
	}

	private void _setSearchRequestString(
		BaseSearchResponse baseSearchResponse, String searchRequestString) {

		baseSearchResponse.setSearchRequestString(
			StringUtil.removeSubstrings(
				searchRequestString, ADJUST_PURE_NEGATIVE_STRING,
				AUTO_GENERATE_SYNONYMS_PHRASE_QUERY_STRING, BOOST_STRING,
				FUZZY_TRANSPOSITIONS_STRING, LENIENT_STRING,
				MAX_EXPANSIONS_STRING, OPERATOR_STRING, PREFIX_LENGTH_STRING,
				SLOP_STRING, ZERO_TERMS_QUERY_STRING));
	}

	private void _setTerminatedEarly(
		BaseSearchResponse baseSearchResponse, SearchResponse searchResponse) {

		baseSearchResponse.setTerminatedEarly(
			GetterUtil.getBoolean(searchResponse.isTerminatedEarly()));
	}

	private void _setTimedOut(
		BaseSearchResponse baseSearchResponse, SearchResponse searchResponse) {

		baseSearchResponse.setTimedOut(searchResponse.isTimedOut());
	}

	private void _updateStatsResponse(
		Map<String, Aggregation> aggregationsMap,
		BaseSearchResponse baseSearchResponse, StatsRequest statsRequest) {

		baseSearchResponse.addStatsResponse(
			_statsTranslator.translateResponse(aggregationsMap, statsRequest));
	}

	private void _updateStatsResponses(
		Aggregations aggregations, BaseSearchResponse baseSearchResponse,
		Collection<StatsRequest> statsRequests) {

		if (aggregations == null) {
			return;
		}

		_updateStatsResponses(
			aggregations.getAsMap(), baseSearchResponse, statsRequests);
	}

	private void _updateStatsResponses(
		Map<String, Aggregation> aggregationsMap,
		BaseSearchResponse baseSearchResponse,
		Collection<StatsRequest> statsRequests) {

		for (StatsRequest statsRequest : statsRequests) {
			_updateStatsResponse(
				aggregationsMap, baseSearchResponse, statsRequest);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommonSearchResponseAssemblerImpl.class);

	@Reference
	private StatsTranslator _statsTranslator;

}