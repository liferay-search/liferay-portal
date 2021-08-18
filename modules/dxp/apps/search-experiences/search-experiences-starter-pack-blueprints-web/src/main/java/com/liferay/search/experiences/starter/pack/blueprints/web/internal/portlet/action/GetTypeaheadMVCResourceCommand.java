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
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributesBuilder;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributesBuilderFactory;
import com.liferay.search.experiences.predict.suggestions.constants.SuggestionConstants;
import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;
import com.liferay.search.experiences.predict.suggestions.service.SuggestionService;
import com.liferay.search.experiences.predict.suggestions.suggestion.Suggestion;
import com.liferay.search.experiences.predict.typeahead.field.constants.FieldTypeaheadConstants;
import com.liferay.search.experiences.predict.typeahead.field.definition.FieldTypeaheadSourceDefinition;
import com.liferay.search.experiences.predict.typeahead.field.definition.FieldsSourceDefinition;
import com.liferay.search.experiences.predict.typeahead.field.definition.NestedFieldSourceDefinition;
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

	private void _addSourceDefinitions(
		DataProviderSettings dataProviderSettings, JSONArray jsonArray) {

		if (jsonArray == null) {
			return;
		}

		List<FieldTypeaheadSourceDefinition> fieldTypeaheadSourceDefinitions =
			new ArrayList<>();

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);

			FieldTypeaheadSourceDefinition fieldTypeaheadSourceDefinition =
				null;

			String path = jsonObject.getString("path");

			if (!Validator.isBlank(path)) {
				fieldTypeaheadSourceDefinition =
					_getNestedFieldSourceDefinition(jsonObject);
			}
			else {
				fieldTypeaheadSourceDefinition = _getFieldsSourceDefinition(
					jsonObject);
			}

			fieldTypeaheadSourceDefinitions.add(fieldTypeaheadSourceDefinition);
		}

		dataProviderSettings.addAttribute(
			"sourceDefinitions", fieldTypeaheadSourceDefinitions);
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

	private FieldTypeaheadSourceDefinition _getFieldsSourceDefinition(
		JSONObject jsonObject) {

		JSONObject fieldsBoostsJSONObject = jsonObject.getJSONObject(
			"fields_boosts");

		Map<String, Float> fieldsBoosts = new HashMap<>();

		if (fieldsBoostsJSONObject != null) {
			Set<String> keySet = fieldsBoostsJSONObject.keySet();

			keySet.forEach(
				term -> fieldsBoosts.put(
					term,
					GetterUtil.getFloat(fieldsBoostsJSONObject.get(term))));
		}

		return new FieldsSourceDefinition.FieldsSourceDefinitionBuilder().
			fieldsBoosts(
				fieldsBoosts
			).termFilterMap(
				_getTermFilterMap(jsonObject)
			).build();
	}

	private DataProviderSettings _getFieldTypeaheadDataProviderSettings(
		JSONObject jsonObject) {

		DataProviderSettings dataProviderSettings = new DataProviderSettings();

		dataProviderSettings.addAttribute(
			FieldTypeaheadConstants.DISPLAY_FIELD,
			jsonObject.getString("display_field", null));

		dataProviderSettings.addAttribute(
			FieldTypeaheadConstants.ENTRY_CLASS_NAMES,
			JSONUtil.toStringArray(
				jsonObject.getJSONArray("entry_class_names")));

		dataProviderSettings.addAttribute(
			FieldTypeaheadConstants.EXCLUDE_DDM_STRUCTURE_CONTENT_FIELD,
			GetterUtil.getBoolean(
				jsonObject.get("exclude_ddm_structure_content_field"), true));

		dataProviderSettings.addAttribute(
			SuggestionConstants.FUZZINESS,
			jsonObject.getString("fuzziness", null));
		dataProviderSettings.addAttribute(
			FieldTypeaheadConstants.INDICES,
			JSONUtil.toStringArray(jsonObject.getJSONArray("indices")));
		dataProviderSettings.addAttribute(
			FieldTypeaheadConstants.OFFSET, jsonObject.getInt("offset"));
		dataProviderSettings.addAttribute(
			SuggestionConstants.OPERATOR, jsonObject.getString("operator"));
		dataProviderSettings.addAttribute(
			SuggestionConstants.PREFIX_LENGTH,
			jsonObject.getInt("prefix_length"));
		dataProviderSettings.addAttribute(
			FieldTypeaheadConstants.PRE_SANITIZER_REGEXP,
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
				FieldTypeaheadConstants.SORT_FIELD_MAP, sortFieldMap);
		}

		dataProviderSettings.addAttribute(
			SuggestionConstants.SOURCE_GROUP_IDS,
			JSONUtil.toLongArray(jsonObject.getJSONArray("source_group_ids")));

		dataProviderSettings.addAttribute(
			FieldTypeaheadConstants.TRIM_STOPWORDS,
			GetterUtil.getBoolean(jsonObject.get("trim_stop_words"), true));

		dataProviderSettings.addAttribute(
			FieldTypeaheadConstants.TYPE, jsonObject.getString("type"));

		dataProviderSettings.addAttribute(
			SuggestionConstants.WEIGHT,
			GetterUtil.getFloat(jsonObject.get("weight")));

		_addSourceDefinitions(
			dataProviderSettings,
			jsonObject.getJSONArray("source_definitions"));

		return dataProviderSettings;
	}

	private FieldTypeaheadSourceDefinition _getNestedFieldSourceDefinition(
		JSONObject jsonObject) {

		JSONObject nestedMustTermsJSONObject = jsonObject.getJSONObject(
			"nested_must_terms");

		Map<String, String> nestedMustTermMap = new HashMap<>();

		if (nestedMustTermsJSONObject != null) {
			Set<String> keySet = nestedMustTermsJSONObject.keySet();

			keySet.forEach(
				term -> nestedMustTermMap.put(
					term, nestedMustTermsJSONObject.getString(term)));
		}

		return new NestedFieldSourceDefinition.
			NestedFieldSourceDefinitionBuilder().nestedMustTermMap(
				nestedMustTermMap
			).path(
				jsonObject.getString("path")
			).termFilterMap(
				_getTermFilterMap(jsonObject)
			).valueFieldName(
				jsonObject.getString("value_field")
			).build();
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

	private Map<String, String> _getTermFilterMap(JSONObject jsonObject) {
		JSONObject termFiltersJSONObject = jsonObject.getJSONObject(
			"term_filters");

		Map<String, String> map = new HashMap<>();

		if (termFiltersJSONObject != null) {
			Set<String> keySet = termFiltersJSONObject.keySet();

			keySet.forEach(
				term -> map.put(term, termFiltersJSONObject.getString(term)));
		}

		return map;
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