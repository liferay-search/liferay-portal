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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.exception.SearchRequestDataException;
import com.liferay.portal.search.tuning.blueprints.engine.response.ResponseAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.util.SearchClientHelper;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.BlueprintsWebKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.BlueprintsWebPortletKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.ResourceRequestKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferences;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferencesImpl;
import com.liferay.portal.search.tuning.blueprints.web.internal.util.BlueprintsLocalizationHelper;

import java.util.Arrays;
import java.util.Locale;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

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

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
				WebKeys.THEME_DISPLAY);
		
		Locale locale = themeDisplay.getLocale();

		JSONObject responseJsonObject = null;

		HttpServletRequest httpServletRequest = _portal.getHttpServletRequest(resourceRequest);

		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences =
				new BlueprintsWebPortletPreferencesImpl(resourceRequest.getPreferences());

		String activeResultLayout = _getActiveResultLayout(resourceRequest, blueprintsWebPortletPreferences);

		ResponseAttributes resultAttributes = _getResultAttributes(blueprintsWebPortletPreferences, activeResultLayout, locale);
		
		long blueprintId = blueprintsWebPortletPreferences.getblueprintId();
		
		try {

			responseJsonObject = _searchClientHelper.search(
				httpServletRequest, resultAttributes, blueprintId);
			
			if (responseJsonObject.getJSONArray(JSONResponseKeys.ITEMS).length() > 0) {

				responseJsonObject.put(JSONResponseKeys.RESULT_LAYOUT, activeResultLayout);

				_localizeFacets(responseJsonObject, locale);

				_localizeResultItemType(responseJsonObject, locale);
			}
			
		}
		catch (JSONException jsonException) {
			
			_log.error(jsonException.getMessage(), jsonException);
			
			responseJsonObject = JSONFactoryUtil.createJSONObject();

			responseJsonObject.put(JSONResponseKeys.ERROR,
					jsonException.getMessage());

		}
		catch (SearchRequestDataException searchRequestDataException) {
			
			_log.error(searchRequestDataException.getMessage(), 
					searchRequestDataException);
			
			responseJsonObject = JSONFactoryUtil.createJSONObject();

			responseJsonObject.put(JSONResponseKeys.ERROR,
					searchRequestDataException.getMessage());
		}
		catch (PortalException portalException) {
			
			_log.error(portalException.getMessage(), portalException);
			
			responseJsonObject = JSONFactoryUtil.createJSONObject();

			responseJsonObject.put(JSONResponseKeys.ERROR,
					portalException.getMessage());
		}

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse, responseJsonObject);
	}
	

	/**
	 * Gets active result layout. 
	 * 
	 * Explain and raw document layouts are unavailable for 
	 * security reasons unless explicitly enabled.
	 * 
	 * @param resourceRequest
	 * @param blueprintsWebPortletPreferences
	 * @return
	 */
	private  String _getActiveResultLayout(ResourceRequest resourceRequest, 
			BlueprintsWebPortletPreferences blueprintsWebPortletPreferences) {

		String resultLayout =
			ParamUtil.getString(resourceRequest, BlueprintsWebKeys.RESULT_LAYOUT, "list");

		String[] enabledResultLayouts = blueprintsWebPortletPreferences.getResultLayouts();

		if (Arrays.stream(enabledResultLayouts).anyMatch(resultLayout::equals)) {
			return resultLayout;
		}

		return "list";
	}

	private ResponseAttributes _getResultAttributes(
			BlueprintsWebPortletPreferences blueprintsWebPortletPreferences, String activeResultLayout, Locale locale) {

		ResponseAttributes resultAttributes = new ResponseAttributes();

		String[] fieldsToDisplay = blueprintsWebPortletPreferences.getFieldsToDisplay();
		
		if (Arrays.stream(fieldsToDisplay).anyMatch("assetTags"::equals)) {
			resultAttributes.addAdditionalDocumentField(Field.ASSET_TAG_NAMES, true);
		}		

		if (Arrays.stream(fieldsToDisplay).anyMatch("assetCategories"::equals)) {
			StringBundler sb = new StringBundler(2);
			sb.append(Field.ASSET_CATEGORY_TITLES);
			sb.append("_");
			sb.append(locale.toString());
			
			resultAttributes.addAdditionalDocumentField(sb.toString(), true);
		}		

		if (Arrays.stream(fieldsToDisplay).anyMatch("userName"::equals)) {
			resultAttributes.addAdditionalDocumentField(Field.USER_NAME, false);
		}

		if (blueprintsWebPortletPreferences.isConversionTrackingEnabled()) {
			resultAttributes.addAdditionalDocumentField(Field.ENTRY_CLASS_PK, false);
		}
		
		if (Arrays.stream(fieldsToDisplay).anyMatch("userImage"::equals)) {
			resultAttributes.setIncludeUserPortrait(true);
		}

		if (activeResultLayout.equals("thumbnail") ||  
				activeResultLayout.equals("card")) {
			resultAttributes.setIncludeThumbnail(true);
		} else if (activeResultLayout.equals("document")) {
			resultAttributes.setIncludeRawDocument(true);
		} else if (activeResultLayout.equals("explain")) {
			resultAttributes.setIncludeExplanation(true);
		}		

		/*
		
		queryContext.setParameter(
			ParameterNames.VIEW_RESULTS_IN_CONTEXT,
			_moduleConfiguration.isViewResultsInContext());
		*/
				
		return resultAttributes;		
	}


	private void _localizeFacets(
		JSONObject searchResultsJsonObject, Locale locale) {
		
		if (!searchResultsJsonObject.has(JSONResponseKeys.FACETS)) {
			return;
		}
		
		JSONArray facetsJsonArray = 
				searchResultsJsonObject.getJSONArray(JSONResponseKeys.FACETS);
		
		for (int i = 0; i < facetsJsonArray.length(); i++) {

			JSONObject facetJsonObject = facetsJsonArray.getJSONObject(i);
			
			facetJsonObject.put(
				"anyOption", _blueprintsLocalizationHelper.
					get(locale, "any-" + facetJsonObject.getString(
						FacetConfigurationKeys.PARAMETER_NAME.getJsonKey()).toLowerCase()));
			
			JSONArray facetValuesJsonArray = facetJsonObject.getJSONArray(FacetConfigurationKeys.
					VALUE_AGGREGATION_VALUES.getJsonKey());

			for (int j = 0; j < facetValuesJsonArray.length(); j++) {

				JSONObject valueJsonObject = facetValuesJsonArray.getJSONObject(j);

				valueJsonObject.put(
				"text", _blueprintsLocalizationHelper.get(
					locale, valueJsonObject.getString(JSONResponseKeys.NAME).toLowerCase()) + 
					" (" + valueJsonObject.getString(JSONResponseKeys.FREQUENCY) + ")");

				valueJsonObject.put("text_",_blueprintsLocalizationHelper.get(
						locale, valueJsonObject.getString(JSONResponseKeys.NAME).toLowerCase()));
			}
		}
	}
	
	private void _localizeResultItemType (
		JSONObject searchResultsJsonObject, Locale locale) {

		if (!searchResultsJsonObject.has(JSONResponseKeys.ITEMS)) {
			return;
		}
		
		JSONArray itemsJsonArray = searchResultsJsonObject.getJSONArray(JSONResponseKeys.ITEMS);

		for (int i = 0; i < itemsJsonArray.length(); i++) {
			
			JSONObject itemJsonObject = itemsJsonArray.getJSONObject(i);

			itemsJsonArray.getJSONObject(i).put(
				"type", _blueprintsLocalizationHelper.get(
					locale, itemJsonObject.getString("type").toLowerCase()));
		}
	}	
	
	private static final Log _log =
			LogFactoryUtil.getLog(GetSearchResultsMVCResourceCommand.class);

	@Reference
	private BlueprintsLocalizationHelper _blueprintsLocalizationHelper;

	@Reference 
	private Portal _portal;
	
	@Reference
	private SearchClientHelper _searchClientHelper;
}
