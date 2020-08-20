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
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.AggregationConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.spi.response.ResponseContributor;

import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ResponseContributor.class)
public class AggregationsResponseContributor implements ResponseContributor {

	@Override
	public void contribute(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse,
		Map<String, Object> responseAttributes, JSONObject responseJsonObject) {

		responseJsonObject.put(
			JSONResponseKeys.AGGREGATIONS,
			_getAggregations(searchRequestContext, searchResponse));
	}

	private JSONObject _getAggregations(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse) {

		JSONObject aggregationsJsonObject = JSONFactoryUtil.createJSONObject();

		Optional<JSONArray> aggregationsConfigurationJsonArrayOptional =
			searchRequestContext.getAggregationConfiguration();

		Map<String, AggregationResult> aggregations =
			searchResponse.getAggregationResultsMap();

		if ((aggregations == null) ||
			!aggregationsConfigurationJsonArrayOptional.isPresent()) {

			return aggregationsJsonObject;
		}

		JSONArray configuration =
			aggregationsConfigurationJsonArrayOptional.get();

		for (int i = 0; i < configuration.length(); i++) {
			JSONObject aggregationJsonObject = configuration.getJSONObject(i);

			String aggregationName = aggregationJsonObject.getString(
				AggregationConfigurationKeys.NAME.getJsonKey());

			for (Map.Entry<String, AggregationResult> entry :
					aggregations.entrySet()) {

				String aggregationResultName = entry.getKey();

				if (!aggregationResultName.equalsIgnoreCase(aggregationName)) {
					continue;
				}

				aggregationsJsonObject.put(aggregationName, entry.getValue());
			}
		}
		return aggregationsJsonObject;
	}
}
