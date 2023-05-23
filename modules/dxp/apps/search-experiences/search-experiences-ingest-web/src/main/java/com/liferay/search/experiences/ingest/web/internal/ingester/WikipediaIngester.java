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

package com.liferay.search.experiences.ingest.web.internal.ingester;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.search.experiences.ingest.web.internal.importer.JournalArticleImporter;
import com.liferay.search.experiences.ingest.web.internal.iterator.LoopingIterator;
import com.liferay.search.experiences.ingest.web.internal.stats.IngestionStats;
import com.liferay.search.experiences.ingest.web.internal.util.CSVUtil;
import com.liferay.search.experiences.ingest.web.internal.util.IngesterUtil;
import com.liferay.search.experiences.ingest.web.internal.util.TagUtil;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	enabled = false, property = "type=wikipedia", service = Ingester.class
)
public class WikipediaIngester implements Ingester {

	@Override
	public IngestionStats ingest(ActionRequest actionRequest) {
		IngestionStats ingestionStats = new IngestionStats();

		List<String> wikiArticlesList = CSVUtil.csvtoStringList(
			ParamUtil.getString(actionRequest, "wikiArticles"));

		if (wikiArticlesList.isEmpty()) {
			_log.error("No Wiki articles to import");

			return ingestionStats;
		}

		try {
			_ingest(
				IngesterUtil.getGroupIdsLoopingIterator(actionRequest),
				ingestionStats,
				ParamUtil.getInteger(actionRequest, "numberOfWikiArticles", 10),
				wikiArticlesList,
				ParamUtil.getString(actionRequest, "wikiLanguage", "en"),
				IngesterUtil.getServiceContext(actionRequest),
				IngesterUtil.getUserIdsLoopingIterator(actionRequest));
		}
		catch (PortalException portalException) {
			_log.error(portalException);
		}

		return ingestionStats;
	}

	private String[] _getAssetTagNames(JSONObject jsonObject) {
		JSONArray jsonArray = jsonObject.getJSONArray("categories");

		if (jsonArray.length() == 0) {
			return new String[0];
		}

		List<String> assetTagNames = new ArrayList<>();

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject categoryJSONObject = jsonArray.getJSONObject(i);

			if (categoryJSONObject.getBoolean("hidden")) {
				continue;
			}

			String tag = categoryJSONObject.getString("*");

			if (TagUtil.isValidTag(tag)) {
				assetTagNames.add(TagUtil.cleanTag(tag));
			}
		}

		return assetTagNames.toArray(new String[0]);
	}

	private String _getContent(JSONObject jsonObject) {
		JSONObject textJSONObject = jsonObject.getJSONObject("text");

		return textJSONObject.getString("*");
	}

	private Http.Options _getHttpOptions(
		String wikiArticle, String wikiLanguage) {

		Http.Options options = new Http.Options();

		options.setCookieSpec(Http.CookieSpec.STANDARD);
		options.setLocation(
			StringBundler.concat(
				"https://", wikiLanguage, _API_URL_SUFFIX,
				URLCodec.encodeURL(wikiArticle), "&format=json"));

		return options;
	}

	private List<String> _getWikiArticleLinks(JSONObject jsonObject) {
		List<String> articleLinks = new ArrayList<>();

		JSONArray jsonArray = jsonObject.getJSONArray("links");

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject linkJSONObject = jsonArray.getJSONObject(i);

			int ns = linkJSONObject.getInt("ns");

			if (ns != 0) {
				continue;
			}

			articleLinks.add(linkJSONObject.getString("*"));
		}

		return articleLinks;
	}

	private void _ingest(
		LoopingIterator<Long> groupIdsLoopingIterator,
		IngestionStats ingestionStats, int numberOfWikiArticlesToIngest,
		List<String> wikiArticlesList, String wikiLanguage,
		ServiceContext serviceContext,
		LoopingIterator<Long> userIdsLoopingIterator) {

		List<String> wikiArticleLinks = new ArrayList<>();

		for (String wikiArticle : wikiArticlesList) {
			if (ingestionStats.getTotalProcessedItemsCount() >=
					numberOfWikiArticlesToIngest) {

				return;
			}

			try {
				JSONObject rootJSONObject = _jsonFactory.createJSONObject(
					_http.URLtoString(
						_getHttpOptions(wikiArticle, wikiLanguage)));

				JSONObject parseJSONObject = rootJSONObject.getJSONObject(
					"parse");

				if (parseJSONObject == null) {
					ingestionStats.addSkippedItem();

					continue;
				}

				String title = parseJSONObject.getString("title");

				if (Validator.isBlank(title) ||
					ingestionStats.hasIngestedTitle(title)) {

					ingestionStats.addSkippedItem();

					continue;
				}

				serviceContext.setAssetTagNames(
					_getAssetTagNames(parseJSONObject));

				_journalArticleImporter.importBasicWebContentJournalArticle(
					_getContent(parseJSONObject), serviceContext, title);

				serviceContext.setScopeGroupId(groupIdsLoopingIterator.next());
				serviceContext.setUserId(userIdsLoopingIterator.next());

				ingestionStats.addIngestedTitle(title);

				wikiArticleLinks.addAll(_getWikiArticleLinks(parseJSONObject));
			}
			catch (Exception exception) {
				ingestionStats.addFailedItem();

				_log.error(exception);
			}
		}

		if (wikiArticleLinks.isEmpty()) {
			return;
		}

		_ingest(
			groupIdsLoopingIterator, ingestionStats,
			numberOfWikiArticlesToIngest, wikiArticleLinks, wikiLanguage,
			serviceContext, userIdsLoopingIterator);
	}

	private static final String _API_URL_SUFFIX =
		".wikipedia.org/w/api.php?action=parse&page=";

	private static final Log _log = LogFactoryUtil.getLog(
		WikipediaIngester.class);

	@Reference
	private Http _http;

	@Reference
	private JournalArticleImporter _journalArticleImporter;

	@Reference
	private JSONFactory _jsonFactory;

}