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

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.metrics.TopHitsAggregation;
import com.liferay.portal.search.highlight.Highlight;
import com.liferay.portal.search.script.Script;
import com.liferay.portal.search.script.ScriptField;
import com.liferay.portal.search.script.ScriptFieldBuilder;
import com.liferay.portal.search.script.Scripts;
import com.liferay.search.experiences.blueprints.constants.json.keys.aggregation.metric.TopHitsAggregationBodyConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.aggregation.AggregationWrapper;
import com.liferay.search.experiences.blueprints.engine.internal.aggregation.util.AggregationHelper;
import com.liferay.search.experiences.blueprints.engine.internal.util.HighlightHelper;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.aggregation.AggregationTranslator;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.util.util.SetterHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=top_hits",
	service = AggregationTranslator.class
)
public class TopHitsAggregationTranslator implements AggregationTranslator {

	@Override
	public Optional<AggregationWrapper> translate(
		String aggregationName, JSONObject jsonObject,
		ParameterData parameterData, Messages messages) {

		TopHitsAggregation aggregation = _aggregations.topHits(aggregationName);

		_setDocValueFields(aggregation, jsonObject);

		_setterHelper.setBooleanValue(
			jsonObject,
			TopHitsAggregationBodyConfigurationKeys.EXPLAIN.getJsonKey(),
			aggregation::setExplain);

		_setterHelper.setIntegerValue(
			jsonObject,
			TopHitsAggregationBodyConfigurationKeys.FROM.getJsonKey(),
			aggregation::setFrom);

		_setHighLight(aggregation, jsonObject, parameterData, messages);

		_setScriptFields(aggregation, jsonObject);

		_setterHelper.setIntegerValue(
			jsonObject,
			TopHitsAggregationBodyConfigurationKeys.SIZE.getJsonKey(),
			aggregation::setSize);

		_setSource(aggregation, jsonObject);

		_setterHelper.setBooleanValue(
			jsonObject,
			TopHitsAggregationBodyConfigurationKeys.TRACK_SCORES.getJsonKey(),
			aggregation::setTrackScores);

		_setterHelper.setBooleanValue(
			jsonObject,
			TopHitsAggregationBodyConfigurationKeys.VERSION.getJsonKey(),
			aggregation::setVersion);

		return _aggregationHelper.wrap(aggregation);
	}

	private String _getScriptFieldName(JSONObject scriptFieldJSONObject) {
		Iterator<String> iterator = scriptFieldJSONObject.keys();

		return iterator.next();
	}

	private void _setDocValueFields(
		TopHitsAggregation aggregation, JSONObject jsonObject) {

		JSONArray fieldsJSONArray = jsonObject.getJSONArray(
			TopHitsAggregationBodyConfigurationKeys.DOCVALUE_FIELDS.
				getJsonKey());

		if (fieldsJSONArray == null) {
			return;
		}

		aggregation.setSelectedFields(JSONUtil.toStringList(fieldsJSONArray));
	}

	private void _setHighLight(
		TopHitsAggregation aggregation, JSONObject jsonObject,
		ParameterData parameterData, Messages messages) {

		JSONObject hightlightJSONObject = jsonObject.getJSONObject(
			TopHitsAggregationBodyConfigurationKeys.HIGHLIGHT.getJsonKey());

		if (hightlightJSONObject == null) {
			return;
		}

		Optional<Highlight> optional = _highlightHelper.getHighlight(
			hightlightJSONObject, parameterData, messages);

		if (optional.isPresent()) {
			aggregation.setHighlight(optional.get());
		}
	}

	private void _setScriptFields(
		TopHitsAggregation aggregation, JSONObject jsonObject) {

		JSONArray jsonArray = jsonObject.getJSONArray(
			TopHitsAggregationBodyConfigurationKeys.SCRIPT_FIELDS.getJsonKey());

		if (jsonArray == null) {
			return;
		}

		List<ScriptField> scriptFields = new ArrayList<>();

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject scriptFieldJSONObject = jsonArray.getJSONObject(i);

			String fieldName = _getScriptFieldName(scriptFieldJSONObject);

			Optional<Script> optional = _aggregationHelper.getScript(
				scriptFieldJSONObject.get(fieldName));

			if (optional.isPresent()) {
				ScriptFieldBuilder scriptFieldBuilder = _scripts.fieldBuilder();

				scriptFieldBuilder.script(optional.get());

				scriptFields.add(scriptFieldBuilder.build());
			}
		}

		aggregation.setScriptFields(scriptFields);
	}

	private void _setSource(
		TopHitsAggregation aggregation, JSONObject jsonObject) {

		Object object = jsonObject.get(
			TopHitsAggregationBodyConfigurationKeys.SOURCE.getJsonKey());

		if (Objects.isNull(object)) {
			return;
		}

		if (object instanceof JSONObject) {
			JSONObject sourceJSONObject = (JSONObject)object;

			if (sourceJSONObject.length() == 0) {
				return;
			}

			_setSourceIncludeExclude(aggregation, sourceJSONObject);
		}
		else {
			aggregation.setFetchSource(GetterUtil.getBoolean(object));
		}
	}

	private void _setSourceIncludeExclude(
		TopHitsAggregation aggregation, JSONObject jsonObject) {

		String[] excludesArray = null;

		JSONArray excludesJSONArray = jsonObject.getJSONArray("excludes");

		if (excludesJSONArray != null) {
			excludesArray = JSONUtil.toStringArray(excludesJSONArray);
		}

		String[] includesArray = null;

		JSONArray includesJSONArray = jsonObject.getJSONArray("includes");

		if (includesJSONArray != null) {
			includesArray = JSONUtil.toStringArray(includesJSONArray);
		}

		aggregation.setFetchSourceIncludeExclude(includesArray, excludesArray);
	}

	@Reference
	private AggregationHelper _aggregationHelper;

	@Reference
	private Aggregations _aggregations;

	@Reference
	private HighlightHelper _highlightHelper;

	@Reference
	private Scripts _scripts;

	@Reference
	private SetterHelper _setterHelper;

}