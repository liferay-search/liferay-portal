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

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
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

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = {})
public class BlueprintsAdminSearchUtil {

	public static SearchHits searchBlueprints(
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

	public static Optional<Blueprint> toBlueprintOptional(SearchHit searchHit) {
		long entryClassPK = _getEntryClassPK(searchHit);

		try {
			return Optional.of(_blueprintService.getBlueprint(entryClassPK));
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Search index is stale and contains a Blueprint entry " +
						entryClassPK);
			}

			return Optional.empty();
		}
	}

	@Reference(unbind = "-")
	protected void setBlueprintService(BlueprintService blueprintService) {
		_blueprintService = blueprintService;
	}

	@Reference(unbind = "-")
	protected void setQueries(Queries queries) {
		_queries = queries;
	}

	@Reference(unbind = "-")
	protected void setSearcher(Searcher searcher) {
		_searcher = searcher;
	}

	@Reference(unbind = "-")
	protected void setSearchRequestBuilderFactory(
		SearchRequestBuilderFactory searchRequestBuilderFactory) {

		_searchRequestBuilderFactory = searchRequestBuilderFactory;
	}

	@Reference(unbind = "-")
	protected void setSorts(Sorts sorts) {
		_sorts = sorts;
	}

	private static void _addBlueprintTypeFilterClause(
		BooleanQuery booleanQuery) {

		booleanQuery.addFilterQueryClauses(
			_queries.term(Field.TYPE, BlueprintTypes.BLUEPRINT));
	}

	private static void _addFragmentTypeFilterClause(
		BooleanQuery booleanQuery) {

		TermsQuery termsQuery = _queries.terms(Field.TYPE + "_sortable");

		termsQuery.addValues(_getFragmentTypes());

		booleanQuery.addFilterQueryClauses(termsQuery);
	}

	private static void _addGroupFilterClause(
		BooleanQuery booleanQuery, long groupId) {

		TermQuery groupQuery = _queries.term(Field.GROUP_ID, groupId);

		booleanQuery.addFilterQueryClauses(groupQuery);
	}

	private static void _addSearchClauses(
		BooleanQuery booleanQuery, String keywords, String languageId) {

		if (Validator.isBlank(keywords)) {
			booleanQuery.addMustQueryClauses(_queries.matchAll());
		}
		else {
			booleanQuery.addMustQueryClauses(
				_queries.multiMatch(keywords, _getSearchFields(languageId)));
		}
	}

	private static void _addStatusFilterClause(
		BooleanQuery booleanQuery, int status) {

		booleanQuery.addFilterQueryClauses(_queries.term(Field.STATUS, status));
	}

	private static long _getEntryClassPK(SearchHit searchHit) {
		Document document = searchHit.getDocument();

		return document.getLong(Field.ENTRY_CLASS_PK);
	}

	private static Object[] _getFragmentTypes() {
		return new Object[] {
			String.valueOf(BlueprintTypes.AGGREGATION_FRAGMENT),
			String.valueOf(BlueprintTypes.FACET_FRAGMENT),
			String.valueOf(BlueprintTypes.QUERY_FRAGMENT),
			String.valueOf(BlueprintTypes.SUGGESTER_FRAGMENT)
		};
	}

	private static String _getKeywords(HttpServletRequest httpServletRequest) {
		return ParamUtil.getString(httpServletRequest, "keywords");
	}

	private static Query _getQuery(
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

	private static Set<String> _getSearchFields(String languageId) {
		Set<String> fields = new HashSet<>();

		fields.add(_getTitleField(languageId));
		fields.add(
			LocalizationUtil.getLocalizedName(Field.DESCRIPTION, languageId));

		return fields;
	}

	private static Sort _getSort(
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

	private static String _getTitleField(String languageId) {
		return LocalizationUtil.getLocalizedName(
			"localized_" + Field.TITLE, languageId);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintsAdminSearchUtil.class);

	private static BlueprintService _blueprintService;
	private static Queries _queries;
	private static Searcher _searcher;
	private static SearchRequestBuilderFactory _searchRequestBuilderFactory;
	private static Sorts _sorts;

}