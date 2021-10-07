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

package com.liferay.search.experiences.starter.pack.blueprints.web.internal.portlet.preferences;

import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.constants.BlueprintsWebPortletPreferenceKeys;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.util.PortletPreferencesHelper;

import java.util.Optional;

import javax.portlet.PortletPreferences;

/**
 * @author Petteri Karttunen
 */
public class BlueprintsWebPortletPreferencesImpl
	implements BlueprintsWebPortletPreferences {

	public BlueprintsWebPortletPreferencesImpl(
		PortletPreferences portletPreferences) {

		_portletPreferencesHelper = new PortletPreferencesHelper(
			portletPreferences);
	}

	@Override
	public long getBlueprintId() {
		return getBlueprintIdOptional().orElse(0L);
	}

	@Override
	public Optional<Long> getBlueprintIdOptional() {
		return _portletPreferencesHelper.getLongOptional(
			BlueprintsWebPortletPreferenceKeys.SEARCH_BLUEPRINT_ID);
	}

	@Override
	public int getKeywordIndexingHitsThreshold() {
		return _portletPreferencesHelper.getInteger(
			BlueprintsWebPortletPreferenceKeys.KEYWORD_INDEXING_HITS_THRESHOLD,
			3);
	}

	@Override
	public int getMaxSpellCheckSuggestions() {
		return _portletPreferencesHelper.getInteger(
			BlueprintsWebPortletPreferenceKeys.MAX_SPELL_CHECK_SUGGESTIONS, 10);
	}

	@Override
	public int getSpellCheckHitsThreshold() {
		return _portletPreferencesHelper.getInteger(
			BlueprintsWebPortletPreferenceKeys.SPELL_CHECK_HITS_THRESHOLD, 5);
	}

	@Override
	public String getTypeaheadConfiguration() {
		String defaultConfiguration = StringBundler.concat(
			"{\"size\": 10,\"data_provider_configuration\":{\"field\":",
			"{\"entry_class_names\": [\"com.liferay.journal.model.",
			"JournalArticle\",\"com.liferay.document.library.kernel.model.",
			"DLFileEntry\"],\"fuzziness\": \"1\",\"offset\": 1,",
			"\"operator\": \"and\",\"source_definitions\":",
			"[{\"fields_boosts\": {\"title_en_US\": 1,\"content_en_US\":",
			"1},\"term_filters\": {}},{\"nested_must_terms\":",
			"{\"ddmFieldArray.ddmFieldName\": \"ddmFieldArray.",
			"ddM_FOO_JUSTA_SAMPLE\"},\"path\": \"ddmFieldArray\",",
			"\"term_filters\": {},\"value_field\": \"ddmFieldArray.",
			"ddmFieldValueText_en_US\"}],\"type\": \"highlighter\",",
			"\"weight\": 1},\"keyword_index\": {\"weight\": 1},",
			"\"synonyms\": {\"weight\": 1},\"misspellings\":",
			"{\"weight\": 1}}}");

		return _portletPreferencesHelper.getString(
			BlueprintsWebPortletPreferenceKeys.TYPEAHEAD_CONFIGURATION,
			defaultConfiguration);
	}

	@Override
	public boolean isKeywordIndexingEnabled() {
		return _portletPreferencesHelper.getBoolean(
			BlueprintsWebPortletPreferenceKeys.KEYWORD_INDEXING_ENABLED, true);
	}

	@Override
	public boolean isMisspellingsEnabled() {
		return _portletPreferencesHelper.getBoolean(
			BlueprintsWebPortletPreferenceKeys.MISSPELLINGS_ENABLED, true);
	}

	@Override
	public boolean isSpellCheckEnabled() {
		return _portletPreferencesHelper.getBoolean(
			BlueprintsWebPortletPreferenceKeys.SPELL_CHECK_ENABLED, true);
	}

	@Override
	public boolean isTypeaheadEnabled() {
		return _portletPreferencesHelper.getBoolean(
			BlueprintsWebPortletPreferenceKeys.TYPEAHEAD_ENABLED, true);
	}

	private final PortletPreferencesHelper _portletPreferencesHelper;

}