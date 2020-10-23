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

package com.liferay.portal.search.tuning.blueprints.engine.internal.clause.translator;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.query.Operator;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.query.SimpleStringQuery;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.query.SimpleQueryStringConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.clause.ClauseTranslator;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "type=simple_query_string",
	service = ClauseTranslator.class
)
public class SimpleQueryStringClauseTranslator implements ClauseTranslator {

	@Override
	public Optional<Query> translate(
		Blueprint blueprint, ParameterData parameterData, Messages messages,
		JSONObject configurationJsonObject) {

		String keywords = configurationJsonObject.getString(
			SimpleQueryStringConfigurationKeys.QUERY.getJsonKey(),
			parameterData.getKeywords());

		SimpleStringQuery simpleStringQuery = _queries.simpleString(keywords);

		if (configurationJsonObject.has(
				SimpleQueryStringConfigurationKeys.ANALYZE_WILDCARD.
					getJsonKey())) {

			simpleStringQuery.setAnalyzeWildcard(
				configurationJsonObject.getBoolean(
					SimpleQueryStringConfigurationKeys.ANALYZE_WILDCARD.
						getJsonKey()));
		}

		if (configurationJsonObject.has(
				SimpleQueryStringConfigurationKeys.ANALYZER.getJsonKey())) {

			simpleStringQuery.setAnalyzer(
				SimpleQueryStringConfigurationKeys.ANALYZER.getJsonKey());
		}

		if (configurationJsonObject.has(
				SimpleQueryStringConfigurationKeys.
					AUTO_GENERATE_SYNONYMS_PHRASE_QUERY.getJsonKey())) {

			simpleStringQuery.setAutoGenerateSynonymsPhraseQuery(
				configurationJsonObject.getBoolean(
					SimpleQueryStringConfigurationKeys.
						AUTO_GENERATE_SYNONYMS_PHRASE_QUERY.getJsonKey()));
		}

		_setOperator(simpleStringQuery, configurationJsonObject);

		_setFields(simpleStringQuery, configurationJsonObject);

		if (configurationJsonObject.has(
				SimpleQueryStringConfigurationKeys.FUZZY_MAX_EXPANSIONS.
					getJsonKey())) {

			simpleStringQuery.setFuzzyMaxExpansions(
				configurationJsonObject.getInt(
					SimpleQueryStringConfigurationKeys.FUZZY_MAX_EXPANSIONS.
						getJsonKey()));
		}

		if (configurationJsonObject.has(
				SimpleQueryStringConfigurationKeys.FUZZY_PREFIX_LENGTH.
					getJsonKey())) {

			simpleStringQuery.setFuzzyPrefixLength(
				configurationJsonObject.getInt(
					SimpleQueryStringConfigurationKeys.FUZZY_PREFIX_LENGTH.
						getJsonKey()));
		}

		if (configurationJsonObject.has(
				SimpleQueryStringConfigurationKeys.FUZZY_TRANSPOSITIONS.
					getJsonKey())) {

			simpleStringQuery.setFuzzyTranspositions(
				configurationJsonObject.getBoolean(
					SimpleQueryStringConfigurationKeys.FUZZY_TRANSPOSITIONS.
						getJsonKey()));
		}

		if (configurationJsonObject.has(
				SimpleQueryStringConfigurationKeys.LENIENT.getJsonKey())) {

			simpleStringQuery.setLenient(
				configurationJsonObject.getBoolean(
					SimpleQueryStringConfigurationKeys.LENIENT.getJsonKey()));
		}

		if (configurationJsonObject.has(
				SimpleQueryStringConfigurationKeys.QUOTE_FIELD_SUFFIX.
					getJsonKey())) {

			simpleStringQuery.setQuoteFieldSuffix(
				configurationJsonObject.getString(
					SimpleQueryStringConfigurationKeys.QUOTE_FIELD_SUFFIX.
						getJsonKey()));
		}

		return Optional.of(simpleStringQuery);
	}

	private void _setFields(
		SimpleStringQuery simpleStringQuery,
		JSONObject configurationJsonObject) {

		if (configurationJsonObject.has(
				SimpleQueryStringConfigurationKeys.FIELDS.getJsonKey())) {

			JSONArray fieldsJsonArray = configurationJsonObject.getJSONArray(
				SimpleQueryStringConfigurationKeys.FIELDS.getJsonKey());

			for (int i = 0; i < fieldsJsonArray.length(); i++) {
				JSONObject fieldJsonObject = fieldsJsonArray.getJSONObject(i);

				String fieldName = fieldJsonObject.getString(
					SimpleQueryStringConfigurationKeys.FIELD.getJsonKey());

				float boost = GetterUtil.getFloat(
					fieldJsonObject.getString(
						SimpleQueryStringConfigurationKeys.BOOST.getJsonKey()),
					1.0F);

				simpleStringQuery.addField(fieldName, boost);
			}
		}
	}

	private void _setOperator(
		SimpleStringQuery simpleStringQuery,
		JSONObject configurationJsonObject) {

		if (configurationJsonObject.has(
				SimpleQueryStringConfigurationKeys.DEFAULT_OPERATOR.
					getJsonKey())) {

			String operator = configurationJsonObject.getString(
				SimpleQueryStringConfigurationKeys.DEFAULT_OPERATOR.
					getJsonKey(),
				Operator.AND.name());

			if (StringUtil.equalsIgnoreCase(operator, Operator.AND.name())) {
				simpleStringQuery.setDefaultOperator(Operator.AND);
			}
			else {
				simpleStringQuery.setDefaultOperator(Operator.OR);
			}
		}
	}

	@Reference
	private Queries _queries;

}