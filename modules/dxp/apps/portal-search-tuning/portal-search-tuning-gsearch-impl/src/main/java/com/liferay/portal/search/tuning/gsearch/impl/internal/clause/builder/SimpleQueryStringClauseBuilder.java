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

package com.liferay.portal.search.tuning.gsearch.impl.internal.clause.builder;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.query.Operator;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.query.StringQuery;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.json.keys.query.SimpleQueryStringConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.context.SearchRequestContext;
import com.liferay.portal.search.tuning.gsearch.spi.clause.ClauseBuilder;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "type=simple_query_string",
	service = ClauseBuilder.class
)
public class SimpleQueryStringClauseBuilder implements ClauseBuilder {

	@Override
	public Optional<Query> buildClause(
		SearchRequestContext queryContext, JSONObject clauseJsonObject) {

		String keywords = clauseJsonObject.getString(
			SimpleQueryStringConfigurationKeys.QUERY.getJsonKey());

		if (Validator.isBlank(keywords)) {
			keywords = queryContext.getKeywords();
		}

		StringQuery simpleQueryString = _queries.string(keywords);

		if (clauseJsonObject.has(
				SimpleQueryStringConfigurationKeys.ANALYZE_WILDCARD.
					getJsonKey())) {

			simpleQueryString.setAnalyzeWildcard(
				clauseJsonObject.getBoolean(
					SimpleQueryStringConfigurationKeys.ANALYZE_WILDCARD.
						getJsonKey()));
		}

		if (clauseJsonObject.has(
				SimpleQueryStringConfigurationKeys.ANALYZER.getJsonKey())) {

			simpleQueryString.setAnalyzer(
				SimpleQueryStringConfigurationKeys.ANALYZER.getJsonKey());
		}

		if (clauseJsonObject.has(
				SimpleQueryStringConfigurationKeys.
					AUTO_GENERATE_SYNONYMS_PHRASE_QUERY.getJsonKey())) {

			simpleQueryString.setAutoGenerateSynonymsPhraseQuery(
				clauseJsonObject.getBoolean(
					SimpleQueryStringConfigurationKeys.
						AUTO_GENERATE_SYNONYMS_PHRASE_QUERY.getJsonKey()));
		}

		if (clauseJsonObject.has(
				SimpleQueryStringConfigurationKeys.DEFAULT_OPERATOR.
					getJsonKey())) {

			String operator = clauseJsonObject.getString(
				SimpleQueryStringConfigurationKeys.DEFAULT_OPERATOR.
					getJsonKey(),
				Operator.AND.name());

			if (operator.equalsIgnoreCase(Operator.OR.name())) {
				simpleQueryString.setDefaultOperator(Operator.OR);
			}
			else {
				simpleQueryString.setDefaultOperator(Operator.AND);
			}
		}

		JSONArray fields = clauseJsonObject.getJSONArray(
			SimpleQueryStringConfigurationKeys.FIELDS.getJsonKey());

		for (int i = 0; i < fields.length(); i++) {
			JSONObject field = fields.getJSONObject(i);

			String fieldName = field.getString(
				SimpleQueryStringConfigurationKeys.FIELD.getJsonKey());

			float boost = GetterUtil.getFloat(
				field.getString(
					SimpleQueryStringConfigurationKeys.BOOST.getJsonKey()),
				1.0F);

			simpleQueryString.addField(fieldName, boost);
		}

		if (clauseJsonObject.has(
				SimpleQueryStringConfigurationKeys.FUZZY_MAX_EXPANSIONS.
					getJsonKey())) {

			simpleQueryString.setFuzzyMaxExpansions(
				clauseJsonObject.getInt(
					SimpleQueryStringConfigurationKeys.FUZZY_MAX_EXPANSIONS.
						getJsonKey()));
		}

		if (clauseJsonObject.has(
				SimpleQueryStringConfigurationKeys.FUZZY_PREFIX_LENGTH.
					getJsonKey())) {

			simpleQueryString.setFuzzyPrefixLength(
				clauseJsonObject.getInt(
					SimpleQueryStringConfigurationKeys.FUZZY_PREFIX_LENGTH.
						getJsonKey()));
		}

		if (clauseJsonObject.has(
				SimpleQueryStringConfigurationKeys.FUZZY_TRANSPOSITIONS.
					getJsonKey())) {

			simpleQueryString.setLenient(
				clauseJsonObject.getBoolean(
					SimpleQueryStringConfigurationKeys.FUZZY_TRANSPOSITIONS.
						getJsonKey()));
		}

		if (clauseJsonObject.has(
				SimpleQueryStringConfigurationKeys.LENIENT.getJsonKey())) {

			simpleQueryString.setLenient(
				clauseJsonObject.getBoolean(
					SimpleQueryStringConfigurationKeys.LENIENT.getJsonKey()));
		}

		if (clauseJsonObject.has(
				SimpleQueryStringConfigurationKeys.QUOTE_FIELD_SUFFIX.
					getJsonKey())) {

			simpleQueryString.setQuoteFieldSuffix(
				clauseJsonObject.getString(
					SimpleQueryStringConfigurationKeys.QUOTE_FIELD_SUFFIX.
						getJsonKey()));
		}

		return Optional.of(simpleQueryString);
	}

	@Reference
	private Queries _queries;

}