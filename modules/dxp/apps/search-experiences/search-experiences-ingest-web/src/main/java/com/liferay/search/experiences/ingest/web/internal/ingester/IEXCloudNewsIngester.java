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
import com.liferay.portal.kernel.util.Validator;
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
 * @author Petteri Karttunen
 */
@Component(
	enabled = false, property = "type=iexcloud_news", service = Ingester.class
)
public class IEXCloudNewsIngester implements Ingester {

	@Override
	public IngestionStats ingest(ActionRequest actionRequest) {
		IngestionStats ingestionStats = new IngestionStats();

		String apiUrl = _getAPIUrl(actionRequest);

		if (Validator.isBlank(apiUrl)) {
			return ingestionStats;
		}

		LoopingIterator<Long> groupIdsLoopingIterator =
			IngesterUtil.getGroupIdsLoopingIterator(actionRequest);

		LoopingIterator<Long> userIdsLoopingIterator =
			IngesterUtil.getUserIdsLoopingIterator(actionRequest);

		try {
			JSONArray jsonArray = _jsonFactory.createJSONArray(
				_http.URLtoString(apiUrl));

			ServiceContext serviceContext = IngesterUtil.getServiceContext(
				actionRequest);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject resultJSONObject = jsonArray.getJSONObject(i);

				String title = resultJSONObject.getString("headline");

				serviceContext.setAssetTagNames(
					_getAssetTagNames(resultJSONObject));

				_journalArticleImporter.importBasicWebContentJournalArticle(
					resultJSONObject.getString("summary"), serviceContext,
					title);

				serviceContext.setScopeGroupId(groupIdsLoopingIterator.next());
				serviceContext.setUserId(userIdsLoopingIterator.next());

				ingestionStats.addIngestedTitle(title);
			}
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		return ingestionStats;
	}

	private String _getAPIUrl(ActionRequest actionRequest) {
		String iexCloudNewsRange = ParamUtil.getString(
			actionRequest, "iexCloudNewsRange", "1d");

		String iexCloudNewsSymbol = ParamUtil.getString(
			actionRequest, "iexCloudNewsSymbol", "AAPL");

		String iexCloudNewsToken = ParamUtil.getString(
			actionRequest, "iexCloudNewsToken");

		String iexCloudNewsWorkspaceName = ParamUtil.getString(
			actionRequest, "iexCloudNewsWorkspaceName");

		if (Validator.isBlank(iexCloudNewsRange) ||
			Validator.isBlank(iexCloudNewsSymbol) ||
			Validator.isBlank(iexCloudNewsToken) ||
			Validator.isBlank(iexCloudNewsWorkspaceName)) {

			return null;
		}

		int iexCloudNewsLimit = ParamUtil.getInteger(
			actionRequest, "iexCloudNewsLimit", 10);

		StringBundler sb = new StringBundler(10);

		sb.append("https://");
		sb.append(iexCloudNewsWorkspaceName);
		sb.append(".iex.cloud/v1/time-series/news/");
		sb.append(iexCloudNewsSymbol);
		sb.append("?token=");
		sb.append(iexCloudNewsToken);
		sb.append("&range=");
		sb.append(iexCloudNewsRange);
		sb.append("&limit=");
		sb.append(iexCloudNewsLimit);

		return sb.toString();
	}

	private String[] _getAssetTagNames(JSONObject jsonObject) {
		List<String> assetTagNames = new ArrayList<>();

		String provider = jsonObject.getString("provider");

		if (TagUtil.isValidTag(provider)) {
			assetTagNames.add(TagUtil.cleanTag(provider));
		}

		String source = jsonObject.getString("source");

		if (TagUtil.isValidTag(source)) {
			assetTagNames.add(TagUtil.cleanTag(source));
		}

		String symbol = jsonObject.getString("symbol");

		if (TagUtil.isValidTag(symbol)) {
			assetTagNames.add(TagUtil.cleanTag(symbol));
		}

		return assetTagNames.toArray(new String[0]);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		IEXCloudNewsIngester.class);

	@Reference
	private Http _http;

	@Reference
	private JournalArticleImporter _journalArticleImporter;

	@Reference
	private JSONFactory _jsonFactory;

}