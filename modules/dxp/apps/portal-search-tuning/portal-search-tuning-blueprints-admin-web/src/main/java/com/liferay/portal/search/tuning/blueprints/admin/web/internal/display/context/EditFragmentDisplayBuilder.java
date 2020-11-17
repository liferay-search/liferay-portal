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

package com.liferay.portal.search.tuning.blueprints.admin.web.internal.display.context;

import com.liferay.exportimport.kernel.exception.NoSuchConfigurationException;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminWebKeys;
import com.liferay.portal.search.tuning.blueprints.constants.BlueprintTypes;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Kevin Tan
 */
public class EditFragmentDisplayBuilder {

	public EditFragmentDisplayBuilder(
		HttpServletRequest httpServletRequest, Language language, Log log,
		JSONFactory jsonFactory, RenderRequest renderRequest,
		RenderResponse renderResponse, BlueprintService blueprintService) {

		_httpServletRequest = httpServletRequest;
		_language = language;
		_log = log;
		_jsonFactory = jsonFactory;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;
		_blueprintService = blueprintService;

		_blueprintId = ParamUtil.getLong(
			renderRequest, BlueprintsAdminWebKeys.BLUEPRINT_ID);
		_blueprintType = ParamUtil.getInteger(
			renderRequest, BlueprintsAdminWebKeys.BLUEPRINT_TYPE,
			BlueprintTypes.QUERY_FRAGMENT);

		_blueprint = _getBlueprint();

		_themeDisplay = (ThemeDisplay)_httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public EditFragmentDisplayContext build() {
		EditFragmentDisplayContext editFragmentDisplayContext =
			new EditFragmentDisplayContext();

		_setConfigurationId(editFragmentDisplayContext);
		_setConfigurationType(editFragmentDisplayContext);
		_setData(editFragmentDisplayContext);
		_setPageTitle(editFragmentDisplayContext);
		_setRedirect(editFragmentDisplayContext);

		return editFragmentDisplayContext;
	}

	private Blueprint _getBlueprint() {
		Blueprint blueprint = null;

		if (_blueprintId > 0) {
			try {
				blueprint = _blueprintService.getBlueprint(_blueprintId);

				_blueprintType = blueprint.getType();
			}
			catch (NoSuchConfigurationException noSuchConfigurationException) {
				_log.error(
					"Blueprint " + _blueprintId + " not found.",
					noSuchConfigurationException);

				SessionErrors.add(
					_renderRequest, BlueprintsAdminWebKeys.ERROR_DETAILS,
					noSuchConfigurationException);
			}
			catch (PortalException portalException) {
				_log.error(portalException.getMessage(), portalException);

				SessionErrors.add(
					_renderRequest, BlueprintsAdminWebKeys.ERROR_DETAILS,
					portalException);
			}
		}

		return blueprint;
	}

	private Map<String, Object> _getContext() {
		return HashMapBuilder.<String, Object>put(
			"defaultLocale", LocaleUtil.toLanguageId(LocaleUtil.getDefault())
		).put(
			"locale", _themeDisplay.getLanguageId()
		).put(
			"namespace", _renderResponse.getNamespace()
		).build();
	}

	private JSONObject _getDescriptionJSONObject() {
		Map<Locale, String> descriptionMap = _blueprint.getDescriptionMap();

		JSONObject descriptionJSONObject = _jsonFactory.createJSONObject();

		descriptionMap.forEach(
			(key, value) -> descriptionJSONObject.put(
				StringUtil.replace(key.toString(), '_', "-"), value));

		return descriptionJSONObject;
	}

	private List<JSONObject> _getPredefinedVariables() {

		// TODO This is a placeholder for LPS-123115 to get predefinedVariables

		JSONObject parameterJSONObject = _jsonFactory.createJSONObject();

		return ListUtil.fromArray(parameterJSONObject);
	}

	private Map<String, Object> _getProps() {
		Map<String, Object> props = HashMapBuilder.<String, Object>put(
			"blueprintId", _blueprintId
		).put(
			"blueprintType", _blueprintType
		).put(
			"redirectURL", _getRedirect()
		).put(
			"submitFormURL", _getSubmitFormURL()
		).build();

		if (_blueprint != null) {
			props.put(
				"initialConfigurationString", _blueprint.getConfiguration());
			props.put("initialDescription", _getDescriptionJSONObject());
			props.put("initialTitle", _getTitleJSONObject());

			// TODO This is a placeholder for LPS-123115 to get predefinedVariables
			// props.put("predefinedVariables", _getPredefinedVariables());

		}

		return props;
	}

	private String _getRedirect() {
		String redirect = ParamUtil.getString(_httpServletRequest, "redirect");

		if (Validator.isNull(redirect)) {
			redirect = String.valueOf(_renderResponse.createRenderURL());
		}

		return redirect;
	}

	private String _getSubmitFormURL() {
		ActionURL actionURL = _renderResponse.createActionURL();

		actionURL.setParameter(
			ActionRequest.ACTION_NAME,
			BlueprintsAdminMVCCommandNames.EDIT_FRAGMENT);
		actionURL.setParameter(
			Constants.CMD,
			(_blueprint != null) ? Constants.EDIT : Constants.ADD);
		actionURL.setParameter("redirect", _getRedirect());

		return actionURL.toString();
	}

	private JSONObject _getTitleJSONObject() {
		Map<Locale, String> titleMap = _blueprint.getTitleMap();

		JSONObject titleJSONObject = _jsonFactory.createJSONObject();

		titleMap.forEach(
			(key, value) -> titleJSONObject.put(
				StringUtil.replace(key.toString(), '_', "-"), value));

		return titleJSONObject;
	}

	private void _setConfigurationId(
		EditFragmentDisplayContext editFragmentDisplayContext) {

		editFragmentDisplayContext.setBlueprintId(_blueprintId);
	}

	private void _setConfigurationType(
		EditFragmentDisplayContext editFragmentDisplayContext) {

		editFragmentDisplayContext.setBlueprintType(_blueprintType);
	}

	private void _setData(
		EditFragmentDisplayContext editFragmentDisplayContext) {

		editFragmentDisplayContext.setData(
			HashMapBuilder.<String, Object>put(
				"context", _getContext()
			).put(
				"props", _getProps()
			).build());
	}

	private void _setPageTitle(
		EditFragmentDisplayContext editFragmentDisplayContext) {

		StringBundler sb = new StringBundler(2);

		sb.append((_blueprint != null) ? "edit-" : "add-");

		sb.append("query-fragment");

		editFragmentDisplayContext.setPageTitle(
			_language.get(_httpServletRequest, sb.toString()));
	}

	private void _setRedirect(
		EditFragmentDisplayContext editFragmentDisplayContext) {

		editFragmentDisplayContext.setRedirect(_getRedirect());
	}

	private final Blueprint _blueprint;
	private final long _blueprintId;
	private final BlueprintService _blueprintService;
	private int _blueprintType;
	private final HttpServletRequest _httpServletRequest;
	private final JSONFactory _jsonFactory;
	private final Language _language;
	private final Log _log;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final ThemeDisplay _themeDisplay;

}