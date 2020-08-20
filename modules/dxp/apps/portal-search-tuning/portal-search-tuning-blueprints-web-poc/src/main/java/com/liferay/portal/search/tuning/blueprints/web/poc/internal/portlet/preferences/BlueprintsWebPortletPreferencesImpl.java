/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.search.tuning.blueprints.web.poc.internal.portlet.preferences;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.web.poc.internal.constants.BlueprintsWebPortletPreferenceKeys;
import com.liferay.portal.search.tuning.blueprints.web.poc.internal.util.PortletPreferencesHelper;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.portlet.PortletPreferences;

/**
 * @author Petteri Karttunen
 */
public class BlueprintsWebPortletPreferencesImpl implements BlueprintsWebPortletPreferences {

	public BlueprintsWebPortletPreferencesImpl(
	
			PortletPreferences portletPreferences) {

		_portletPreferencesHelper = new PortletPreferencesHelper(
			portletPreferences);
	}
	
	public  Optional<String> getDatePickerDateFormatOptional() {
		return _portletPreferencesHelper.getString(
				BlueprintsWebPortletPreferenceKeys.DATE_PICKER_DATE_FORMAT);
	}

	public  String getDatePickerDateFormatString() {
		return getDatePickerDateFormatOptional().orElse(StringPool.BLANK);
	}

	@Override
	public String[] getFieldsToDisplay() {
		
		String[] defaultValue = new String[] {
			"assetTags",
			"assetCategories",
			"userName",
			"userImage"
		};
		
		return _portletPreferencesHelper.getStringArray(
				BlueprintsWebPortletPreferenceKeys.FIELDS_TO_DISPLAY,
				defaultValue);
	}
	
	@Override
	public Optional<String> getGoogleMapsAPIKeyOptional() {
		return _portletPreferencesHelper.getString(
				BlueprintsWebPortletPreferenceKeys.GOOGLE_MAPS_API_KEY);
	}

	@Override
	public String getGoogleMapsAPIKeyString() {
		return getGoogleMapsAPIKeyOptional().orElse(StringPool.BLANK);
	}

	@Override
	public Optional<String> getHelpTextArticleOptional() {
		return _portletPreferencesHelper.getString(
				BlueprintsWebPortletPreferenceKeys.HELP_TEXT_ARTICLE);
	}

	@Override
	public String getHelpTextArticleString() {
		return getHelpTextArticleOptional().orElse(StringPool.BLANK);
	}

	@Override
	public String getKeywordPlaceholder(Locale locale) {

		Map<Locale, String> keywordPlaceholderMap = getKeywordPlaceholderMap();

		String s = keywordPlaceholderMap.get(locale);
		
		if (Validator.isBlank(s)) {
			StringBundler sb = new StringBundler(2);
			sb.append(LanguageUtil.get(locale, "search"));
			sb.append("...");
			return sb.toString();
		}
		
		return s;
	}

	@Override
	public Map<Locale, String> getKeywordPlaceholderMap() {
		
		return _portletPreferencesHelper.getLocalizationMap(
				BlueprintsWebPortletPreferenceKeys.KEYWORD_PLACEHOLDER_MAP);
		
	}

	@Override
	public int getKeywordSuggesterRequestDelay() {
		return _portletPreferencesHelper.getInteger(
				BlueprintsWebPortletPreferenceKeys.KEYWORD_SUGGESTER_REQUEST_DELAY,
					150);
	}
	
	@Override
	public int getQueryMinLength() {
		return _portletPreferencesHelper.getInteger(
				BlueprintsWebPortletPreferenceKeys.QUERY_MIN_LENGTH,
					3);
	}

	@Override
	public int getRequestTimeout() {
		return _portletPreferencesHelper.getInteger(
				BlueprintsWebPortletPreferenceKeys.REQUEST_TIMEOUT,
					10000);
	}

	@Override
	public String[] getResultLayouts() {
		
		String[] defaultValue = new String[] {
				"list",
				"card",
				"thumbnails",
				"maps",
				"raw",
				"explain"
			};
			
			return _portletPreferencesHelper.getStringArray(
					BlueprintsWebPortletPreferenceKeys.RESULT_LAYOUTS,
					defaultValue);
	}

	@Override
	public Optional<Long> getBlueprintIdOptional() {
		return _portletPreferencesHelper.getLong(
				BlueprintsWebPortletPreferenceKeys.BLUEPRINT_ID);
	}

	@Override
	public long getblueprintId() {
		return getBlueprintIdOptional().orElse(0L);
	}

	@Override
	public boolean isAppendRedirectToURL() {
		return _portletPreferencesHelper.getBoolean(
				BlueprintsWebPortletPreferenceKeys.APPEND_REDIRECT_TO_URL,
				true);
	}

	@Override
	public boolean isConversionTrackingEnabled() {
		return _portletPreferencesHelper.getBoolean(
				BlueprintsWebPortletPreferenceKeys.CONVERSION_TRACKING_ENABLED,
				true);
	}

	@Override
	public boolean isKeywordSuggesterEnabled() {
		return _portletPreferencesHelper.getBoolean(
				BlueprintsWebPortletPreferenceKeys.KEYWORD_SUGGESTER_ENABLED,
				true);
	}

	@Override
	public boolean isScopeFilterVisible() {
		return _portletPreferencesHelper.getBoolean(
				BlueprintsWebPortletPreferenceKeys.SCOPE_FILTER_ENABLED,
				true);
	}

	@Override
	public boolean isTimeFilterVisible() {
		return _portletPreferencesHelper.getBoolean(
				BlueprintsWebPortletPreferenceKeys.TIME_FILTER_ENABLED,
				true);
	}
	
	@Override
	public boolean isViewInContext() {
		return _portletPreferencesHelper.getBoolean(
			BlueprintsWebPortletPreferenceKeys.VIEW_IN_CONTEXT,
			true);
	}

	private final PortletPreferencesHelper _portletPreferencesHelper;
}
