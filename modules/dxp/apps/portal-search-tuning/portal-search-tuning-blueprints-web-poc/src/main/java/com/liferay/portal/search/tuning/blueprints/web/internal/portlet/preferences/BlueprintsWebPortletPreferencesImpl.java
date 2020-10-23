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

package com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences;

import com.liferay.portal.search.tuning.blueprints.web.internal.constants.BlueprintsWebPortletPreferenceKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.util.PortletPreferencesHelper;

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
	public long getSearchBlueprintId() {
		return getSearchBlueprintIdOptional().orElse(0L);
	}

	@Override
	public Optional<Long> getSearchBlueprintIdOptional() {
		return _portletPreferencesHelper.getLongOptional(
			BlueprintsWebPortletPreferenceKeys.SEARCH_BLUEPRINT_ID);
	}

	@Override
	public long getTypeaheadBlueprintId() {
		return getTypeaheadBlueprintIdOptional().orElse(0L);
	}

	@Override
	public Optional<Long> getTypeaheadBlueprintIdOptional() {
		return _portletPreferencesHelper.getLongOptional(
			BlueprintsWebPortletPreferenceKeys.TYPEAHEAD_BLUEPRINT_ID);
	}

	private final PortletPreferencesHelper _portletPreferencesHelper;

}