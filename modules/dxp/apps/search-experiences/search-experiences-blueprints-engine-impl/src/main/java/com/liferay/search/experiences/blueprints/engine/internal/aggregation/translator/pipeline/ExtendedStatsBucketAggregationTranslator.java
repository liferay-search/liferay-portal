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

package com.liferay.search.experiences.blueprints.engine.internal.aggregation.translator.pipeline;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.pipeline.ExtendedStatsBucketPipelineAggregation;
import com.liferay.search.experiences.blueprints.constants.json.keys.aggregation.pipeline.ExtendedStatsBucketAggregationBodyConfigurationKeys;
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
	immediate = true, property = "name=extended_stats_bucket",
	service = AggregationTranslator.class
)
public class ExtendedStatsBucketAggregationTranslator
	implements AggregationTranslator {

	@Override
	public Optional<AggregationWrapper> translate(
		String aggregationName, JSONObject jsonObject,
		ParameterData parameterData, Messages messages) {

		ExtendedStatsBucketPipelineAggregation aggregation =
			_aggregations.extendedStatsBucket(
				aggregationName,
				jsonObject.getString(
					ExtendedStatsBucketAggregationBodyConfigurationKeys.
						BUCKETS_PATH.getJsonKey()));

		_setterHelper.setStringValue(
			jsonObject,
			ExtendedStatsBucketAggregationBodyConfigurationKeys.FORMAT.
				getJsonKey(),
			aggregation::setFormat);

		_aggregationHelper.setGapPolicy(
			jsonObject, aggregation::setGapPolicy, messages);

		_setterHelper.setDoubleValue(
			jsonObject,
			ExtendedStatsBucketAggregationBodyConfigurationKeys.SIGMA.
				getJsonKey(),
			aggregation::setSigma);

		return _aggregationHelper.wrap(aggregation);
	}

	@Reference
	private AggregationHelper _aggregationHelper;

	@Reference
	private Aggregations _aggregations;

	@Reference
	private SetterHelper _setterHelper;

}