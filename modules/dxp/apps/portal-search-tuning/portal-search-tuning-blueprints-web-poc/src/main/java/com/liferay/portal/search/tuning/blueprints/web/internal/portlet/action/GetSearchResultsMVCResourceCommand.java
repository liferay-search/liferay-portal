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

package com.liferay.portal.search.tuning.blueprints.web.internal.portlet.action;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributesBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.exception.BlueprintsEngineException;
import com.liferay.portal.search.tuning.blueprints.engine.util.BlueprintsEngineHelper;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetJSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.response.BlueprintsResponseBuilder;
import com.liferay.portal.search.tuning.blueprints.response.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.util.attributes.BlueprintsAttributesHelper;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.BlueprintsWebPortletKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.ResourceRequestKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferences;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferencesImpl;
import com.liferay.portal.search.tuning.blueprints.web.internal.util.BlueprintsLocalizationHelper;

import java.util.Locale;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + BlueprintsWebPortletKeys.BLUEPRINTS_WEB,
		"mvc.command.name=" + ResourceRequestKeys.GET_SEARCH_RESULTS
	},
	service = MVCResourceCommand.class
)
public class GetSearchResultsMVCResourceCommand extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		long blueprintId = _getSearchBlueprintId(resourceRequest);

		JSONObject responseJsonObject = null;

		try {
			Messages requestMessages = new Messages();

			BlueprintsAttributes blueprintsRequestAttributes =
				_getBlueprintsRequestAttributes(resourceRequest, blueprintId);

			SearchResponse searchResponse = _blueprintsEngineHelper.search(
				blueprintsRequestAttributes, requestMessages, blueprintId);

			BlueprintsAttributes blueprintsResponseAttributes =
				_getBlueprintsResponseAttributes(
					resourceRequest, resourceResponse, blueprintId);

			Messages responseMessages = new Messages();

			responseJsonObject = _blueprintsResponseBuilder.buildJSONObject(
				searchResponse, blueprintsResponseAttributes, responseMessages,
				blueprintId);

			_processResults(resourceRequest, responseJsonObject);
		}
		catch (JSONException jsonException) {
			_log.error(jsonException.getMessage(), jsonException);

			responseJsonObject = JSONUtil.put(
				JSONResponseKeys.ERRORS, jsonException.getMessage());
		}
		catch (BlueprintsEngineException blueprintsEngineException) {
			_log.error(
				blueprintsEngineException.getMessage(),
				blueprintsEngineException);

			responseJsonObject = JSONUtil.put(
				JSONResponseKeys.ERRORS,
				blueprintsEngineException.getMessage());
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);

			responseJsonObject = JSONUtil.put(
				JSONResponseKeys.ERRORS, portalException.getMessage());
		}

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse, responseJsonObject);
	}

	private BlueprintsAttributes _getBlueprintsRequestAttributes(
		ResourceRequest resourceRequest, long blueprintId) {

		BlueprintsAttributesBuilder blueprintsAttributesBuilder =
			_blueprintsAttributesHelper.getBlueprintsRequestAttributesBuilder(
				resourceRequest, blueprintId);

		// Add here any other attributes

		return blueprintsAttributesBuilder.build();
	}

	private BlueprintsAttributes _getBlueprintsResponseAttributes(
		ResourceRequest resourceRequest, ResourceResponse resourceResponse,
		long blueprintId) {

		BlueprintsAttributesBuilder blueprintsAttributesBuilder =
			_blueprintsAttributesHelper.getBlueprintsResponsetAttributesBuilder(
				resourceRequest, resourceResponse, blueprintId);

		// Add here any other attributes

		return blueprintsAttributesBuilder.build();
	}

	private long _getSearchBlueprintId(ResourceRequest resourceRequest) {
		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences =
			new BlueprintsWebPortletPreferencesImpl(
				resourceRequest.getPreferences());

		return blueprintsWebPortletPreferences.getSearchBlueprintId();
	}

	private void _localizeFacets(
		JSONObject searchResultsJsonObject, Locale locale) {

		if (!searchResultsJsonObject.has(FacetJSONResponseKeys.FACETS)) {
			return;
		}

		JSONArray facetsJsonArray = searchResultsJsonObject.getJSONArray(
			FacetJSONResponseKeys.FACETS);

		for (int i = 0; i < facetsJsonArray.length(); i++) {
			JSONObject facetJsonObject = facetsJsonArray.getJSONObject(i);

			facetJsonObject.put(
				"anyOption",
				_blueprintsLocalizationHelper.get(
					locale,
					"any-" +
						facetJsonObject.getString(
							FacetJSONResponseKeys.PARAMETER_NAME
						).toLowerCase()));

			JSONArray facetValuesJsonArray = facetJsonObject.getJSONArray(
				FacetConfigurationKeys.VALUE_AGGREGATION_VALUES.getJsonKey());

			for (int j = 0; j < facetValuesJsonArray.length(); j++) {
				JSONObject valueJsonObject = facetValuesJsonArray.getJSONObject(
					j);

				String value = valueJsonObject.getString(
					FacetJSONResponseKeys.VALUE);

				value = StringUtil.toLowerCase(value);

				StringBundler sb = new StringBundler(4);

				sb.append(_blueprintsLocalizationHelper.get(locale, value));
				sb.append(" (");
				sb.append(
					valueJsonObject.getString(FacetJSONResponseKeys.FREQUENCY));
				sb.append(")");

				valueJsonObject.put("text", sb.toString());
			}
		}
	}

	private void _localizeResultItemType(
		JSONObject responseJsonObject, Locale locale) {

		JSONArray itemsJsonArray = responseJsonObject.getJSONArray(
			JSONResponseKeys.ITEMS);

		for (int i = 0; i < itemsJsonArray.length(); i++) {
			JSONObject itemJsonObject = itemsJsonArray.getJSONObject(i);

			String type = itemJsonObject.getString("type");

			type = StringUtil.toLowerCase(type);

			itemsJsonArray.getJSONObject(
				i
			).put(
				"type", _blueprintsLocalizationHelper.get(locale, type)
			);
		}
	}

	private void _processResults(
		ResourceRequest resourceRequest, JSONObject responseJsonObject) {

		if (responseJsonObject.getJSONArray(
				JSONResponseKeys.ITEMS
			).length() > 0) {

			ThemeDisplay themeDisplay =
				(ThemeDisplay)resourceRequest.getAttribute(
					WebKeys.THEME_DISPLAY);

			_localizeFacets(responseJsonObject, themeDisplay.getLocale());

			_localizeResultItemType(
				responseJsonObject, themeDisplay.getLocale());
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		GetSearchResultsMVCResourceCommand.class);

	@Reference
	private BlueprintsAttributesHelper _blueprintsAttributesHelper;

	@Reference
	private BlueprintsEngineHelper _blueprintsEngineHelper;

	@Reference
	private BlueprintsLocalizationHelper _blueprintsLocalizationHelper;

	@Reference
	private BlueprintsResponseBuilder _blueprintsResponseBuilder;

	@Reference
	private Portal _portal;

}