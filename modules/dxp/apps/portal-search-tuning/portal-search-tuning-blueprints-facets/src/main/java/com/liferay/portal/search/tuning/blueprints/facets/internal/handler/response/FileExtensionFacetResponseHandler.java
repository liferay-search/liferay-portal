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

package com.liferay.portal.search.tuning.blueprints.facets.internal.handler.response;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.spi.facet.FacetRequestHandler;
import com.liferay.portal.search.tuning.blueprints.engine.spi.facet.FacetResponseHandler;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetJSONResponseKeys;

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
	public Optional<JSONObject> getResultObject(
		SearchRequestContext searchRequestContext,
		AggregationResult aggregationResult,
		JSONObject configurationJsonObject) {

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResult;

		JSONArray termsArray = null;

		try {
			JSONObject handlerParametersJsonObject =
				configurationJsonObject.getJSONObject(
					FacetConfigurationKeys.HANDLER_PARAMETERS.getJsonKey());

			JSONArray aggregations = handlerParametersJsonObject.getJSONArray(
				FacetConfigurationKeys.VALUE_AGGREGATIONS.getJsonKey());

			Map<String, Integer> termsMap = new HashMap<>();

			for (Bucket bucket : termsAggregationResult.getBuckets()) {
				if (Validator.isNull(bucket.getKey())) {
					continue;
				}

				boolean mappingFound = false;

				for (int i = 0; i < aggregations.length(); i++) {
					JSONObject aggregation = aggregations.getJSONObject(i);

					String key = aggregation.getString(
						FacetConfigurationKeys.VALUE_AGGREGATION_KEY.
							getJsonKey());
					String[] values = aggregation.getString(
						FacetConfigurationKeys.VALUE_AGGREGATION_VALUES.
							getJsonKey()
					).split(
						","
					);

					for (int j = 0; j < values.length; j++) {
						if (values[j].equals(bucket.getKey())) {
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

			termsArray = _createTermsArray(termMapOrdered);
		}
		catch (Exception exception) {
			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.unknown-exception",
					exception.getMessage(), exception, configurationJsonObject,
					null, null));

			_log.error(exception.getMessage(), exception);
		}

		return createResultObject(termsArray, configurationJsonObject);
	}

	private JSONArray _createTermsArray(Map<String, Integer> termsMap) {
		JSONArray termArray = JSONFactoryUtil.createJSONArray();

		for (Map.Entry<String, Integer> entry : termsMap.entrySet()) {
			JSONObject item = JSONFactoryUtil.createJSONObject();

			item.put(FacetJSONResponseKeys.FREQUENCY, entry.getValue());
			item.put(FacetJSONResponseKeys.NAME, entry.getKey());
			item.put(FacetJSONResponseKeys.VALUE, entry.getKey());

			termArray.put(item);
		}

		return termArray;
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