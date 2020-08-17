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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.searchrequest.data.contributor;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.Aggregation;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.TermQuery;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.AggregationConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.CommonConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.FilterMode;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.Operator;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.aggregations.AggregationBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.util.ConfigurationUtil;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.searchrequest.SearchRequestData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.aggregation.AggregationBuilder;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SearchRequestDataContributor.class)
public class AggregationsSearchRequestDataContributor
	implements SearchRequestDataContributor {

	public void contribute(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData) {

		Optional<JSONArray> aggregationConfigurationJsonArrayOptional =
			searchRequestContext.getAggregationConfiguration();

		if (!aggregationConfigurationJsonArrayOptional.isPresent()) {
			return;
		}

		JSONArray aggregationConfigurationJsonArray =
			aggregationConfigurationJsonArrayOptional.get();

		for (int i = 0; i < aggregationConfigurationJsonArray.length(); i++) {
			JSONObject aggregationJsonObject =
				aggregationConfigurationJsonArray.getJSONObject(i);

			boolean enabled = aggregationJsonObject.getBoolean(
				AggregationConfigurationKeys.ENABLED.getJsonKey(), false);

			if (!enabled) {
				continue;
			}

			String type = aggregationJsonObject.getString(
				CommonConfigurationKeys.TYPE.getJsonKey());

			try {
				JSONObject aggregationConfigurationJsonObject =
					aggregationJsonObject.getJSONObject(
						AggregationConfigurationKeys.BODY.getJsonKey());

				String name = aggregationConfigurationJsonObject.getString(
					AggregationConfigurationKeys.NAME.getJsonKey());

				AggregationBuilder aggregationBuilder =
					_aggregationBuilderFactory.getBuilder(type);

				Optional<Aggregation> aggregationOptional =
					aggregationBuilder.build(
						searchRequestContext,
						aggregationConfigurationJsonObject, name);

				if (aggregationOptional.isPresent()) {
					searchRequestData.getAggregations(
					).add(
						aggregationOptional.get()
					);

					// Facet

					if (aggregationJsonObject.has(
							AggregationConfigurationKeys.FACET.getJsonKey())) {

						_addFacetFilters(
							searchRequestContext, searchRequestData,
							aggregationJsonObject);
					}
				}
			}
			catch (IllegalArgumentException illegalArgumentException) {
				searchRequestContext.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.unknown-aggregation-type",
						illegalArgumentException.getMessage(),
						illegalArgumentException, aggregationJsonObject,
						CommonConfigurationKeys.TYPE.getJsonKey(), type));

				_log.error(
					illegalArgumentException.getMessage(),
					illegalArgumentException);
			}
			catch (Exception e) {
				searchRequestContext.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.unknown-aggregation-configuration-error",
						e.getMessage(), e, aggregationJsonObject, null, null));

				_log.error(e.getMessage(), e);
			}
		}
	}

	private void _addFacetFilterClauses(
		SearchRequestContext searchRequestContext,
		BooleanQuery facetPreFilterQuery, BooleanQuery facetPostFilterQuery,
		JSONObject facetConfigurationJsonObject, Object value) {

		BooleanQuery query = _queries.booleanQuery();

		String indexField = facetConfigurationJsonObject.getString(
			FacetConfigurationKeys.INDEX_FIELD.getJsonKey());

		if (Validator.isBlank(indexField)) {
			indexField = facetConfigurationJsonObject.getString(
				CommonConfigurationKeys.PARAMETER_NAME.getJsonKey());
		}

		String filterModeString = facetConfigurationJsonObject.getString(
			FacetConfigurationKeys.FILTER_MODE.getJsonKey(),
			FilterMode.PRE.getjsonValue());
		FilterMode filterMode;

		try {
			filterMode = ConfigurationUtil.getFilterMode(filterModeString);
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
		Operator multiValueOperator;

		try {
			multiValueOperator = ConfigurationUtil.getOperator(
				multiValueOperatorString);
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

		// TODO: can facet values be of other types?

		if (value instanceof String) {
			query.addMustQueryClauses(_queries.term(indexField, value));
		}
		else if (value instanceof String[]) {
			String[] values = (String[])value;

			for (String val : values) {
				TermQuery condition = _queries.term(indexField, val);

				if (values.length > 1) {
					if (multiValueOperator.equals(Operator.AND)) {
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

	private void _addFacetFilters(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData, JSONObject aggregationJsonObject) {

		BooleanQuery facetPreFilterQuery = _queries.booleanQuery();

		BooleanQuery facetPostFilterQuery = _queries.booleanQuery();

		JSONObject facetConfigurationJsonObject =
			aggregationJsonObject.getJSONObject(
				AggregationConfigurationKeys.FACET.getJsonKey());

		for (Parameter parameter :
				searchRequestContext.getSearchParameterData(
				).getParameters()) {

			String parameterName = facetConfigurationJsonObject.getString(
				CommonConfigurationKeys.PARAMETER_NAME.getJsonKey());

			if (!parameter.getName(
				).equals(
					parameterName
				)) {

				continue;
			}

			try {
				_addFacetFilterClauses(
					searchRequestContext, facetPreFilterQuery,
					facetPostFilterQuery, facetConfigurationJsonObject,
					parameter.getValue());
			}
			catch (Exception e) {
				searchRequestContext.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.unknown-error-in-creating-facet-filter",
						e.getMessage(), e, facetConfigurationJsonObject, null,
						null));

				_log.error(e.getMessage(), e);
			}

			if (facetPreFilterQuery.hasClauses()) {
				searchRequestData.getQuery(
				).addFilterQueryClauses(
					facetPreFilterQuery
				);
			}

			if (facetPostFilterQuery.hasClauses()) {
				searchRequestData.getPostFilterQuery(
				).addMustQueryClauses(
					facetPostFilterQuery
				);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AggregationsSearchRequestDataContributor.class);

	@Reference
	private AggregationBuilderFactory _aggregationBuilderFactory;

	@Reference
	private Queries _queries;

}