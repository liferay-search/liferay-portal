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

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			SearchConfiguration.class.getName(), actionRequest);

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		int type = ParamUtil.getInteger(
			actionRequest,
			SearchConfigurationWebKeys.SEARCH_CONFIGURATION_TYPE);

		long searchConfigurationId = ParamUtil.getLong(
			actionRequest, SearchConfigurationWebKeys.SEARCH_CONFIGURATION_ID);

		Map<Locale, String> titleMap = LocalizationUtil.getLocalizationMap(
			actionRequest, SearchConfigurationWebKeys.TITLE);

		Map<Locale, String> descriptionMap =
			LocalizationUtil.getLocalizationMap(
				actionRequest, SearchConfigurationWebKeys.DESCRIPTION);

		String configuration = _buildConfigurationFromRequest(actionRequest);

		try {
			if (Constants.ADD.equals(cmd)) {
				_searchConfigurationService.addConfiguration(
					titleMap, descriptionMap, configuration, type,
					serviceContext);
			}
			else if (searchConfigurationId > 0) {
				_searchConfigurationService.updateConfiguration(
					searchConfigurationId, titleMap, descriptionMap,
					configuration, serviceContext);
			}

			sendRedirect(actionRequest, actionResponse);
		}
		catch (SearchConfigurationValidationException ave) {
			ave.printStackTrace();

			actionResponse.setRenderParameter(
				"mvcRenderCommandName",
				SearchConfigurationMVCCommandNames.EDIT_SEARCH_CONFIGURATION);
		}
		catch (PortalException pe) {
			pe.printStackTrace();

			actionResponse.setRenderParameter(
				"mvcRenderCommandName",
				SearchConfigurationMVCCommandNames.EDIT_SEARCH_CONFIGURATION);
		}
	}

	@Reference
	protected SearchConfigurationService _searchConfigurationService;

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

}