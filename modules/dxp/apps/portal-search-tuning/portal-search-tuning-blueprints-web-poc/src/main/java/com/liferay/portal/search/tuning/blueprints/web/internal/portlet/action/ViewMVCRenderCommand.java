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

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.BlueprintKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.SortConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.dataprovider.GeoLocationDataProvider;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.BlueprintsWebKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.BlueprintsWebPortletKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.JSONConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.ResourceRequestKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferences;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferencesImpl;
import com.liferay.portal.search.tuning.blueprints.web.internal.util.BlueprintsLocalizationHelper;

import java.util.Arrays;
import java.util.Locale;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, 
	property = {
		"javax.portlet.name=" + BlueprintsWebPortletKeys.BLUEPRINTS_WEB,
		"mvc.command.name=/",

	}, 
	service = MVCRenderCommand.class
)
public class ViewMVCRenderCommand implements MVCRenderCommand {

	@Override
	public String render(
		RenderRequest renderRequest, RenderResponse renderResponse) {

		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences =
				new BlueprintsWebPortletPreferencesImpl(renderRequest.getPreferences());
		
		long blueprintId =  blueprintsWebPortletPreferences.getblueprintId();
		
		if (blueprintId == 0) {
			SessionErrors.add(
					renderRequest, "error.blueprint-not-defined");
			return "/view.jsp";
		}
		
		try {

			Blueprint blueprint = 
					_blueprintService.getBlueprint(blueprintId);
			
			JSONObject blueprintJsonObject = 
					JSONFactoryUtil.createJSONObject(blueprint.getConfiguration());

			renderRequest.setAttribute(
					BlueprintsWebKeys.CONFIGURATION, _getConfiguration(
							renderRequest, renderResponse, 
							blueprintJsonObject, blueprintsWebPortletPreferences));

		} catch (PortalException portalException) {
			SessionErrors.add(
					renderRequest, "unable-to-set-configuration");
			_log.error(portalException.getMessage(), portalException);
		}
		
		return "/view.jsp";

	}

	private String _createResourceURL(RenderResponse renderResponse, String resourceId) {

		ResourceURL portletURL = renderResponse.createResourceURL();

		portletURL.setResourceID(resourceId);

		return portletURL.toString();
	}

	private JSONObject _createResultLayoutOption(String icon, String text, String value, Locale locale) {

		JSONObject optionJsonObject =
				JSONFactoryUtil.createJSONObject();

		optionJsonObject.put(
				"icon", icon);
		optionJsonObject.put(
				"key", value);
		optionJsonObject.put(
				"text", _blueprintsLocalizationHelper.get(locale, text));
		optionJsonObject.put(
				"value", value);
		
		return optionJsonObject;
	}
	
	private JSONObject _getConfiguration(RenderRequest renderRequest, 
			RenderResponse renderResponse, JSONObject blueprintJsonObject,
			BlueprintsWebPortletPreferences blueprintsWebPortletPreferences) 
			throws JSONException {
		
		ThemeDisplay themeDisplay =
				(ThemeDisplay) renderRequest.getAttribute(
					WebKeys.THEME_DISPLAY);

		Locale locale = themeDisplay.getLocale();
		
		JSONObject configuration = JSONFactoryUtil.createJSONObject();

		configuration.put(JSONConfigurationKeys.CONVERSION_TRACKING_CONFIGURATION, _getConversionTrackingConfiguration(blueprintsWebPortletPreferences));		
		configuration.put(JSONConfigurationKeys.FILTER_CONFIGURATION, _getFilterConfiguration(blueprintsWebPortletPreferences));
		configuration.put(JSONConfigurationKeys.GOOGLE_MAPS_CONFIGURATION, _getGoogleMapsConfiguration(renderRequest, blueprintsWebPortletPreferences, locale));
		configuration.put(JSONConfigurationKeys.RESULT_ITEM_CONFIGURATION, _getResultItemConfiguration(blueprintsWebPortletPreferences));
		configuration.put(JSONConfigurationKeys.RESULT_LAYOUT_CONFIGURATION, _getResultLayoutConfiguration(blueprintsWebPortletPreferences, locale));
		configuration.put(JSONConfigurationKeys.SEARCHFIELD_CONFIGURATION, _getSearchFieldConfiguration(blueprintsWebPortletPreferences, locale));
		configuration.put(JSONConfigurationKeys.SORT_CONFIGURATION, _getSortConfiguration(blueprintJsonObject, locale));
		configuration.put(JSONConfigurationKeys.URL_CONFIGURATION, _getURLConfiguration(renderResponse, blueprintsWebPortletPreferences));
		
		return configuration;
	}
	
	private JSONObject _getConversionTrackingConfiguration(
			BlueprintsWebPortletPreferences blueprintsWebPortletPreferences) {

		JSONObject configurationJsonObject = JSONFactoryUtil.createJSONObject();

		configurationJsonObject.put(JSONConfigurationKeys.ENABLED, 
				blueprintsWebPortletPreferences.isConversionTrackingEnabled());
		
		return configurationJsonObject;
	}
	
