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

package com.liferay.portal.search.tuning.blueprints.web.internal.portlet.action;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
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
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributesBuilder;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributesBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.portal.search.tuning.blueprints.engine.exception.BlueprintsEngineException;
import com.liferay.portal.search.tuning.blueprints.engine.util.BlueprintsEngineHelper;
import com.liferay.portal.search.tuning.blueprints.json.response.BlueprintsJSONResponseBuilder;
import com.liferay.portal.search.tuning.blueprints.json.response.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.misspellings.processor.MisspellingsProcessor;
import com.liferay.portal.search.tuning.blueprints.query.index.util.QueryIndexHelper;
import com.liferay.portal.search.tuning.blueprints.suggestions.attributes.SuggestionsAttributes;
import com.liferay.portal.search.tuning.blueprints.suggestions.attributes.SuggestionsAttributesBuilder;
import com.liferay.portal.search.tuning.blueprints.suggestions.spellcheck.SpellCheckService;
import com.liferay.portal.search.tuning.blueprints.suggestions.suggestion.Suggestion;
import com.liferay.portal.search.tuning.blueprints.util.attributes.BlueprintsAttributesHelper;
import com.liferay.portal.search.tuning.blueprints.util.attributes.SuggestionsAttributesHelper;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.BlueprintsWebPortletKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.ResourceRequestKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferences;
import com.liferay.portal.search.tuning.blueprints.web.internal.portlet.preferences.BlueprintsWebPortletPreferencesImpl;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

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

		long blueprintId = _getSearchBlueprintId(resourceRequest);

		JSONObject responseJSONObject = null;

		try {
			BlueprintsWebPortletPreferences blueprintsWebPortletPreferences =
				new BlueprintsWebPortletPreferencesImpl(
					resourceRequest.getPreferences());

			Messages messages = new Messages();

			BlueprintsAttributes blueprintsRequestAttributes =
				_getBlueprintsRequestAttributes(
					resourceRequest, blueprintId,
					blueprintsWebPortletPreferences);

			SearchResponse searchResponse = _blueprintsEngineHelper.search(
				blueprintsRequestAttributes, messages, blueprintId);

			BlueprintsAttributes blueprintsResponseAttributes =
				_getBlueprintsResponseAttributes(
					resourceRequest, resourceResponse,
					blueprintsRequestAttributes, blueprintId);

			responseJSONObject = _blueprintsJSONResponseBuilder.buildJSONObject(
				searchResponse, blueprintsResponseAttributes,
				_getResourceBundle(resourceRequest), messages, blueprintId);

			// TODO: remove condition after
			// https://issues.liferay.com/browse/LPS-125124

			if ((searchResponse.getTotalHits() == 0) &&
				_shouldAddDidYouMean(
					blueprintsWebPortletPreferences,
					searchResponse.getTotalHits()) &&
				!Validator.isBlank(blueprintsRequestAttributes.getKeywords())) {

				_addDidYouMean(
					resourceRequest, blueprintsWebPortletPreferences,
					blueprintsRequestAttributes.getKeywords(),
					responseJSONObject);
			}

			// TODO: remove condition after
			// https://issues.liferay.com/browse/LPS-125124

			if ((searchResponse.getTotalHits() > 0) &&
				_shouldIndexQuery(
					blueprintsWebPortletPreferences,
					searchResponse.getTotalHits())) {

				_indexQuery(
					resourceRequest, blueprintsRequestAttributes.getKeywords());
			}
		}
		catch (JSONException jsonException) {
			_log.error(jsonException.getMessage(), jsonException);

			responseJSONObject = JSONUtil.put(
				JSONResponseKeys.ERRORS, jsonException.getMessage());
		}
		catch (BlueprintsEngineException blueprintsEngineException) {
			_log.error(
				blueprintsEngineException.getMessage(),
				blueprintsEngineException);

			responseJSONObject = JSONUtil.put(
				JSONResponseKeys.ERRORS,
				blueprintsEngineException.getMessage());
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);

			responseJSONObject = JSONUtil.put(
				JSONResponseKeys.ERRORS, portalException.getMessage());
		}

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse, responseJSONObject);
	}

	private void _addDidYouMean(
		ResourceRequest resourceRequest,
		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences,
		String keywords, JSONObject responseJSONObject) {

		if (_spellCheckService == null) {
			return;
		}

		List<Suggestion> suggestions = _spellCheckService.getSuggestions(
			_getDidYouMeanSuggestionsAttributes(
				resourceRequest, keywords, blueprintsWebPortletPreferences));

		if (suggestions.isEmpty()) {
			return;
		}

		_addDidYouMeanSuggestions(responseJSONObject, suggestions);
	}

	private void _addDidYouMeanSuggestions(
		JSONObject responseJSONObject, List<Suggestion> suggestions) {

		JSONArray suggestionsJSONArray = JSONFactoryUtil.createJSONArray();

		Stream<Suggestion> stream = suggestions.stream();

		stream.forEach(
			suggestion -> suggestionsJSONArray.put(suggestion.getText()));

		responseJSONObject.put("didyoumean", suggestionsJSONArray);
	}

	private boolean _allowMisspellings(PortletRequest portletRequest) {
		return ParamUtil.getBoolean(
			portletRequest, ReservedParameterNames.ALLOW_MISSPELLINGS.getKey());
	}

	private BlueprintsAttributes _getBlueprintsRequestAttributes(
		ResourceRequest resourceRequest, long blueprintId,
		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences) {

		BlueprintsAttributesBuilder blueprintsAttributesBuilder =
			_blueprintsAttributesHelper.getBlueprintsRequestAttributesBuilder(
				resourceRequest, blueprintId);

		if ((_misspellingsProcessor != null) &&
			blueprintsWebPortletPreferences.isMisspellingsEnabled() &&
			!_allowMisspellings(resourceRequest)) {

			return _processMisspellings(
				resourceRequest, blueprintsAttributesBuilder);
		}

		return blueprintsAttributesBuilder.build();
	}

	private BlueprintsAttributes _getBlueprintsResponseAttributes(
		ResourceRequest resourceRequest, ResourceResponse resourceResponse,
		BlueprintsAttributes blueprintsRequestAttributes, long blueprintId) {

		BlueprintsAttributesBuilder blueprintsAttributesBuilder =
			_blueprintsAttributesHelper.getBlueprintsResponseAttributesBuilder(
				resourceRequest, resourceResponse, blueprintsRequestAttributes,
				blueprintId);

		return blueprintsAttributesBuilder.build();
	}

	private SuggestionsAttributes _getDidYouMeanSuggestionsAttributes(
		ResourceRequest resourceRequest, String keywords,
		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences) {

		SuggestionsAttributesBuilder suggestionsAttributesBuilder =
			_suggestionsAttributesHelper.getSuggestionsAttributesBuilder(
				resourceRequest);

		return suggestionsAttributesBuilder.keywords(
			keywords
		).size(
			blueprintsWebPortletPreferences.getMaxDidYouMeanSuggestions()
		).build();
	}

	private ResourceBundle _getResourceBundle(ResourceRequest resourceRequest) {
		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		return ResourceBundleUtil.getBundle(
			"content.Language", themeDisplay.getLocale(), getClass());
	}

	private long _getSearchBlueprintId(ResourceRequest resourceRequest) {
		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences =
			new BlueprintsWebPortletPreferencesImpl(
				resourceRequest.getPreferences());

		return blueprintsWebPortletPreferences.getBlueprintId();
	}

	private void _indexQuery(ResourceRequest resourceRequest, String keywords) {
		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		_queryIndexHelper.indexKeywords(
			themeDisplay.getCompanyId(), themeDisplay.getScopeGroupId(),
			themeDisplay.getLanguageId(), keywords);
	}

	private BlueprintsAttributes _processMisspellings(
		ResourceRequest resourceRequest,
		BlueprintsAttributesBuilder blueprintsAttributesBuilder1) {

		BlueprintsAttributes blueprintsAttributes =
			blueprintsAttributesBuilder1.build();

		String keywords = blueprintsAttributes.getKeywords();

		BlueprintsAttributesBuilder blueprintsAttributesBuilder2 =
			_blueprintsAttributesBuilderFactory.builder(blueprintsAttributes);

		if (Validator.isBlank(keywords)) {
			return blueprintsAttributesBuilder2.build();
		}

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String misspellCheckedWords = _misspellingsProcessor.process(
			themeDisplay.getCompanyId(), themeDisplay.getScopeGroupId(),
			themeDisplay.getLanguageId(), keywords);

		if (!keywords.equals(misspellCheckedWords)) {
			blueprintsAttributesBuilder2.addAttribute(
				ReservedParameterNames.SHOWING_INSTEAD_OF.getKey(), keywords);
			blueprintsAttributesBuilder2.keywords(misspellCheckedWords);
		}

		return blueprintsAttributesBuilder2.build();
	}

	private boolean _shouldAddDidYouMean(
		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences,
		int hitCount) {

		if (blueprintsWebPortletPreferences.isDidYouMeanEnabled() &&
			(blueprintsWebPortletPreferences.getDidYouMeanHitsThreshold() >=
				hitCount)) {

			return true;
		}

		return false;
	}

	private boolean _shouldIndexQuery(
		BlueprintsWebPortletPreferences blueprintsWebPortletPreferences,
		int hitCount) {

		if (blueprintsWebPortletPreferences.isQueryIndexingEnabled() &&
			(blueprintsWebPortletPreferences.getQueryIndexingHitsThreshold() <=
				hitCount)) {

			return true;
		}

		return false;
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
	private BlueprintsJSONResponseBuilder _blueprintsJSONResponseBuilder;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	private volatile MisspellingsProcessor _misspellingsProcessor;

	@Reference
	private Portal _portal;

	@Reference
	private QueryIndexHelper _queryIndexHelper;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	private SpellCheckService _spellCheckService;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	private SuggestionsAttributesHelper _suggestionsAttributesHelper;

}