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

package com.liferay.portal.search.tuning.blueprints.facets.internal.searchrequest;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.DateRangeAggregation;
import com.liferay.portal.search.aggregation.bucket.TermsAggregation;
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.TermQuery;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.FilterMode;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.Operator;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.portal.search.tuning.blueprints.engine.util.BlueprintTemplateVariableParser;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetsBlueprintContributorKeys;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Optional;

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
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, Messages messages) {

		Optional<JSONArray> configurationJsonArrayOptional =
			_blueprintHelper.getJSONArrayConfigurationOptional(
				blueprint,
				"JSONArray/" +
					FacetsBlueprintContributorKeys.CONFIGURATION_SECTION);

		if (!configurationJsonArrayOptional.isPresent()) {
			return;
		}

		JSONArray configurationJsonArray = configurationJsonArrayOptional.get();

		for (int i = 0; i < configurationJsonArray.length(); i++) {
			JSONObject rawConfigurationJsonObject =
				configurationJsonArray.getJSONObject(i);

			JSONObject configurationJsonObject = null;

			try {
				configurationJsonObject =
					_blueprintTemplateVariableParser.parse(
						parameterData, messages, rawConfigurationJsonObject);
			}
			catch (Exception exception) {
				messages.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.unknown-facet-configuration-error",
						exception.getMessage(), exception,
						configurationJsonObject, null, null));

				_log.error(exception.getMessage(), exception);

				continue;
			}

			boolean enabled = configurationJsonObject.getBoolean(
				FacetConfigurationKeys.ENABLED.getJsonKey(), true);

			if (!enabled) {
				continue;
			}

			_addAggregation(searchRequestBuilder, configurationJsonObject);

			_addFilter(
				searchRequestBuilder, parameterData, messages,
				configurationJsonObject);
		}
	}

	private void _addAggregation(
		SearchRequestBuilder searchRequestBuilder,
		JSONObject configurationJsonObject) {

		String aggregationType = configurationJsonObject.getString(
			FacetConfigurationKeys.AGGREGATION_TYPE.getJsonKey(), "terms");

		if (aggregationType.contentEquals("terms")) {
			_addTermsAggregation(searchRequestBuilder, configurationJsonObject);
		}
		else if (aggregationType.contentEquals("date_range")) {
			_addDateRangeAggregation(
				searchRequestBuilder, configurationJsonObject);
		}
	}

	private void _addDateRangeAggregation(
		SearchRequestBuilder searchRequestBuilder,
		JSONObject configurationJsonObject) {

		JSONObject handlerParametersJsonObject =
			configurationJsonObject.getJSONObject(
				FacetConfigurationKeys.HANDLER_PARAMETERS.getJsonKey());

		if ((handlerParametersJsonObject == null) ||
			!handlerParametersJsonObject.has("ranges")) {

			return;
		}

		JSONArray rangesJsonArray = handlerParametersJsonObject.getJSONArray(
			"ranges");

		String dateFormat = handlerParametersJsonObject.getString(
			"date_format");

		String field = _getFieldName(configurationJsonObject);

		DateRangeAggregation dateRangeAggregation = _aggregations.dateRange(
			field, field);

		for (int i = 0; i < rangesJsonArray.length(); i++) {
			JSONObject rangeJsonObject = rangesJsonArray.getJSONObject(i);

			String from = _getDateRangeString(
				rangeJsonObject.getString("from"), dateFormat, true);
			String label = rangeJsonObject.getString("label", "label");
			String to = _getDateRangeString(
				rangeJsonObject.getString("to"), dateFormat, false);

			dateRangeAggregation.addRange(label, from, to);
		}

		searchRequestBuilder.addAggregation(dateRangeAggregation);
	}

	private void _addDateRangeFacetFilter(
		SearchRequestBuilder searchRequestBuilder, Messages messages,
		JSONObject configurationJsonObject, Object value) {

		String range = GetterUtil.getString(value);

		Optional<FilterMode> filterModeOptional = _getFilterMode(
			configurationJsonObject, messages);

		if (!filterModeOptional.isPresent()) {
			return;
		}

		Optional<Operator> operatorOptional = _getOperator(
			configurationJsonObject, messages);

		if (!operatorOptional.isPresent()) {
			return;
		}

		BooleanQuery query = _getDateRangeFilterQuery(
			operatorOptional.get(), configurationJsonObject, range);

		if (query.hasClauses()) {
			if (FilterMode.PRE.equals(filterModeOptional.get())) {
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

	private void _addFilter(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Messages messages, JSONObject configurationJsonObject) {

		Optional<Parameter> parameterOptional = parameterData.getByNameOptional(
			configurationJsonObject.getString(
				FacetConfigurationKeys.PARAMETER_NAME.getJsonKey()));

		if (!parameterOptional.isPresent()) {
			return;
		}

		Parameter parameter = parameterOptional.get();

		String handlerName = configurationJsonObject.getString(
			FacetConfigurationKeys.HANDLER.getJsonKey());

		try {
			if (handlerName.equals("date_range")) {
				_addDateRangeFacetFilter(
					searchRequestBuilder, messages, configurationJsonObject,
					parameter.getValue());
			}
			else {
				_addTermsFacetFilter(
					searchRequestBuilder, messages, configurationJsonObject,
					parameter.getValue());
			}
		}
		catch (Exception exception) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.unknown-error-in-creating-facet-filter",
					exception.getMessage(), exception, configurationJsonObject,
					null, null));

			_log.error(exception.getMessage(), exception);
		}
	}

	private void _addTermsAggregation(
		SearchRequestBuilder searchRequestBuilder,
		JSONObject configurationJsonObject) {

		String field = _getFieldName(configurationJsonObject);

		int size = configurationJsonObject.getInt(
			FacetConfigurationKeys.SIZE.getJsonKey(), 50);

		TermsAggregation aggregation = _aggregations.terms(field, field);

		aggregation.setSize(size);

		searchRequestBuilder.addAggregation(aggregation);
	}

	private void _addTermsFacetFilter(
		SearchRequestBuilder searchRequestBuilder, Messages messages,
		JSONObject configurationJsonObject, Object value) {

		String field = _getFieldName(configurationJsonObject);

		Optional<FilterMode> filterModeOptional = _getFilterMode(
			configurationJsonObject, messages);

		if (!filterModeOptional.isPresent()) {
			return;
		}

		Optional<Operator> operatorOptional = _getOperator(
			configurationJsonObject, messages);

		if (!operatorOptional.isPresent()) {
			return;
		}

		BooleanQuery query = _getTermFilterQuery(
			operatorOptional.get(), field, value);

		if (query.hasClauses()) {
			if (FilterMode.PRE.equals(filterModeOptional.get())) {
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
		Operator operator, JSONObject configurationJsonObject, String value) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		String field = _getFieldName(configurationJsonObject);

		JSONObject handlerParametersJsonObject =
			configurationJsonObject.getJSONObject(
				FacetConfigurationKeys.HANDLER_PARAMETERS.getJsonKey());

		if ((handlerParametersJsonObject == null) ||
			!handlerParametersJsonObject.has("ranges")) {

			return booleanQuery;
		}

		JSONArray rangesJsonArray = handlerParametersJsonObject.getJSONArray(
			"ranges");

		String dateFormatString = handlerParametersJsonObject.getString(
			"date_format");

		for (int i = 0; i < rangesJsonArray.length(); i++) {
			JSONObject jsonObject = rangesJsonArray.getJSONObject(i);

			String label = jsonObject.getString("label");

			if (value.equals(label)) {
				String startDate = _getDateRangeString(
					jsonObject.getString("from"), dateFormatString, false);

				String endDate = _getDateRangeString(
					jsonObject.getString("to"), dateFormatString, true);

				booleanQuery.addMustQueryClauses(
					_queries.dateRangeTerm(
						field, true, true, startDate, endDate));

				break;
			}
		}

		return booleanQuery;
	}

	private String _getDateRangeString(
		String str, String dateFormatString, boolean future) {

		try {
			DateFormat dateFormat = new SimpleDateFormat(dateFormatString);

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
				return dateFormat.format(date);
			}
		}
		catch (Exception exception) {
			_log.error(exception.getMessage(), exception);
		}

		return str;
	}

	private String _getFieldName(JSONObject configurationJsonObject) {
		String field = configurationJsonObject.getString(
			FacetConfigurationKeys.FIELD.getJsonKey());

		if (Validator.isBlank(field)) {
			field = configurationJsonObject.getString(
				FacetConfigurationKeys.PARAMETER_NAME.getJsonKey());
		}

		return field;
	}

	private Optional<FilterMode> _getFilterMode(
		JSONObject configurationJsonObject, Messages messages) {

		String filterModeString = configurationJsonObject.getString(
			FacetConfigurationKeys.FILTER_MODE.getJsonKey(),
			FilterMode.PRE.getjsonValue());

		try {
			FilterMode filterMode = FilterMode.valueOf(
				StringUtil.toUpperCase(filterModeString));

			return Optional.of(filterMode);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.unknown-facet-filter-mode",
					illegalArgumentException.getMessage(),
					illegalArgumentException, configurationJsonObject,
					FacetConfigurationKeys.FILTER_MODE.getJsonKey(),
					filterModeString));

			_log.error(
				illegalArgumentException.getMessage(),
				illegalArgumentException);
		}

		return Optional.empty();
	}

	private Optional<Operator> _getOperator(
		JSONObject configurationJsonObject, Messages messages) {

		String operatorString = configurationJsonObject.getString(
			FacetConfigurationKeys.MULTI_VALUE_OPERATOR.getJsonKey(),
			Operator.AND.getjsonValue());

		try {
			Operator operator = Operator.valueOf(
				StringUtil.toUpperCase(operatorString));

			return Optional.of(operator);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.unknown-facet-multi-value-operator",
					illegalArgumentException.getMessage(),
					illegalArgumentException, configurationJsonObject,
					FacetConfigurationKeys.MULTI_VALUE_OPERATOR.getJsonKey(),
					operatorString));

			_log.error(
				illegalArgumentException.getMessage(),
				illegalArgumentException);
		}

		return Optional.empty();
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

	private static final Log _log = LogFactoryUtil.getLog(
		FacetsSearchRequestBodyContributor.class);

	@Reference
	private Aggregations _aggregations;

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Reference
	private Queries _queries;

}