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
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.CollectionMode;
import com.liferay.portal.search.aggregation.bucket.TermsAggregation;
import com.liferay.search.experiences.blueprints.constants.json.keys.aggregation.bucket.TermsAggregationBodyConfigurationKeys;
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
	immediate = true, property = "name=terms",
	service = AggregationTranslator.class
)
public class TermsAggregationTranslator implements AggregationTranslator {

	@Override
	public Optional<AggregationWrapper> translate(
		String aggregationName, JSONObject jsonObject,
		ParameterData parameterData, Messages messages) {

		TermsAggregation aggregation = _aggregations.terms(
			aggregationName,
			jsonObject.getString(
				TermsAggregationBodyConfigurationKeys.FIELD.getJsonKey()));

		_setCollectMode(aggregation, jsonObject);

		_setterHelper.setStringValue(
			jsonObject,
			TermsAggregationBodyConfigurationKeys.EXECUTION_HINT.getJsonKey(),
			aggregation::setExecutionHint);

		_aggregationHelper.setIncludeExcludeClause(
			jsonObject, aggregation::setIncludeExcludeClause);

		_setterHelper.setIntegerValue(
			jsonObject,
			TermsAggregationBodyConfigurationKeys.MIN_DOC_COUNT.getJsonKey(),
			aggregation::setMinDocCount);

		_setterHelper.setStringValue(
			jsonObject,
			TermsAggregationBodyConfigurationKeys.MISSING.getJsonKey(),
			aggregation::setMissing);

		_aggregationHelper.setOrders(
			jsonObject, aggregation::addOrders, messages);

		_aggregationHelper.setScript(
			jsonObject, aggregation::setScript, messages);

		_setterHelper.setIntegerValue(
			jsonObject,
			TermsAggregationBodyConfigurationKeys.SHARD_MIN_DOC_COUNT.
				getJsonKey(),
			aggregation::setShardMinDocCount);

		_setterHelper.setIntegerValue(
			jsonObject,
			TermsAggregationBodyConfigurationKeys.SHARD_SIZE.getJsonKey(),
			aggregation::setShardSize);

		_setterHelper.setBooleanValue(
			jsonObject,
			TermsAggregationBodyConfigurationKeys.SHOW_TERM_DOC_COUNT_ERROR.
				getJsonKey(),
			aggregation::setShowTermDocCountError);

		_setterHelper.setIntegerValue(
			jsonObject, TermsAggregationBodyConfigurationKeys.SIZE.getJsonKey(),
			aggregation::setSize);

		return _aggregationHelper.wrap(aggregation);
	}

	private void _setCollectMode(
		TermsAggregation aggregation, JSONObject jsonObject) {

		String collectModeString = jsonObject.getString(
			TermsAggregationBodyConfigurationKeys.COLLECT_MODE.getJsonKey());

		if (Validator.isBlank(collectModeString)) {
			return;
		}

		aggregation.setCollectionMode(
			CollectionMode.valueOf(StringUtil.toUpperCase(collectModeString)));
	}

	@Reference
	private AggregationHelper _aggregationHelper;

	@Reference
	private Aggregations _aggregations;

	@Reference
	private SetterHelper _setterHelper;

}