	private JSONObject _getFilterConfiguration(
			BlueprintsWebPortletPreferences blueprintsWebPortletPreferences) {
		
		JSONObject configurationJsonObject = JSONFactoryUtil.createJSONObject();

		configurationJsonObject.put(JSONConfigurationKeys.SHOW_SCOPE_FILTER, 
				blueprintsWebPortletPreferences.isScopeFilterVisible());
		
		configurationJsonObject.put(JSONConfigurationKeys.SHOW_TIME_FILTER,
				blueprintsWebPortletPreferences.isTimeFilterVisible());
		
		configurationJsonObject.put(JSONConfigurationKeys.DATE_PICKER_DATE_FORMAT,
				blueprintsWebPortletPreferences.getDatePickerDateFormatString());		
		
		return configurationJsonObject;
	}
	
	private GeoLocationPoint _getGoogleMapsCenterPoint(
			RenderRequest renderRequest, Locale locale) {

		SearchParameterData searchParameterData = new SearchParameterData();

		HttpServletRequest httpServletRequest = 
				_portal.getHttpServletRequest(renderRequest);

		GeoLocationPoint geoLocationPoint = null;
		
		if (_geoLocationDataProvider != null) {
				_geoLocationDataProvider.getGeoLocationPoint(searchParameterData, 
						httpServletRequest.getRemoteAddr());
		}
		
		if (geoLocationPoint == null) {
			
			for (Message message : searchParameterData.getMessages()) {
				_log.warn(
						_blueprintsLocalizationHelper.get(
								locale, message.getMessageKey()), (Throwable)message.getException());
			}
		}
		
		return geoLocationPoint;
		
	}
	
	private JSONObject _getGoogleMapsConfiguration(
			RenderRequest renderRequest,
			BlueprintsWebPortletPreferences blueprintsWebPortletPreferences, Locale locale) {
		
		JSONObject configurationJsonObject = JSONFactoryUtil.createJSONObject();

		String apiKey = blueprintsWebPortletPreferences.getGoogleMapsAPIKeyString();

		configurationJsonObject.put(JSONConfigurationKeys.GMAP_API_KEY, 
				blueprintsWebPortletPreferences.getGoogleMapsAPIKeyString());

		if (Validator.isBlank(apiKey)) {
			return configurationJsonObject;
		}
		
		JSONObject center = JSONFactoryUtil.createJSONObject();

		GeoLocationPoint geoLocationPoint = _getGoogleMapsCenterPoint(renderRequest, locale);

		if (geoLocationPoint != null) {
			center.put("lat", geoLocationPoint.getLatitude());
			center.put("lng", geoLocationPoint.getLongitude());
			configurationJsonObject.put(JSONConfigurationKeys.GMAP_DEFAULT_CENTER, center);
		}

		return configurationJsonObject;
	}
	
	private JSONObject _getResultItemConfiguration(
			BlueprintsWebPortletPreferences blueprintsWebPortletPreferences) {
		
		JSONObject configurationJsonObject = JSONFactoryUtil.createJSONObject();
				
		configurationJsonObject.put(JSONConfigurationKeys.APPEND_REDIRECT, 
				blueprintsWebPortletPreferences.isAppendRedirectToURL());
		
		return configurationJsonObject;
	}
	
	private JSONObject _getResultLayoutConfiguration(
			BlueprintsWebPortletPreferences blueprintsWebPortletPreferences, Locale locale) {
		
		JSONObject configurationJsonObject = JSONFactoryUtil.createJSONObject();

		String[] resultLayouts = blueprintsWebPortletPreferences.getResultLayouts();

		JSONArray optionsJsonArray = JSONFactoryUtil.createJSONArray();

		if (Arrays.stream(resultLayouts).anyMatch("card"::equals)) {
			optionsJsonArray.put(_createResultLayoutOption("icon-card", "card-layout", "card", locale));
		} else 	if (Arrays.stream(resultLayouts).anyMatch("explain"::equals)) {
			optionsJsonArray.put(_createResultLayoutOption("icon-tools", "explain-layout", "explain", locale));
		} else if (Arrays.stream(resultLayouts).anyMatch("list"::equals)) {
			optionsJsonArray.put(_createResultLayoutOption("icon-list", "list-layout", "list", locale));
		} else 	if (Arrays.stream(resultLayouts).anyMatch("maps"::equals)) {
			optionsJsonArray.put(_createResultLayoutOption("icon-map", "maps-layout", "maps", locale));
		} else 	if (Arrays.stream(resultLayouts).anyMatch("raw"::equals)) {
			optionsJsonArray.put(_createResultLayoutOption("icon-tools", "raw-document-layout", "document", locale));
		} else 	if (Arrays.stream(resultLayouts).anyMatch("thumbnails"::equals)) {
			optionsJsonArray.put(_createResultLayoutOption("icon-layout", "thumbnail-layout", "thumbnail", locale));
		}
		
		configurationJsonObject.put(
				JSONConfigurationKeys.OPTIONS, optionsJsonArray);

		return configurationJsonObject;
	}

