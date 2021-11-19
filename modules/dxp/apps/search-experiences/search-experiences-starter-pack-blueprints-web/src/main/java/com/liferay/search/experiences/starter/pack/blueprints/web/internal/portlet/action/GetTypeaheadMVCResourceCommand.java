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
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.constants.BlueprintsWebPortletKeys;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.constants.ResourceRequestKeys;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferences;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferencesImpl;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.util.BlueprintsWebPortletHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences =
			new BlueprintsWebPortletPreferencesImpl(
				resourceRequest.getPreferences());

		String keywords = ParamUtil.getString(resourceRequest, "q");

		if (1==1) {
			return;
		}

	}

	@Reference
	private BlueprintsWebPortletHelper _blueprintsWebPortletHelper;

}
