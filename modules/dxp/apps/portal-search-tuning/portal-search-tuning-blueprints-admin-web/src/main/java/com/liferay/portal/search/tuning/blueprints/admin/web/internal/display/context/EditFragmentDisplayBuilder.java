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

import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;

import java.util.Map;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Kevin Tan
 */
public class EditFragmentDisplayBuilder extends EditEntryDisplayBuilder {

	public EditFragmentDisplayBuilder(
		HttpServletRequest httpServletRequest, Language language,
		JSONFactory jsonFactory, RenderRequest renderRequest,
		RenderResponse renderResponse, BlueprintService blueprintService) {

		super(
			httpServletRequest, language, jsonFactory, renderRequest,
			renderResponse, blueprintService);
	}

	public BlueprintDisplayContext build() {
		BlueprintDisplayContext blueprintDisplayContext =
			new BlueprintDisplayContext();

		setConfigurationId(blueprintDisplayContext);
		setConfigurationType(blueprintDisplayContext);
		setData(blueprintDisplayContext, _getProps());
		setPageTitle(blueprintDisplayContext);
		setRedirect(blueprintDisplayContext);

		return blueprintDisplayContext;
	}

	private Map<String, Object> _getProps() {
		Map<String, Object> props = HashMapBuilder.<String, Object>put(
			"blueprintId", blueprintId
		).put(
			"blueprintType", blueprintType
		).put(
			"redirectURL", getRedirect()
		).put(
			"submitFormURL",
			getSubmitFormURL(BlueprintsAdminMVCCommandNames.EDIT_FRAGMENT)
		).build();

		if (blueprint != null) {
			props.put(
				"initialConfigurationString", blueprint.getConfiguration());
			props.put("initialDescription", getDescriptionJSONObject());
			props.put("initialTitle", getTitleJSONObject());

			/*
			TODO This is a placeholder for LPS-123115 to get predefinedVariables
			props.put("predefinedVariables", _getPredefinedVariables());
 			*/
		}

		return props;
	}

}