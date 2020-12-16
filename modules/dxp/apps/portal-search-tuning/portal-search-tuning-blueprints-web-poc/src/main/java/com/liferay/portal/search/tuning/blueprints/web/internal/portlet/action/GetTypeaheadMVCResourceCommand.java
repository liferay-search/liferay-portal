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
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributesBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.exception.BlueprintsEngineException;
import com.liferay.portal.search.tuning.blueprints.engine.util.BlueprintsEngineHelper;
import com.liferay.portal.search.tuning.blueprints.json.response.BlueprintsJSONResponseBuilder;
import com.liferay.portal.search.tuning.blueprints.json.response.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.util.attributes.BlueprintsRequestAttributesHelper;
import com.liferay.portal.search.tuning.blueprints.util.attributes.BlueprintsResponseAttributesHelper;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.BlueprintsWebPortletKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.ResourceRequestKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferences;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferencesImpl;

import java.util.ResourceBundle;

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
		"mvc.command.name=" + ResourceRequestKeys.GET_TYPEAHEAD
	},
	service = MVCResourceCommand.class
)
public class GetTypeaheadMVCResourceCommand extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		long blueprintId = _getTypeaheadBlueprintId(resourceRequest);

		if (blueprintId == 0) {
			return;
		}

		JSONObject responseJSONObject = null;

		try {
			Messages requestMessages = new Messages();

			BlueprintsAttributes blueprintsRequestAttributes =
				_getBlueprintsRequestAttributes(resourceRequest, blueprintId);

			SearchResponse searchResponse = _blueprintsEngineHelper.search(
				blueprintsRequestAttributes, requestMessages, blueprintId);

			BlueprintsAttributes blueprintsResponseAttributes =
				_getBlueprintsResponseAttributes(
					resourceRequest, resourceResponse,
					blueprintsRequestAttributes, blueprintId);

			Messages responseMessages = new Messages();

			ThemeDisplay themeDisplay =
				(ThemeDisplay)resourceRequest.getAttribute(
					WebKeys.THEME_DISPLAY);

			ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
				"content.Language", themeDisplay.getLocale(), getClass());

			responseJSONObject = _blueprintsResponseBuilder.buildJSONObject(
				searchResponse, blueprintsResponseAttributes, resourceBundle,
				responseMessages, blueprintId);
		}
		catch (JSONException jsonException) {
			_log.error(jsonException.getMessage(), jsonException);

			responseJSONObject = JSONUtil.put(
				JSONResponseKeys.ERRORS, jsonException.getMessage());
		}
		catch (BlueprintsEngineException blueprintsEngineException) {
			_log.error(
				blueprintsEngineException.getMessage(),
				blueprintsEngineException);

			responseJSONObject = JSONUtil.put(
				JSONResponseKeys.ERRORS,
				blueprintsEngineException.getMessage());
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);

			responseJSONObject = JSONUtil.put(
				JSONResponseKeys.ERRORS, portalException.getMessage());
		}

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse, responseJSONObject);
	}

	private BlueprintsAttributes _getBlueprintsRequestAttributes(
		ResourceRequest resourceRequest, long blueprintId) {

		BlueprintsAttributesBuilder blueprintsAttributesBuilder =
			_blueprintsRequestAttributesHelper.
				getBlueprintsRequestAttributesBuilder(
					resourceRequest, blueprintId);

		return blueprintsAttributesBuilder.build();
	}

	private BlueprintsAttributes _getBlueprintsResponseAttributes(
		ResourceRequest resourceRequest, ResourceResponse resourceResponse,
		BlueprintsAttributes blueprintsRequestAttributes, long blueprintId) {

		BlueprintsAttributesBuilder blueprintsAttributesBuilder =
			_blueprintsResponseAttributesHelper.
				getBlueprintsResponseAttributesBuilder(
					resourceRequest, resourceResponse,
					blueprintsRequestAttributes, blueprintId);

		return blueprintsAttributesBuilder.build();
	}

	private long _getTypeaheadBlueprintId(ResourceRequest resourceRequest) {
		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences =
			new BlueprintsWebPortletPreferencesImpl(
				resourceRequest.getPreferences());

		return blueprintsWebPortletPreferences.getTypeaheadBlueprintId();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		GetTypeaheadMVCResourceCommand.class);

	@Reference
	private BlueprintsEngineHelper _blueprintsEngineHelper;

	@Reference
	private BlueprintsRequestAttributesHelper
		_blueprintsRequestAttributesHelper;

	@Reference
	private BlueprintsResponseAttributesHelper
		_blueprintsResponseAttributesHelper;

	@Reference
	private BlueprintsJSONResponseBuilder _blueprintsResponseBuilder;

	@Reference
	private Portal _portal;

}