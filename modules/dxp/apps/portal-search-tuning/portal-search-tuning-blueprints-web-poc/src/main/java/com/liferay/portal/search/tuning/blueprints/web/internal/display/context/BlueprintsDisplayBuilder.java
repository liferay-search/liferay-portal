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

package com.liferay.portal.search.tuning.blueprints.web.internal.display.context;

import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.ResourceRequestKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferences;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferencesImpl;

import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Kevin Tan
 */
public class BlueprintsDisplayBuilder {

	public BlueprintsDisplayBuilder(
		HttpServletRequest httpServletRequest, RenderRequest renderRequest,
		RenderResponse renderResponse) {

		_httpServletRequest = httpServletRequest;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;

		_themeDisplay = (ThemeDisplay)_httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public BlueprintsDisplayContext build() {
		return new BlueprintsDisplayContext(
			_getData(), _isConfigured(_renderRequest));
	}

	private Map<String, Object> _getContext() {
		return HashMapBuilder.<String, Object>put(
			"namespace", _renderResponse.getNamespace()
		).put(
			"spritemap",
			_themeDisplay.getPathThemeImages() + "/lexicon/icons.svg"
		).build();
	}

	private Map<String, Object> _getData() {
		return HashMapBuilder.<String, Object>put(
			"context", _getContext()
		).put(
			"props", _getProps()
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

		resourceURL.setResourceID(ResourceRequestKeys.GET_TYPEAHEAD);

		return resourceURL.toString();
	}

	private String _getSuggestMode() {
		PortletPreferences preferences = _renderRequest.getPreferences();

		return preferences.getValue("suggestMode", "contents");
	}

	private boolean _isConfigured(RenderRequest renderRequest) {
		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences =
			new BlueprintsWebPortletPreferencesImpl(
				renderRequest.getPreferences());

		long blueprintId =
			blueprintsWebPortletPreferences.getSearchBlueprintId();

		if (blueprintId != 0) {
			return true;
		}

		return false;
	}

	private final HttpServletRequest _httpServletRequest;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final ThemeDisplay _themeDisplay;

}