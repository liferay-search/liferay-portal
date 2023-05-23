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
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.search.experiences.ingest.web.internal.importer.JournalArticleImporter;
import com.liferay.search.experiences.ingest.web.internal.iterator.LoopingIterator;
import com.liferay.search.experiences.ingest.web.internal.stats.IngestionStats;
import com.liferay.search.experiences.ingest.web.internal.util.IngesterUtil;
import com.liferay.search.experiences.ingest.web.internal.util.TagUtil;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author André de Oliveira
 * @author Petteri Karttunen
 * @author Gustavo Lima
 */
@Component(
	enabled = false, property = "type=liferay_help_center",
	service = Ingester.class
)
public class LiferayHelpCenterIngester implements Ingester {

	@Override
	public IngestionStats ingest(ActionRequest actionRequest) {
		IngestionStats ingestionStats = new IngestionStats();

		int itemsPerPage = ParamUtil.getInteger(
			actionRequest, "liferayHelpCenterItemsPerPage", 30);

		int numOfPages = ParamUtil.getInteger(
			actionRequest, "liferayHelpCenterNumOfPages", 1);

		int startPage = ParamUtil.getInteger(
			actionRequest, "liferayHelpCenterStartPage", 1);

		LoopingIterator<Long> groupIdsLoopingIterator =
			IngesterUtil.getGroupIdsLoopingIterator(actionRequest);

		LoopingIterator<Long> userIdsLoopingIterator =
			IngesterUtil.getUserIdsLoopingIterator(actionRequest);

		try {
			ServiceContext serviceContext = IngesterUtil.getServiceContext(
				actionRequest);

			for (int i = 0; i < numOfPages; i++) {
				JSONObject jsonObject = _jsonFactory.createJSONObject(
					_http.URLtoString(
						_getApiUrl(itemsPerPage, startPage + i + 1)));

				JSONArray jsonArray = jsonObject.getJSONArray("articles");

				for (int j = 0; j < jsonArray.length(); j++) {
					JSONObject resultJSONObject = jsonArray.getJSONObject(j);

					String title = resultJSONObject.getString("title");

					serviceContext.setAssetTagNames(
						_getAssetTagNames(resultJSONObject));

					_journalArticleImporter.importBasicWebContentJournalArticle(
						_getContent(resultJSONObject), serviceContext, title);

					serviceContext.setScopeGroupId(
						groupIdsLoopingIterator.next());
					serviceContext.setUserId(userIdsLoopingIterator.next());

					ingestionStats.addIngestedTitle(title);
				}
			}
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		return ingestionStats;
	}

	private String _getApiUrl(int itemsPerPage, int page) {
		return StringBundler.concat(
			_API_URL, "?page=", page, "&per_page=", itemsPerPage);
	}

	private String[] _getAssetTagNames(JSONObject jsonObject) {
		List<String> assetTagNames = new ArrayList<>();

		assetTagNames.add("Liferay Help Center");

		JSONArray jsonArray = jsonObject.getJSONArray("label_names");

		for (int i = 0; i < jsonArray.length(); i++) {
			String tag = jsonArray.getString(i);

			if (TagUtil.isValidTag(tag)) {
				assetTagNames.add(TagUtil.cleanTag(tag));
			}
		}

		return assetTagNames.toArray(new String[0]);
	}

	private String _getContent(JSONObject jsonObject) {
		return jsonObject.getString("body");
	}

	private static final String _API_URL =
		"https://liferay-support.zendesk.com/api/v2/help_center/en-us" +
			"/articles.json";

	private static final Log _log = LogFactoryUtil.getLog(
		LiferayHelpCenterIngester.class);

	@Reference
	private Http _http;

	@Reference
	private JournalArticleImporter _journalArticleImporter;

	@Reference
	private JSONFactory _jsonFactory;

}