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

package com.liferay.search.experiences.starter.pack.blueprints.web.internal.portlet.action;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributesBuilder;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributesBuilderFactory;
import com.liferay.search.experiences.blueprints.engine.exception.BlueprintsEngineException;
import com.liferay.search.experiences.blueprints.engine.portlet.attributes.BlueprintsAttributesHelper;
import com.liferay.search.experiences.blueprints.engine.util.BlueprintsEngineHelper;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilderFactory;
import com.liferay.search.experiences.searchresponse.json.translator.SearchResponseJSONTranslator;
import com.liferay.search.experiences.searchresponse.json.translator.constants.ResponseAttributeKeys;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.constants.BlueprintsWebPortletKeys;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.constants.ResourceRequestKeys;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferences;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferencesImpl;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.util.BlueprintsWebPortletHelper;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

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
		"mvc.command.name=" + ResourceRequestKeys.GET_SEARCH_RESULTS
	},
	service = MVCResourceCommand.class
)
public class GetSearchResultsMVCResourceCommand extends BaseMVCResourceCommand {

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

		Optional<Blueprint> blueprintOptional =
			_blueprintsWebPortletHelper.getBlueprint(resourceRequest);

		Blueprint blueprint = blueprintOptional.get();

		try {
			BlueprintsWebPortletPreferences blueprintsWebPortletPreferences =
				new BlueprintsWebPortletPreferencesImpl(
					resourceRequest.getPreferences());

			ProblemsHolderBuilder problemsHolderBuilder =
				_problemsHolderBuilderFactory.builder();

			BlueprintsAttributes requestBlueprintsAttributes =
				_getRequestBlueprintsAttributes(
					resourceRequest, blueprint,
					blueprintsWebPortletPreferences);

			SearchResponse searchResponse = _blueprintsEngineHelper.search(
				blueprint, requestBlueprintsAttributes, problemsHolderBuilder);

			return _createResponseJSONString(
				resourceRequest, resourceResponse, searchResponse, blueprint,
				requestBlueprintsAttributes, blueprintsWebPortletPreferences,
				problemsHolderBuilder);
		}
		catch (BlueprintsEngineException blueprintsEngineException) {
			_log.error(
				blueprintsEngineException.getMessage(),
				blueprintsEngineException);

			return String.valueOf(
				JSONUtil.put("errors", blueprintsEngineException.getMessage()));
		}
	}


	private boolean _allowMisspellings(PortletRequest portletRequest) {
		return ParamUtil.getBoolean(portletRequest, "allow_misspellings");
	}

	private JSONObject _createJSONObject(String translate) {
		try {
			return _jsonFactory.createJSONObject(translate);
		}
		catch (JSONException jsonException) {
			throw new RuntimeException(jsonException);
		}
	}

	private String _createResponseJSONString(
		ResourceRequest resourceRequest, ResourceResponse resourceResponse,
		SearchResponse searchResponse, Blueprint blueprint,
		BlueprintsAttributes requestBlueprintsAttributes,
		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences,
		ProblemsHolderBuilder problemsHolderBuilder) {

		BlueprintsAttributes responseBlueprintsAttributes =
			_getResponseBlueprintsAttributes(
				resourceRequest, resourceResponse, blueprint,
				requestBlueprintsAttributes);

		JSONObject jsonObject = _createJSONObject(
			_searchResponseJSONTranslator.translate(
				searchResponse, blueprint, responseBlueprintsAttributes,
				_getResourceBundle(resourceRequest),
				problemsHolderBuilder::addExceptions, problemsHolderBuilder));

		return jsonObject.toString();
	}

	private BlueprintsAttributes _getRequestBlueprintsAttributes(
		ResourceRequest resourceRequest, Blueprint blueprint,
		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences) {

		BlueprintsAttributesBuilder blueprintsAttributesBuilder =
			_blueprintsAttributesHelper.getBlueprintsRequestAttributesBuilder(
				resourceRequest, blueprint);

		if ((_misspellingsProcessor != null) &&
			blueprintsWebPortletPreferences.isMisspellingsEnabled() &&
			!_allowMisspellings(resourceRequest)) {

			return _processMisspellings(
				resourceRequest, blueprintsAttributesBuilder);
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
			ResponseAttributeKeys.INCLUDE_RESULT, true);

		return blueprintsAttributesBuilder.build();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		GetSearchResultsMVCResourceCommand.class);

	@Reference
	private BlueprintsAttributesBuilderFactory
		_blueprintsAttributesBuilderFactory;

	@Reference
	private BlueprintsAttributesHelper _blueprintsAttributesHelper;

	@Reference
	private BlueprintsEngineHelper _blueprintsEngineHelper;

	@Reference
	private BlueprintsWebPortletHelper _blueprintsWebPortletHelper;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Portal _portal;

	@Reference
	private ProblemsHolderBuilderFactory _problemsHolderBuilderFactory;

	@Reference
	private SearchResponseJSONTranslator _searchResponseJSONTranslator;


}
