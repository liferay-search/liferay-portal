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

package com.liferay.search.experiences.predict.typeahead.field.internal.provider;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.highlight.FieldConfig;
import com.liferay.portal.search.highlight.FieldConfigBuilder;
import com.liferay.portal.search.highlight.FieldConfigBuilderFactory;
import com.liferay.portal.search.highlight.HighlightBuilder;
import com.liferay.portal.search.highlight.HighlightBuilderFactory;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.MultiMatchQuery;
import com.liferay.portal.search.query.NestedQuery;
import com.liferay.portal.search.query.Operator;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributes;
import com.liferay.search.experiences.predict.suggestions.constants.SuggestionConstants;
import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, service = FieldTypeaheadDataProviderSearchHelper.class
)
public class FieldTypeaheadDataProviderSearchHelper {

	public SearchRequestBuilder getSearchRequestBuilder(
		SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings, Map<String, Float> fieldMap,
		Map<String, Map<String, Float>> nestedFieldMap, String type) {

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder(
			).companyId(
				suggestionAttributes.getCompanyId()
			).emptySearchEnabled(
				true
			).fetchSourceIncludes(
				_getFieldNames(fieldMap, nestedFieldMap)
			).locale(
				suggestionAttributes.getLocale()
			).modelIndexerClassNames(
				_getModelIndexerClassNames(dataProviderSettings)
			).query(
				_getQuery(
					fieldMap, nestedFieldMap, suggestionAttributes,
					dataProviderSettings)
			).size(
				suggestionAttributes.getSize()
			).from(
				0
			);

		if (type.equals("highlighter")) {
			_setHighlight(searchRequestBuilder, fieldMap.keySet());
		}

		_setIndices(searchRequestBuilder, dataProviderSettings);

		_setSorts(searchRequestBuilder, dataProviderSettings);

		return searchRequestBuilder;
	}

	public SearchResponse search(SearchRequestBuilder searchRequestBuilder) {
		return _searcher.search(searchRequestBuilder.build());
	}

	private void _addGroupFilterClause(
		BooleanQuery booleanQuery, SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		long[] groupIds = GetterUtil.getLongValues(
			dataProviderSettings.getAttribute(
				SuggestionConstants.SOURCE_GROUP_IDS));

		if (groupIds.length != 0) {
			TermsQuery termsQuery = _queries.terms(Field.SCOPE_GROUP_ID);

			termsQuery.addValues(ArrayUtil.toStringArray(groupIds));

			booleanQuery.addFilterQueryClauses(termsQuery);
		}
		else {
			booleanQuery.addFilterQueryClauses(
				_queries.term(
					Field.SCOPE_GROUP_ID, suggestionAttributes.getGroupId()));
		}
	}

	private void _addNestedFieldNames(
		Set<String> fieldNameSet, Map<String, Float> nestedFieldMap) {

		fieldNameSet.addAll(nestedFieldMap.keySet());
	}

	private void _addNestedSearchClause(
		BooleanQuery booleanQuery, Map.Entry<String, Map<String, Float>> entry,
		SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		NestedQuery nestedQuery = _queries.nested(
			entry.getKey(),
			_getSearchQuery(
				entry.getValue(), suggestionAttributes, dataProviderSettings));

		booleanQuery.addShouldQueryClauses(nestedQuery);
	}

	private void _addSearchClauses(
		BooleanQuery booleanQuery, Map<String, Float> fieldMap,
		Map<String, Map<String, Float>> nestedFieldMap,
		SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		Query query1 = _getSearchQuery(
			fieldMap, suggestionAttributes, dataProviderSettings);

		Query query2 = _getNestedSearchQuery(
			nestedFieldMap, suggestionAttributes, dataProviderSettings);

		if (query2 == null) {
			booleanQuery.addMustQueryClauses(query1);
		}
		else {
			BooleanQuery searchQuery = _queries.booleanQuery();

			searchQuery.addShouldQueryClauses(query1, query2);

			booleanQuery.addMustQueryClauses(searchQuery);
		}
	}

	private void _addSort(
		SearchRequestBuilder searchRequestBuilder, String field, String order) {

		if (Validator.isBlank(order)) {
			searchRequestBuilder.addSort(_sorts.field(field));
		}
		else {
			searchRequestBuilder.addSort(
				_sorts.field(
					field, SortOrder.valueOf(StringUtil.toUpperCase(order))));
		}
	}

