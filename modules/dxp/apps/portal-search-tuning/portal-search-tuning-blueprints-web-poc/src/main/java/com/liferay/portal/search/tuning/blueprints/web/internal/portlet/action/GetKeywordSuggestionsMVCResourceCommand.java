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

package com.liferay.portal.search.tuning.blueprints.web.internal.portlet.action;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.suggester.KeywordSuggester;
import com.liferay.portal.search.tuning.blueprints.engine.util.SearchClientHelper;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.BlueprintsWebPortletKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.ResourceRequestKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferences;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferencesImpl;
import com.liferay.portal.search.tuning.blueprints.web.internal.util.BlueprintsLocalizationHelper;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, 
	property = {
		"javax.portlet.name=" + BlueprintsWebPortletKeys.BLUEPRINTS_WEB,
		"mvc.command.name=" + ResourceRequestKeys.GET_SUGGESTIONS
	}, 
	service = MVCResourceCommand.class
)
public class GetKeywordSuggestionsMVCResourceCommand extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
		ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		JSONObject responseJsonObject= JSONFactoryUtil.createJSONObject();

		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences =
				new BlueprintsWebPortletPreferencesImpl(resourceRequest.getPreferences());
		
		long blueprintId = blueprintsWebPortletPreferences.getblueprintId();

		try {
			HttpServletRequest httpServletRequest =
				_portal.getHttpServletRequest(resourceRequest);

			SearchRequestContext searchRequestContext = 
					_searchClientHelper.getSearchRequestContext(httpServletRequest, blueprintId);
			
			JSONArray suggestionsJsonArray = 
					_keywordSuggester.getSuggestions(searchRequestContext);
			
			responseJsonObject.put(JSONResponseKeys.KEYWORD_SUGGESTIONS, suggestionsJsonArray);
						
		} catch (PortalException portalException) {
			
			_log.error(portalException.getMessage(), portalException);

			responseJsonObject.put(JSONResponseKeys.ERROR, portalException.getMessage());

		}

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse, responseJsonObject);
	}

	private static final Log _log =
		LogFactoryUtil.getLog(GetKeywordSuggestionsMVCResourceCommand.class);

	@Reference
	private KeywordSuggester _keywordSuggester;

	@Reference
	private BlueprintsLocalizationHelper _blueprintsLocalizationHelper;
	
	@Reference
	private Portal _portal;

	@Reference 
	private SearchClientHelper _searchClientHelper;
	
	@Reference
	private BlueprintService _blueprintService;
}
