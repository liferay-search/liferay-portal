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
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributesBuilder;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributesBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.parameter.KeywordsConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.parameter.ParameterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.portal.search.tuning.blueprints.misspellings.processor.MisspellingsProcessor;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;
import com.liferay.portal.search.tuning.blueprints.util.attributes.BlueprintsResponseAttributesHelper;

import java.util.Optional;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintsResponseAttributesHelper.class)
public class BlueprintsResponseAttributesHelperImpl
	implements BlueprintsResponseAttributesHelper {

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
		).locale(
			themeDisplay.getLocale()
		).userId(
			themeDisplay.getUserId()
		).addAttribute(
			"portletRequest", portletRequest
		).addAttribute(
			"portletResponse", portletResponse
		);

		_addKeywords(
			blueprintsRequestAttributes, blueprintsAttributesBuilder,
			blueprintId);

		_addShowingInsteadOf(
			blueprintsAttributesBuilder, blueprintsRequestAttributes);

		return blueprintsAttributesBuilder;
	}

	private void _addKeywords(
		BlueprintsAttributes blueprintsRequestAttributes,
		BlueprintsAttributesBuilder blueprintsAttributesBuilder,
		long blueprintId) {

		blueprintsAttributesBuilder.addAttribute(
			"keywords", _getKeywords(blueprintsRequestAttributes, blueprintId));
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

	private Blueprint _getBlueprint(long blueprintId) throws PortalException {
		return _blueprintService.getBlueprint(blueprintId);
	}

	private String _getKeywords(
		BlueprintsAttributes blueprintsRequestAttributes, long blueprintId) {

		JSONObject parameterConfigurationJSONObject =
			_getParameterConfigurationJSONObject(blueprintId);

		if (parameterConfigurationJSONObject == null) {
			return null;
		}

		JSONObject keywordconfigurationJSONObject =
			parameterConfigurationJSONObject.getJSONObject(
				ParameterConfigurationKeys.KEYWORDS.getJsonKey());

		if (keywordconfigurationJSONObject == null) {
			return null;
		}

		Optional<Object> keywordsOptional =
			blueprintsRequestAttributes.getAttributeOptional(
				keywordconfigurationJSONObject.getString(
					KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey()));

		if (keywordsOptional.isPresent()) {
			return (String)keywordsOptional.get();
		}

		return null;
	}

	private JSONObject _getParameterConfigurationJSONObject(long blueprintId) {
		try {
			Blueprint blueprint = _getBlueprint(blueprintId);

			Optional<JSONObject> configurationJSONObjectOptional =
				_blueprintHelper.getParameterConfigurationOptional(blueprint);

			if (configurationJSONObjectOptional.isPresent()) {
				return configurationJSONObjectOptional.get();
			}
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);
		}
		catch (Exception exception) {
			_log.error(exception.getMessage(), exception);
		}

		return null;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintsResponseAttributesHelperImpl.class);

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