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

package com.liferay.portal.search.tuning.blueprints.web.poc.internal.display.context;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.web.poc.internal.constants.ResourceRequestKeys;
import com.liferay.portal.search.tuning.blueprints.web.poc.internal.display.context.BlueprintDisplayContext;

import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Kevin Tan
 */
public class BlueprintDisplayBuilder {

	public BlueprintDisplayBuilder(
		HttpServletRequest httpServletRequest, RenderRequest renderRequest,
		RenderResponse renderResponse) {

		_httpServletRequest = httpServletRequest;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;

		_themeDisplay = (ThemeDisplay)_httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public BlueprintDisplayContext build() {
		BlueprintDisplayContext blueprintDisplayContext =
			new BlueprintDisplayContext();

		_setData(blueprintDisplayContext);

		return blueprintDisplayContext;
	}


	private Map<String, Object> _getContext() {
		return HashMapBuilder.<String, Object>put(
			"namespace", _renderResponse.getNamespace()
		).put(
			"spritemap",
			_themeDisplay.getPathThemeImages() + "/lexicon/icons.svg"
		).build();
	}

	private String _getFetchResultsURL() {
		ResourceURL resourceURL = _renderResponse.createResourceURL();

		resourceURL.setResourceID(ResourceRequestKeys.GET_SEARCH_RESULTS);

		return resourceURL.toString();
	}

	private Map<String, Object> _getProps() {
		return HashMapBuilder.<String, Object>put(
			"fetchResultsURL", _getFetchResultsURL()
		).put(
			"suggestionsURL", _getSuggestionsURL()
		).build();
	}

	private void _setData(BlueprintDisplayContext blueprintDisplayContext) {
		blueprintDisplayContext.setData(
			HashMapBuilder.<String, Object>put(
				"context", _getContext()
			).put(
				"props", _getProps()
			).build());
	}

	private String _getSuggestionsURL(){
		PortletPreferences preferences = _renderRequest.getPreferences();

		String suggestMode = preferences.getValue("suggestMode", "contents");

		String suggestionsURL = null;

		JSONObject uiConfiguration = (JSONObject)_renderRequest.getAttribute("configuration");

		if (uiConfiguration != null && uiConfiguration.has("urlConfiguration")) {
			JSONObject urlConfig = uiConfiguration.getJSONObject("urlConfiguration");

			if (suggestMode.equals("contents")) {
				suggestionsURL = urlConfig.getString("searchResultsURL");
			} else {
				suggestionsURL = urlConfig.getString("suggestionsURL");
			}
		}

		return suggestionsURL;
	}

	private final HttpServletRequest _httpServletRequest;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final ThemeDisplay _themeDisplay;

}