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
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.RangeAggregationResult;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.facets.internal.util.FacetConfigurationUtil;
import com.liferay.search.experiences.blueprints.facets.spi.response.FacetResponseHandler;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=date_range",
	service = FacetResponseHandler.class
)
public class DateRangeFacetResponseHandler
	extends BaseTermsFacetResponseHandler implements FacetResponseHandler {

	@Override
	public Optional<JSONObject> getResultOptional(
		AggregationResult aggregationResult, String type,
		BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle, Messages messages,
		JSONObject jsonObject) {

		RangeAggregationResult rangeAggregationResult =
			(RangeAggregationResult)aggregationResult;

		Collection<Bucket> buckets = rangeAggregationResult.getBuckets();

		if (buckets.isEmpty()) {
			return Optional.empty();
		}

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		List<String> excludeValues = FacetConfigurationUtil.getExcludeValues(
			jsonObject);

		List<String> includeValues = FacetConfigurationUtil.getIncludeValues(
			jsonObject);

		for (Bucket bucket : buckets) {
			if (!FacetConfigurationUtil.includeValue(
					bucket.getKey(), includeValues, excludeValues)) {

				continue;
			}

			try {
				jsonArray.put(
					createBucketJSONObject(
						bucket, blueprintsAttributes, resourceBundle,
						messages));
			}
			catch (Exception exception) {
				MessagesUtil.error(
					messages, getClass().getName(), exception, jsonObject, null,
					null, "facets.error.unknown-error");
			}
		}

		return createResultObject(jsonArray, type, jsonObject, resourceBundle);
	}

}