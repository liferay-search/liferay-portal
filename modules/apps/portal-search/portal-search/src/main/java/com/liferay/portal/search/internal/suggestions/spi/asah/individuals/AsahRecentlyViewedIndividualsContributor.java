/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.suggestions.spi.asah.individuals;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.search.rest.dto.v1_0.SuggestionsContributorConfiguration;
import com.liferay.portal.search.spi.suggestions.SuggestionsContributor;
import com.liferay.portal.search.suggestions.SuggestionsContributorResults;
import com.liferay.portal.search.suggestions.spi.constants.AsahSuggestionsConstants;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Gustavo Lima
 */
@Component(
	configurationPid = "com.liferay.portal.search.internal.configuration.AsahIndividualsConfiguration",
	property = "search.suggestions.contributor.name=asahRecentAssets",
	service = SuggestionsContributor.class
)
public class AsahRecentlyViewedIndividualsContributor
	extends BaseAsahIndividualsSuggestionsContributor
	implements SuggestionsContributor {

	@Override
	public SuggestionsContributorResults getSuggestionsContributorResults(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		SearchContext searchContext,
		SuggestionsContributorConfiguration
			suggestionsContributorConfiguration) {

		_liferayPortletRequest = liferayPortletRequest;
		_liferayPortletResponse = liferayPortletResponse;

		return getSuggestionsContributorResults(
			StringBundler.concat(
				AsahSuggestionsConstants.INDIVIDUALS, "/",
				getHashedEmail(portal.getUserId(liferayPortletRequest))),
			AsahSuggestionsConstants.RECENT_ASSETS, searchContext,
			"lastVisitDate,visits,assetTitle,firstVisitDate,url,assetId",
			suggestionsContributorConfiguration);
	}

	protected String getAssetURL(
		String destinationBaseURL, JSONObject itemJSONObject) {

		String url = itemJSONObject.getString("url");

		if (url.endsWith("/search")) {
			AssetEntry assetEntry = null;

			if (Objects.equals(
					itemJSONObject.getString("contentType"), "web-content")) {

				String articleId = itemJSONObject.getString("assetId");

				JournalArticle journalArticle =
					_journalArticleLocalService.fetchArticle(
						itemJSONObject.getLong("groupId"), articleId);

				assetEntry = _assetEntryLocalService.fetchEntry(
					journalArticle.getClassNameId(),
					journalArticle.getResourcePrimKey());
			}
			else {
				assetEntry = _assetEntryLocalService.fetchEntry(
					itemJSONObject.getLong("assetId"));
			}

			AssetRenderer<?> assetRenderer = assetEntry.getAssetRenderer();

			try {
				return assetRenderer.getURLViewInContext(
					_liferayPortletRequest, _liferayPortletResponse, "/search");
			}
			catch (Exception exception) {
				_log.error(exception);
			}
		}

		return url;
	}

	protected String getText(JSONObject itemJSONObject) {
		return itemJSONObject.getString("assetTitle");
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AsahRecentlyViewedIndividualsContributor.class);

	@Reference
	private AssetEntryLocalService _assetEntryLocalService;

	@Reference
	private JournalArticleLocalService _journalArticleLocalService;

	private LiferayPortletRequest _liferayPortletRequest;
	private LiferayPortletResponse _liferayPortletResponse;

}