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
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.Aggregations;
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
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetsBlueprintContributorKeys;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

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
			JSONObject configurationJsonObject =
				configurationJsonArray.getJSONObject(i);

			_addAggregation(searchRequestBuilder, configurationJsonObject);
			
			_addFilter(searchRequestBuilder, parameterData, messages, 
					configurationJsonObject);

		}
	}

	private void _addAggregation(
		SearchRequestBuilder searchRequestBuilder,
		JSONObject configurationJsonObject) {

		String field = _getFieldName(configurationJsonObject);

		int size = configurationJsonObject.getInt(
			FacetConfigurationKeys.SIZE.getJsonKey(), 50);

		TermsAggregation aggregation = _aggregations.terms(field, field);

		aggregation.setSize(size);

		searchRequestBuilder.addAggregation(aggregation);
	}

	private void _addFacetFilter(
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

		BooleanQuery query = _getFilterQuery(
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

	private void _addFilter(
			SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
			Messages messages, JSONObject configurationJsonObject) {

		Optional<Parameter> parameterOptional =
			parameterData.getByNameOptional(
				configurationJsonObject.getString(
					FacetConfigurationKeys.PARAMETER_NAME.getJsonKey()));

		if (!parameterOptional.isPresent()) {
			return;
		}

		Parameter parameter = parameterOptional.get();

		try {
			_addFacetFilter(
				searchRequestBuilder, messages, configurationJsonObject,
				parameter.getValue());
		}
		catch (Exception exception) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.unknown-error-in-creating-facet-filter",
					exception.getMessage(), exception,
					configurationJsonObject, null, null));

			_log.error(exception.getMessage(), exception);
		}		
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

	private BooleanQuery _getFilterQuery(
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

	private Optional<Operator> _getOperator(
		JSONObject configurationJsonObject, Messages messages) {

		String operatorString = configurationJsonObject.getString(
			FacetConfigurationKeys.MULTI_VALUE_OPERATOR.getJsonKey(),
			Operator.OR.getjsonValue());

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

	private static final Log _log = LogFactoryUtil.getLog(
		FacetsSearchRequestBodyContributor.class);

	@Reference
	private Aggregations _aggregations;

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Reference
	private Queries _queries;

}