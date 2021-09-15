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

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.admin.web.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.search.experiences.blueprints.admin.web.internal.util.BlueprintsAdminRequestUtil;
import com.liferay.search.experiences.blueprints.constants.BlueprintsPortletKeys;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributesBuilder;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributesBuilderFactory;
import com.liferay.search.experiences.blueprints.engine.exception.BlueprintsEngineException;
import com.liferay.search.experiences.blueprints.engine.portlet.attributes.BlueprintsAttributesHelper;
import com.liferay.search.experiences.blueprints.engine.util.BlueprintsEngineHelper;
import com.liferay.search.experiences.blueprints.exception.BlueprintValidationException;
import com.liferay.search.experiences.blueprints.service.BlueprintLocalService;
import com.liferay.search.experiences.blueprints.validator.BlueprintValidator;
import com.liferay.search.experiences.problems.Problem;
import com.liferay.search.experiences.problems.ProblemsHolder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilderFactory;
import com.liferay.search.experiences.searchresponse.json.translator.SearchResponseJSONTranslator;
import com.liferay.search.experiences.searchresponse.json.translator.constants.ResponseAttributeKeys;

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

	public static class PreviewBlueprint implements Blueprint {

		public PreviewBlueprint(String configuration) {
			_configuration = configuration;
		}

		@Override
		public long getBlueprintId() {
			return 0;
		}

		@Override
		public String getConfiguration() {
			return _configuration;
		}

		private final String _configuration;

	}

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse,
			getResponseJSONString(resourceRequest, resourceResponse));
	}

	protected String getResponseJSONString(
		ResourceRequest resourceRequest, ResourceResponse resourceResponse) {

		try {
			Blueprint blueprint = new PreviewBlueprint(
				BlueprintsAdminRequestUtil.getConfiguration(resourceRequest));

			_blueprintValidator.validateConfiguration(
				blueprint.getConfiguration());

			BlueprintsAttributes requestBlueprintsAttributes =
				_getRequestBlueprintsAttributes(resourceRequest, blueprint);

			ProblemsHolderBuilder problemsHolderBuilder =
				_problemsHolderBuilderFactory.builder();

			SearchResponse searchResponse = _blueprintsEngineHelper.search(
				blueprint, requestBlueprintsAttributes, problemsHolderBuilder);

			BlueprintsAttributes responseBlueprintsAttributes =
				_getResponseBlueprintsAttributes(
					resourceRequest, resourceResponse, blueprint,
					requestBlueprintsAttributes);

			String jsonString = _searchResponseJSONTranslator.translate(
				searchResponse, blueprint, responseBlueprintsAttributes,
				_getResourceBundle(resourceRequest),
				problemsHolderBuilder::addExceptions, problemsHolderBuilder);

			ProblemsHolder problemsHolder = problemsHolderBuilder.build();

			List<Problem> problems = problemsHolder.getAllProblems();

			if (problems.isEmpty()) {
				return jsonString;
			}

			JSONObject jsonObject1 = _createJSONObject(jsonString);

			JSONObject jsonObject2 = _createJSONObject(
				_problemToJSONTranslator.translate(
					problems, _getResourceBundle(resourceRequest)));

			jsonObject1.put("errors", jsonObject2.get("errors"));

			return jsonObject1.toString();
		}
		catch (BlueprintsEngineException blueprintsEngineException) {
			_log.error(
				blueprintsEngineException.getMessage(),
				blueprintsEngineException);

			return _problemToJSONTranslator.translate(
				blueprintsEngineException.getProblems(),
				_getResourceBundle(resourceRequest));
		}
		catch (BlueprintValidationException blueprintValidationException) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					blueprintValidationException.getMessage(),
					blueprintValidationException);
			}

			return _problemToJSONTranslator.translate(
				blueprintValidationException.getProblems(),
				_getResourceBundle(resourceRequest));
		}
	}

	private JSONObject _createJSONObject(String translate) {
		try {
			return _jsonFactory.createJSONObject(translate);
		}
		catch (JSONException jsonException) {
			throw new RuntimeException(jsonException);
		}
	}

	private JSONArray _getPreviewAttributesJSONArray(
		ResourceRequest resourceRequest) {

		String previewAttributesString =
			BlueprintsAdminRequestUtil.getPreviewAttributes(resourceRequest);

		if (Validator.isBlank(previewAttributesString)) {
			return JSONFactoryUtil.createJSONArray();
		}

		try {
			return JSONFactoryUtil.createJSONArray(previewAttributesString);
		}
		catch (JSONException jsonException) {
			_log.error(
				"Unable to create a JSON array from: " +
					previewAttributesString,
				jsonException);

			return JSONFactoryUtil.createJSONArray();
		}
	}

	private BlueprintsAttributes _getRequestBlueprintsAttributes(
		ResourceRequest resourceRequest, Blueprint blueprint) {

		BlueprintsAttributesBuilder blueprintsAttributesBuilder =
			_blueprintsAttributesHelper.getBlueprintsRequestAttributesBuilder(
				resourceRequest, blueprint);

		blueprintsAttributesBuilder.addAttribute("explain", true);

		blueprintsAttributesBuilder.addAttribute("preview", true);

		blueprintsAttributesBuilder.addAttribute(
			"include_response_string", true);

		for (Object object : _getPreviewAttributesJSONArray(resourceRequest)) {
			JSONObject jsonObject = (JSONObject)object;

			blueprintsAttributesBuilder.addAttribute(
				jsonObject.getString("key"), jsonObject.get("value"));
		}

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
		return ListUtil.fromArray(
			"id", "score", "b_assetEntryId", "b_author", "b_created",
			"b_modified", "b_summary", "b_title", "b_type");
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
	private BlueprintValidator _blueprintValidator;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Portal _portal;

	@Reference
	private ProblemsHolderBuilderFactory _problemsHolderBuilderFactory;

	@Reference
	private ProblemToJSONTranslator _problemToJSONTranslator;

	@Reference
	private SearchResponseJSONTranslator _searchResponseJSONTranslator;

}