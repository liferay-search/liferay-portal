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

package com.liferay.search.experiences.blueprints.engine.internal.aggregation.translator.metric;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.metrics.PercentilesAggregation;
import com.liferay.portal.search.aggregation.metrics.PercentilesMethod;
import com.liferay.search.experiences.blueprints.constants.json.keys.aggregation.metric.PercentileRanksAggregationBodyConfigurationKeys;
import com.liferay.search.experiences.blueprints.constants.json.keys.aggregation.metric.PercentilesAggregationBodyConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.aggregation.AggregationWrapper;
import com.liferay.search.experiences.blueprints.engine.internal.aggregation.util.AggregationHelper;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.aggregation.AggregationTranslator;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.util.util.SetterHelper;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=percentiles",
	service = AggregationTranslator.class
)
public class PercentilesAggregationTranslator implements AggregationTranslator {

	@Override
	public Optional<AggregationWrapper> translate(
		String aggregationName, JSONObject jsonObject,
		ParameterData parameterData, Messages messages) {

		PercentilesAggregation aggregation = _aggregations.percentiles(
			aggregationName,
			jsonObject.getString(
				PercentilesAggregationBodyConfigurationKeys.FIELD.
					getJsonKey()));

		_setterHelper.setBooleanValue(
			jsonObject,
			PercentileRanksAggregationBodyConfigurationKeys.KEYED.getJsonKey(),
			aggregation::setKeyed);

		_setMethod(aggregation, jsonObject);

		_setterHelper.setStringValue(
			jsonObject,
			PercentileRanksAggregationBodyConfigurationKeys.MISSING.
				getJsonKey(),
			aggregation::setMissing);

		_setterHelper.setDoubleArrayValue(
			jsonObject,
			PercentilesAggregationBodyConfigurationKeys.PERCENTS.getJsonKey(),
			aggregation::setPercents);

		_aggregationHelper.setScript(
			jsonObject, aggregation::setScript, messages);

		return _aggregationHelper.wrap(aggregation);
	}

	private void _setMethod(
		PercentilesAggregation aggregation, JSONObject jsonObject) {

		Integer digits = _aggregationHelper.getHdrSignificantValueDigits(
			jsonObject);

		if (digits != null) {
			aggregation.setPercentilesMethod(PercentilesMethod.HDR);
			aggregation.setHdrSignificantValueDigits(
				jsonObject.getInt("number_of_significant_value_digits"));
		}
		else {
			Integer compression = _aggregationHelper.getTDigestCompression(
				jsonObject);

			if (compression != null) {
				aggregation.setPercentilesMethod(PercentilesMethod.TDIGEST);
				aggregation.setCompression(jsonObject.getInt("compression"));
			}
		}
	}

	@Reference
	private AggregationHelper _aggregationHelper;

	@Reference
	private Aggregations _aggregations;

	@Reference
	private SetterHelper _setterHelper;

}