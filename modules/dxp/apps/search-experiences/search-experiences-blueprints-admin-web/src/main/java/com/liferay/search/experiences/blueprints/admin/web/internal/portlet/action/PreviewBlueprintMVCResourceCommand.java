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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
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
import com.liferay.search.experiences.blueprints.admin.web.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.search.experiences.blueprints.admin.web.internal.util.BlueprintsAdminRequestUtil;
import com.liferay.search.experiences.blueprints.constants.BlueprintsPortletKeys;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributesBuilder;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributesBuilderFactory;
import com.liferay.search.experiences.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.search.experiences.blueprints.engine.exception.BlueprintsEngineException;
import com.liferay.search.experiences.blueprints.engine.util.BlueprintsEngineHelper;
import com.liferay.search.experiences.blueprints.exception.BlueprintValidationException;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.service.BlueprintLocalService;
import com.liferay.search.experiences.blueprints.util.attributes.BlueprintsAttributesHelper;
import com.liferay.search.experiences.blueprints.validator.BlueprintValidator;
import com.liferay.search.experiences.searchresponse.json.translator.SearchResponseJSONTranslator;
import com.liferay.search.experiences.searchresponse.json.translator.constants.JSONKeys;
import com.liferay.search.experiences.searchresponse.json.translator.constants.ResponseAttributeKeys;

import java.util.ArrayList;
import java.util.List;
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
		"mvc.command.name=" + BlueprintsAdminMVCCommandNames.PREVIEW_BLUEPRINT
	},
	service = MVCResourceCommand.class
)
public class PreviewBlueprintMVCResourceCommand extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		JSONObject responseJSONObject = null;

		try {
			Blueprint blueprint = _getBlueprint(resourceRequest);

			_blueprintValidator.validateConfiguration(
				blueprint.getConfiguration());

			BlueprintsAttributes requestBlueprintsAttributes =
				_getRequestBlueprintsAttributes(resourceRequest, blueprint);

			Messages messages = new Messages();

			SearchResponse searchResponse = _blueprintsEngineHelper.search(
				blueprint, requestBlueprintsAttributes, messages);

			BlueprintsAttributes responseBlueprintsAttributes =
				_getResponseBlueprintsAttributes(
					resourceRequest, resourceResponse, blueprint,
					requestBlueprintsAttributes);

			responseJSONObject = _blueprintsJSONResponseBuilder.translate(
				searchResponse, blueprint, responseBlueprintsAttributes,
				_getResourceBundle(resourceRequest), messages);
		}
		catch (BlueprintsEngineException blueprintsEngineException) {
			_log.error(
				blueprintsEngineException.getMessage(),
				blueprintsEngineException);

			responseJSONObject =
				_blueprintsJSONResponseBuilder.translateErrorMessages(
					blueprintsEngineException.getMessages(),
					_getResourceBundle(resourceRequest));
		}
		catch (BlueprintValidationException blueprintValidationException) {
			_log.error(
				blueprintValidationException.getMessage(),
				blueprintValidationException);

			responseJSONObject =
				_blueprintsJSONResponseBuilder.translateErrorMessages(
					blueprintValidationException.getMessages(),
					_getResourceBundle(resourceRequest));
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);

			responseJSONObject = JSONUtil.put(
				JSONKeys.ERRORS, portalException.getMessage());
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

	private BlueprintsAttributes _getRequestBlueprintsAttributes(
		ResourceRequest resourceRequest, Blueprint blueprint) {

		BlueprintsAttributesBuilder blueprintsAttributesBuilder =
			_blueprintsAttributesHelper.getBlueprintsRequestAttributesBuilder(
				resourceRequest, blueprint);

		blueprintsAttributesBuilder.addAttribute(
			ReservedParameterNames.EXPLAIN.getKey(), true);

		blueprintsAttributesBuilder.addAttribute(
			ReservedParameterNames.INCLUDE_RESPONSE_STRING.getKey(), true);

		return blueprintsAttributesBuilder.build();
	}

	private ResourceBundle _getResourceBundle(ResourceRequest resourceRequest) {
		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		return ResourceBundleUtil.getBundle(
			"content.Language", themeDisplay.getLocale(), getClass());
	}

	private BlueprintsAttributes _getResponseBlueprintsAttributes(
		ResourceRequest resourceRequest, ResourceResponse resourceResponse,
		Blueprint blueprint, BlueprintsAttributes requestBlueprintsAttributes) {

		BlueprintsAttributesBuilder blueprintsAttributesBuilder =
			_blueprintsAttributesHelper.getBlueprintsResponseAttributesBuilder(
				resourceRequest, resourceResponse, blueprint,
				requestBlueprintsAttributes);

		blueprintsAttributesBuilder.addAttribute(
			ResponseAttributeKeys.INCLUDE_DOCUMENT, true);

		blueprintsAttributesBuilder.addAttribute(
			ResponseAttributeKeys.INCLUDE_REQUEST_STRING, true);

		blueprintsAttributesBuilder.addAttribute(
			ResponseAttributeKeys.INCLUDE_RESULT, true);

		blueprintsAttributesBuilder.addAttribute(
			ResponseAttributeKeys.RESULT_FIELDS, _getResultFields());

		return blueprintsAttributesBuilder.build();
	}

	private List<String> _getResultFields() {
		List<String> resultFields = new ArrayList<>();

		resultFields.add("id");
		resultFields.add("score");
		resultFields.add("b_assetEntryId");
		resultFields.add("b_author");
		resultFields.add("b_created");
		resultFields.add("b_modified");
		resultFields.add("b_summary");
		resultFields.add("b_title");
		resultFields.add("b_type");

		return resultFields;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		PreviewBlueprintMVCResourceCommand.class);

	@Reference
	private BlueprintLocalService _blueprintLocalService;

	@Reference
	private BlueprintsAttributesBuilderFactory
		_blueprintsAttributesBuilderFactory;

	@Reference
	private BlueprintsAttributesHelper _blueprintsAttributesHelper;

	@Reference
	private BlueprintsEngineHelper _blueprintsEngineHelper;

	@Reference
	private SearchResponseJSONTranslator _blueprintsJSONResponseBuilder;

	@Reference
	private BlueprintValidator _blueprintValidator;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Portal _portal;

}