	private void _addTermFilterClauses(
		BooleanQuery booleanQuery, DataProviderSettings dataProviderSettings) {

		BooleanQuery termFilterClauses = _getTermFilterQuery(
			dataProviderSettings);

		if (termFilterClauses == null) {
			return;
		}

		booleanQuery.addFilterQueryClauses(termFilterClauses);
	}

	private Query _getAndMatchQuery(
		Map<String, Float> fieldMap,
		SuggestionAttributes suggestionAttributes) {

		MultiMatchQuery multiMatchQuery = _queries.multiMatch(
			suggestionAttributes.getKeywords(), fieldMap);

		multiMatchQuery.setBoost(2.0F);
		multiMatchQuery.setOperator(Operator.AND);

		return multiMatchQuery;
	}

	private Query _getBoolPrefixMatchQuery(
		Map<String, Float> fieldMap, SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		MultiMatchQuery multiMatchQuery = _queries.multiMatch(
			suggestionAttributes.getKeywords(), fieldMap);

		multiMatchQuery.setFuzziness(_getFuzziness(dataProviderSettings));
		multiMatchQuery.setOperator(_getOperator(dataProviderSettings));
		multiMatchQuery.setPrefixLength(_getPrefixLength(dataProviderSettings));
		multiMatchQuery.setType(MultiMatchQuery.Type.BOOL_PREFIX);

		return multiMatchQuery;
	}

	private FieldConfig _getFieldConfig(String fieldName) {
		FieldConfigBuilder fieldConfigBuilder =
			_fieldConfigBuilderFactory.builder(fieldName);

		fieldConfigBuilder.fragmentOffset(0);

		return fieldConfigBuilder.build();
	}

	private String[] _getFieldNames(
		Map<String, Float> fieldMap,
		Map<String, Map<String, Float>> nestedFieldMap) {

		Set<String> fieldNameSet = new HashSet<>();

		if (fieldMap != null) {
			fieldNameSet.addAll(fieldMap.keySet());
		}

		if (nestedFieldMap != null) {
			Set<Map.Entry<String, Map<String, Float>>> entrySet =
				nestedFieldMap.entrySet();

			entrySet.forEach(
				entry -> _addNestedFieldNames(fieldNameSet, entry.getValue()));
		}

		return fieldNameSet.toArray(new String[0]);
	}

	private String _getFuzziness(DataProviderSettings dataProviderSettings) {
		if (dataProviderSettings == null) {
			return _DEFAULT_FUZZINESS;
		}

		return GetterUtil.getString(
			dataProviderSettings.getAttribute(SuggestionConstants.FUZZINESS),
			_DEFAULT_FUZZINESS);
	}

	private String[] _getModelIndexerClassNames(
		DataProviderSettings dataProviderSettings) {

		return GetterUtil.getStringValues(
			dataProviderSettings.getAttribute(SuggestionConstants.ENTRY_CLASS_NAMES));
	}

	private Query _getNestedSearchQuery(
		Map<String, Map<String, Float>> nestedFieldMap,
		SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		if (MapUtil.isEmpty(nestedFieldMap)) {
			return null;
		}

		BooleanQuery booleanQuery = _queries.booleanQuery();

		Set<Map.Entry<String, Map<String, Float>>> entrySet =
			nestedFieldMap.entrySet();

		entrySet.forEach(
			entry -> _addNestedSearchClause(
				booleanQuery, entry, suggestionAttributes,
				dataProviderSettings));

		return booleanQuery;
	}

	private Operator _getOperator(DataProviderSettings dataProviderSettings) {
		if (dataProviderSettings == null) {
			return _DEFAULT_OPERATOR;
		}

		try {
			String operator = GetterUtil.getString(
				dataProviderSettings.getAttribute(
					SuggestionConstants.OPERATOR));

			if (Validator.isBlank(operator)) {
				return _DEFAULT_OPERATOR;
			}

			return Operator.valueOf(StringUtil.toUpperCase(operator));
		}
		catch (IllegalArgumentException illegalArgumentException) {
			_log.error(
				illegalArgumentException.getMessage(),
				illegalArgumentException);
		}

		return _DEFAULT_OPERATOR;
	}

	private Query _getPhraseMatchQuery(
		Map<String, Float> fieldMap,
		SuggestionAttributes suggestionAttributes) {

		MultiMatchQuery multiMatchQuery = _queries.multiMatch(
			suggestionAttributes.getKeywords(), fieldMap);

		multiMatchQuery.setType(MultiMatchQuery.Type.PHRASE);
		multiMatchQuery.setBoost(4.0F);

		return multiMatchQuery;
	}

