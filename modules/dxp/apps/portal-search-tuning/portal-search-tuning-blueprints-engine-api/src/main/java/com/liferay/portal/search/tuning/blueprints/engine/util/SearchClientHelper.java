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

package com.liferay.portal.search.tuning.blueprints.engine.util;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.exception.SearchRequestDataException;
import com.liferay.portal.search.tuning.blueprints.engine.searchrequest.SearchRequestData;

import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public interface SearchClientHelper {

	public SearchRequestContext getSearchRequestContext(
			HttpServletRequest httpServletRequest, long blueprintId)
		throws JSONException, PortalException;

	public SearchRequestContext getSearchRequestContext(
			SearchContext searchContext, long blueprintId)
		throws JSONException, PortalException;

	public SearchRequestData getSearchRequestData(
			SearchRequestContext searchRequestContext)
		throws SearchRequestDataException;

	public SearchSearchResponse getSearchResponse(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData);

	public JSONObject getSearchResults(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse, Map<String, Object> responseAttributes);

	public JSONObject search(
			HttpServletRequest httpServletRequest,
			Map<String, Object> responseAttributes, long blueprintId)
		throws JSONException, PortalException, SearchRequestDataException;

	public JSONObject search(
			PortletRequest portletRequest, PortletResponse portletResponse,
			Map<String, Object> responseAttributes, long blueprintId)
		throws JSONException, PortalException, SearchRequestDataException;
}