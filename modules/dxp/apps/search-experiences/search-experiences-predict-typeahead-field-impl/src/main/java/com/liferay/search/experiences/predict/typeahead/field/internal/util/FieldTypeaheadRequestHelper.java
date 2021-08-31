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

package com.liferay.search.experiences.predict.typeahead.field.internal.util;

import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.highlight.FieldConfig;
import com.liferay.portal.search.highlight.FieldConfigBuilder;
import com.liferay.portal.search.highlight.FieldConfigBuilderFactory;
import com.liferay.portal.search.highlight.HighlightBuilder;
import com.liferay.portal.search.highlight.HighlightBuilderFactory;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.search.experiences.predict.typeahead.field.definition.FieldTypeaheadSourceDefinition;
import com.liferay.search.experiences.predict.typeahead.field.internal.data.FieldTypeaheadContext;
import com.liferay.search.experiences.predict.typeahead.field.internal.definition.SourceDefinitionTranslator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = FieldTypeaheadRequestHelper.class)
public class FieldTypeaheadRequestHelper {

	public SearchRequestBuilder getSearchRequestBuilder(
		FieldTypeaheadContext fieldTypeaheadContext) {

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder(
			).companyId(
				fieldTypeaheadContext.getCompanyId()
			).emptySearchEnabled(
				true
			).fetchSourceIncludes(
				fieldTypeaheadContext.getAllFieldNames()
			).locale(
				fieldTypeaheadContext.getLocale()
			).modelIndexerClassNames(
				fieldTypeaheadContext.getEntryClassNames()
			).query(
				_getQuery(fieldTypeaheadContext)
			).size(
				fieldTypeaheadContext.getSize()
			).from(
				0
			);

		String type = fieldTypeaheadContext.getType();

		if (type.equals("highlighter")) {
			_setHighlight(
				searchRequestBuilder, fieldTypeaheadContext.getFieldNames());
		}

		_setIndices(searchRequestBuilder, fieldTypeaheadContext);

		_setSorts(searchRequestBuilder, fieldTypeaheadContext);

		return searchRequestBuilder;
	}

	public SearchResponse search(SearchRequestBuilder searchRequestBuilder) {
		return _searcher.search(searchRequestBuilder.build());
	}

	private void _addGroupFilterClause(
		BooleanQuery booleanQuery,
		FieldTypeaheadContext fieldTypeaheadContext) {

		long[] groupIds = fieldTypeaheadContext.getSourceGroupIds();

		if (groupIds.length != 0) {
			TermsQuery termsQuery = _queries.terms(Field.SCOPE_GROUP_ID);

			termsQuery.addValues(ArrayUtil.toStringArray(groupIds));

			booleanQuery.addFilterQueryClauses(termsQuery);
		}
		else {
			booleanQuery.addFilterQueryClauses(
				_queries.term(
					Field.SCOPE_GROUP_ID, fieldTypeaheadContext.getGroupId()));
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

	private FieldConfig _getFieldConfig(String fieldName) {
		FieldConfigBuilder fieldConfigBuilder =
			_fieldConfigBuilderFactory.builder(fieldName);

		fieldConfigBuilder.fragmentOffset(0);

		return fieldConfigBuilder.build();
	}

	private BooleanQuery _getQuery(
		FieldTypeaheadContext fieldTypeaheadContext) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		List<Query> queries = new ArrayList<>();

		List<FieldTypeaheadSourceDefinition> sourceDefinitions =
			fieldTypeaheadContext.getSourceDefinitions();

		sourceDefinitions.forEach(
			sourceDefinition -> queries.add(
				sourceDefinition.accept(
					new SourceDefinitionTranslator(
						_queries, fieldTypeaheadContext.getKeywords()),
					fieldTypeaheadContext.getFuzziness(),
					fieldTypeaheadContext.getOperator(),
					fieldTypeaheadContext.getPrefixLength())));

		if (queries.size() == 1) {
			booleanQuery.addMustQueryClauses(queries.get(0));
		}
		else {
			BooleanQuery criteriaQuery = _queries.booleanQuery();

			criteriaQuery.addShouldQueryClauses(queries.toArray(new Query[0]));

			booleanQuery.addMustQueryClauses(criteriaQuery);
		}

		_addGroupFilterClause(booleanQuery, fieldTypeaheadContext);

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
		FieldTypeaheadContext fieldTypeaheadContext) {

		String[] indices = fieldTypeaheadContext.getIndices();

		if (indices.length > 0) {
			searchRequestBuilder.indexes(indices);
		}
	}

	private void _setSorts(
		SearchRequestBuilder searchRequestBuilder,
		FieldTypeaheadContext fieldTypeaheadContext) {

		Map<String, String> sortFieldMap =
			fieldTypeaheadContext.getSortFieldMap();

		if (sortFieldMap == null) {
			return;
		}

		Set<Map.Entry<String, String>> entrySet = sortFieldMap.entrySet();

		entrySet.forEach(
			entry -> _addSort(
				searchRequestBuilder, entry.getKey(), entry.getValue()));
	}

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