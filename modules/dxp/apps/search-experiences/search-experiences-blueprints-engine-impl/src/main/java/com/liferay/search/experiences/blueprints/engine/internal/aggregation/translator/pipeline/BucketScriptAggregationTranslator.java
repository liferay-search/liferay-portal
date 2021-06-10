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
import com.liferay.portal.search.aggregation.pipeline.BucketScriptPipelineAggregation;
import com.liferay.portal.search.script.Script;
import com.liferay.search.experiences.blueprints.constants.json.keys.aggregation.pipeline.BucketScriptAggregationBodyConfigurationKeys;
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
	immediate = true, property = "name=bucket_script",
	service = AggregationTranslator.class
)
public class BucketScriptAggregationTranslator
	implements AggregationTranslator {

	@Override
	public Optional<AggregationWrapper> translate(
		String aggregationName, JSONObject jsonObject,
		ParameterData parameterData, Messages messages) {

		Optional<Script> scriptOptional = _aggregationHelper.getScript(
			jsonObject.get(
				BucketScriptAggregationBodyConfigurationKeys.SCRIPT.
					getJsonKey()));

		if (!scriptOptional.isPresent()) {
			return Optional.empty();
		}

		BucketScriptPipelineAggregation aggregation =
			_aggregations.bucketScript(aggregationName, scriptOptional.get());

		_aggregationHelper.setBucketPaths(
			jsonObject, aggregation::addBucketPath, messages);

		_setterHelper.setStringValue(
			jsonObject,
			BucketScriptAggregationBodyConfigurationKeys.FORMAT.getJsonKey(),
			aggregation::setFormat);

		return _aggregationHelper.wrap(aggregation);
	}

	@Reference
	private AggregationHelper _aggregationHelper;

	@Reference
	private Aggregations _aggregations;

	@Reference
	private SetterHelper _setterHelper;

}