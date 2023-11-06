/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.suggestions.spi.asah.individuals;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.search.rest.dto.v1_0.SuggestionsContributorConfiguration;
import com.liferay.portal.search.spi.suggestions.SuggestionsContributor;
import com.liferay.portal.search.suggestions.SuggestionsContributorResults;
import com.liferay.portal.search.suggestions.spi.constants.AsahSuggestionsConstants;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Gustavo Lima
 */
@Component(
	configurationPid = "com.liferay.portal.search.internal.configuration.AsahIndividualsConfiguration",
	property = "search.suggestions.contributor.name=asahRecentSites",
	service = SuggestionsContributor.class
)
public class AsahRecentSitesIndividualsContributor
	extends BaseAsahIndividualsSuggestionsContributor
	implements SuggestionsContributor {

	@Override
	public SuggestionsContributorResults getSuggestionsContributorResults(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		SearchContext searchContext,
		SuggestionsContributorConfiguration
			suggestionsContributorConfiguration) {

		_locale = searchContext.getLocale();

		return getSuggestionsContributorResults(
			StringBundler.concat(
				AsahSuggestionsConstants.INDIVIDUALS, "/",
				getHashedEmail(portal.getUserId(liferayPortletRequest))),
			AsahSuggestionsConstants.RECENT_SITES, searchContext,
			"lastVisitDate,visits,firstVisitDate,groupId",
			suggestionsContributorConfiguration);
	}

	@Override
	protected String getAssetURL(
		String destinationBaseURL, JSONObject itemJSONObject) {

		Group group = _fetchGroup(itemJSONObject);

		if (group == null) {
			return StringPool.BLANK;
		}

		return "/web" + group.getFriendlyURL();
	}

	@Override
	protected String getText(JSONObject itemJSONObject) {
		Group group = _fetchGroup(itemJSONObject);

		if (group == null) {
			return StringPool.BLANK;
		}

		return group.getName(_locale, true);
	}

	private Group _fetchGroup(JSONObject itemJSONObject) {
		long groupId = itemJSONObject.getLong("groupId");

		return _groupLocalService.fetchGroup(groupId);
	}

	// do we want to display guest site?

	@Reference
	private GroupLocalService _groupLocalService;

	private Locale _locale;

}