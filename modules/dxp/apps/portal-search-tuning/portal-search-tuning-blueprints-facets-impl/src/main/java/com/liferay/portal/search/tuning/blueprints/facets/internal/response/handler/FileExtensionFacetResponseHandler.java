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

package com.liferay.portal.search.tuning.blueprints.facets.internal.response.handler;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetJSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.facets.spi.response.FacetResponseHandler;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=file_extension",
	service = FacetResponseHandler.class
)
public class FileExtensionFacetResponseHandler
	extends BaseFacetResponseHandler implements FacetResponseHandler {

	@Override
	public Optional<JSONObject> getResultOptional(
		AggregationResult aggregationResult,
		BlueprintsAttributes blueprintsAttributes, Messages messages,
		JSONObject configurationJsonObject) {

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResult;

		JSONArray jsonArray = null;

		try {
			JSONObject handlerParametersJsonObject =
				configurationJsonObject.getJSONObject(
					FacetConfigurationKeys.HANDLER_PARAMETERS.getJsonKey());

			JSONArray aggregationsJsonArray =
				handlerParametersJsonObject.getJSONArray(
					FacetConfigurationKeys.VALUE_AGGREGATIONS.getJsonKey());

			Map<String, Integer> termsMap = new HashMap<>();

			for (Bucket bucket : termsAggregationResult.getBuckets()) {
				if (Validator.isBlank(bucket.getKey())) {
					continue;
				}

				boolean mappingFound = false;

				for (int i = 0; i < aggregationsJsonArray.length(); i++) {
					JSONObject aggregationJsonObject =
						aggregationsJsonArray.getJSONObject(i);

					String key = aggregationJsonObject.getString(
						FacetConfigurationKeys.VALUE_AGGREGATION_KEY.
							getJsonKey());
					String[] values = aggregationJsonObject.getString(
						FacetConfigurationKeys.VALUE_AGGREGATION_VALUES.
							getJsonKey()
					).split(
						","
					);

					for (String val : values) {
						if (StringUtil.equals(val, bucket.getKey())) {
							if (termsMap.get(key) != null) {
								int newValue =
									termsMap.get(key) +
										(int)bucket.getDocCount();

								termsMap.put(key, newValue);
							}
							else {
								termsMap.put(key, (int)bucket.getDocCount());
							}

							mappingFound = true;
						}
					}
				}

				if (!mappingFound) {
					termsMap.put(bucket.getKey(), (int)bucket.getDocCount());
				}
			}

			Map<String, Integer> termMapOrdered = _sort(termsMap);

			jsonArray = _getTermsJSONArray(termMapOrdered);
		}
		catch (Exception exception) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.unknown-exception",
					exception.getMessage(), exception, configurationJsonObject,
					null, null));

			_log.error(exception.getMessage(), exception);
		}

		return createResultObject(jsonArray, configurationJsonObject);
	}

	private JSONArray _getTermsJSONArray(Map<String, Integer> termsMap) {
		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (Map.Entry<String, Integer> entry : termsMap.entrySet()) {
			jsonArray.put(
				JSONUtil.put(
					FacetJSONResponseKeys.FREQUENCY, entry.getValue()
				).put(
					FacetJSONResponseKeys.NAME, entry.getKey()
				).put(
					FacetJSONResponseKeys.VALUE, entry.getKey()
				));
		}

		return jsonArray;
	}

	private Map<String, Integer> _sort(Map<String, Integer> termsMap)
		throws Exception {

		Set<Map.Entry<String, Integer>> entrySet = termsMap.entrySet();

		Stream<Map.Entry<String, Integer>> stream = entrySet.stream();

		return stream.sorted(
			Map.Entry.comparingByValue(Comparator.reverseOrder())
		).collect(
			Collectors.toMap(
				Map.Entry::getKey, Map.Entry::getValue,
				(oldValue, newValue) -> oldValue, LinkedHashMap::new)
		);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FileExtensionFacetResponseHandler.class);

}