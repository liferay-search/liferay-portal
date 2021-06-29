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
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributesBuilder;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributesBuilderFactory;
import com.liferay.search.experiences.predict.suggestions.constants.SuggestionConstants;
import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;
import com.liferay.search.experiences.predict.suggestions.service.SuggestionService;
import com.liferay.search.experiences.predict.suggestions.suggestion.Suggestion;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.constants.BlueprintsWebPortletKeys;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.constants.ResourceRequestKeys;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferences;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferencesImpl;
import com.liferay.search.experiences.starter.pack.blueprints.web.internal.util.BlueprintsWebPortletHelper;

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

		if (!_isTypeaheadEnabled(blueprintsWebPortletPreferences) ||
			(keywords.length() <= 1)) {

			return;
		}

		JSONObject jsonObject = _getTypeaheadConfigurationJSONObject(
			blueprintsWebPortletPreferences);

		if (jsonObject == null) {
			_log.error("Typeahead configuration is not set");

			return;
		}

		SuggestionAttributesBuilder suggestionAttributesBuilder =
			_getSuggestionAttributesBuilder(
				resourceRequest, jsonObject, keywords);

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse,
			_getResponseJSONObject(
				_suggestionService.getSuggestions(
					suggestionAttributesBuilder.build())));
	}

	private void _addTypeaheadDataProviderSettings(
		SuggestionAttributesBuilder suggestionAttributesBuilder,
		String dataProvider, JSONObject jsonObject) {

		DataProviderSettings dataProviderSettings;

		if (dataProvider.equals("field")) {
			dataProviderSettings = _getFieldTypeaheadDataProviderSettings(
				jsonObject);
		}
		else {
			dataProviderSettings = new DataProviderSettings();

			dataProviderSettings.addAttribute(
				SuggestionConstants.FUZZINESS,
				jsonObject.getString("fuzziness", null));
			dataProviderSettings.addAttribute(
				SuggestionConstants.LANGUAGE_IDS,
				JSONUtil.toStringArray(jsonObject.getJSONArray("languageIds")));
			dataProviderSettings.addAttribute(
				SuggestionConstants.PREFIX_LENGTH,
				jsonObject.getInt("prefix_length"));
			dataProviderSettings.addAttribute(
				SuggestionConstants.SOURCE_GROUP_IDS,
				JSONUtil.toLongArray(
					jsonObject.getJSONArray("source_group_ids")));
		}

		suggestionAttributesBuilder.addDataProviderSettings(
			dataProvider, dataProviderSettings);
	}

	private String[] _getDataProviders(JSONObject jsonObject) {
		JSONObject dataProviderConfigurationJSONObject =
			jsonObject.getJSONObject("data_provider_configuration");

		if (dataProviderConfigurationJSONObject == null) {
			return null;
		}

		return ArrayUtil.toStringArray(
			dataProviderConfigurationJSONObject.keySet());
	}

	private DataProviderSettings _getFieldTypeaheadDataProviderSettings(
		JSONObject jsonObject) {

		DataProviderSettings dataProviderSettings = new DataProviderSettings();

		dataProviderSettings.addAttribute(
			SuggestionConstants.ENTRY_CLASS_NAMES,
			JSONUtil.toStringArray(
				jsonObject.getJSONArray("entry_class_names")));

		dataProviderSettings.addAttribute(
			SuggestionConstants.DISPLAY_FIELD,
			jsonObject.getString("display_field", null));

		JSONObject fieldMapJSONObject = jsonObject.getJSONObject("field_map");

		if (fieldMapJSONObject != null) {
			Map<String, Float> fieldMap = new HashMap<>();

			Set<String> keySet = fieldMapJSONObject.keySet();

			keySet.forEach(
				field -> fieldMap.put(
					field, GetterUtil.getFloat(fieldMapJSONObject.get(field))));

			dataProviderSettings.addAttribute(
				SuggestionConstants.FIELD_MAP, fieldMap);
		}

		dataProviderSettings.addAttribute(
			SuggestionConstants.FUZZINESS,
			jsonObject.getString("fuzziness", null));
		dataProviderSettings.addAttribute(
			SuggestionConstants.INDICES,
			JSONUtil.toStringArray(jsonObject.getJSONArray("indices")));

		JSONObject nestedFieldMapJSONObject = jsonObject.getJSONObject(
			"nested_field_map");

		if (nestedFieldMapJSONObject != null) {
			Map<String, Float> nestedFieldMap = new HashMap<>();

			Set<String> keySet = nestedFieldMapJSONObject.keySet();

			keySet.forEach(
				field -> nestedFieldMap.put(
					field,
					GetterUtil.getFloat(nestedFieldMapJSONObject.get(field))));

			dataProviderSettings.addAttribute(
				SuggestionConstants.NESTED_FIELD_MAP, nestedFieldMap);
		}

		dataProviderSettings.addAttribute(
			SuggestionConstants.OFFSET, jsonObject.getInt("offset"));
		dataProviderSettings.addAttribute(
			SuggestionConstants.OPERATOR, jsonObject.getString("operator"));
		dataProviderSettings.addAttribute(
			SuggestionConstants.PREFIX_LENGTH,
			jsonObject.getInt("prefix_length"));
		dataProviderSettings.addAttribute(
			SuggestionConstants.SANITIZER_REGEXP,
			jsonObject.getString("sanitizer_regexp", null));

		JSONObject sortFieldMapJSONObject = jsonObject.getJSONObject(
			"sort_field_map");

		if (sortFieldMapJSONObject != null) {
			Map<String, String> sortFieldMap = new HashMap<>();

			Set<String> keySet = sortFieldMapJSONObject.keySet();

			keySet.forEach(
				field -> sortFieldMap.put(
					field, sortFieldMapJSONObject.getString(field)));

			dataProviderSettings.addAttribute(
				SuggestionConstants.SORT_FIELD_MAP, sortFieldMap);
		}

		dataProviderSettings.addAttribute(
			SuggestionConstants.SOURCE_GROUP_IDS,
			JSONUtil.toLongArray(jsonObject.getJSONArray("source_group_ids")));

		JSONObject termFiltersJSONObject = jsonObject.getJSONObject(
			"term_filters");

		if (termFiltersJSONObject != null) {
			Map<String, String> termFilterMap = new HashMap<>();

			Set<String> keySet = termFiltersJSONObject.keySet();

			keySet.forEach(
				term -> termFilterMap.put(
					term, termFiltersJSONObject.getString(term)));

			dataProviderSettings.addAttribute(
				SuggestionConstants.TERM_FILTERS, termFilterMap);
		}

		dataProviderSettings.addAttribute(
			SuggestionConstants.TYPE, jsonObject.getString("type"));

		dataProviderSettings.addAttribute(
			SuggestionConstants.WEIGHT,
			GetterUtil.getFloat(jsonObject.get("weight")));

		return dataProviderSettings;
	}

	private JSONObject _getResponseJSONObject(
		List<Suggestion<String>> suggestions) {

		JSONObject responseJSONObject = _jsonFactory.createJSONObject();

		if (suggestions.isEmpty()) {
			return responseJSONObject;
		}

		JSONArray suggestionsJSONArray = _jsonFactory.createJSONArray();

		suggestions.forEach(
			suggestion -> suggestionsJSONArray.put(suggestion.getPayload()));

		return responseJSONObject.put("suggestions", suggestionsJSONArray);
	}

	private int _getSize(JSONObject jsonObject) {
		return jsonObject.getInt("size", 10);
	}

	private SuggestionAttributesBuilder _getSuggestionAttributesBuilder(
		ResourceRequest resourceRequest, JSONObject jsonObject,
		String keywords) {

		String[] dataProviders = _getDataProviders(jsonObject);

		if ((dataProviders == null) || (dataProviders.length == 0)) {
			_log.error("No typeahead data providers configured");

			return null;
		}

		SuggestionAttributesBuilder suggestionAttributesBuilder =
			_blueprintsWebPortletHelper.getSuggestionAttributesBuilder(
				resourceRequest, dataProviders, keywords, _getSize(jsonObject));

		_setDataProviderSettings(suggestionAttributesBuilder, jsonObject);

		return suggestionAttributesBuilder;
	}

	private JSONObject _getTypeaheadConfigurationJSONObject(
			BlueprintsWebPortletPreferences blueprintsWebPortletPreferences)
		throws Exception {

		return _jsonFactory.createJSONObject(
			blueprintsWebPortletPreferences.getTypeaheadConfiguration());
	}

	private boolean _isTypeaheadEnabled(
		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences) {

		return blueprintsWebPortletPreferences.isTypeaheadEnabled();
	}

	private void _setDataProviderSettings(
		SuggestionAttributesBuilder suggestionAttributesBuilder,
		JSONObject jsonObject) {

		JSONObject dataProviderConfigurationJSONObject =
			jsonObject.getJSONObject("data_provider_configuration");

		Set<String> keySet = dataProviderConfigurationJSONObject.keySet();

		keySet.forEach(
			dataProvider -> _addTypeaheadDataProviderSettings(
				suggestionAttributesBuilder, dataProvider,
				dataProviderConfigurationJSONObject.getJSONObject(
					dataProvider)));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		GetTypeaheadMVCResourceCommand.class);

	@Reference
	private BlueprintsWebPortletHelper _blueprintsWebPortletHelper;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Portal _portal;

	@Reference
	private SuggestionAttributesBuilderFactory
		_suggestionAttributesBuilderFactory;

	@Reference(target = "(suggestion.type=typeahead)")
	private SuggestionService _suggestionService;

}