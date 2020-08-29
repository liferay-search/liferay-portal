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

package com.liferay.portal.search.tuning.blueprints.response.internal.contributor;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.aggregation.AggregationConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.response.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.response.internal.aggregation.AggregationResponseBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.response.spi.aggregation.AggregationResponseBuilder;
import com.liferay.portal.search.tuning.blueprints.response.spi.contributor.ResponseContributor;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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

		Optional<JSONArray> aggregationsConfigurationOptional =
				_blueprintHelper.getAggregationConfigurationOptional(
						searchRequestContext.getBlueprint());
		
		Map<String, AggregationResult> aggregations =
			searchResponse.getAggregationResultsMap();

		if ((aggregations == null) ||
			(!aggregationsConfigurationOptional.isPresent())) {

			return aggregationsJsonObject;
		}
		
		JSONArray aggregationsConfigurationJsonArray = 
				aggregationsConfigurationOptional.get();

		for (int i = 0; i < aggregationsConfigurationJsonArray.length(); i++) {
			JSONObject aggregationJsonObject =
				aggregationsConfigurationJsonArray.getJSONObject(i);

			String aggregationName = aggregationJsonObject.getString(
				AggregationConfigurationKeys.NAME.getJsonKey());

			String type = aggregationJsonObject.getString(
					AggregationConfigurationKeys.TYPE.getJsonKey());

			for (Map.Entry<String, AggregationResult> entry :
					aggregations.entrySet()) {

				String aggregationResultName = entry.getKey();

				if (!aggregationResultName.equalsIgnoreCase(aggregationName)) {
					continue;
				}

				try {
					AggregationResponseBuilder aggregationResponseBuilder =
							_aggregationResponseBuilderFactory.getBuilder(type);
	
					Optional<JSONObject> aggregationJsonOptional =
							aggregationResponseBuilder.build(entry.getValue());
	
					if (aggregationJsonOptional.isPresent()) {
						aggregationsJsonObject.put(aggregationName, aggregationJsonOptional.get());
					}
				}	
				catch (IllegalArgumentException illegalArgumentException) {
					searchRequestContext.addMessage(
						new Message(
							Severity.ERROR, "core",
							"core.error.unknown-aggregation-type",
							illegalArgumentException.getMessage(),
							illegalArgumentException, aggregationJsonObject,
							AggregationConfigurationKeys.TYPE.getJsonKey(), type));

					_log.error(
						illegalArgumentException.getMessage(),
						illegalArgumentException);
				}
			}
		}

		return aggregationsJsonObject;
	}

	private static final Log _log = LogFactoryUtil.getLog(
			AggregationsResponseContributor.class);

	@Reference
	private AggregationResponseBuilderFactory _aggregationResponseBuilderFactory;

	@Reference
	private BlueprintHelper _blueprintHelper;
	
}