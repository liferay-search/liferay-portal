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

package com.liferay.search.experiences.blueprints.facets.internal.searchrequest;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.DateRangeAggregation;
import com.liferay.portal.search.aggregation.bucket.TermsAggregation;
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.TermQuery;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.constants.json.values.FilterMode;
import com.liferay.search.experiences.blueprints.constants.json.values.Operator;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.search.experiences.blueprints.engine.template.variable.BlueprintTemplateVariableParser;
import com.liferay.search.experiences.blueprints.facets.constants.FacetsBlueprintKeys;
import com.liferay.search.experiences.blueprints.facets.internal.request.handler.FacetRequestHandlerFactory;
import com.liferay.search.experiences.blueprints.facets.internal.util.BlueprintJSONUtil;
import com.liferay.search.experiences.blueprints.facets.internal.util.FacetConfigurationUtil;
import com.liferay.search.experiences.blueprints.facets.spi.request.FacetRequestHandler;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.problems.ProblemBuilders;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=facets",
	service = SearchRequestBodyContributor.class
)
public class FacetsSearchRequestBodyContributor
	implements SearchRequestBodyContributor {

	@Override
	public void contribute(
		SearchRequestBuilder searchRequestBuilder, Blueprint blueprint,
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<JSONObject> optional =
			_blueprintHelper.getJSONObjectConfigurationOptional(
				blueprint,
				"JSONObject/" + FacetsBlueprintKeys.CONFIGURATION_SECTION);

		if (!optional.isPresent()) {
			return;
		}

		_processFacets(
			searchRequestBuilder, optional.get(), parameterData,
			problemsHolderBuilder);
	}

	protected void setIntegerValue(
		JSONObject jsonObject, String key, Consumer<Integer> setter) {

		if (!jsonObject.has(key)) {
			return;
		}

		setter.accept(jsonObject.getInt(key));
	}

	private void _addAggregation(
		SearchRequestBuilder searchRequestBuilder, String type,
		JSONObject jsonObject, ProblemsHolderBuilder problemsHolderBuilder) {

		FacetRequestHandler facetRequestHandler =
			_facetRequestHandlerFactory.getHandler(type);

		String aggregationType = facetRequestHandler.getAggregationType();

		if (aggregationType.equals("terms")) {
			_addTermsAggregation(searchRequestBuilder, jsonObject);
		}
		else if (aggregationType.equals("date_range")) {
			_addDateRangeAggregation(
				searchRequestBuilder, jsonObject, problemsHolderBuilder);
		}
	}

	private void _addDateRangeAggregation(
		SearchRequestBuilder searchRequestBuilder, JSONObject jsonObject,
		ProblemsHolderBuilder problemsHolderBuilder) {

		JSONObject parametersJSONObject = jsonObject.getJSONObject(
			"parameters");

		JSONArray rangesJSONArray = parametersJSONObject.getJSONArray("ranges");

		String dateFormat = parametersJSONObject.getString("date_format");

		DateRangeAggregation dateRangeAggregation = _aggregations.dateRange(
			FacetConfigurationUtil.getAggregationName(jsonObject),
			FacetConfigurationUtil.getFieldName(jsonObject));

		for (int i = 0; i < rangesJSONArray.length(); i++) {
			JSONObject rangeJSONObject = rangesJSONArray.getJSONObject(i);

			String from = _getDateRangeString(
				rangeJSONObject.getString("from"), dateFormat, true,
				problemsHolderBuilder);
			String label = rangeJSONObject.getString("label", "label");
			String to = _getDateRangeString(
				rangeJSONObject.getString("to"), dateFormat, false,
				problemsHolderBuilder);

			dateRangeAggregation.addRange(label, from, to);
		}

		searchRequestBuilder.addAggregation(dateRangeAggregation);
	}

	private void _addDateRangeFacetFilter(
		SearchRequestBuilder searchRequestBuilder, JSONObject jsonObject,
		Object value, ProblemsHolderBuilder problemsHolderBuilder) {

		FilterMode filterMode = FacetConfigurationUtil.getFilterMode(
			jsonObject, problemsHolderBuilder);

		if (filterMode == null) {
			return;
		}

		BooleanQuery query = _getDateRangeFilterQuery(
			jsonObject, GetterUtil.getString(value), problemsHolderBuilder);

		if (query.hasClauses()) {
			if (FilterMode.PRE.equals(filterMode)) {
				searchRequestBuilder.addComplexQueryPart(
					_complexQueryPartBuilderFactory.builder(
					).query(
						query
					).occur(
						"filter"
					).build());
			}
			else {
				searchRequestBuilder.addPostFilterQueryPart(
					_complexQueryPartBuilderFactory.builder(
					).query(
						query
					).occur(
						"must"
					).build());
			}
		}
	}

	private void _addFacet(
		SearchRequestBuilder searchRequestBuilder, String facetName,
		JSONObject jsonObject, ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		JSONObject nameJSONObject = jsonObject.getJSONObject(facetName);

		Optional<String> optional1 = BlueprintJSONUtil.getFirstKeyOptional(
			nameJSONObject);

		if (!optional1.isPresent()) {
			return;
		}

		String type = optional1.get();

		JSONObject typeJSONObject = nameJSONObject.getJSONObject(type);

		if (!FacetConfigurationUtil.isEnabled(typeJSONObject)) {
			return;
		}

		Optional<JSONObject> optional2 =
			_blueprintTemplateVariableParser.parseObject(
				typeJSONObject, parameterData, problemsHolderBuilder);

		if (!optional2.isPresent()) {
			return;
		}

		JSONObject parsedJSONObject = optional2.get();

		_addAggregation(
			searchRequestBuilder, type, parsedJSONObject,
			problemsHolderBuilder);

		_addFilter(
			searchRequestBuilder, type, parsedJSONObject, parameterData,
			problemsHolderBuilder);
	}

	private void _addFilter(
		SearchRequestBuilder searchRequestBuilder, String type,
		JSONObject jsonObject, ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<Parameter> optional = parameterData.getByNameOptional(
			FacetConfigurationUtil.getParameterName(jsonObject));

		if (!optional.isPresent()) {
			return;
		}

		Parameter parameter = optional.get();

		try {
			if (type.equals("date_range")) {
				_addDateRangeFacetFilter(
					searchRequestBuilder, jsonObject, parameter.getValue(),
					problemsHolderBuilder);
			}
			else {
				_addTermsFacetFilter(
					searchRequestBuilder, jsonObject, parameter.getValue(),
					problemsHolderBuilder);
			}
		}
		catch (Exception exception) {
			problemsHolderBuilder.addProblem(
				_problemBuilders.error(
					getClass().getName(), "facets.error.unknown-error",
					jsonObject, null, null, exception
				).build());
		}
	}

	private void _addTermsAggregation(
		SearchRequestBuilder searchRequestBuilder, JSONObject jsonObject) {

		TermsAggregation aggregation = _aggregations.terms(
			FacetConfigurationUtil.getAggregationName(jsonObject),
			FacetConfigurationUtil.getFieldName(jsonObject));

		int size = jsonObject.getInt("size", 50);

		aggregation.setSize(size);

		if (jsonObject.has("shard_size")) {
			int defaultShardSize = (int)Math.floor((size * 1.5) + 10);

			aggregation.setShardSize(
				jsonObject.getInt("shard_size", defaultShardSize));
		}

		setIntegerValue(
			jsonObject, "min_doc_count", aggregation::setMinDocCount);

		searchRequestBuilder.addAggregation(aggregation);
	}

	private void _addTermsFacetFilter(
		SearchRequestBuilder searchRequestBuilder, JSONObject jsonObject,
		Object value, ProblemsHolderBuilder problemsHolderBuilder) {

		FilterMode filterMode = FacetConfigurationUtil.getFilterMode(
			jsonObject, problemsHolderBuilder);

		if (filterMode == null) {
			return;
		}

		Operator operator = FacetConfigurationUtil.getOperator(
			jsonObject, problemsHolderBuilder);

		if (operator == null) {
			return;
		}

		BooleanQuery query = _getTermFilterQuery(
			operator, FacetConfigurationUtil.getFieldName(jsonObject), value);

		if (query.hasClauses()) {
			if (FilterMode.PRE.equals(filterMode)) {
				searchRequestBuilder.addComplexQueryPart(
					_complexQueryPartBuilderFactory.builder(
					).query(
						query
					).occur(
						"filter"
					).build());
			}
			else {
				searchRequestBuilder.addPostFilterQueryPart(
					_complexQueryPartBuilderFactory.builder(
					).query(
						query
					).occur(
						"must"
					).build());
			}
		}
	}

	private BooleanQuery _getDateRangeFilterQuery(
		JSONObject jsonObject, String value,
		ProblemsHolderBuilder problemsHolderBuilder) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		String field = FacetConfigurationUtil.getFieldName(jsonObject);

		JSONObject parametersJSONObject = jsonObject.getJSONObject(
			"parameters");

		JSONArray rangesJSONArray = parametersJSONObject.getJSONArray("ranges");

		String dateFormatString = parametersJSONObject.getString("date_format");

		for (int i = 0; i < rangesJSONArray.length(); i++) {
			JSONObject rangeJSONObject = rangesJSONArray.getJSONObject(i);

			String label = rangeJSONObject.getString("label");

			if (value.equals(label)) {
				booleanQuery.addMustQueryClauses(
					_queries.dateRangeTerm(
						field, true, true,
						_getDateRangeString(
							rangeJSONObject.getString("from"), dateFormatString,
							false, problemsHolderBuilder),
						_getDateRangeString(
							rangeJSONObject.getString("to"), dateFormatString,
							true, problemsHolderBuilder)));

				break;
			}
		}

		return booleanQuery;
	}

	private String _getDateRangeString(
		String str, String dateFormatString, boolean future,
		ProblemsHolderBuilder problemsHolderBuilder) {

		try {
			Date date = null;

			if (str.equals("*")) {
				if (future) {
					date = new Date(Long.MAX_VALUE);
				}
				else {
					date = new Date(Long.MIN_VALUE);
				}
			}
			else if (Validator.isBlank(str)) {
				date = new Date();
			}

			if (date != null) {
				DateFormat dateFormat = new SimpleDateFormat(dateFormatString);

				return dateFormat.format(date);
			}
		}
		catch (Exception exception) {
			problemsHolderBuilder.addProblem(
				_problemBuilders.error(
					getClass().getName(), "core.error.invalid-date-format",
					null, null, dateFormatString, exception
				).build());
		}

		return str;
	}

	private BooleanQuery _getTermFilterQuery(
		Operator operator, String field, Object value) {

		BooleanQuery query = _queries.booleanQuery();

		if (value instanceof String) {
			query.addMustQueryClauses(_queries.term(field, value));
		}
		else if (value instanceof String[]) {
			String[] values = (String[])value;

			for (String val : values) {
				TermQuery condition = _queries.term(field, val);

				if (values.length > 1) {
					if (operator.equals(Operator.AND)) {
						query.addMustQueryClauses(condition);
					}
					else {
						query.addShouldQueryClauses(condition);
					}
				}
				else {
					query.addMustQueryClauses(condition);
				}
			}
		}

		return query;
	}

	private void _processFacets(
		SearchRequestBuilder searchRequestBuilder, JSONObject jsonObject,
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Set<String> keySet = jsonObject.keySet();

		keySet.forEach(
			facetName -> _addFacet(
				searchRequestBuilder, facetName, jsonObject, parameterData,
				problemsHolderBuilder));
	}

	@Reference
	private Aggregations _aggregations;

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Reference(target = "(type=internal)")
	private FacetRequestHandlerFactory _facetRequestHandlerFactory;

	@Reference
	private ProblemBuilders _problemBuilders;

	@Reference
	private Queries _queries;

}