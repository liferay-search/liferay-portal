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

package com.liferay.search.experiences.blueprints.engine.internal.aggregation.translator.bucket;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.DateHistogramAggregation;
import com.liferay.search.experiences.blueprints.constants.json.keys.aggregation.bucket.DateHistogramAggregationBodyConfigurationKeys;
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
	immediate = true, property = "name=date_histogram",
	service = AggregationTranslator.class
)
public class DateHistogramAggregationTranslator
	implements AggregationTranslator {

	@Override
	public Optional<AggregationWrapper> translate(
		String aggregationName, JSONObject jsonObject,
		ParameterData parameterData, Messages messages) {

		DateHistogramAggregation aggregation = _aggregations.dateHistogram(
			aggregationName,
			jsonObject.getString(
				DateHistogramAggregationBodyConfigurationKeys.FIELD.
					getJsonKey()));

		_aggregationHelper.setLongBounds(jsonObject, aggregation::setBounds);

		_setterHelper.setStringValue(
			jsonObject,
			DateHistogramAggregationBodyConfigurationKeys.
				DATE_HISTOGRAM_INTERVAL.getJsonKey(),
			aggregation::setDateHistogramInterval);

		_setterHelper.setBooleanValue(
			jsonObject,
			DateHistogramAggregationBodyConfigurationKeys.KEYED.getJsonKey(),
			aggregation::setKeyed);

		_setterHelper.setLongValue(
			jsonObject,
			DateHistogramAggregationBodyConfigurationKeys.MIN_DOC_COUNT.
				getJsonKey(),
			aggregation::setMinDocCount);

		_setterHelper.setStringValue(
			jsonObject,
			DateHistogramAggregationBodyConfigurationKeys.MISSING.getJsonKey(),
			aggregation::setMissing);

		_setterHelper.setLongValue(
			jsonObject,
			DateHistogramAggregationBodyConfigurationKeys.OFFSET.getJsonKey(),
			aggregation::setOffset);

		_aggregationHelper.setOrders(
			jsonObject, aggregation::addOrders, messages);

		_aggregationHelper.setScript(
			jsonObject, aggregation::setScript, messages);

		return _aggregationHelper.wrap(aggregation);
	}

	@Reference
	private AggregationHelper _aggregationHelper;

	@Reference
	private Aggregations _aggregations;

	@Reference
	private SetterHelper _setterHelper;

}