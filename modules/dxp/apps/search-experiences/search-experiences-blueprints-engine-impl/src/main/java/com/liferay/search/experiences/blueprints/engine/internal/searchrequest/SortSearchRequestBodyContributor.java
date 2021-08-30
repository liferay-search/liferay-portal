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
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.sort.ScoreSort;
import com.liferay.portal.search.sort.Sort;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.search.experiences.blueprints.engine.internal.sort.SortTranslatorFactory;
import com.liferay.search.experiences.blueprints.engine.internal.util.BlueprintJSONUtil;
import com.liferay.search.experiences.blueprints.engine.internal.util.ProblemBuilders;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.search.experiences.blueprints.engine.spi.sort.SortTranslator;
import com.liferay.search.experiences.blueprints.engine.template.variable.BlueprintTemplateVariableParser;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

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
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		if (_sortsExist(searchRequestBuilder)) {
			return;
		}

		List<Sort> sorts = _getSortsFromParameters(
			parameterData, blueprint, problemsHolderBuilder);

		if (sorts.isEmpty()) {
			sorts = _getDefaultSorts(
				parameterData, blueprint, problemsHolderBuilder);
		}

		if (!sorts.isEmpty()) {
			Stream<Sort> stream = sorts.stream();

			stream.forEach(sort -> searchRequestBuilder.addSort(sort));
		}
	}

	private Optional<Sort> _getDefaultSort(
		ParameterData parameterData, Object object,
		ProblemsHolderBuilder problemsHolderBuilder) {

		if (object instanceof String) {
			return _sortFromString((String)object, null);
		}

		Optional<JSONObject> optional =
			_blueprintTemplateVariableParser.parseObject(
				(JSONObject)object, parameterData, problemsHolderBuilder);

		if (!optional.isPresent()) {
			return Optional.empty();
		}

		return _sortFromObject(optional.get(), problemsHolderBuilder);
	}

	private List<Sort> _getDefaultSorts(
		ParameterData parameterData, Blueprint blueprint,
		ProblemsHolderBuilder problemsHolderBuilder) {

		List<Sort> sorts = new ArrayList<>();

		Optional<JSONArray> optional1 =
			_blueprintHelper.getDefaultSortConfigurationOptional(blueprint);

		if (!optional1.isPresent()) {
			return sorts;
		}

		JSONArray jsonArray = optional1.get();

		for (int i = 0; i < jsonArray.length(); i++) {
			Optional<Sort> optional2 = _getDefaultSort(
				parameterData, jsonArray.get(i), problemsHolderBuilder);

			if (optional2.isPresent()) {
				sorts.add(optional2.get());
			}
		}

		return sorts;
	}

	private Optional<Sort> _getSort(
		JSONObject jsonObject, String key, SortOrder sortOrder,
		ProblemsHolderBuilder problemsHolderBuilder) {

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
				jsonObject, field, sortOrder, problemsHolderBuilder);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			_log.error(illegalArgumentException);

			ProblemBuilder problemBuilder =
				_problemBuilders1.invalidConfigurationValueError(
					getClass().getName(), illegalArgumentException, jsonObject,
					"type", key);

			problemsHolderBuilder.addProblem(problemBuilder.build());
		}

		return Optional.empty();
	}

	private Optional<Sort> _getSortFromParameter(
		ParameterData parameterData, JSONObject jsonObject, String key,
		ProblemsHolderBuilder problemsHolderBuilder) {

		JSONObject configurationJSONObject = jsonObject.getJSONObject(key);

		Optional<JSONObject> optional =
			_blueprintTemplateVariableParser.parseObject(
				configurationJSONObject, parameterData, problemsHolderBuilder);

		if (!optional.isPresent()) {
			return Optional.empty();
		}

		JSONObject parsedJSONObject = optional.get();

		SortOrder sortOrder = _getSortOrderFromParameter(
			parameterData, parsedJSONObject, problemsHolderBuilder);

		if (sortOrder == null) {
			return Optional.empty();
		}

		return _getSort(
			parsedJSONObject, key, sortOrder, problemsHolderBuilder);
	}

	private SortOrder _getSortOrder(
		String s, ProblemsHolderBuilder problemsHolderBuilder) {

		try {
			return SortOrder.valueOf(StringUtil.toUpperCase(s));
		}
		catch (IllegalArgumentException illegalArgumentException) {
			ProblemBuilder problemBuilder = _problemBuilders2.error(
				getClass().getName(), "core.error.invalid.sort-order", null,
				null, s, illegalArgumentException);

			problemsHolderBuilder.addProblem(problemBuilder.build());
		}

		return null;
	}

	private SortOrder _getSortOrderFromParameter(
		ParameterData parameterData, JSONObject jsonObject,
		ProblemsHolderBuilder problemsHolderBuilder) {

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
			GetterUtil.getString(parameter.getValue()), problemsHolderBuilder);
	}

	private List<Sort> _getSortsFromParameters(
		ParameterData parameterData, Blueprint blueprint,
		ProblemsHolderBuilder problemsHolderBuilder) {

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
					parameterData, jsonObject, key, problemsHolderBuilder);

				if (optional2.isPresent()) {
					sorts.add(optional2.get());
				}
			});

		return sorts;
	}

	private Optional<Sort> _sortFromObject(
		JSONObject jsonObject, ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<String> optional = BlueprintJSONUtil.getFirstKeyOptional(
			jsonObject);

		if (!optional.isPresent()) {
			return Optional.empty();
		}

		String key = optional.get();

		Object object = jsonObject.get(key);

		if (object instanceof String) {
			return _sortFromString(
				key, _getSortOrder((String)object, problemsHolderBuilder));
		}

		JSONObject configurationJSONObject = (JSONObject)object;

		return _getSort(
			jsonObject, key,
			_getSortOrder(
				configurationJSONObject.getString("order"),
				problemsHolderBuilder),
			problemsHolderBuilder);
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

	private boolean _sortsExist(SearchRequestBuilder searchRequestBuilder) {
		return searchRequestBuilder.withSearchContextGet(
			searchContext -> {
				SearchRequest searchRequest =
					(SearchRequest)searchContext.getAttribute("search.request");

				List<Sort> sorts = searchRequest.getSorts();

				return !sorts.isEmpty();
			});
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SortSearchRequestBodyContributor.class);

	private static final List<String> _fixedTypes = new ArrayList<>(
		Arrays.asList("_geo_distance", "_script", "_score"));

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private ProblemBuilders _problemBuilders1;

	@Reference
	private com.liferay.search.experiences.problems.ProblemBuilders
		_problemBuilders2;

	@Reference
	private Sorts _sorts;

	@Reference
	private SortTranslatorFactory _sortTranslatorFactory;

}