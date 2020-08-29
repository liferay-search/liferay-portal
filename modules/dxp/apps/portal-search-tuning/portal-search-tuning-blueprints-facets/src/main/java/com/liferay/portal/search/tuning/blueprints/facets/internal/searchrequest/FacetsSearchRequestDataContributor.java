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
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.TermQuery;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.FilterMode;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.Operator;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.searchrequest.SearchRequestData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.searchrequest.SearchRequestDataContributor;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.poc.util.POCMockUtil;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SearchRequestDataContributor.class)
public class FacetsSearchRequestDataContributor
	implements SearchRequestDataContributor {

	@Override
	public void contribute(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData) {

		_addFacetFilterClauses(searchRequestContext, searchRequestData);
	}

	private void _addFacetFilterClauses(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData) {

		// TODO

		JSONArray facetConfigurationJsonArray =
			POCMockUtil.getFacetsParameterConfigurationMock();

		BooleanQuery facetPreFilterQuery = _queries.booleanQuery();

		BooleanQuery facetPostFilterQuery = _queries.booleanQuery();

		for (int i = 0; i < facetConfigurationJsonArray.length(); i++) {
			JSONObject facetConfigurationJsonObject =
				facetConfigurationJsonArray.getJSONObject(i);

			Optional<Parameter> parameterOptional =
				searchRequestContext.getSearchParameterData(
				).getByName(
					facetConfigurationJsonObject.getString(
						FacetConfigurationKeys.PARAMETER_NAME.getJsonKey())
				);

			if (!parameterOptional.isPresent()) {
				continue;
			}

			try {
				_addFacetFilters(
					searchRequestContext, facetPreFilterQuery,
					facetPostFilterQuery, facetConfigurationJsonObject,
					parameterOptional.get(
					).getValue());
			}
			catch (Exception exception) {
				searchRequestContext.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.unknown-error-in-creating-facet-filter",
						exception.getMessage(), exception,
						facetConfigurationJsonObject, null, null));

				_log.error(exception.getMessage(), exception);
			}

			if (facetPreFilterQuery.hasClauses()) {
				searchRequestData.getQuery(
				).addFilterQueryClauses(
					facetPreFilterQuery
				);
			}

			if (facetPostFilterQuery.hasClauses()) {
				BooleanQuery postFilterQuery =
					searchRequestData.getPostFilterQuery();

				postFilterQuery.addMustQueryClauses(facetPostFilterQuery);
			}
		}
	}

	private void _addFacetFilters(
		SearchRequestContext searchRequestContext,
		BooleanQuery facetPreFilterQuery, BooleanQuery facetPostFilterQuery,
		JSONObject facetConfigurationJsonObject, Object value) {

		BooleanQuery query = _queries.booleanQuery();

		String indexField = facetConfigurationJsonObject.getString(
			FacetConfigurationKeys.FIELD.getJsonKey());

		if (Validator.isBlank(indexField)) {
			indexField = facetConfigurationJsonObject.getString(
				FacetConfigurationKeys.PARAMETER_NAME.getJsonKey());
		}

		String filterModeString = facetConfigurationJsonObject.getString(
			FacetConfigurationKeys.FILTER_MODE.getJsonKey(),
			FilterMode.PRE.getjsonValue());
		FilterMode filterMode;

		try {
			filterMode = FilterMode.valueOf(
				StringUtil.toUpperCase(filterModeString));
		}
		catch (IllegalArgumentException illegalArgumentException) {
			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.unknown-facet-filter-mode",
					illegalArgumentException.getMessage(),
					illegalArgumentException, facetConfigurationJsonObject,
					FacetConfigurationKeys.FILTER_MODE.getJsonKey(),
					filterModeString));

			_log.error(
				illegalArgumentException.getMessage(),
				illegalArgumentException);

			return;
		}

		String multiValueOperatorString =
			facetConfigurationJsonObject.getString(
				FacetConfigurationKeys.MULTI_VALUE_OPERATOR.getJsonKey(),
				Operator.OR.getjsonValue());
		Operator operator;

		try {
			operator = Operator.valueOf(
				StringUtil.toUpperCase(multiValueOperatorString));
		}
		catch (IllegalArgumentException illegalArgumentException) {
			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.unknown-facet-multi-value-operator",
					illegalArgumentException.getMessage(),
					illegalArgumentException, facetConfigurationJsonObject,
					FacetConfigurationKeys.MULTI_VALUE_OPERATOR.getJsonKey(),
					multiValueOperatorString));

			_log.error(
				illegalArgumentException.getMessage(),
				illegalArgumentException);

			return;
		}

		if (value instanceof String) {
			query.addMustQueryClauses(_queries.term(indexField, value));
		}
		else if (value instanceof String[]) {
			String[] values = (String[])value;

			for (String val : values) {
				TermQuery condition = _queries.term(indexField, val);

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

		if (query.hasClauses()) {
			if (FilterMode.PRE.equals(filterMode)) {
				facetPreFilterQuery.addMustQueryClauses(query);
			}
			else {
				facetPostFilterQuery.addShouldQueryClauses(query);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FacetsSearchRequestDataContributor.class);

	@Reference
	private Queries _queries;

}