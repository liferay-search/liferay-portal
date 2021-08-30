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
import com.liferay.portal.search.aggregation.bucket.FiltersAggregation;
import com.liferay.portal.search.query.Query;
import com.liferay.search.experiences.blueprints.engine.aggregation.AggregationWrapper;
import com.liferay.search.experiences.blueprints.engine.internal.aggregation.util.AggregationHelper;
import com.liferay.search.experiences.blueprints.engine.internal.clause.util.ClauseHelper;
import com.liferay.search.experiences.blueprints.engine.internal.util.SetterHelper;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.aggregation.AggregationTranslator;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=filters",
	service = AggregationTranslator.class
)
public class FiltersAggregationTranslator implements AggregationTranslator {

	@Override
	public Optional<AggregationWrapper> translate(
		String aggregationName, JSONObject jsonObject,
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		FiltersAggregation aggregation = _aggregations.filters(
			aggregationName, jsonObject.getString("field"));

		_addQueries(
			aggregation, jsonObject, parameterData, problemsHolderBuilder);

		_setterHelper.setBooleanValue(
			jsonObject, "other_bucket", aggregation::setOtherBucket);

		_setterHelper.setStringValue(
			jsonObject, "other_bucket_key", aggregation::setOtherBucketKey);

		return _aggregationHelper.wrap(aggregation);
	}

	private void _addQueries(
		FiltersAggregation aggregation, JSONObject jsonObject,
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		JSONObject filtersJSONObject = jsonObject.getJSONObject("filters");

		if (filtersJSONObject == null) {
			return;
		}

		Set<String> keySet = filtersJSONObject.keySet();

		Stream<String> stream = keySet.stream();

		stream.forEach(
			key -> _addQuery(
				aggregation, key, filtersJSONObject.getJSONObject(key),
				parameterData, problemsHolderBuilder));
	}

	private void _addQuery(
		FiltersAggregation aggregation, String key, JSONObject jsonObject,
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<Query> optional = _clauseHelper.getQueryOptional(
			jsonObject, parameterData, problemsHolderBuilder);

		if (optional.isPresent()) {
			aggregation.addKeyedQuery(key, optional.get());
		}
	}

	@Reference
	private AggregationHelper _aggregationHelper;

	@Reference
	private Aggregations _aggregations;

	@Reference
	private ClauseHelper _clauseHelper;

	@Reference
	private SetterHelper _setterHelper;

}