package com.liferay.portal.search.tuning.blueprints.facets.internal.response.handler;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.RangeAggregationResult;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetJSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.facets.spi.response.FacetResponseHandler;
import com.liferay.portal.search.tuning.blueprints.message.Messages;

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
	extends BaseFacetResponseHandler implements FacetResponseHandler {

	@Override
	public Optional<JSONObject> getResultOptional(
		AggregationResult aggregationResult,
		BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle, Messages messages,
		JSONObject configurationJsonObject) {

		RangeAggregationResult rangeAggregationResult =
			(RangeAggregationResult)aggregationResult;

		if (rangeAggregationResult.getBuckets(
			).size() == 0) {

			return Optional.empty();
		}

		long frequencyThreshold = configurationJsonObject.getLong(
			FacetConfigurationKeys.FREQUENCY_THRESHOLD.getJsonKey(), 1);

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (Bucket bucket : rangeAggregationResult.getBuckets()) {
			if (bucket.getDocCount() < frequencyThreshold) {
				continue;
			}

			long frequency = bucket.getDocCount();

			JSONObject jsonObject = JSONUtil.put(
				FacetJSONResponseKeys.FREQUENCY, frequency);

			String value = bucket.getKey();

			jsonObject.put(FacetJSONResponseKeys.VALUE, value);

			jsonObject.put(
				FacetJSONResponseKeys.TEXT,
				getText(value, frequency, resourceBundle));

			jsonArray.put(jsonObject);
		}

		return createResultObject(
			jsonArray, configurationJsonObject, resourceBundle);
	}

}