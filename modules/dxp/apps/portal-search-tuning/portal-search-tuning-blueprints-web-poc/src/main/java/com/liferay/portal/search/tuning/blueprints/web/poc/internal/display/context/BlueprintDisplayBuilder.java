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

import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.web.poc.internal.constants.ResourceRequestKeys;

import java.util.Map;

import javax.portlet.PortletPreferences;
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
		).put(
			"suggestMode", _getSuggestMode()
		).build();
	}

	private String _getSuggestionsURL() {
		ResourceURL resourceURL = _renderResponse.createResourceURL();

		resourceURL.setResourceID(ResourceRequestKeys.GET_SUGGESTIONS);

		return resourceURL.toString();
	}

	private String _getSuggestMode() {
		PortletPreferences preferences = _renderRequest.getPreferences();

		return preferences.getValue("suggestMode", "contents");
	}

	private void _setData(BlueprintDisplayContext blueprintDisplayContext) {
		blueprintDisplayContext.setData(
			HashMapBuilder.<String, Object>put(
				"context", _getContext()
			).put(
				"props", _getProps()
			).build());
	}

	private final HttpServletRequest _httpServletRequest;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final ThemeDisplay _themeDisplay;

}