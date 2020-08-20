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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.response.contributor;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.tuning.blueprints.engine.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.spi.response.ResponseContributor;

import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ResponseContributor.class)
public class QuerySuggestionsResponseContributor
	implements ResponseContributor {

	@Override
	public void contribute(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse,
		Map<String, Object> responseAttributes, JSONObject responseJsonObject) {

		JSONArray querySuggestionsJsonArray = _getQuerySuggestions(
			searchResponse);

		responseJsonObject.put(
			JSONResponseKeys.QUERY_SUGGESTIONS, querySuggestionsJsonArray);
	}

	private JSONArray _getQuerySuggestions(
		SearchSearchResponse searchResponse) {

		Hits hits = searchResponse.getHits();

		String[] querySuggestions = hits.getQuerySuggestions();

		if ((querySuggestions != null) && (querySuggestions.length > 0)) {
			return JSONFactoryUtil.createJSONArray(querySuggestions);
		}

		return JSONFactoryUtil.createJSONArray();
	}

}