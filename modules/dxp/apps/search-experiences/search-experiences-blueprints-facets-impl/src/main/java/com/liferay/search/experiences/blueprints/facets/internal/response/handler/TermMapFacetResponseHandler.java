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

package com.liferay.search.experiences.blueprints.facets.internal.response.handler;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.facets.internal.util.FacetConfigurationUtil;
import com.liferay.search.experiences.blueprints.facets.spi.response.FacetResponseHandler;
import com.liferay.search.experiences.problems.ProblemBuilders;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=term_map",
	service = FacetResponseHandler.class
)
public class TermMapFacetResponseHandler
	extends BaseTermsFacetResponseHandler implements FacetResponseHandler {

	@Override
	public Optional<JSONObject> getResultOptional(
		AggregationResult aggregationResult, String type,
		BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle,
		ProblemsHolderBuilder problemsHolderBuilder, JSONObject jsonObject) {

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResult;

		JSONArray jsonArray = null;

		try {
			JSONObject parametersJSONObject = jsonObject.getJSONObject(
				"parameters");

			JSONArray aggregationsJSONArray = parametersJSONObject.getJSONArray(
				"map");

			Map<String, Integer> termsMap = new HashMap<>();

			List<String> excludeValues =
				FacetConfigurationUtil.getExcludeValues(jsonObject);

			List<String> includeValues =
				FacetConfigurationUtil.getIncludeValues(jsonObject);

			for (Bucket bucket : termsAggregationResult.getBuckets()) {
				if (Validator.isBlank(bucket.getKey()) ||
					!FacetConfigurationUtil.includeValue(
						bucket.getKey(), includeValues, excludeValues)) {

					continue;
				}

				boolean mappingFound = false;

				for (int i = 0; i < aggregationsJSONArray.length(); i++) {
					JSONObject aggregationJSONObject =
						aggregationsJSONArray.getJSONObject(i);

					String key = aggregationJSONObject.getString("key");
					JSONArray valuesJSONArray =
						aggregationJSONObject.getJSONArray("values");

					for (int j = 0; j < valuesJSONArray.length(); j++) {
						String val = valuesJSONArray.getString(j);

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

			long frequencyThreshold = jsonObject.getLong("min_doc_count", 1);

			jsonArray = _getTermsJSONArray(
				termMapOrdered, frequencyThreshold, resourceBundle);
		}
		catch (Exception exception) {
			problemsHolderBuilder.addProblem(
				_problemBuilders.error(
					getClass().getName(), "facets.error.unknown-error",
					jsonObject, null, null, exception
				).build());
		}

		return createResultObject(jsonArray, type, jsonObject, resourceBundle);
	}

	private JSONArray _getTermsJSONArray(
		Map<String, Integer> termsMap, long frequencyThreshold,
		ResourceBundle resourceBundle) {

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (Map.Entry<String, Integer> entry : termsMap.entrySet()) {
			long frequency = entry.getValue();

			if (frequency < frequencyThreshold) {
				continue;
			}

			String value = entry.getKey();

			jsonArray.put(
				createBucketJSONObject(
					frequency, null, value,
					getText(value, frequency, resourceBundle), value));
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

	@Reference
	private ProblemBuilders _problemBuilders;

}