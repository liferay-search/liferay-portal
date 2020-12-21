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

package com.liferay.portal.search.tuning.blueprints.util.internal.attributes;

import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.suggestions.attributes.SuggestionsAttributesBuilder;
import com.liferay.portal.search.tuning.blueprints.suggestions.attributes.SuggestionsAttributesBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.util.attributes.SuggestionsAttributesHelper;

import java.util.TimeZone;

import javax.portlet.PortletRequest;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SuggestionsAttributesHelper.class)
public class SuggestionsAttributesHelperImpl
	implements SuggestionsAttributesHelper {

	@Override
	public SuggestionsAttributesBuilder getSuggestionsAttributesBuilder(
		PortletRequest portletRequest) {

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		HttpServletRequest httpServletRequest = _portal.getHttpServletRequest(
			portletRequest);

		TimeZone timeZone = themeDisplay.getTimeZone();

		return _suggestionsAttributesBuilderFactory.builder(
		).companyId(
			themeDisplay.getCompanyId()
		).groupIds(
			new Long[] {themeDisplay.getScopeGroupId()}
		).languageIds(
			new String[] {themeDisplay.getLanguageId()}
		).userId(
			themeDisplay.getUserId()
		).addAttribute(
			"ipAddress", httpServletRequest.getRemoteAddr()
		).addAttribute(
			"plid", themeDisplay.getPlid()
		).addAttribute(
			"timeZoneId", timeZone.getID()
		);
	}

	@Reference
	private Portal _portal;

	@Reference
	private SuggestionsAttributesBuilderFactory
		_suggestionsAttributesBuilderFactory;

}