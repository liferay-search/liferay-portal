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
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminWebKeys;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.util.JSONHelperUtil;
import com.liferay.portal.search.tuning.blueprints.constants.BlueprintTypes;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.BlueprintKeys;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.portlet.ActionRequest;
import javax.portlet.ActionURL;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Kevin Tan
 */
public class EditBlueprintDisplayBuilder {

	public EditBlueprintDisplayBuilder(
		HttpServletRequest httpServletRequest, Language language, Log log,
		JSONFactory jsonFactory, RenderRequest renderRequest,
		RenderResponse renderResponse,
		BlueprintService blueprintService) {

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
			BlueprintTypes.BLUEPRINT);

		_blueprint = _getBlueprint();

		_themeDisplay = (ThemeDisplay)_httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public EditBlueprintDisplayContext build() {
		EditBlueprintDisplayContext
			editBlueprintDisplayContext =
				new EditBlueprintDisplayContext();

		_setConfigurationId(editBlueprintDisplayContext);
		_setConfigurationType(editBlueprintDisplayContext);
		_setData(editBlueprintDisplayContext);
		_setPageTitle(editBlueprintDisplayContext);
		_setRedirect(editBlueprintDisplayContext);

		return editBlueprintDisplayContext;
	}

	private JSONArray _getAvailableLocales() {
		JSONArray jsonArray = _jsonFactory.createJSONArray();

		Set<Locale> locales = _language.getAvailableLocales();

		Stream<Locale> stream = locales.stream();

		stream.map(
			this::_getLocaleJSONObject
		).forEach(
			jsonArray::put
		);

		return jsonArray;
	}

	private Map<String, Object> _getContext() {
		return HashMapBuilder.<String, Object>put(
			"locale", _themeDisplay.getLanguageId()
		).put(
			"namespace", _renderResponse.getNamespace()
		).build();
	}

	private JSONObject _getLocaleJSONObject(Locale locale) {
		JSONObject jsonObject = _jsonFactory.createJSONObject();

		String languageId = LocaleUtil.toLanguageId(locale);

		jsonObject.put(
			"label", StringUtil.replace(languageId, '_', "-")
		).put(
			"symbol",
			StringUtil.replace(
				languageId, '_', "-"
			).toLowerCase()
		);

		return jsonObject;
	}

	private Map<String, Object> _getProps() {
		Map<String, Object> props = HashMapBuilder.<String, Object>put(
			"availableLocales", _getAvailableLocales()
		).put(
			"blueprintId", _blueprintId
		).put(
			"blueprintType", _blueprintType
		).put(
			"redirectURL", _getRedirect()
		).put(
			"submitFormURL", _getSubmitFormURL()
		).build();

		if (_blueprint != null) {
			try {
				props.put(
					"initialClauseConfiguration",
					JSONHelperUtil.getConfigurationSection(
						_blueprint,
						BlueprintKeys.CLAUSE_CONFIGURATION.
							getJsonKey()));
			}
			catch (JSONException jsonException) {
				_log.error(
					"Unable to parse Blueprint JSON", jsonException);
			}

			props.put("initialTitle", _getTitle());
		}

		return props;
	}

	private String _getRedirect() {
		String redirect = ParamUtil.getString(_httpServletRequest, "redirect");

		if (Validator.isNull(redirect)) {
			PortletURL portletURL = _renderResponse.createRenderURL();

			redirect = portletURL.toString();
		}

		return redirect;
	}

	private Blueprint _getBlueprint() {
		Blueprint blueprint = null;

		if (_blueprintId > 0) {
			try {
				blueprint =
					_blueprintService.getBlueprint(
						_blueprintId);

				_blueprintType = blueprint.getType();
			}
			catch (NoSuchConfigurationException noSuchConfigurationException) {
				_log.error(
					"Blueprint " + _blueprintId +
						" not found.",
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

	private String _getSubmitFormURL() {
		ActionURL actionURL = _renderResponse.createActionURL();

		actionURL.setParameter(
			ActionRequest.ACTION_NAME,
			BlueprintsAdminMVCCommandNames.EDIT_BLUEPRINT);
		actionURL.setParameter(
			Constants.CMD,
			(_blueprint != null) ? Constants.EDIT : Constants.ADD);
		actionURL.setParameter("redirect", _getRedirect());

		return actionURL.toString();
	}

	private JSONObject _getTitle() {
		Map<Locale, String> titleMap = _blueprint.getTitleMap();

		JSONObject titleJSONObject = _jsonFactory.createJSONObject();

		titleMap.forEach(
			(key, value) -> titleJSONObject.put(
				StringUtil.replace(key.toString(), '_', "-"), value));

		return titleJSONObject;
	}

	private void _setConfigurationId(
		EditBlueprintDisplayContext
			editBlueprintDisplayContext) {

		editBlueprintDisplayContext.setBlueprintId(
			_blueprintId);
	}

	private void _setConfigurationType(
		EditBlueprintDisplayContext
			editBlueprintDisplayContext) {

		editBlueprintDisplayContext.setBlueprintType(
			_blueprintType);
	}

	private void _setData(
		EditBlueprintDisplayContext
			editBlueprintDisplayContext) {

		editBlueprintDisplayContext.setData(
			HashMapBuilder.<String, Object>put(
				"context", _getContext()
			).put(
				"props", _getProps()
			).build());
	}

	private void _setPageTitle(
		EditBlueprintDisplayContext
			editBlueprintDisplayContext) {

		StringBundler sb = new StringBundler(2);

		sb.append(_blueprint != null ? "edit-" : "add-");

		if (_blueprintType == BlueprintTypes.BLUEPRINT) {
			sb.append("blueprint");
		}
		else if (_blueprintType == BlueprintTypes.AGGREGATION_FRAGMENT) {
			sb.append("aggregation-fragment");
		}
		else if (_blueprintType == BlueprintTypes.QUERY_FRAGMENT) {
			sb.append("query-fragment");
		}
		else if (_blueprintType == BlueprintTypes.TEMPLATE) {
			sb.append("template");
		}
		
		editBlueprintDisplayContext.setPageTitle(
			_language.get(_httpServletRequest, sb.toString()));
	}

	private void _setRedirect(
		EditBlueprintDisplayContext
			editBlueprintDisplayContext) {

		editBlueprintDisplayContext.setRedirect(_getRedirect());
	}

	private final HttpServletRequest _httpServletRequest;
	private final JSONFactory _jsonFactory;
	private final Language _language;
	private final Log _log;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final Blueprint _blueprint;
	private final long _blueprintId;
	private final BlueprintService _blueprintService;
	private int _blueprintType;
	private final ThemeDisplay _themeDisplay;

}