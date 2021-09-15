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

package com.liferay.search.experiences.searchresponse.json.translator.internal.aggregations;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.AggregationResults;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.problems.InvalidConfigurationValueException;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;
import com.liferay.search.experiences.searchresponse.json.translator.spi.aggregation.AggregationJSONTranslator;
import com.liferay.search.experiences.searchresponse.json.translator.spi.contributor.JSONTranslationContributor;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=aggs",
	service = JSONTranslationContributor.class
)
public class AggregationsJSONTranslationContributor
	implements JSONTranslationContributor {

	@Override
	public void contribute(
		JSONObject responseJSONObject, SearchResponse searchResponse,
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle,
		ProblemsHolderBuilder problemsHolderBuilder) {

		problemsHolderBuilder = null;

		Map<String, AggregationResult> aggregationResultsMap =
			searchResponse.getAggregationResultsMap();

		if (aggregationResultsMap.isEmpty()) {
			return;
		}

		RuntimeException runtimeException = new RuntimeException();

		JSONObject aggregationsJSONObject = _jsonFactory.createJSONObject();

		aggregationResultsMap.forEach(
			(name, aggregationResult) -> {
				try {
					_addResult(
						aggregationsJSONObject, name, aggregationResult,
						blueprint);
				}
				catch (Exception exception) {
					runtimeException.addSuppressed(exception);
				}
			});

		responseJSONObject.put("aggregations", aggregationsJSONObject);

		if (!ArrayUtil.isEmpty(runtimeException.getSuppressed())) {
			throw runtimeException;
		}
	}

	@Reference(unbind = "-")
	public void setAggregationJSONTranslatorsHolder(
		AggregationJSONTranslatorsHolder aggregationJSONTranslatorsHolder) {

		_aggregationJSONTranslatorsHolder = aggregationJSONTranslatorsHolder;
	}

	@Reference(unbind = "-")
	public void setJSONFactory(JSONFactory jsonFactory) {
		_jsonFactory = jsonFactory;
	}

	protected String getType(String aggregationName, Blueprint blueprint) {
		if (blueprint == null) {
			return null;
		}

		Optional<JSONObject> optional =
			_blueprintHelper.getAggsConfigurationOptional(blueprint);

		return optional.map(
			jsonObject1 -> {
				JSONObject jsonObject2 = jsonObject1.getJSONObject(
					aggregationName);

				if (jsonObject2 == null) {
					return null;
				}

				Iterator<String> iterator = jsonObject2.keys();

				return iterator.next();
			}
		).orElse(
			null
		);
	}

	protected Optional<JSONObject> translate(
		AggregationResult aggregationResult, String type) {

		AggregationJSONTranslator aggregationJSONTranslator =
			_aggregationJSONTranslatorsHolder.getTranslator(type);

		if (aggregationJSONTranslator != null) {
			return aggregationJSONTranslator.translate(aggregationResult);
		}

		if (_log.isWarnEnabled()) {
			_log.warn(
				StringBundler.concat(
					"No registered handler for ", type,
					". Falling back to default"));
		}

		try {
			String json = _jsonFactory.looseSerializeDeep(aggregationResult);

			return Optional.of(_jsonFactory.createJSONObject(json));
		}
		catch (JSONException jsonException) {
			throw new RuntimeException(jsonException);
		}
	}

	private void _addResult(
		JSONObject aggregationsJSONObject, String aggregationName,
		AggregationResult aggregationResult, Blueprint blueprint) {

		String type = getType(aggregationName, blueprint);

		try {
			Optional<JSONObject> optional = translate(aggregationResult, type);

			optional.ifPresent(
				jsonObject -> aggregationsJSONObject.put(
					aggregationName, jsonObject));
		}
		catch (IllegalArgumentException illegalArgumentException) {
			_log.error(illegalArgumentException);

			throw InvalidConfigurationValueException.ofClassAndType(
				getClass().getName(), type, illegalArgumentException);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AggregationsJSONTranslationContributor.class);

	private AggregationJSONTranslatorsHolder _aggregationJSONTranslatorsHolder;

	@Reference
	private AggregationResults _aggregations;

	@Reference
	private BlueprintHelper _blueprintHelper;

	private JSONFactory _jsonFactory;

}