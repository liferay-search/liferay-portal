/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.suggestions.spi.asah.individuals;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.search.rest.dto.v1_0.SuggestionsContributorConfiguration;
import com.liferay.portal.search.spi.suggestions.SuggestionsContributor;
import com.liferay.portal.search.suggestions.SuggestionsContributorResults;
import com.liferay.portal.search.suggestions.spi.constants.AsahSuggestionsConstants;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Gustavo Lima
 */
@Component(
	configurationPid = "com.liferay.portal.search.internal.configuration.AsahIndividualsConfiguration",
	property = "search.suggestions.contributor.name=asahRecentPages",
	service = SuggestionsContributor.class
)
public class AsahRecentPagesIndividualsContributor
	extends BaseAsahIndividualsSuggestionsContributor
	implements SuggestionsContributor {

	@Override
	public SuggestionsContributorResults getSuggestionsContributorResults(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		SearchContext searchContext,
		SuggestionsContributorConfiguration
			suggestionsContributorConfiguration) {

		return getSuggestionsContributorResults(
			StringBundler.concat(
				AsahSuggestionsConstants.INDIVIDUALS, "/",
				getHashedEmail(portal.getUserId(liferayPortletRequest))),
			AsahSuggestionsConstants.RECENT_PAGES, searchContext,
			"lastVisitDate,visits,displayLanguageId,firstVisitDate,url",
			suggestionsContributorConfiguration);
	}

	protected String getAssetURL(
		String destinationBaseURL, JSONObject itemJSONObject) {

		return itemJSONObject.getString("url");
	}

	protected String getText(JSONObject itemJSONObject) {
		Layout layout = _layoutLocalService.fetchLayoutByFriendlyURL(
			itemJSONObject.getLong("groupId"), true,
			itemJSONObject.getString("url"));

		return layout.getName();
	}

	@Reference
	private LayoutLocalService _layoutLocalService;

}