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

package com.liferay.search.experiences.blueprints.admin.web.internal.portlet.action;

import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.search.experiences.blueprints.admin.web.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.search.experiences.blueprints.admin.web.internal.util.BlueprintsAdminRequestUtil;
import com.liferay.search.experiences.blueprints.constants.BlueprintsPortletKeys;
import com.liferay.search.experiences.blueprints.exception.ElementValidationException;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.service.BlueprintLocalService;
import com.liferay.search.experiences.blueprints.validator.ElementValidator;
import com.liferay.search.experiences.searchresponse.json.translator.SearchResponseJSONTranslator;

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
		"javax.portlet.name=" + BlueprintsPortletKeys.BLUEPRINTS_ADMIN,
		"mvc.command.name=" + BlueprintsAdminMVCCommandNames.VALIDATE_ELEMENT
	},
	service = MVCResourceCommand.class
)
public class ValidateElementMVCResourceCommand extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		JSONObject responseJSONObject = _jsonFactory.createJSONObject();

		try {
			Blueprint blueprint = _getBlueprint(resourceRequest);

			_elementValidator.validateConfiguration(
				blueprint.getConfiguration(),
				BlueprintsAdminRequestUtil.getElementType(resourceRequest));
		}
		catch (ElementValidationException elementValidationException) {
			_log.error(
				elementValidationException.getMessage(),
				elementValidationException);

			responseJSONObject =
				_blueprintsJSONResponseBuilder.translateErrorMessages(
					elementValidationException.getMessages(),
					_getResourceBundle(resourceRequest));
		}

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse, responseJSONObject);
	}

	private Blueprint _getBlueprint(ResourceRequest resourceRequest) {
		Blueprint blueprint = _blueprintLocalService.createBlueprint(0L);

		blueprint.setConfiguration(
			BlueprintsAdminRequestUtil.getConfiguration(resourceRequest));

		return blueprint;
	}

	private ResourceBundle _getResourceBundle(ResourceRequest resourceRequest) {
		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		return ResourceBundleUtil.getBundle(
			"content.Language", themeDisplay.getLocale(), getClass());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ValidateElementMVCResourceCommand.class);

	@Reference
	private BlueprintLocalService _blueprintLocalService;

	@Reference
	private SearchResponseJSONTranslator _blueprintsJSONResponseBuilder;

	@Reference
	private ElementValidator _elementValidator;

	@Reference
	private JSONFactory _jsonFactory;

}