	private JSONObject _getSearchFieldConfiguration(
			BlueprintsWebPortletPreferences blueprintsWebPortletPreferences, Locale locale) {

		JSONObject configurationJsonObject = JSONFactoryUtil.createJSONObject();

		configurationJsonObject.put(JSONConfigurationKeys.QUERY_MIN_LENGTH, 
				blueprintsWebPortletPreferences.getQueryMinLength());
		configurationJsonObject.put(JSONConfigurationKeys.KEYWORDS_PLACEHOLDER, 
				blueprintsWebPortletPreferences.getKeywordPlaceholder(locale));
		configurationJsonObject.put(JSONConfigurationKeys.KEYWORD_SUGGESTER_ENABLED, 
				blueprintsWebPortletPreferences.isKeywordSuggesterEnabled());
		configurationJsonObject.put(JSONConfigurationKeys.KEYWORD_SUGGESTER_REQUEST_DELAY,
				blueprintsWebPortletPreferences.getKeywordSuggesterRequestDelay());
		
		return configurationJsonObject;
	}

	private JSONObject _getSortConfiguration(
			JSONObject blueprintJsonObject, Locale locale) 
			throws JSONException {
		
		JSONObject configurationJsonObject = JSONFactoryUtil.createJSONObject();

		if (!blueprintJsonObject.has(
				BlueprintKeys.SORT_CONFIGURATION.getJsonKey())) {
			
			configurationJsonObject.put(JSONConfigurationKeys.OPTIONS, 
					JSONFactoryUtil.createJSONArray());
			configurationJsonObject.put(JSONConfigurationKeys.DEFAULT_VALUE, 
					StringPool.BLANK);
			
			return configurationJsonObject;
		}
		
		JSONArray sortConfigurationJsonArray = 
				blueprintJsonObject.getJSONArray(
				BlueprintKeys.SORT_CONFIGURATION.getJsonKey());
		
		
		int currentPriority = -1;
		String defaultValue = "";
		
		JSONArray optionsJsonArray = JSONFactoryUtil.createJSONArray();

		for (int i = 0; i < sortConfigurationJsonArray.length(); i++) {

			JSONObject itemJsonObject = sortConfigurationJsonArray.getJSONObject(i);
			
			int priority = -1;
			
			if (i == 0) {
				priority = itemJsonObject.getInt(SortConfigurationKeys.DEFAULT_PRIORITY.getJsonKey());
				defaultValue = itemJsonObject.getString(SortConfigurationKeys.PARAMETER_NAME.getJsonKey());
			} else if (priority < currentPriority) {
				currentPriority = priority;
				defaultValue = itemJsonObject.getString(SortConfigurationKeys.PARAMETER_NAME.getJsonKey());
			}

			JSONObject optionJsonObject = JSONFactoryUtil.createJSONObject();

			optionJsonObject.put(JSONConfigurationKeys.VALUE, 
					itemJsonObject.getString(SortConfigurationKeys.PARAMETER_NAME.getJsonKey()));

			optionJsonObject.put(JSONConfigurationKeys.TEXT, 
					_blueprintsLocalizationHelper.get(
							locale, "sort-by-" + 
							itemJsonObject.getString(
									SortConfigurationKeys.PARAMETER_NAME.getJsonKey()).toLowerCase()));

			
			optionsJsonArray.put(optionJsonObject);
		}

		configurationJsonObject.put(
				JSONConfigurationKeys.OPTIONS, optionsJsonArray);
		configurationJsonObject.put(
				JSONConfigurationKeys.DEFAULT_VALUE, defaultValue);
		
		return configurationJsonObject;
	}
	
	private JSONObject _getURLConfiguration(
			RenderResponse renderResponse, 
			BlueprintsWebPortletPreferences blueprintsWebPortletPreferences) {
		
		JSONObject configurationJSONObject = JSONFactoryUtil.createJSONObject();

		configurationJSONObject.put(JSONConfigurationKeys.HELP_TEXT_URL, 
				_createResourceURL(renderResponse, 
						ResourceRequestKeys.GET_HELP_TEXT));		
		
		if (blueprintsWebPortletPreferences.isConversionTrackingEnabled()) {
			configurationJSONObject.put(JSONConfigurationKeys.CONVERSION_TRACKING_URL,
					_createResourceURL(renderResponse, 
							ResourceRequestKeys.TRACK_CONVERSION));
		}
		
		configurationJSONObject.put(JSONConfigurationKeys.SEARCH_RESULTS_URL,
				_createResourceURL(renderResponse, 
						ResourceRequestKeys.GET_SEARCH_RESULTS));

		configurationJSONObject.put(JSONConfigurationKeys.SUGGESTIONS_URL,
				_createResourceURL(renderResponse, 
						ResourceRequestKeys.GET_SUGGESTIONS));

		return configurationJSONObject;
	}
	
	private static final Log _log =
		LogFactoryUtil.getLog(ViewMVCRenderCommand.class);


	@Reference (cardinality=ReferenceCardinality.OPTIONAL)
	private GeoLocationDataProvider _geoLocationDataProvider;
	
	@Reference
	private BlueprintsLocalizationHelper _blueprintsLocalizationHelper;
	
	@Reference
	private Portal _portal;	

	@Reference
	private BlueprintService _blueprintService;
}