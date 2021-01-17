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

package com.liferay.portal.search.tuning.blueprints.admin.web.internal.util;

import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.index.IndexInformation;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.query.TermQuery;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.sort.Sort;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.portal.search.tuning.blueprints.constants.BlueprintTypes;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintsAdminIndexHelper.class)
public class BlueprintsAdminIndexHelper {

	public Map<String, List<String>> getMappedFields(long companyId) {
		String indexName = _indexNameBuilder.getIndexName(companyId);

		String fieldMappings = _indexInformation.getFieldMappings(indexName);

		Map<String, List<String>> fieldMap = new HashMap<>();

		try {
			JSONObject propertiesJSONObject = JSONUtil.getValueAsJSONObject(
				JSONFactoryUtil.createJSONObject(fieldMappings),
				"JSONObject/" + indexName, "JSONObject/mappings",
				"JSONObject/properties");

			Set<String> fieldKeySet = propertiesJSONObject.keySet();

			for (String fieldName : fieldKeySet) {
				JSONObject fieldJSONObject = propertiesJSONObject.getJSONObject(
					fieldName);

				String type = fieldJSONObject.getString("type");

				if (!fieldMap.containsKey(type)) {
					fieldMap.put(type, new ArrayList<String>());
				}

				List<String> fields = fieldMap.get(type);

				fields.add(fieldName);
			}
		}
		catch (JSONException jsonException) {
			_log.error(jsonException.getMessage(), jsonException);
		}

		return fieldMap;
	}

	public SearchHits searchBlueprints(
		HttpServletRequest httpServletRequest, long groupId, int status,
		String blueprintType, int size, int start, String orderByCol,
		String orderByType) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		String languageId = themeDisplay.getLanguageId();

		SearchRequest searchRequest = _searchRequestBuilderFactory.builder(
		).companyId(
			themeDisplay.getCompanyId()
		).from(
			start
		).modelIndexerClasses(
			Blueprint.class
		).query(
			_getQuery(
				_getKeywords(httpServletRequest), blueprintType, groupId,
				status, languageId)
		).size(
			size
		).addSort(
			_getSort(orderByCol, orderByType, languageId)
		).build();

		SearchResponse searchResponse = _searcher.search(searchRequest);

		return searchResponse.getSearchHits();
	}

	private void _addBlueprintTypeFilterClause(BooleanQuery booleanQuery) {
		booleanQuery.addFilterQueryClauses(
			_queries.term(Field.TYPE, BlueprintTypes.BLUEPRINT));
	}

	private void _addFragmentTypeFilterClause(BooleanQuery booleanQuery) {
		TermsQuery termsQuery = _queries.terms(Field.TYPE + "_sortable");

		termsQuery.addValues(_getFragmentTypes());

		booleanQuery.addFilterQueryClauses(termsQuery);
	}

	private void _addGroupFilterClause(
		BooleanQuery booleanQuery, long groupId) {

		TermQuery groupQuery = _queries.term(Field.GROUP_ID, groupId);

		booleanQuery.addFilterQueryClauses(groupQuery);
	}

	private void _addSearchClauses(
		BooleanQuery booleanQuery, String keywords, String languageId) {

		if (Validator.isBlank(keywords)) {
			booleanQuery.addMustQueryClauses(_queries.matchAll());
		}
		else {
			booleanQuery.addMustQueryClauses(
				_queries.multiMatch(keywords, _getSearchFields(languageId)));
		}
	}

	private void _addStatusFilterClause(BooleanQuery booleanQuery, int status) {
		booleanQuery.addFilterQueryClauses(_queries.term(Field.STATUS, status));
	}

	private Object[] _getFragmentTypes() {
		return new Object[] {
			String.valueOf(BlueprintTypes.AGGREGATION_FRAGMENT),
			String.valueOf(BlueprintTypes.FACET_FRAGMENT),
			String.valueOf(BlueprintTypes.QUERY_FRAGMENT),
			String.valueOf(BlueprintTypes.SUGGESTER_FRAGMENT)
		};
	}

	private String _getKeywords(HttpServletRequest httpServletRequest) {
		return ParamUtil.getString(httpServletRequest, "keywords");
	}

	private Query _getQuery(
		String keywords, String blueprintType, long groupId, int status,
		String languageId) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		_addGroupFilterClause(booleanQuery, groupId);

		if (blueprintType.equals("blueprints")) {
			_addBlueprintTypeFilterClause(booleanQuery);
		}
		else {
			_addFragmentTypeFilterClause(booleanQuery);
		}

		_addStatusFilterClause(booleanQuery, status);

		_addSearchClauses(booleanQuery, keywords, languageId);

		return booleanQuery;
	}

	private Set<String> _getSearchFields(String languageId) {
		Set<String> fields = new HashSet<>();

		fields.add(_getTitleField(languageId));
		fields.add(
			LocalizationUtil.getLocalizedName(Field.DESCRIPTION, languageId));

		return fields;
	}

	private Sort _getSort(
		String orderByCol, String orderByType, String languageId) {

		SortOrder sortOrder = SortOrder.ASC;

		if (orderByType.equals("desc")) {
			sortOrder = SortOrder.DESC;
		}

		if (Objects.equals(orderByCol, Field.TITLE)) {
			return _sorts.field(
				_getTitleField(languageId) + "_String_sortable", sortOrder);
		}

		return _sorts.field(orderByCol, sortOrder);
	}

	private String _getTitleField(String languageId) {
		return LocalizationUtil.getLocalizedName(
			"localized_" + Field.TITLE, languageId);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintsAdminIndexHelper.class);

	@Reference
	private BlueprintService _blueprintService;

	@Reference
	private IndexInformation _indexInformation;

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private Queries _queries;

	@Reference
	private Searcher _searcher;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Reference
	private Sorts _sorts;

}