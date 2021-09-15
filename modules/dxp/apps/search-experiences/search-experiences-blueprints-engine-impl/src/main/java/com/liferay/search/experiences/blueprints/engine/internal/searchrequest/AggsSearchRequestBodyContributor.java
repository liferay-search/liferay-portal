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

package com.liferay.search.experiences.blueprints.engine.internal.searchrequest;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.aggregation.Aggregation;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.engine.aggregation.AggregationWrapper;
import com.liferay.search.experiences.blueprints.engine.internal.aggregation.AggregationTranslatorFactory;
import com.liferay.search.experiences.blueprints.engine.internal.util.BlueprintJSONUtil;
import com.liferay.search.experiences.blueprints.engine.internal.util.ProblemBuilders;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.aggregation.AggregationTranslator;
import com.liferay.search.experiences.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.search.experiences.blueprints.engine.template.variable.BlueprintTemplateVariableParser;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.util.Optional;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=aggs",
	service = SearchRequestBodyContributor.class
)
public class AggsSearchRequestBodyContributor
	implements SearchRequestBodyContributor {

	@Override
	public void contribute(
		SearchRequestBuilder searchRequestBuilder, Blueprint blueprint,
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<JSONObject> optional =
			_blueprintHelper.getAggsConfigurationOptional(blueprint);

		if (!optional.isPresent()) {
			return;
		}

		_processAggregations(
			searchRequestBuilder, null, optional.get(), parameterData,
			problemsHolderBuilder);
	}

	private void _addAggregation(
		SearchRequestBuilder searchRequestBuilder,
		AggregationWrapper aggregationWrapper) {

		if (aggregationWrapper.isPipeline()) {
			searchRequestBuilder.addPipelineAggregation(
				aggregationWrapper.getPipelineAggregation());
		}
		else {
			searchRequestBuilder.addAggregation(
				aggregationWrapper.getAggregation());
		}
	}

	private void _addChildAggregation(
		AggregationWrapper parentAggregationWrapper,
		AggregationWrapper childAggregationWrapper) {

		if (!parentAggregationWrapper.isPipeline()) {
			Aggregation aggregation = parentAggregationWrapper.getAggregation();

			if (childAggregationWrapper.isPipeline()) {
				aggregation.addPipelineAggregation(
					childAggregationWrapper.getPipelineAggregation());
			}
			else {
				aggregation.addChildAggregation(
					childAggregationWrapper.getAggregation());
			}
		}
	}

	private Optional<AggregationWrapper> _getAggregationOptional(
		String name, String type, JSONObject jsonObject,
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		if (!_isEnabled(jsonObject)) {
			return Optional.empty();
		}

		Optional<JSONObject> optional =
			_blueprintTemplateVariableParser.parseObject(
				jsonObject, parameterData, problemsHolderBuilder);

		if (!optional.isPresent()) {
			return Optional.empty();
		}

		AggregationTranslator aggregationTranslator =
			_aggregationTranslatorFactory.getTranslator(type);

		return aggregationTranslator.translate(
			name, optional.get(), parameterData, problemsHolderBuilder);
	}

	private boolean _isEnabled(JSONObject jsonObject) {
		return jsonObject.getBoolean("enabled", true);
	}

	private void _processAggregation(
		SearchRequestBuilder searchRequestBuilder,
		AggregationWrapper parentAggregationWrapper, String aggregationName,
		JSONObject jsonObject, ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		JSONObject nameJSONObject = jsonObject.getJSONObject(aggregationName);

		Optional<String> typeOptional = BlueprintJSONUtil.getFirstKeyOptional(
			nameJSONObject);

		if (!typeOptional.isPresent()) {
			return;
		}

		String type = typeOptional.get();

		JSONObject typeJSONObject = nameJSONObject.getJSONObject(type);

		AggregationWrapper aggregationWrapper;

		try {
			Optional<AggregationWrapper> aggregationWrapperOptional =
				_getAggregationOptional(
					aggregationName, type, typeJSONObject, parameterData,
					problemsHolderBuilder);

			if (!aggregationWrapperOptional.isPresent()) {
				return;
			}

			aggregationWrapper = aggregationWrapperOptional.get();
		}
		catch (IllegalArgumentException illegalArgumentException) {
			_log.error(illegalArgumentException);

			ProblemBuilder problemBuilder =
				_problemBuilders.invalidConfigurationValueError(
					getClass().getName(), illegalArgumentException,
					nameJSONObject, null, type);

			problemsHolderBuilder.addProblem(problemBuilder.build());

			return;
		}

		if (!aggregationWrapper.isPipeline()) {
			JSONObject aggsJSONObject = nameJSONObject.getJSONObject("aggs");

			if (aggsJSONObject != null) {
				_processAggregations(
					searchRequestBuilder, aggregationWrapper, aggsJSONObject,
					parameterData, problemsHolderBuilder);
			}
		}

		if (parentAggregationWrapper == null) {
			_addAggregation(searchRequestBuilder, aggregationWrapper);
		}
		else {
			_addChildAggregation(parentAggregationWrapper, aggregationWrapper);
		}
	}

	private void _processAggregations(
		SearchRequestBuilder searchRequestBuilder,
		AggregationWrapper parentAggregationWrapper,
		JSONObject aggregationJSONObject, ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Set<String> keySet = aggregationJSONObject.keySet();

		keySet.forEach(
			aggregationName -> _processAggregation(
				searchRequestBuilder, parentAggregationWrapper, aggregationName,
				aggregationJSONObject, parameterData, problemsHolderBuilder));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AggsSearchRequestBodyContributor.class);

	@Reference
	private AggregationTranslatorFactory _aggregationTranslatorFactory;

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private ProblemBuilders _problemBuilders;

}