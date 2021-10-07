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

package com.liferay.search.experiences.predict.typeahead.field.internal.definition;

import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.MultiMatchQuery;
import com.liferay.portal.search.query.Operator;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.search.experiences.predict.typeahead.field.definition.FieldTypeaheadSourceDefinitionVisitor;
import com.liferay.search.experiences.predict.typeahead.field.definition.FieldsSourceDefinition;
import com.liferay.search.experiences.predict.typeahead.field.definition.NestedFieldSourceDefinition;

import java.util.Map;
import java.util.Set;

/**
 * @author Petteri Karttunen
 */
public class SourceDefinitionTranslator
	implements FieldTypeaheadSourceDefinitionVisitor {

	public SourceDefinitionTranslator(Queries queries, String keywords) {
		_queries = queries;
		_keywords = keywords;
	}

	@Override
	public Query visit(
		FieldsSourceDefinition fieldsSourceDefinition, String fuzziness,
		Operator operator, int prefixLength) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		booleanQuery.addMustQueryClauses(
			_getSearchQuery(
				fieldsSourceDefinition.getFieldsBoosts(), fuzziness, operator,
				prefixLength));

		_addTermFilterClauses(
			booleanQuery, fieldsSourceDefinition.getTermFilterMap());

		return booleanQuery;
	}

	@Override
	public Query visit(
		NestedFieldSourceDefinition nestedFieldSourceDefinition,
		String fuzziness, Operator operator, int prefixLength) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		booleanQuery.addMustQueryClauses(
			_getNestedSearchQuery(
				nestedFieldSourceDefinition, fuzziness, operator,
				prefixLength));

		_addTermFilterClauses(
			booleanQuery, nestedFieldSourceDefinition.getTermFilterMap());

		return booleanQuery;
	}

	private void _addNestedFieldMustClauses(
		BooleanQuery booleanQuery,
		NestedFieldSourceDefinition nestedFieldSourceDefinition) {

		Map<String, String> nestedMustTermMap =
			nestedFieldSourceDefinition.getNestedMustTermMap();

		if (nestedMustTermMap == null) {
			return;
		}

		Set<Map.Entry<String, String>> entrySet = nestedMustTermMap.entrySet();

		entrySet.forEach(
			entry -> booleanQuery.addMustQueryClauses(
				_queries.term(entry.getKey(), entry.getValue())));
	}

	private void _addTermFilterClauses(
		BooleanQuery booleanQuery, Map<String, String> termFilterMap) {

		if (termFilterMap == null) {
			return;
		}

		Set<Map.Entry<String, String>> entrySet = termFilterMap.entrySet();

		entrySet.forEach(
			entry -> booleanQuery.addFilterQueryClauses(
				_queries.term(entry.getKey(), entry.getValue())));
	}

	private Query _getAndMatchQuery(
		Map<String, Float> fieldsBoosts, String keywords) {

		MultiMatchQuery multiMatchQuery = _queries.multiMatch(
			keywords, fieldsBoosts);

		multiMatchQuery.setBoost(2.0F);
		multiMatchQuery.setOperator(Operator.AND);

		return multiMatchQuery;
	}

	private Query _getBoolPrefixMatchQuery(
		Map<String, Float> fieldsBoosts, String keywords, Operator operator,
		String fuzziness, int prefixLength) {

		MultiMatchQuery multiMatchQuery = _queries.multiMatch(
			keywords, fieldsBoosts);

		multiMatchQuery.setFuzziness(fuzziness);
		multiMatchQuery.setOperator(operator);
		multiMatchQuery.setPrefixLength(prefixLength);
		multiMatchQuery.setType(MultiMatchQuery.Type.BOOL_PREFIX);

		return multiMatchQuery;
	}

	private Query _getNestedSearchQuery(
		NestedFieldSourceDefinition nestedFieldSourceDefinition,
		String fuzziness, Operator operator, int prefixLength) {

		Map<String, Float> fieldsBoosts = HashMapBuilder.put(
			nestedFieldSourceDefinition.getValueFieldName(), 1F
		).build();

		BooleanQuery booleanQuery = _queries.booleanQuery();

		booleanQuery.addMustQueryClauses(
			_getSearchQuery(fieldsBoosts, fuzziness, operator, prefixLength));

		_addNestedFieldMustClauses(booleanQuery, nestedFieldSourceDefinition);

		return _queries.nested(
			nestedFieldSourceDefinition.getPath(), booleanQuery);
	}

	private Query _getPhraseMatchQuery(
		Map<String, Float> fieldsBoosts, String keywords) {

		MultiMatchQuery multiMatchQuery = _queries.multiMatch(
			keywords, fieldsBoosts);

		multiMatchQuery.setType(MultiMatchQuery.Type.PHRASE);
		multiMatchQuery.setBoost(4.0F);

		return multiMatchQuery;
	}

	private Query _getSearchQuery(
		Map<String, Float> fieldsBoosts, String fuzziness, Operator operator,
		int prefixLength) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		booleanQuery.addShouldQueryClauses(
			_getPhraseMatchQuery(fieldsBoosts, _keywords),
			_getAndMatchQuery(fieldsBoosts, _keywords),
			_getBoolPrefixMatchQuery(
				fieldsBoosts, _keywords, operator, fuzziness, prefixLength));

		return booleanQuery;
	}

	private final String _keywords;
	private final Queries _queries;

}