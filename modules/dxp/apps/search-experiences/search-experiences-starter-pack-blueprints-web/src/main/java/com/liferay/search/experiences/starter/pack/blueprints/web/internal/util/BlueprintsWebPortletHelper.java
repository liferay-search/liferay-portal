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

package com.liferay.search.experiences.starter.pack.blueprints.web.internal.util;

import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.BlueprintLookup;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributesBuilder;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributesBuilderFactory;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferences;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferencesImpl;

import java.util.Optional;
import java.util.TimeZone;

import javax.portlet.PortletRequest;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintsWebPortletHelper.class)
public class BlueprintsWebPortletHelper {

	public Optional<Blueprint> getBlueprint(PortletRequest portletRequest) {
		long blueprintId = getBlueprintId(portletRequest);

		if (blueprintId == 0) {
			return Optional.empty();
		}

		return _blueprintLookup.getBlueprintOptional(blueprintId);
	}

	public long getBlueprintId(PortletRequest portletRequest) {
		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences =
			new BlueprintsWebPortletPreferencesImpl(
				portletRequest.getPreferences());

		return blueprintsWebPortletPreferences.getBlueprintId();
	}

	public SuggestionAttributesBuilder getSuggestionAttributesBuilder(
		PortletRequest portletRequest, String[] dataProviders, String keywords,
		int size) {

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		return _suggestionAttributesBuilderFactory.builder(
		).companyId(
			themeDisplay.getCompanyId()
		).groupId(
			themeDisplay.getScopeGroupId()
		).ipAddress(
			_getIPAddress(portletRequest)
		).includedDataProviders(
			dataProviders
		).keywords(
			keywords
		).locale(
			themeDisplay.getLocale()
		).plid(
			themeDisplay.getPlid()
		).size(
			size
		).timezoneId(
			_getTimezoneId(themeDisplay)
		).userId(
			themeDisplay.getUserId()
		);
	}

	private String _getIPAddress(PortletRequest portletRequest) {
		HttpServletRequest httpServletRequest = _portal.getHttpServletRequest(
			portletRequest);

		return httpServletRequest.getRemoteAddr();
	}

	private String _getTimezoneId(ThemeDisplay themeDisplay) {
		TimeZone timeZone = themeDisplay.getTimeZone();

		return timeZone.getID();
	}

	@Reference
	private BlueprintLookup _blueprintLookup;

	@Reference
	private Portal _portal;

	@Reference
	private SuggestionAttributesBuilderFactory
		_suggestionAttributesBuilderFactory;

}