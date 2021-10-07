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

import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Tibor Lipusz
 */
@Component(immediate = true, service = NPSNationalParksImporter.class)
public class NPSNationalParksImporter {

	public void doImport(
			PortletRequest portletRequest, PortletResponse portletResponse,
			List<Long> userIds, List<Long> groupIds, String languageId,
			String fileName, InputStream file, String importType)
		throws Exception {

		try {
			_createLocationExpandoField(portletRequest);
			_createVisitedUserIdsExpandoField(portletRequest);
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);

			return;
		}

		_importArticles(
			portletRequest, userIds, groupIds, languageId, fileName, file,
			importType);
	}

	private void _addExpandoAttributes(
		JournalArticle journalArticle, String lat, String lng) {

		JSONObject jsonObject = JSONUtil.put(
			"latitude", GetterUtil.getDouble(lat)
		).put(
			"longitude", GetterUtil.getDouble(lng)
		);

		ExpandoBridge expandoBridge = journalArticle.getExpandoBridge();

		expandoBridge.setAttribute(_LOCATION_EXPANDO_FIELD, jsonObject, false);
		expandoBridge.setAttribute(
			_VISITED_USER_ID_EXPANDO_FIELD, new Integer[] {99999,88888});

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
				_LOCATION_EXPANDO_FIELD,
				_getUnicodePropertiesLocation(expandoBridge));
		}
	}

	private void _createVisitedUserIdsExpandoField(PortletRequest portletRequest)
		throws PortalException {

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		ExpandoBridge expandoBridge = ExpandoBridgeFactoryUtil.getExpandoBridge(
			themeDisplay.getCompanyId(), JournalArticle.class.getName());

		if (!expandoBridge.hasAttribute(_VISITED_USER_ID_EXPANDO_FIELD)) {
			expandoBridge.addAttribute(
				_VISITED_USER_ID_EXPANDO_FIELD,
				ExpandoColumnConstants.INTEGER_ARRAY);

			expandoBridge.setAttributeProperties(
				_VISITED_USER_ID_EXPANDO_FIELD,
				_getUnicodePropertiesVisited(expandoBridge));
		}
	}

	private String _getContent(String content, String lat, String lng) {
		StringBundler sb = new StringBundler(5);

		sb.append(":");
		sb.append(content);
		sb.append(lat);
		sb.append(",");
		sb.append(lng);

		return sb.toString();
	}

	private JsonArray _getCoordinatesJsonArray(JsonObject jsonObject) {
		JsonElement geometryJsonElement = jsonObject.get("geometry");

		JsonObject geometryJsonObject = geometryJsonElement.getAsJsonObject();

		return geometryJsonObject.getAsJsonArray("coordinates");
	}

	private JsonArray _getFeaturesJsonArray(JsonElement jsonElement) {
		JsonObject rootJsonObject = jsonElement.getAsJsonObject();

		return rootJsonObject.getAsJsonArray("features");
	}

	private String _getLat(JsonArray jsonArray) {
		JsonElement latJsonElement = jsonArray.get(1);

		return latJsonElement.getAsString();
	}

	private String _getLng(JsonArray jsonArray) {
		JsonElement latJsonElement = jsonArray.get(0);

		return latJsonElement.getAsString();
	}

	private String _getTitle(JsonObject jsonObject) {
		JsonElement propertiesJsonElement = jsonObject.get("properties");

		JsonObject propertiesJsonObject =
			propertiesJsonElement.getAsJsonObject();

		JsonElement nameJsonElement = propertiesJsonObject.get("Name");

		return nameJsonElement.getAsString();
	}

	private UnicodeProperties _getUnicodePropertiesLocation(
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

	private UnicodeProperties _getUnicodePropertiesVisited(
		ExpandoBridge expandoBridge) {

		UnicodeProperties unicodeProperties =
			expandoBridge.getAttributeProperties(_VISITED_USER_ID_EXPANDO_FIELD);

		unicodeProperties.setProperty(
			ExpandoColumnConstants.INDEX_TYPE,
			String.valueOf(ExpandoColumnConstants.INDEX_TYPE_KEYWORD));

		unicodeProperties.setProperty(
			ExpandoColumnConstants.PROPERTY_LOCALIZE_FIELD_NAME, "false");

		return unicodeProperties;
	}

	private void _importArticles(
			PortletRequest portletRequest, List<Long> userIds,
			List<Long> groupIds, String languageId, String uploadFileName,
			InputStream uploadInputStream, String importType)
		throws Exception {

		if (!importType.equals(ImportTypeKeys.NPS_NATIONAL_PARKS)) {
			return;
		}

		JsonParser parser = new JsonParser();

		int groupIdx = 0;

		int userIdx = 0;

		try (InputStream inputStream = getClass().getResourceAsStream(
				_NPS_NATIONAL_PARKS_JSON)) {

			if (_log.isInfoEnabled()) {
				_log.info("Importing " + _NPS_NATIONAL_PARKS_JSON);
			}

			JsonElement rootJsonElement = parser.parse(
				new InputStreamReader(inputStream));

			JsonArray featuresJsonArray = _getFeaturesJsonArray(
				rootJsonElement);

			for (int i = 0; i < featuresJsonArray.size(); i++) {
				JsonElement featureJsonElement = featuresJsonArray.get(i);

				JsonObject featureJsonObject =
					featureJsonElement.getAsJsonObject();

				JsonArray locationJsonArray = _getCoordinatesJsonArray(
					featureJsonObject);

				String lat = _getLat(locationJsonArray);

				String lng = _getLng(locationJsonArray);

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
						_getTitle(featureJsonObject),
						_getContent(_CONTENT_, lat, lng), new String[0]);

				_addExpandoAttributes(journalArticle, lat, lng);
			}
		}
		catch (Exception exception) {
			_log.error(exception.getMessage(), exception);
		}
	}

	private static final String _CONTENT_ = "National Park";

	private static final String _LOCATION_EXPANDO_FIELD = "location";

	private static final String _NPS_NATIONAL_PARKS_JSON =
		"nps-national-parks.geojson";

	private static final String _VISITED_USER_ID_EXPANDO_FIELD =
		"visitedUserId";

	private static final Log _log = LogFactoryUtil.getLog(
		NPSNationalParksImporter.class);

	@Reference
	private JournalArticleHelper _journalArticleHelper;

}