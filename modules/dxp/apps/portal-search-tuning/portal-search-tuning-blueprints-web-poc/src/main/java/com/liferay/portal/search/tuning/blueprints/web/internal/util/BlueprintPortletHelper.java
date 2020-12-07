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

package com.liferay.portal.search.tuning.blueprints.web.internal.util;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferences;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferencesImpl;

import java.util.Optional;

import javax.portlet.PortletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintPortletHelper.class)
public class BlueprintPortletHelper {

	public Optional<Blueprint> getBlueprint(long blueprintId) {
		try {
			return Optional.of(_blueprintService.getBlueprint(blueprintId));
		} catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);
		}
		
		return Optional.empty();
	}
	
	public Optional<Blueprint> getSearchBlueprint(PortletRequest portletRequest) {
		
		long blueprintId = getSearchBlueprintId(portletRequest);
		
		if (blueprintId == 0) {
			return Optional.empty();
		}
		
		return getBlueprint(blueprintId);
	}

	public long getSearchBlueprintId(PortletRequest portletRequest) {
		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences =
			new BlueprintsWebPortletPreferencesImpl(
					portletRequest.getPreferences());

		return blueprintsWebPortletPreferences.getSearchBlueprintId();
	}

	private static final Log _log = LogFactoryUtil.getLog(
			BlueprintPortletHelper.class);

	@Reference
	private BlueprintService _blueprintService;
}
