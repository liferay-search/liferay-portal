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

package com.liferay.portal.search.tuning.blueprints.json.response.internal.contributor;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.aggregation.AggregationConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.json.response.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.json.response.internal.aggregation.AggregationResponseBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.response.spi.aggregation.AggregationResponseBuilder;
import com.liferay.portal.search.tuning.blueprints.response.spi.contributor.ResponseContributor;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=aggregations",
	service = ResponseContributor.class
)
public class AggregationsResponseContributor implements ResponseContributor {

	@Override
	public void contribute(
		JSONObject responseJsonObject, SearchResponse searchResponse,
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle,
		Messages messages) {

		responseJsonObject.put(
			JSONResponseKeys.AGGREGATIONS,
			_getAggregationsJSONObject(searchResponse, blueprint, messages));
	}

	private JSONObject _getAggregationsJSONObject(
		SearchResponse searchResponse, Blueprint blueprint, Messages messages) {

		JSONObject aggregationsJsonObject = JSONFactoryUtil.createJSONObject();

		Optional<JSONArray> aggregationsConfigurationOptional =
			_blueprintHelper.getAggsConfigurationOptional(blueprint);

		Map<String, AggregationResult> aggregations =
			searchResponse.getAggregationResultsMap();

		if (aggregations.isEmpty() ||
			!aggregationsConfigurationOptional.isPresent()) {

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

				if (!StringUtil.equalsIgnoreCase(
						aggregationResultName, aggregationName)) {

					continue;
				}

				try {
					AggregationResponseBuilder aggregationResponseBuilder =
						_aggregationResponseBuilderFactory.getBuilder(type);

					Optional<JSONObject> aggregationJsonOptional =
						aggregationResponseBuilder.buildJSONObjectOptional(
							entry.getValue());

					if (aggregationJsonOptional.isPresent()) {
						aggregationsJsonObject.put(
							aggregationName, aggregationJsonOptional.get());
					}
				}
				catch (IllegalArgumentException illegalArgumentException) {
					messages.addMessage(
						new Message(
							Severity.ERROR, "core",
							"core.error.unknown-aggregation-type",
							illegalArgumentException.getMessage(),
							illegalArgumentException, aggregationJsonObject,
							AggregationConfigurationKeys.TYPE.getJsonKey(),
							type));

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
	private AggregationResponseBuilderFactory
		_aggregationResponseBuilderFactory;

	@Reference
	private BlueprintHelper _blueprintHelper;

}