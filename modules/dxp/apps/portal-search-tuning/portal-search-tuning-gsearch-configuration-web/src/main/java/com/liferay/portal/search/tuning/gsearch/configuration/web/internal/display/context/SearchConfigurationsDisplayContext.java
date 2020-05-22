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

package com.liferay.portal.search.tuning.gsearch.configuration.web.internal.display.context;

import com.liferay.portal.kernel.dao.search.EmptyOnClickRowChecker;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.portlet.PortletURLUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchContextFactory;
import com.liferay.portal.kernel.search.SearchResult;
import com.liferay.portal.kernel.search.SearchResultUtil;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SearchConfigurationActionKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SearchConfigurationPortletKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SearchConfiguration;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.constants.SearchConfigurationMVCCommandNames;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.security.permission.resource.SearchConfigurationEntryPermission;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.portlet.PortletException;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public class SearchConfigurationsDisplayContext {

	public SearchConfigurationsDisplayContext(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		int searchConfigurationType) {

		_liferayPortletRequest = liferayPortletRequest;
		_liferayPortletResponse = liferayPortletResponse;

		_portalPreferences = PortletPreferencesFactoryUtil.getPortalPreferences(
			liferayPortletRequest);

		_httpServletRequest = _liferayPortletRequest.getHttpServletRequest();

		_searchConfigurationType = searchConfigurationType;
	}

	public List<String> getAvailableActions(
			SearchConfiguration searchConfiguration)
		throws PortalException {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)_httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		if (SearchConfigurationEntryPermission.contains(
				themeDisplay.getPermissionChecker(), searchConfiguration,
				SearchConfigurationActionKeys.DELETE_CONFIGURATION)) {

			return Collections.singletonList("deleteEntries");
		}

		return Collections.emptyList();
	}

	public String getDisplayStyle() {
		String displayStyle = ParamUtil.getString(
			_httpServletRequest, "displayStyle");

		if (Validator.isNull(displayStyle)) {
			return _portalPreferences.getValue(
				SearchConfigurationPortletKeys.SEARCH_CONFIGURATION_ADMIN,
				"entries-display-style", "icon");
		}

		_portalPreferences.setValue(
			SearchConfigurationPortletKeys.SEARCH_CONFIGURATION_ADMIN,
			"entries-display-style", displayStyle);

		_httpServletRequest.setAttribute(
			WebKeys.SINGLE_PAGE_APPLICATION_CLEAR_CACHE, Boolean.TRUE);

		return displayStyle;
	}

	public SearchContainer getSearchContainer()
		throws PortalException, PortletException {

		PortletURL portletURL = _liferayPortletResponse.createRenderURL();

		portletURL.setParameter(
			"mvcRenderCommandName",
			SearchConfigurationMVCCommandNames.VIEW_SEARCH_CONFIGURATIONS);

		SearchContainer<SearchConfiguration> entriesSearchContainer =
			new SearchContainer<>(
				_liferayPortletRequest,
				PortletURLUtil.clone(portletURL, _liferayPortletResponse), null,
				"no-entries-were-found");

		String orderByCol = ParamUtil.getString(
			_httpServletRequest, "orderByCol", "title");

		entriesSearchContainer.setOrderByCol(orderByCol);

		String orderByType = ParamUtil.getString(
			_httpServletRequest, "orderByType", "asc");

		entriesSearchContainer.setOrderByType(orderByType);

		/*
		entriesSearchContainer.setOrderByComparator(
			BlogsUtil.getOrderByComparator(
				entriesSearchContainer.getOrderByCol(),
				entriesSearchContainer.getOrderByType()));
*/
		entriesSearchContainer.setRowChecker(
			new EmptyOnClickRowChecker(_liferayPortletResponse));

		_populateResults(entriesSearchContainer);

		return entriesSearchContainer;
	}

	private static OrderByComparator<SearchConfiguration> getOrderByComparator(
		String orderByCol, String orderByType) {

		boolean orderByAsc = true;

		if (orderByType.equals("desc")) {
			orderByAsc = false;
		}

		OrderByComparator<SearchConfiguration> orderByComparator = null;
		/*

					if (orderByCol.equals("display-date")) {
						orderByComparator = new EntryDisplayDateComparator(orderByAsc);
					}
					else {
						orderByComparator = new EntryTitleComparator(orderByAsc);
					}
		*/

		return orderByComparator;
	}

	private void _populateResults(SearchContainer searchContainer)
		throws PortalException {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)_httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		List entriesResults = null;

		String keywords = ParamUtil.getString(_httpServletRequest, "keywords");
		/*

				if ((assetCategoryId != 0) || Validator.isNotNull(assetTagName)) {
					SearchContainerResults<AssetEntry> searchContainerResults =
						BlogsUtil.getSearchContainerResults(searchContainer);

					searchContainer.setTotal(searchContainerResults.getTotal());
					List<AssetEntry> assetEntries = searchContainerResults.getResults();

					entriesResults = new ArrayList<>(assetEntries.size());

					for (AssetEntry assetEntry : assetEntries) {
						entriesResults.add(
								SearchConfigurationServiceUtil.getEntry(
								assetEntry.getClassPK()));
					}
				}
				*/
		if (Validator.isNull(keywords)) {
			searchContainer.setTotal(
				SearchConfigurationServiceUtil.getGroupConfigurationsCount(
					themeDisplay.getScopeGroupId(),
					WorkflowConstants.STATUS_ANY, _searchConfigurationType));

			entriesResults =
				SearchConfigurationServiceUtil.getGroupConfigurations(
					themeDisplay.getScopeGroupId(),
					WorkflowConstants.STATUS_ANY, _searchConfigurationType,
					searchContainer.getStart(), searchContainer.getEnd(),
					searchContainer.getOrderByComparator());
		}
		else {
			Indexer indexer = IndexerRegistryUtil.getIndexer(
				SearchConfiguration.class);

			SearchContext searchContext = SearchContextFactory.getInstance(
				_httpServletRequest);

			searchContext.setAttribute(
				Field.STATUS, WorkflowConstants.STATUS_ANY);
			searchContext.setEnd(searchContainer.getEnd());
			searchContext.setIncludeDiscussions(true);
			searchContext.setKeywords(keywords);
			searchContext.setStart(searchContainer.getStart());

			String orderByCol = ParamUtil.getString(
				_httpServletRequest, "orderByCol", "title");
			String orderByType = ParamUtil.getString(
				_httpServletRequest, "orderByType", "asc");

			Sort sort = null;

			boolean orderByAsc = false;

			if (Objects.equals(orderByType, "asc")) {
				orderByAsc = true;
			}

			if (Objects.equals(orderByCol, "modified-date")) {
				sort = new Sort(
					Field.DISPLAY_DATE, Sort.LONG_TYPE, !orderByAsc);
			}
			else {
				sort = new Sort(orderByCol, !orderByAsc);
			}

			searchContext.setSorts(sort);

			Hits hits = indexer.search(searchContext);

			searchContainer.setTotal(hits.getLength());

			List<SearchResult> searchResults =
				SearchResultUtil.getSearchResults(
					hits, LocaleUtil.getDefault());

			Stream<SearchResult> stream = searchResults.stream();

			entriesResults = stream.map(
				this::_toBlogsEntryOptional
			).filter(
				Optional::isPresent
			).map(
				Optional::get
			).collect(
				Collectors.toList()
			);
		}

		searchContainer.setResults(entriesResults);
	}

	private Optional<SearchConfiguration> _toBlogsEntryOptional(
		SearchResult searchResult) {

		try {
			return Optional.of(
				SearchConfigurationServiceUtil.getConfiguration(
					searchResult.getClassPK()));
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Blogs search index is stale and contains entry " +
						searchResult.getClassPK());
			}

			return Optional.empty();
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SearchConfigurationsDisplayContext.class);

	private final HttpServletRequest _httpServletRequest;
	private final LiferayPortletRequest _liferayPortletRequest;
	private final LiferayPortletResponse _liferayPortletResponse;
	private final PortalPreferences _portalPreferences;
	private final int _searchConfigurationType;

}