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

import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.expando.kernel.util.ExpandoBridgeFactoryUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.search.experiences.starter.pack.bulkloader.internal.constants.ImportTypeKeys;
import com.liferay.search.experiences.starter.pack.bulkloader.internal.constants.PlacesConstants;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = GooglePlacesImporter.class)
public class GooglePlacesImporter {

	public void doImport(
			PortletRequest portletRequest, PortletResponse portletResponse,
			List<Long> userIds, List<Long> groupIds, String languageId,
			String importType)
		throws Exception {

		try {
			_createLocationExpandoField(portletRequest);
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);

			return;
		}

		_importArticles(
			portletRequest, userIds, groupIds, languageId, importType);
	}

	private void _addLocationAttribute(
		JournalArticle journalArticle, String lat, String lng) {

		JSONObject jsonObject = JSONUtil.put(
			"latitude", GetterUtil.getDouble(lat)
		).put(
			"longitude", GetterUtil.getDouble(lng)
		);

		ExpandoBridge expandoBridge = journalArticle.getExpandoBridge();

		expandoBridge.setAttribute(_LOCATION_EXPANDO_FIELD, jsonObject, false);

		_journalArticleHelper.updateJournalArticle(journalArticle);
	}

	private void _createLocationExpandoField(PortletRequest portletRequest)
		throws PortalException {

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		ExpandoBridge expandoBridge = ExpandoBridgeFactoryUtil.getExpandoBridge(
			themeDisplay.getCompanyId(), JournalArticle.class.getName());

		if (!expandoBridge.hasAttribute(_LOCATION_EXPANDO_FIELD)) {
			expandoBridge.addAttribute(
				_LOCATION_EXPANDO_FIELD, ExpandoColumnConstants.GEOLOCATION,
				JSONUtil.put(
					"latitude", 0D
				).put(
					"longitude", 0D
				),
				false);

			expandoBridge.setAttributeProperties(
				_LOCATION_EXPANDO_FIELD, _getUnicodeProperties(expandoBridge));
		}
	}

	private String[] _getAssetTagNames(JsonObject jsonObject) {
		JsonArray jsonArray = jsonObject.getAsJsonArray("types");

		List<String> tags = new ArrayList<>();

		for (int i = 0; i < jsonArray.size(); i++) {
			JsonElement typeJsonElement = jsonArray.get(i);

			String type = typeJsonElement.getAsString();

			if (!_journalArticleHelper.isValidTagValue(type)) {
				tags.add(_journalArticleHelper.cleanTagValue(type));
			}
		}

		return tags.toArray(new String[0]);
	}

	private String _getContent(String fileName, String lat, String lng) {
		StringBundler sb = new StringBundler(5);

		sb.append(PlacesConstants.fileNameToCityMap.get(fileName));
		sb.append(":");
		sb.append(lat);
		sb.append(",");
		sb.append(lng);

		return sb.toString();
	}

	private String _getLat(JsonObject jsonObject) {
		JsonElement latJsonElement = jsonObject.get("lat");

		return latJsonElement.getAsString();
	}

	private String _getLng(JsonObject jsonObject) {
		JsonElement latJsonElement = jsonObject.get("lng");

		return latJsonElement.getAsString();
	}

	private JsonObject _getLocationJsonObject(JsonObject jsonObject) {
		JsonElement geometryJsonElement = jsonObject.get("geometry");

		JsonObject geometryJsonObject = geometryJsonElement.getAsJsonObject();

		JsonElement locationJsonElement = geometryJsonObject.get("location");

		return locationJsonElement.getAsJsonObject();
	}

	private JsonArray _getResultsJsonArray(JsonElement jsonElement) {
		JsonObject rootJsonObject = jsonElement.getAsJsonObject();

		return rootJsonObject.getAsJsonArray("results");
	}

	private String _getTitle(JsonObject jsonObject) {
		JsonElement jsonElement = jsonObject.get("name");

		return jsonElement.getAsString();
	}

	private UnicodeProperties _getUnicodeProperties(
		ExpandoBridge expandoBridge) {

		UnicodeProperties unicodeProperties =
			expandoBridge.getAttributeProperties(_LOCATION_EXPANDO_FIELD);

		unicodeProperties.setProperty(
			ExpandoColumnConstants.INDEX_TYPE,
			String.valueOf(ExpandoColumnConstants.INDEX_TYPE_KEYWORD));

		unicodeProperties.setProperty(
			ExpandoColumnConstants.PROPERTY_LOCALIZE_FIELD_NAME, "false");

		return unicodeProperties;
	}

	private void _importArticles(
			PortletRequest portletRequest, List<Long> userIds,
			List<Long> groupIds, String languageId, String importType)
		throws Exception {

		JsonParser parser = new JsonParser();

		int groupIdx = 0;

		int userIdx = 0;

		Set<String> fileNames = PlacesConstants.fileNameToCityMap.keySet();

		for (String fileName : fileNames) {
			if (importType.equals(ImportTypeKeys.RESTAURANTS) &&
				!fileName.endsWith("-restaurant.json")) {

				continue;
			}

			if (importType.equals(ImportTypeKeys.TOURIST_ATTRACTIONS) &&
				!fileName.endsWith("-tourist.json")) {

				continue;
			}

			if (_log.isInfoEnabled()) {
				_log.info("Importing " + fileName);
			}

			try (InputStream inputStream = getClass().getResourceAsStream(
					fileName)) {

				JsonElement rootJsonElement = parser.parse(
					new InputStreamReader(inputStream));

				JsonArray resultsJsonArray = _getResultsJsonArray(
					rootJsonElement);

				for (int i = 0; i < resultsJsonArray.size(); i++) {
					JsonElement resultJsonElement = resultsJsonArray.get(i);

					JsonObject resultJsonObject =
						resultJsonElement.getAsJsonObject();

					JsonObject locationJsonObject = _getLocationJsonObject(
						resultJsonObject);

					String lat = _getLat(locationJsonObject);

					String lng = _getLng(locationJsonObject);

					if (userIdx == userIds.size()) {
						userIdx = 0;
					}

					long userId = userIds.get(userIdx++);

					if (groupIdx == groupIds.size()) {
						groupIdx = 0;
					}

					long groupId = groupIds.get(groupIdx++);

					JournalArticle journalArticle =
						_journalArticleHelper.addJournalArticle(
							portletRequest, userId, groupId, languageId,
							_getTitle(resultJsonObject),
							_getContent(fileName, lat, lng),
							_getAssetTagNames(resultJsonObject));

					_addLocationAttribute(journalArticle, lat, lng);
				}
			}
			catch (Exception exception) {
				_log.error(exception.getMessage(), exception);
			}
		}
	}

	private static final String _LOCATION_EXPANDO_FIELD = "location";

	private static final Log _log = LogFactoryUtil.getLog(
		GooglePlacesImporter.class);

	@Reference
	private JournalArticleHelper _journalArticleHelper;

}