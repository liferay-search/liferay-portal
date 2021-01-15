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

package com.liferay.portal.search.tuning.blueprints.query.index.web.internal.portlet.action;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.engine.SearchEngineInformation;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryIndexMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryIndexPortletKeys;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryIndexWebKeys;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryStringStatus;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.display.context.ViewActiveEntriesManagementToolbarDisplayContext;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.display.context.ViewBlacklistedEntriesManagementToolbarDisplayContext;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.display.context.ViewQueryStringsDisplayContext;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.display.context.ViewReportedEntriesManagementToolbarDisplayContext;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.DocumentToQueryStringTranslator;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + QueryIndexPortletKeys.QUERY_INDEX_ADMIN,
		"mvc.command.name=" + QueryIndexMVCCommandNames.VIEW_QUERY_STRINGS,
		"mvc.command.name=/"
	},
	service = MVCRenderCommand.class
)
public class ViewQueryStringsMVCRenderCommand implements MVCRenderCommand {

	@Override
	public String render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		QueryStringStatus status = _getStatusFromTab(renderRequest);

		ViewQueryStringsDisplayContext viewQueryStringsDisplayContext =
			_getViewQueryStringsDisplayContext(
				renderRequest, renderResponse, status);

		renderRequest.setAttribute(
			QueryIndexWebKeys.VIEW_QUERY_STRINGS_DISPLAY_CONTEXT,
			viewQueryStringsDisplayContext);

		if (status.equals(QueryStringStatus.ACTIVE)) {
			_setActiveEntriesContext(
				renderRequest, renderResponse, viewQueryStringsDisplayContext);
		}
		else if (status.equals(QueryStringStatus.BLACKLISTED)) {
			_setBlacklistedEntriesContext(
				renderRequest, renderResponse, viewQueryStringsDisplayContext);
		}
		else if (status.equals(QueryStringStatus.REPORTED)) {
			_setReportedEntriesContext(
				renderRequest, renderResponse, viewQueryStringsDisplayContext);
		}

		return "/view.jsp";
	}

	private QueryStringStatus _getStatusFromTab(RenderRequest renderRequest) {
		String status = ParamUtil.getString(
			renderRequest, "tabs", QueryStringStatus.REPORTED.name());

		try {
			return QueryStringStatus.valueOf(status);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			_log.error(
				illegalArgumentException.getMessage(),
				illegalArgumentException);
		}

		return QueryStringStatus.REPORTED;
	}

	private ViewQueryStringsDisplayContext _getViewQueryStringsDisplayContext(
		RenderRequest renderRequest, RenderResponse renderResponse,
		QueryStringStatus status) {

		return new ViewQueryStringsDisplayContext(
			_portal.getLiferayPortletRequest(renderRequest),
			_portal.getLiferayPortletResponse(renderResponse),
			_documentToQueryStringTranslator, _portal, _queries,
			_queryStringIndexNameBuilder, _searchEngineAdapter, _sorts, status);
	}

	private void _setActiveEntriesContext(
		RenderRequest renderRequest, RenderResponse renderResponse,
		ViewQueryStringsDisplayContext viewQueryStringsDisplayContext) {

		try {
			ViewActiveEntriesManagementToolbarDisplayContext
				viewActiveEntriesManagementToolbarDisplayContext =
					new ViewActiveEntriesManagementToolbarDisplayContext(
						_portal.getHttpServletRequest(renderRequest),
						_portal.getLiferayPortletRequest(renderRequest),
						_portal.getLiferayPortletResponse(renderResponse),
						viewQueryStringsDisplayContext.getSearchContainer(),
						viewQueryStringsDisplayContext.getDisplayStyle());

			renderRequest.setAttribute(
				QueryIndexWebKeys.
					ACTIVE_ENTRIES_MANAGEMENT_TOOLBAR_DISPLAY_CONTEXT,
				viewActiveEntriesManagementToolbarDisplayContext);
		}
		catch (PortalException | PortletException exception) {
			_log.error(exception.getMessage(), exception);

			SessionErrors.add(
				renderRequest, QueryIndexWebKeys.ERROR, exception.getMessage());
		}
	}

	private void _setBlacklistedEntriesContext(
		RenderRequest renderRequest, RenderResponse renderResponse,
		ViewQueryStringsDisplayContext viewQueryStringsDisplayContext) {

		try {
			ViewBlacklistedEntriesManagementToolbarDisplayContext
				viewBlacklistedEntriesManagementToolbarDisplayContext =
					new ViewBlacklistedEntriesManagementToolbarDisplayContext(
						_portal.getHttpServletRequest(renderRequest),
						_portal.getLiferayPortletRequest(renderRequest),
						_portal.getLiferayPortletResponse(renderResponse),
						viewQueryStringsDisplayContext.getSearchContainer(),
						viewQueryStringsDisplayContext.getDisplayStyle());

			renderRequest.setAttribute(
				QueryIndexWebKeys.
					BLACKLISTED_ENTRIES_MANAGEMENT_TOOLBAR_DISPLAY_CONTEXT,
				viewBlacklistedEntriesManagementToolbarDisplayContext);
		}
		catch (PortalException | PortletException exception) {
			_log.error(exception.getMessage(), exception);

			SessionErrors.add(
				renderRequest, QueryIndexWebKeys.ERROR, exception.getMessage());
		}
	}

	private void _setReportedEntriesContext(
		RenderRequest renderRequest, RenderResponse renderResponse,
		ViewQueryStringsDisplayContext viewQueryStringsDisplayContext) {

		try {
			ViewReportedEntriesManagementToolbarDisplayContext
				viewReportedEntriesManagementToolbarDisplayContext =
					new ViewReportedEntriesManagementToolbarDisplayContext(
						_portal.getHttpServletRequest(renderRequest),
						_portal.getLiferayPortletRequest(renderRequest),
						_portal.getLiferayPortletResponse(renderResponse),
						viewQueryStringsDisplayContext.getSearchContainer(),
						viewQueryStringsDisplayContext.getDisplayStyle());

			renderRequest.setAttribute(
				QueryIndexWebKeys.
					REPORTED_ENTRIES_MANAGEMENT_TOOLBAR_DISPLAY_CONTEXT,
				viewReportedEntriesManagementToolbarDisplayContext);
		}
		catch (PortalException | PortletException exception) {
			_log.error(exception.getMessage(), exception);

			SessionErrors.add(
				renderRequest, QueryIndexWebKeys.ERROR, exception.getMessage());
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ViewQueryStringsMVCRenderCommand.class);

	@Reference
	private DocumentToQueryStringTranslator _documentToQueryStringTranslator;

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private Language _language;

	@Reference
	private Portal _portal;

	@Reference
	private Queries _queries;

	@Reference
	private QueryStringIndexNameBuilder _queryStringIndexNameBuilder;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

	@Reference
	private SearchEngineInformation _searchEngineInformation;

	@Reference
	private Sorts _sorts;

}