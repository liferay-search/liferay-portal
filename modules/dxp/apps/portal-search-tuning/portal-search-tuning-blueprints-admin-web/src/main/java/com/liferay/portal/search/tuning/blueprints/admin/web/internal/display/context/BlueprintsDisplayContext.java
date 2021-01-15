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

package com.liferay.portal.search.tuning.blueprints.admin.web.internal.display.context;

import com.liferay.portal.kernel.dao.search.EmptyOnClickRowChecker;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminTabNames;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.util.BlueprintsAdminSearchUtil;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.portlet.PortletException;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public abstract class BlueprintsDisplayContext {

	public BlueprintsDisplayContext(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse, String tab) {

		this.liferayPortletRequest = liferayPortletRequest;
		this.liferayPortletResponse = liferayPortletResponse;
		this.tab = tab;

		httpServletRequest = liferayPortletRequest.getHttpServletRequest();
		portalPreferences = PortletPreferencesFactoryUtil.getPortalPreferences(
			liferayPortletRequest);
		themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public SearchContainer<Blueprint> getSearchContainer()
		throws PortalException, PortletException {

		SearchContainer<Blueprint> searchContainer = new SearchContainer<>(
			liferayPortletRequest, getIteratorURL(), null,
			"no-entries-were-found");

		searchContainer.setOrderByCol(getOrderByCol());

		searchContainer.setOrderByType(getOrderByType());

		searchContainer.setRowChecker(
			new EmptyOnClickRowChecker(liferayPortletResponse));

		populateResults(searchContainer);

		return searchContainer;
	}

	protected PortletURL getIteratorURL() {
		PortletURL portletURL = liferayPortletResponse.createRenderURL();

		portletURL.setProperty(
			"mvcRenderCommandName", BlueprintsAdminMVCCommandNames.VIEW);

		if (!Validator.isBlank(tab)) {
			portletURL.setParameter("tabs", tab);
		}

		return portletURL;
	}

	protected String getOrderByCol() {
		return ParamUtil.getString(
			httpServletRequest, "orderByCol", Field.TITLE);
	}

	protected String getOrderByType() {
		return ParamUtil.getString(httpServletRequest, "orderByType", "asc");
	}

	protected void populateResults(SearchContainer<Blueprint> searchContainer)
		throws PortalException {

		SearchHits searchHits = BlueprintsAdminSearchUtil.searchBlueprints(
			httpServletRequest, themeDisplay.getCompanyGroupId(),
			WorkflowConstants.STATUS_APPROVED, _getSearchType(),
			searchContainer.getDelta(), searchContainer.getStart(),
			getOrderByCol(), getOrderByType());

		searchContainer.setTotal(
			GetterUtil.getInteger(searchHits.getTotalHits()));

		List<SearchHit> hits = searchHits.getSearchHits();

		Stream<SearchHit> stream = hits.stream();

		searchContainer.setResults(
			stream.map(
				searchHit -> BlueprintsAdminSearchUtil.toBlueprintOptional(
					searchHit)
			).filter(
				Optional::isPresent
			).map(
				Optional::get
			).collect(
				Collectors.toList()
			));
	}

	protected final HttpServletRequest httpServletRequest;
	protected final LiferayPortletRequest liferayPortletRequest;
	protected final LiferayPortletResponse liferayPortletResponse;
	protected final PortalPreferences portalPreferences;
	protected final String tab;
	protected final ThemeDisplay themeDisplay;

	private String _getSearchType() {
		if (BlueprintsAdminTabNames.FRAGMENTS.equals(tab)) {
			return "fragments";
		}

		return "blueprints";
	}

}