	private int _getPrefixLength(DataProviderSettings dataProviderSettings) {
		if (dataProviderSettings == null) {
			return _DEFAULT_PREFIX_LENGTH;
		}

		return GetterUtil.getInteger(
			dataProviderSettings.getAttribute(
				SuggestionConstants.PREFIX_LENGTH),
			_DEFAULT_PREFIX_LENGTH);
	}

	private BooleanQuery _getQuery(
		Map<String, Float> fieldMap,
		Map<String, Map<String, Float>> nestedFieldMap,
		SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		_addGroupFilterClause(
			booleanQuery, suggestionAttributes, dataProviderSettings);

		_addTermFilterClauses(booleanQuery, dataProviderSettings);

		_addSearchClauses(
			booleanQuery, fieldMap, nestedFieldMap, suggestionAttributes,
			dataProviderSettings);

		return booleanQuery;
	}

	private Query _getSearchQuery(
		Map<String, Float> fieldMap, SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		booleanQuery.addShouldQueryClauses(
			_getPhraseMatchQuery(fieldMap, suggestionAttributes),
			_getAndMatchQuery(fieldMap, suggestionAttributes),
			_getBoolPrefixMatchQuery(
				fieldMap, suggestionAttributes, dataProviderSettings));

		return booleanQuery;
	}

	private BooleanQuery _getTermFilterQuery(
		DataProviderSettings dataProviderSettings) {

		Map<String, String> termFilterMap =
			(Map<String, String>)dataProviderSettings.getAttribute(
				"termFilters");

		if (termFilterMap == null) {
			return null;
		}

		BooleanQuery booleanQuery = _queries.booleanQuery();

		Set<Map.Entry<String, String>> entrySet = termFilterMap.entrySet();

		entrySet.forEach(
			entry -> booleanQuery.addMustQueryClauses(
				_queries.term(entry.getKey(), entry.getValue())));

		return booleanQuery;
	}

	private void _setHighlight(
		SearchRequestBuilder searchRequestBuilder, Set<String> fieldNames) {

		HighlightBuilder highlightBuilder = _highlightBuilderFactory.builder();

		highlightBuilder.fragmentSize(60);
		highlightBuilder.highlighterType("fvh");
		highlightBuilder.numOfFragments(2);
		highlightBuilder.requireFieldMatch(true);
		highlightBuilder.phraseLimit(50);
		highlightBuilder.preTags("<hl>");
		highlightBuilder.postTags("</hl>");

		fieldNames.forEach(
			fieldName -> highlightBuilder.addFieldConfig(
				_getFieldConfig(fieldName)));

		searchRequestBuilder.highlight(highlightBuilder.build());
	}

	private void _setIndices(
		SearchRequestBuilder searchRequestBuilder,
		DataProviderSettings dataProviderSettings) {

		if (dataProviderSettings == null) {
			return;
		}

		String[] indices = GetterUtil.getStringValues(
			dataProviderSettings.getAttribute(SuggestionConstants.INDICES));

		if (indices.length > 0) {
			searchRequestBuilder.indexes(indices);
		}
	}

	private void _setSorts(
		SearchRequestBuilder searchRequestBuilder,
		DataProviderSettings dataProviderSettings) {

		Map<String, String> sortFieldMap =
			(Map<String, String>)dataProviderSettings.getAttribute(
				"sortFieldMap");

		if (sortFieldMap == null) {
			return;
		}

		Set<Map.Entry<String, String>> entrySet = sortFieldMap.entrySet();

		entrySet.forEach(
			entry -> _addSort(
				searchRequestBuilder, entry.getKey(), entry.getValue()));
	}

	private static final String _DEFAULT_FUZZINESS = "1";

	private static final Operator _DEFAULT_OPERATOR = Operator.OR;

	private static final int _DEFAULT_PREFIX_LENGTH = 2;

	private static final Log _log = LogFactoryUtil.getLog(
		FieldTypeaheadDataProviderSearchHelper.class);

	@Reference
	private FieldConfigBuilderFactory _fieldConfigBuilderFactory;

	@Reference
	private HighlightBuilderFactory _highlightBuilderFactory;

	@Reference
	private Queries _queries;

	@Reference
	private Searcher _searcher;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Reference
	private Sorts _sorts;

}