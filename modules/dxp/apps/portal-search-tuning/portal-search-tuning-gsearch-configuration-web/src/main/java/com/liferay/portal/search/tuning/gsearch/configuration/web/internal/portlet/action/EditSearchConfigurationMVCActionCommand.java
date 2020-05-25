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

package com.liferay.portal.search.tuning.gsearch.configuration.web.internal.portlet.action;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SearchConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SearchConfigurationPortletKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.exception.SearchConfigurationValidationException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SearchConfiguration;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationService;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.constants.SearchConfigurationMVCCommandNames;
import com.liferay.portal.search.tuning.gsearch.configuration.web.internal.constants.SearchConfigurationWebKeys;

import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + SearchConfigurationPortletKeys.SEARCH_CONFIGURATION_ADMIN,
		"mvc.command.name=" + SearchConfigurationMVCCommandNames.EDIT_SEARCH_CONFIGURATION
	},
	service = MVCActionCommand.class
)
public class EditSearchConfigurationMVCActionCommand
	extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		long searchConfigurationId = ParamUtil.getLong(
			actionRequest, SearchConfigurationWebKeys.SEARCH_CONFIGURATION_ID);

		int type = ParamUtil.getInteger(
				actionRequest,
				SearchConfigurationWebKeys.SEARCH_CONFIGURATION_TYPE);

		Map<Locale, String> titleMap = LocalizationUtil.getLocalizationMap(
			actionRequest, SearchConfigurationWebKeys.TITLE);

		Map<Locale, String> descriptionMap =
			LocalizationUtil.getLocalizationMap(
				actionRequest, SearchConfigurationWebKeys.DESCRIPTION);

		String configuration = _buildConfigurationFromRequest(actionRequest);

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		try {
			
			ServiceContext serviceContext = ServiceContextFactory.getInstance(
					SearchConfiguration.class.getName(), actionRequest);
			
			if (Constants.ADD.equals(cmd)) {
				_searchConfigurationService.addCompanySearchConfiguration(
					titleMap, descriptionMap, configuration, type,
					serviceContext);
			}
			else if (searchConfigurationId > 0) {
				_searchConfigurationService.updateSearchConfiguration(
					searchConfigurationId, titleMap, descriptionMap,
					configuration, serviceContext);
			}

			sendRedirect(actionRequest, actionResponse);
		}
		catch (SearchConfigurationValidationException scve) {

			_log.error(scve.getMessage(), scve);

			scve.getErrors().forEach(key -> SessionErrors.add(actionRequest, key));
			
			actionResponse.getRenderParameters().setValue(
					"mvcRenderCommandName",
					SearchConfigurationMVCCommandNames.EDIT_SEARCH_CONFIGURATION);
		}
		catch (PortalException pe) {

			_log.error(pe.getMessage(), pe);

			SessionErrors.add(actionRequest, 
					SearchConfigurationWebKeys.ERROR_DETAILS, pe);

			actionResponse.getRenderParameters().setValue(
					"mvcRenderCommandName",
					SearchConfigurationMVCCommandNames.EDIT_SEARCH_CONFIGURATION);
		}
	}

	private String _buildConfigurationFromRequest(ActionRequest actionRequest)
		throws JSONException {

		JSONObject configuration = JSONFactoryUtil.createJSONObject();

		JSONArray clauseConfiguration = _getAutoFieldValues(
			actionRequest, SearchConfigurationWebKeys.CLAUSE_CONFIGURATION,
			SearchConfigurationWebKeys.CLAUSE_CONFIGURATION_INDEXES);

		configuration.put(
			SearchConfigurationKeys.CLAUSE_CONFIGURATION, clauseConfiguration);

		JSONArray misspellings = _getAutoFieldValues(
			actionRequest, SearchConfigurationWebKeys.MISSPELLING,
			SearchConfigurationWebKeys.MISSPELLING_INDEXES);

		configuration.put(SearchConfigurationKeys.MISSPELLINGS, misspellings);

		JSONArray synonyms = _getAutoFieldValues(
			actionRequest, SearchConfigurationWebKeys.SYNONYM,
			SearchConfigurationWebKeys.SYNONYM_INDEXES);

		configuration.put(SearchConfigurationKeys.SYNONYMS, synonyms);

		return configuration.toString();
	}

	private JSONArray _getAutoFieldValues(
			ActionRequest actionRequest, String valueParameterKey,
			String indexParameterKey)
		throws JSONException {

		JSONArray values = JSONFactoryUtil.createJSONArray();

		int[] rowIndexes = ParamUtil.getIntegerValues(
			actionRequest, indexParameterKey, new int[0]);

		for (int i : rowIndexes) {
			String value = ParamUtil.getString(
				actionRequest, valueParameterKey + i);

			JSONObject item = JSONFactoryUtil.createJSONObject(value);

			values.put(item);
		}

		return values;
	}
	
	private static final Logger _log = LoggerFactory.getLogger(
			EditSearchConfigurationMVCActionCommand.class);
	
	@Reference
	private SearchConfigurationService _searchConfigurationService;
}