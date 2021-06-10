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

package com.liferay.search.experiences.searchresponse.json.translator.internal.contributor;

import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.blueprints.util.util.BlueprintJSONUtil;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;
import com.liferay.search.experiences.searchresponse.json.translator.constants.JSONKeys;
import com.liferay.search.experiences.searchresponse.json.translator.internal.aggregation.AggregationJSONTranslatorFactory;
import com.liferay.search.experiences.searchresponse.json.translator.spi.aggregation.AggregationJSONTranslator;
import com.liferay.search.experiences.searchresponse.json.translator.spi.contributor.JSONTranslationContributor;

import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=aggs",
	service = JSONTranslationContributor.class
)
public class AggregationsJSONTranslationContributor
	implements JSONTranslationContributor {

	@Override
	public void contribute(
		JSONObject responseJSONObject, SearchResponse searchResponse,
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle, Messages messages) {

		Optional<JSONObject> optional =
			_blueprintHelper.getAggsConfigurationOptional(blueprint);

		Map<String, AggregationResult> aggregations =
			searchResponse.getAggregationResultsMap();

		if (aggregations.isEmpty() || !optional.isPresent()) {
			return;
		}

		responseJSONObject.put(
			JSONKeys.AGGREGATIONS,
			_getAggregationsJSONObject(aggregations, optional.get(), messages));
	}

	private void _addResult(
		JSONObject responseJSONObject, AggregationResult aggregationResult,
		String aggregationName, String type, Messages messages) {

		try {
			AggregationJSONTranslator aggregationResponseBuilder =
				_aggregationResponseBuilderFactory.getTranslator(type);

			Optional<JSONObject> aggregationJsonOptional =
				aggregationResponseBuilder.translate(aggregationResult);

			if (aggregationJsonOptional.isPresent()) {
				responseJSONObject.put(
					aggregationName, aggregationJsonOptional.get());
			}
		}
		catch (IllegalArgumentException illegalArgumentException) {
			MessagesUtil.invalidConfigurationValueError(
				messages, getClass().getName(), illegalArgumentException, null,
				null, type);
		}
	}

	private JSONObject _getAggregationsJSONObject(
		Map<String, AggregationResult> aggregations, JSONObject jsonObject,
		Messages messages) {

		JSONObject responseJSONObject = _jsonFactory.createJSONObject();

		Set<String> keySet = jsonObject.keySet();

		keySet.forEach(
			aggregationName -> _processAggregation(
				responseJSONObject, aggregations, aggregationName, jsonObject,
				messages));

		return responseJSONObject;
	}

	private void _processAggregation(
		JSONObject responseJSONObject,
		Map<String, AggregationResult> aggregations, String aggregationName,
		JSONObject configurationJSONObject, Messages messages) {

		JSONObject nameJSONObject = configurationJSONObject.getJSONObject(
			aggregationName);

		Optional<String> typeOptional = BlueprintJSONUtil.getFirstKeyOptional(
			nameJSONObject);

		Set<Map.Entry<String, AggregationResult>> entrySet =
			aggregations.entrySet();

		Stream<Map.Entry<String, AggregationResult>> stream = entrySet.stream();

		stream.filter(
			entry -> StringUtil.equalsIgnoreCase(
				entry.getKey(), aggregationName)
		).forEach(
			entry -> _addResult(
				responseJSONObject, entry.getValue(), entry.getKey(),
				typeOptional.get(), messages)
		);
	}

	@Reference
	private AggregationJSONTranslatorFactory _aggregationResponseBuilderFactory;

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private JSONFactory _jsonFactory;

}