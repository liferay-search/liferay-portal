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

package com.liferay.search.experiences.starter.pack.bulkloader.internal.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.URLCodec;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = WikipediaImporter.class)
public class WikipediaImporter {

	public void doImport(
			PortletRequest portletRequest, PortletResponse portletResponse,
			List<Long> userIds, List<Long> groupIds, String languageId)
		throws Exception {

		String wikiArticles = ParamUtil.getString(
			portletRequest, "wikiArticles");

		List<String> articleList = _toStringList(wikiArticles);

		if (articleList.isEmpty()) {
			_log.error("Root article not given");

			return;
		}

		String wikiLanguage = ParamUtil.getString(
			portletRequest, "wikiLanguage", "en");

		int count = ParamUtil.getInteger(portletRequest, "count", 100);

		_importWikipediaArticles(
			portletRequest, articleList, userIds, groupIds, languageId,
			wikiLanguage, 0, count, new ArrayList<>());
	}

	private void _addArticleLinks(
		List<String> articleLinks, JsonObject jsonObject) {

		JsonArray linksJsonArray = jsonObject.getAsJsonArray("links");

		for (int i = 0; i < linksJsonArray.size(); i++) {
			JsonElement linkJsonElement = linksJsonArray.get(i);

			JsonObject linkJsonObject = linkJsonElement.getAsJsonObject();

			JsonElement nsJsonElement = linkJsonObject.get("ns");

			int ns = nsJsonElement.getAsInt();

			if (ns != 0) {
				continue;
			}

			JsonElement linksJsonElement = linkJsonObject.get("*");

			articleLinks.add(linksJsonElement.getAsString());
		}
	}

	private String _getAPIURL(String wikiLanguage, String article) {
		StringBundler sb = new StringBundler(5);

		sb.append("https://");
		sb.append(wikiLanguage);
		sb.append(_API_URL_SUFFIX);
		sb.append(URLCodec.encodeURL(article));
		sb.append("&format=json");

		return sb.toString();
	}

	private String[] _getAssetTagNames(JsonObject jsonObject) {
		List<String> tags = new ArrayList<>();

		JsonArray categoriesJsonArray = jsonObject.getAsJsonArray("categories");

		for (int i = 0; i < categoriesJsonArray.size(); i++) {
			JsonElement categoryJsonElement = categoriesJsonArray.get(i);

			JsonObject categoryJsonObject =
				categoryJsonElement.getAsJsonObject();

			JsonElement hidden = categoryJsonObject.get("hidden");

			if (hidden != null) {
				continue;
			}

			JsonElement valueJsonElement = categoryJsonObject.get("*");

			String value = valueJsonElement.getAsString();

			if (_journalArticleHelper.isValidTagValue(value)) {
				value = _journalArticleHelper.cleanTagValue(value);

				tags.add(value);
			}
		}

		return tags.toArray(new String[0]);
	}

	private String _getContent(JsonObject jsonObject) {
		JsonObject textJsonObject = jsonObject.getAsJsonObject("text");

		JsonElement jsonElement = textJsonObject.get("*");

		return jsonElement.getAsString();
	}

	private String _getTitle(JsonObject jsonObject) {
		JsonElement jsonElement = jsonObject.get("title");

		return jsonElement.getAsString();
	}

	private void _importWikipediaArticles(
			PortletRequest portletRequest, List<String> articleList,
			List<Long> userIds, List<Long> groupIds, String languageId,
			String wikiLanguage, int counter, int count,
			List<String> importedTitles)
		throws Exception {

		List<String> articleLinks = new ArrayList<>();

		int groupIdx = 0;
		int userIdx = 0;

		for (String article : articleList) {
			if (counter >= count) {
				return;
			}

			URL url = new URL(_getAPIURL(wikiLanguage, article));

			URLConnection request = url.openConnection();

			request.connect();

			JsonParser parser = new JsonParser();

			try {
				JsonElement rootJsonElement = parser.parse(
					new InputStreamReader((InputStream)request.getContent()));

				JsonObject rootJsonObject = rootJsonElement.getAsJsonObject();

				JsonObject parseJsonObject = rootJsonObject.getAsJsonObject(
					"parse");

				String title = _getTitle(parseJsonObject);

				if (importedTitles.contains(title)) {
					continue;
				}

				_addArticleLinks(articleLinks, parseJsonObject);

				if (userIdx == userIds.size()) {
					userIdx = 0;
				}

				long userId = userIds.get(userIdx++);

				if (groupIdx == groupIds.size()) {
					groupIdx = 0;
				}

				long groupId = groupIds.get(groupIdx++);

				_journalArticleHelper.addJournalArticle(
					portletRequest, userId, groupId, languageId, title,
					_getContent(parseJsonObject),
					_getAssetTagNames(parseJsonObject));

				importedTitles.add(title);
			}
			catch (Exception exception) {
				_log.error(exception.getMessage(), exception);
			}

			counter++;
		}

		if (articleLinks.isEmpty()) {
			return;
		}

		_importWikipediaArticles(
			portletRequest, articleLinks, userIds, groupIds, languageId,
			wikiLanguage, counter, count, importedTitles);
	}

	private List<String> _toStringList(String ids) throws Exception {
		String[] arr = ids.split(",");
		List<String> values = new ArrayList<>();

		for (String s : arr) {
			s = StringUtil.trim(s);

			values.add(s);
		}

		return values;
	}

	private static final String _API_URL_SUFFIX =
		".wikipedia.org/w/api.php?action=parse&page=";

	private static final Log _log = LogFactoryUtil.getLog(
		WikipediaImporter.class);

	@Reference
	private JournalArticleHelper _journalArticleHelper;

}