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

package com.liferay.portal.search.tuning.blueprints.util.internal.attributes;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributesBuilder;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributesBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.parameter.CustomParameterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.parameter.ParameterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetsBlueprintContributorKeys;
import com.liferay.portal.search.tuning.blueprints.misspellings.processor.MisspellingsProcessor;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;
import com.liferay.portal.search.tuning.blueprints.util.attributes.BlueprintsAttributesHelper;
import com.liferay.portal.search.tuning.blueprints.util.internal.util.AttributesUtil;

import java.util.Optional;
import java.util.TimeZone;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintsAttributesHelper.class)
public class BlueprintsAttributesHelperImpl
	implements BlueprintsAttributesHelper {

	@Override
	public BlueprintsAttributesBuilder getBlueprintsRequestAttributesBuilder(
		PortletRequest portletRequest, long blueprintId) {

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		HttpServletRequest httpServletRequest = _portal.getHttpServletRequest(
			portletRequest);

		TimeZone timeZone = themeDisplay.getTimeZone();

		BlueprintsAttributesBuilder blueprintsAttributesBuilder =
			_blueprintsAttributesBuilderFactory.builder();

		blueprintsAttributesBuilder.companyId(
			themeDisplay.getCompanyId()
		).locale(
			themeDisplay.getLocale()
		).userId(
			themeDisplay.getUserId()
		).addAttribute(
			ReservedParameterNames.IP_ADDRESS.getKey(),
			httpServletRequest.getRemoteAddr()
		).addAttribute(
			ReservedParameterNames.PLID.getKey(), themeDisplay.getPlid()
		).addAttribute(
			ReservedParameterNames.SCOPE_GROUP_ID.getKey(),
			themeDisplay.getScopeGroupId()
		).addAttribute(
			ReservedParameterNames.TIMEZONE_ID.getKey(), timeZone.getID()
		);

		return _addRequestParameters(
			portletRequest, blueprintsAttributesBuilder, blueprintId);
	}

	@Override
	public BlueprintsAttributesBuilder getBlueprintsResponseAttributesBuilder(
		PortletRequest portletRequest, PortletResponse portletResponse,
		BlueprintsAttributes blueprintsRequestAttributes, long blueprintId) {

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		BlueprintsAttributesBuilder blueprintsAttributesBuilder =
			_blueprintsAttributesBuilderFactory.builder();

		blueprintsAttributesBuilder.companyId(
			themeDisplay.getCompanyId()
		).keywords(
			blueprintsRequestAttributes.getKeywords()
		).locale(
			themeDisplay.getLocale()
		).userId(
			themeDisplay.getUserId()
		).addAttribute(
			"portletRequest", portletRequest
		).addAttribute(
			"portletResponse", portletResponse
		);

		_addShowingInsteadOf(
			blueprintsAttributesBuilder, blueprintsRequestAttributes);

		return blueprintsAttributesBuilder;
	}

	private void _addCustomParameters(
		PortletRequest portletRequest,
		BlueprintsAttributesBuilder blueprintsAttributesBuilder,
		JSONObject configurationJSONObject) {

		JSONArray jsonArray = configurationJSONObject.getJSONArray(
			ParameterConfigurationKeys.CUSTOM.getJsonKey());

		if ((jsonArray == null) || (jsonArray.length() == 0)) {
			return;
		}

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);

			String key = jsonObject.getString(
				CustomParameterConfigurationKeys.PARAMETER_NAME.getJsonKey());
			String type = jsonObject.getString(
				CustomParameterConfigurationKeys.TYPE.getJsonKey());

			if (_isArrayValue(type)) {
				_addStringValues(
					portletRequest, blueprintsAttributesBuilder, key);
			}
			else {
				_addStringValue(
					portletRequest, blueprintsAttributesBuilder, key);
			}
		}
	}

	private void _addFacetParameters(
		PortletRequest portletRequest,
		BlueprintsAttributesBuilder blueprintsAttributesBuilder,
		Blueprint blueprint) {

		Optional<JSONArray> configurationJSONArrayOptional =
			_blueprintHelper.getJSONArrayConfigurationOptional(
				blueprint,
				"JSONArray/" +
					FacetsBlueprintContributorKeys.CONFIGURATION_SECTION);

		if (!configurationJSONArrayOptional.isPresent()) {
			return;
		}

		JSONArray configurationJSONArray = configurationJSONArrayOptional.get();

		for (int i = 0; i < configurationJSONArray.length(); i++) {
			JSONObject jsonObject = configurationJSONArray.getJSONObject(i);

			String key = jsonObject.getString(
				FacetConfigurationKeys.PARAMETER_NAME.getJsonKey());

			boolean arrayValue = jsonObject.getBoolean(
				FacetConfigurationKeys.MULTI_VALUE.getJsonKey(), true);

			if (arrayValue) {
				_addStringValues(
					portletRequest, blueprintsAttributesBuilder, key);
			}
			else {
				_addStringValue(
					portletRequest, blueprintsAttributesBuilder, key);
			}
		}
	}

	private void _addKeywordParameter(
		PortletRequest portletRequest,
		BlueprintsAttributesBuilder blueprintsAttributesBuilder,
		Blueprint blueprint, JSONObject configurationJSONObject) {

		String parameterName = AttributesUtil.getKeywordParameterName(
			configurationJSONObject);

		String keywords = ParamUtil.getString(portletRequest, parameterName);

		if (Validator.isBlank(keywords)) {
			return;
		}

		String misspellCheckedWords = _processMisspellings(
			portletRequest, keywords, blueprint);

		if (!keywords.equals(misspellCheckedWords)) {
			blueprintsAttributesBuilder.addAttribute(
				parameterName, misspellCheckedWords);
			blueprintsAttributesBuilder.addAttribute(
				ReservedParameterNames.SHOWING_INSTEAD_OF.getKey(), keywords);
			blueprintsAttributesBuilder.keywords(misspellCheckedWords);
		}
		else {
			blueprintsAttributesBuilder.addAttribute(parameterName, keywords);
			blueprintsAttributesBuilder.keywords(keywords);
		}
	}

	private void _addPagingParameter(
		PortletRequest portletRequest,
		BlueprintsAttributesBuilder blueprintsAttributesBuilder,
		JSONObject configurationJSONObject) {

		String parameterName = AttributesUtil.getPageParameterName(
			configurationJSONObject);

		_addStringValue(
			portletRequest, blueprintsAttributesBuilder, parameterName);
	}

	private BlueprintsAttributesBuilder _addRequestParameters(
		PortletRequest portletRequest,
		BlueprintsAttributesBuilder blueprintsAttributesBuilder,
		long blueprintId) {

		Blueprint blueprint = null;

		try {
			blueprint = _getBlueprint(blueprintId);
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);

			return blueprintsAttributesBuilder;
		}
		catch (Exception exception) {
			_log.error(exception.getMessage(), exception);

			return blueprintsAttributesBuilder;
		}

		Optional<JSONObject> configurationJSONObjectOptional =
			_blueprintHelper.getParameterConfigurationOptional(blueprint);

		if (!configurationJSONObjectOptional.isPresent()) {
			return blueprintsAttributesBuilder;
		}

		JSONObject configurationJSONObject =
			configurationJSONObjectOptional.get();

		_addKeywordParameter(
			portletRequest, blueprintsAttributesBuilder, blueprint,
			configurationJSONObject);

		_addPagingParameter(
			portletRequest, blueprintsAttributesBuilder,
			configurationJSONObject);

		_addSortParameters(
			portletRequest, blueprintsAttributesBuilder, blueprint);

		_addCustomParameters(
			portletRequest, blueprintsAttributesBuilder,
			configurationJSONObject);

		_addFacetParameters(
			portletRequest, blueprintsAttributesBuilder, blueprint);

		return blueprintsAttributesBuilder;
	}

	private void _addShowingInsteadOf(
		BlueprintsAttributesBuilder blueprintsAttributesBuilder,
		BlueprintsAttributes blueprintsRequestAttributes) {

		Optional<Object> showingInsteadOfOptional =
			blueprintsRequestAttributes.getAttributeOptional(
				ReservedParameterNames.SHOWING_INSTEAD_OF.getKey());

		if (showingInsteadOfOptional.isPresent()) {
			blueprintsAttributesBuilder.addAttribute(
				ReservedParameterNames.SHOWING_INSTEAD_OF.getKey(),
				GetterUtil.getString(showingInsteadOfOptional.get()));
		}
	}

	private void _addSortParameters(
		PortletRequest portletRequest,
		BlueprintsAttributesBuilder blueprintsAttributesBuilder,
		Blueprint blueprint) {

		Optional<JSONArray> jsonArrayOptional =
			_blueprintHelper.getSortParameterConfigurationOptional(blueprint);

		if (!jsonArrayOptional.isPresent()) {
			return;
		}

		JSONArray jsonArray = jsonArrayOptional.get();

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);

			String key = jsonObject.getString(
				CustomParameterConfigurationKeys.PARAMETER_NAME.getJsonKey());

			_addStringValue(portletRequest, blueprintsAttributesBuilder, key);
		}
	}

	private void _addStringValue(
		PortletRequest portletRequest,
		BlueprintsAttributesBuilder blueprintsAttributesBuilder, String key) {

		String s = ParamUtil.getString(portletRequest, key);

		if (!Validator.isBlank(s)) {
			blueprintsAttributesBuilder.addAttribute(key, s);
		}
	}

	private void _addStringValues(
		PortletRequest portletRequest,
		BlueprintsAttributesBuilder blueprintsAttributesBuilder, String key) {

		String[] arr = ParamUtil.getStringValues(portletRequest, key);

		if ((arr != null) && (arr.length > 0)) {
			blueprintsAttributesBuilder.addAttribute(key, arr);
		}
	}

	private boolean _allowMisspellings(PortletRequest portletRequest) {
		return ParamUtil.getBoolean(
			portletRequest, ReservedParameterNames.ALLOW_MISSPELLINGS.getKey());
	}

	private Blueprint _getBlueprint(long blueprintId) throws PortalException {
		return _blueprintService.getBlueprint(blueprintId);
	}

	private String[] _getMisspellingSetIds(Blueprint blueprint) {
		Optional<JSONArray> configurationJSONArrayOptional =
			_blueprintHelper.getMisspellingSetIdsOptional(blueprint);

		if (!configurationJSONArrayOptional.isPresent()) {
			return null;
		}

		JSONArray configurationJSONArray = configurationJSONArrayOptional.get();

		return JSONUtil.toStringArray(configurationJSONArray);
	}

	private boolean _isArrayValue(String type) {
		if (StringUtil.equals(type, "string_array") ||
			StringUtil.equals(type, "integer_array") ||
			StringUtil.equals(type, "long_array")) {

			return true;
		}

		return false;
	}

	private String _processMisspellings(
		PortletRequest portletRequest, String keywords, Blueprint blueprint) {

		if (_allowMisspellings(portletRequest) ||
			(_misspellingsProcessor == null)) {

			return keywords;
		}

		String[] misspellingSetIds = _getMisspellingSetIds(blueprint);

		if (misspellingSetIds == null) {
			return keywords;
		}

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		return _misspellingsProcessor.process(
			themeDisplay.getCompanyId(), misspellingSetIds,
			new String[] {themeDisplay.getLanguageId()}, keywords);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintsAttributesHelperImpl.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintsAttributesBuilderFactory
		_blueprintsAttributesBuilderFactory;

	@Reference
	private BlueprintService _blueprintService;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	private volatile MisspellingsProcessor _misspellingsProcessor;

	@Reference
	private Portal _portal;

}