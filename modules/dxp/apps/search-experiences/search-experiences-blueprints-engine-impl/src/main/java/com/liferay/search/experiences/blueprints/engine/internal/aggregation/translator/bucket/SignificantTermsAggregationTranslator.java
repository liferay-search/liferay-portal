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
import com.liferay.portal.search.aggregation.bucket.SignificantTermsAggregation;
import com.liferay.search.experiences.blueprints.constants.json.keys.aggregation.bucket.SignificantTermsAggregationBodyConfigurationKeys;
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
	immediate = true, property = "name=significant_terms",
	service = AggregationTranslator.class
)
public class SignificantTermsAggregationTranslator
	implements AggregationTranslator {

	@Override
	public Optional<AggregationWrapper> translate(
		String aggregationName, JSONObject jsonObject,
		ParameterData parameterData, Messages messages) {

		SignificantTermsAggregation aggregation =
			_aggregations.significantTerms(
				aggregationName,
				jsonObject.getString(
					SignificantTermsAggregationBodyConfigurationKeys.FIELD.
						getJsonKey()));

		_aggregationHelper.setBackgroundFilter(
			jsonObject, aggregation::setBackgroundFilterQuery, parameterData,
			messages);

		_setterHelper.setStringValue(
			jsonObject,
			SignificantTermsAggregationBodyConfigurationKeys.EXECUTION_HINT.
				getJsonKey(),
			aggregation::setExecutionHint);

		_aggregationHelper.setIncludeExcludeClause(
			jsonObject, aggregation::setIncludeExcludeClause);

		_setterHelper.setLongValue(
			jsonObject,
			SignificantTermsAggregationBodyConfigurationKeys.MIN_DOC_COUNT.
				getJsonKey(),
			aggregation::setMinDocCount);

		_setterHelper.setStringValue(
			jsonObject,
			SignificantTermsAggregationBodyConfigurationKeys.MISSING.
				getJsonKey(),
			aggregation::setMissing);

		_aggregationHelper.setScript(
			jsonObject, aggregation::setScript, messages);

		_setterHelper.setLongValue(
			jsonObject,
			SignificantTermsAggregationBodyConfigurationKeys.
				SHARD_MIN_DOC_COUNT.getJsonKey(),
			aggregation::setShardMinDocCount);

		_setterHelper.setIntegerValue(
			jsonObject,
			SignificantTermsAggregationBodyConfigurationKeys.SHARD_SIZE.
				getJsonKey(),
			aggregation::setShardSize);

		_aggregationHelper.setSignificanceHeuristics(
			jsonObject, aggregation::setSignificanceHeuristic, messages);

		_setterHelper.setIntegerValue(
			jsonObject,
			SignificantTermsAggregationBodyConfigurationKeys.SIZE.getJsonKey(),
			aggregation::setSize);

		return _aggregationHelper.wrap(aggregation);
	}

	@Reference
	private AggregationHelper _aggregationHelper;

	@Reference
	private Aggregations _aggregations;

	@Reference
	private SetterHelper _setterHelper;

}