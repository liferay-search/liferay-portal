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

package com.liferay.portal.search.tuning.blueprints.query.index.web.internal.display.context;

import com.liferay.portal.kernel.dao.search.EmptyOnClickRowChecker;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryIndexMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryIndexPortletKeys;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryStringStatus;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.DocumentToQueryStringTranslator;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryString;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringFields;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.request.SearchQueryStringRequest;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.request.SearchQueryStringResponse;

import java.util.Collections;
import java.util.List;

import javax.portlet.PortletException;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public class ViewQueryStringsDisplayContext {

	public ViewQueryStringsDisplayContext(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		DocumentToQueryStringTranslator documentToQueryStringTranslator,
		Portal portal, Queries queries,
		QueryStringIndexNameBuilder queryStringIndexNameBuilder,
		SearchEngineAdapter searchEngineAdapter, Sorts sorts,
		QueryStringStatus status) {

		_liferayPortletRequest = liferayPortletRequest;
		_liferayPortletResponse = liferayPortletResponse;
		_documentToQueryStringTranslator = documentToQueryStringTranslator;
		_portal = portal;
		_queries = queries;
		_queryStringIndexNameBuilder = queryStringIndexNameBuilder;
		_searchEngineAdapter = searchEngineAdapter;
		_sorts = sorts;
		_status = status;

		_httpServletRequest = _liferayPortletRequest.getHttpServletRequest();
		_portalPreferences = PortletPreferencesFactoryUtil.getPortalPreferences(
			liferayPortletRequest);
	}

	public List<String> getAvailableActions(QueryString queryString)
		throws PortalException {

		return Collections.singletonList("deleteEntries");
	}

	public String getDisplayStyle() {
		String displayStyle = ParamUtil.getString(
			_httpServletRequest, "displayStyle");

		String preferenceName =
			"entries-display-style-" + StringUtil.toLowerCase(_status.name());

		if (Validator.isNull(displayStyle)) {
			return _portalPreferences.getValue(
				QueryIndexPortletKeys.QUERY_INDEX_ADMIN, preferenceName,
				"descriptive");
		}

		_portalPreferences.setValue(
			QueryIndexPortletKeys.QUERY_INDEX_ADMIN, preferenceName,
			displayStyle);

		_httpServletRequest.setAttribute(
			WebKeys.SINGLE_PAGE_APPLICATION_CLEAR_CACHE, Boolean.TRUE);

		return displayStyle;
	}

	public SearchContainer<QueryString> getSearchContainer()
		throws PortalException, PortletException {

		SearchContainer<QueryString> searchContainer = new SearchContainer<>(
			_liferayPortletRequest, _getIteratorURL(), null,
			"there-are-no-query-strings");

		searchContainer.setOrderByCol(
			ParamUtil.getString(
				_httpServletRequest, "orderByCol", QueryStringFields.CREATED));

		searchContainer.setOrderByType(
			ParamUtil.getString(_httpServletRequest, "orderByType", "desc"));

		searchContainer.setRowChecker(
			new EmptyOnClickRowChecker(_liferayPortletResponse));

		_populateResults(searchContainer);

		return searchContainer;
	}

	private PortletURL _getIteratorURL() {
		PortletURL portletURL = _liferayPortletResponse.createRenderURL();

		portletURL.setProperty(
			"mvcRenderCommandName",
			QueryIndexMVCCommandNames.VIEW_QUERY_STRINGS);

		portletURL.setParameter("tabs", _status.name());

		return portletURL;
	}

	private void _populateResults(SearchContainer<QueryString> searchContainer)
		throws PortalException {

		SearchQueryStringRequest searchQueryStringRequest =
			new SearchQueryStringRequest(
				_queryStringIndexNameBuilder.getQueryStringIndexName(
					_portal.getCompanyId(_liferayPortletRequest)),
				_httpServletRequest, _queries, searchContainer,
				_searchEngineAdapter, _sorts, _status);

		SearchQueryStringResponse searchQueryStringResponse =
			searchQueryStringRequest.search();

		searchContainer.setResults(
			_documentToQueryStringTranslator.translateAll(
				searchQueryStringResponse.getSearchHits()));

		searchContainer.setSearch(true);
		searchContainer.setTotal(searchQueryStringResponse.getTotalHits());
	}

	private final DocumentToQueryStringTranslator
		_documentToQueryStringTranslator;
	private final HttpServletRequest _httpServletRequest;
	private final LiferayPortletRequest _liferayPortletRequest;
	private final LiferayPortletResponse _liferayPortletResponse;
	private final Portal _portal;
	private final PortalPreferences _portalPreferences;
	private final Queries _queries;
	private final QueryStringIndexNameBuilder _queryStringIndexNameBuilder;
	private final SearchEngineAdapter _searchEngineAdapter;
	private final Sorts _sorts;
	private final QueryStringStatus _status;

}