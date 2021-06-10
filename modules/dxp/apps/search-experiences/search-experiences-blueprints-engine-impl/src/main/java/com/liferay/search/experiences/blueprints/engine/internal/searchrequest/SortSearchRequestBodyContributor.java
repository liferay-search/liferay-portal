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

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.sort.ScoreSort;
import com.liferay.portal.search.sort.Sort;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.search.experiences.blueprints.constants.json.keys.sort.SortConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.internal.sort.SortTranslatorFactory;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.search.experiences.blueprints.engine.spi.sort.SortTranslator;
import com.liferay.search.experiences.blueprints.engine.template.variable.BlueprintTemplateVariableParser;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.blueprints.util.util.BlueprintJSONUtil;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=sort",
	service = SearchRequestBodyContributor.class
)
public class SortSearchRequestBodyContributor
	implements SearchRequestBodyContributor {

	@Override
	public void contribute(
		SearchRequestBuilder searchRequestBuilder, Blueprint blueprint,
		ParameterData parameterData, Messages messages) {

		List<Sort> sorts = _getSortsFromParameters(
			parameterData, blueprint, messages);

		if (sorts.isEmpty()) {
			sorts = _getDefaultSorts(parameterData, blueprint, messages);
		}

		if (!sorts.isEmpty()) {
			Stream<Sort> stream = sorts.stream();

			stream.forEach(sort -> searchRequestBuilder.addSort(sort));
		}
	}

	private Optional<Sort> _getDefaultSort(
		ParameterData parameterData, Object object, Messages messages) {

		if (object instanceof String) {
			return _sortFromString((String)object, null);
		}

		Optional<JSONObject> optional =
			_blueprintTemplateVariableParser.parseObject(
				(JSONObject)object, parameterData, messages);

		if (!optional.isPresent()) {
			return Optional.empty();
		}

		return _sortFromObject(optional.get(), messages);
	}

	private List<Sort> _getDefaultSorts(
		ParameterData parameterData, Blueprint blueprint, Messages messages) {

		List<Sort> sorts = new ArrayList<>();

		Optional<JSONArray> optional1 =
			_blueprintHelper.getDefaultSortConfigurationOptional(blueprint);

		if (!optional1.isPresent()) {
			return sorts;
		}

		JSONArray jsonArray = optional1.get();

		for (int i = 0; i < jsonArray.length(); i++) {
			Optional<Sort> optional2 = _getDefaultSort(
				parameterData, jsonArray.get(i), messages);

			if (optional2.isPresent()) {
				sorts.add(optional2.get());
			}
		}

		return sorts;
	}

	private Optional<Sort> _getSort(
		JSONObject jsonObject, String key, SortOrder sortOrder,
		Messages messages) {

		try {
			SortTranslator sortTranslator;
			String field;

			if (_fixedTypes.contains(key)) {
				sortTranslator = _sortTranslatorFactory.getTranslator(key);
				field = null;
			}
			else {
				sortTranslator = _sortTranslatorFactory.getTranslator("field");
				field = key;
			}

			return sortTranslator.translate(
				jsonObject, field, sortOrder, messages);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			MessagesUtil.invalidConfigurationValueError(
				messages, getClass().getName(), illegalArgumentException,
				jsonObject, SortConfigurationKeys.TYPE.getJsonKey(), key);
		}

		return Optional.empty();
	}

	private Optional<Sort> _getSortFromParameter(
		ParameterData parameterData, JSONObject jsonObject, String key,
		Messages messages) {

		JSONObject configurationJSONObject = jsonObject.getJSONObject(key);

		Optional<JSONObject> optional =
			_blueprintTemplateVariableParser.parseObject(
				configurationJSONObject, parameterData, messages);

		if (!optional.isPresent()) {
			return Optional.empty();
		}

		JSONObject parsedJSONObject = optional.get();

		SortOrder sortOrder = _getSortOrderFromParameter(
			parameterData, parsedJSONObject, messages);

		if (sortOrder == null) {
			return Optional.empty();
		}

		return _getSort(parsedJSONObject, key, sortOrder, messages);
	}

	private SortOrder _getSortOrder(String s, Messages messages) {
		try {
			return SortOrder.valueOf(StringUtil.toUpperCase(s));
		}
		catch (IllegalArgumentException illegalArgumentException) {
			MessagesUtil.error(
				messages, getClass().getName(), illegalArgumentException, null,
				null, s, "core.error.invalid.sort-order");
		}

		return null;
	}

	private SortOrder _getSortOrderFromParameter(
		ParameterData parameterData, JSONObject jsonObject, Messages messages) {

		String parameterName = jsonObject.getString("parameter_name");

		if (Validator.isBlank(parameterName)) {
			return null;
		}

		Optional<Parameter> optional = parameterData.getByNameOptional(
			parameterName);

		if (!optional.isPresent()) {
			return null;
		}

		Parameter parameter = optional.get();

		return _getSortOrder(
			GetterUtil.getString(parameter.getValue()), messages);
	}

	private List<Sort> _getSortsFromParameters(
		ParameterData parameterData, Blueprint blueprint, Messages messages) {

		List<Sort> sorts = new ArrayList<>();

		Optional<JSONObject> optional1 =
			_blueprintHelper.getSortParameterConfigurationOptional(blueprint);

		if (!optional1.isPresent()) {
			return sorts;
		}

		JSONObject jsonObject = optional1.get();

		Set<String> keySet = jsonObject.keySet();

		keySet.forEach(
			key -> {
				Optional<Sort> optional2 = _getSortFromParameter(
					parameterData, jsonObject, key, messages);

				if (optional2.isPresent()) {
					sorts.add(optional2.get());
				}
			});

		return sorts;
	}

	private Optional<Sort> _sortFromObject(
		JSONObject jsonObject, Messages messages) {

		Optional<String> optional = BlueprintJSONUtil.getFirstKeyOptional(
			jsonObject);

		if (!optional.isPresent()) {
			return Optional.empty();
		}

		String key = optional.get();

		Object object = jsonObject.get(key);

		if (object instanceof String) {
			return _sortFromString(
				key, _getSortOrder((String)object, messages));
		}

		JSONObject configurationJSONObject = (JSONObject)object;

		return _getSort(
			(JSONObject)jsonObject, key,
			_getSortOrder(
				configurationJSONObject.getString(
					SortConfigurationKeys.ORDER.getJsonKey()),
				messages),
			messages);
	}

	private Optional<Sort> _sortFromString(String s, SortOrder sortOrder) {
		if (s.equals("_score")) {
			ScoreSort sort = _sorts.score();

			if (sortOrder != null) {
				sort.setSortOrder(sortOrder);
			}

			return Optional.of(sort);
		}

		Sort sort = _sorts.field(s);

		if (sortOrder != null) {
			sort.setSortOrder(sortOrder);
		}

		return Optional.of(sort);
	}

	private static final List<String> _fixedTypes = new ArrayList<>(
		Arrays.asList("_geo_distance", "_script", "_score"));

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private Sorts _sorts;

	@Reference
	private SortTranslatorFactory _sortTranslatorFactory;

}