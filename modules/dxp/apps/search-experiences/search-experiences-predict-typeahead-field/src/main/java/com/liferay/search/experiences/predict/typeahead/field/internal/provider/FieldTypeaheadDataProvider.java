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

package com.liferay.search.experiences.predict.typeahead.field.internal.provider;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.Field;
import com.liferay.portal.search.highlight.HighlightField;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributes;
import com.liferay.search.experiences.predict.suggestions.constants.SuggestionConstants;
import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;
import com.liferay.search.experiences.predict.suggestions.spi.provider.TypeaheadDataProvider;
import com.liferay.search.experiences.predict.suggestions.suggestion.SuggestionResponse;
import com.liferay.search.experiences.predict.typeahead.field.internal.provider.util.StopwordHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "data.provider.key=field",
	service = TypeaheadDataProvider.class
)
public class FieldTypeaheadDataProvider implements TypeaheadDataProvider {

	@Override
	public List<SuggestionResponse<String>> getSuggestions(
		SuggestionAttributes suggestionAttributes) {

		DataProviderSettings dataProviderSettings = _getDataProviderSettings(
			suggestionAttributes);

		if (dataProviderSettings == null) {
			_log.error("Data provider settings missing");

			return new ArrayList<>();
		}

		return _getSuggestions(suggestionAttributes, dataProviderSettings);
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_key = GetterUtil.getString(properties.get("data.provider.key"));
	}

	private void _addDisplayFieldSuggestion(
		List<SuggestionResponse<String>> suggestions, SearchHit searchHit,
		Map<String, Map<String, Float>> nestedFieldMap, String displayField,
		String keywords, String sanitizerRegexp) {

		String value = null;

		Document document = searchHit.getDocument();

		if ((nestedFieldMap != null) &&
			nestedFieldMap.containsKey(displayField)) {

			value = _getNestedFieldValue(document, displayField);
		}
		else {
			value = document.getString(displayField);
		}

		_addSuggestion(
			suggestions, _preSanitize(value, sanitizerRegexp),
			searchHit.getScore(), keywords);
	}

	private void _addDocumentFieldSuggestion(
		List<SuggestionResponse<String>> suggestions, String value, float score,
		String keywords, String sanitizerRegexp, String stopwordLanguageId,
		int wordCount) {

		if (Validator.isBlank(value)) {
			return;
		}

		value = _findDocumentFieldMatch(value, keywords);

		if (Validator.isBlank(value)) {
			return;
		}

		_addSuggestion(
			suggestions,
			_trim(
				_preSanitize(value, sanitizerRegexp), stopwordLanguageId,
				wordCount),
			score, keywords);
	}

	private void _addDocumentFieldSuggestions(
		List<SuggestionResponse<String>> suggestions, SearchHit searchHit,
		Map<String, Float> fieldMap, String keywords, String sanitizerRegexp,
		String stopwordLanguageId, int wordCount) {

		Document document = searchHit.getDocument();

		Set<String> keySet = fieldMap.keySet();

		keySet.forEach(
			fieldName -> _addDocumentFieldSuggestion(
				suggestions, document.getString(fieldName),
				searchHit.getScore(), keywords, sanitizerRegexp,
				stopwordLanguageId, wordCount));
	}

	private void _addFieldMatchSuggestions(
		List<SuggestionResponse<String>> suggestions, SearchHit searchHit,
		Map<String, Float> fieldMap,
		Map<String, Map<String, Float>> nestedFieldMap, String keywords,
		String sanitizerRegexp, String stopwordLanguageId, int wordCount) {

		_addDocumentFieldSuggestions(
			suggestions, searchHit, fieldMap, keywords, sanitizerRegexp,
			stopwordLanguageId, wordCount);

		if (!MapUtil.isEmpty(nestedFieldMap)) {
			_addNestedFieldSuggestions(
				suggestions, searchHit, nestedFieldMap, keywords,
				sanitizerRegexp, stopwordLanguageId, wordCount);
		}
	}

	private void _addHighlighterSuggestions(
		List<SuggestionResponse<String>> suggestions, SearchHit searchHit,
		Map<String, Map<String, Float>> nestedFieldMap, String keywords,
		String sanitizerRegexp, String stopwordLanguageId, int wordCount) {

		Map<String, HighlightField> highlightFieldsMap =
			searchHit.getHighlightFieldsMap();

		if (MapUtil.isNotEmpty(highlightFieldsMap)) {
			_addHighlightFieldSuggestions(
				suggestions, highlightFieldsMap, searchHit.getScore(), keywords,
				sanitizerRegexp, stopwordLanguageId, wordCount);
		}

		if (!MapUtil.isEmpty(nestedFieldMap)) {
			_addNestedFieldSuggestions(
				suggestions, searchHit, nestedFieldMap, keywords,
				sanitizerRegexp, stopwordLanguageId, wordCount);
		}
	}

	private void _addHighlightFieldSuggestion(
		List<SuggestionResponse<String>> suggestions,
		HighlightField highlightField, float score, String keywords,
		String sanitizerRegexp, String stopwordLanguageId, int wordCount) {

		List<String> fragments = highlightField.getFragments();

		fragments.forEach(
			fragment -> {
				String value = _trim(
					_preSanitize(fragment, sanitizerRegexp), stopwordLanguageId,
					wordCount);

				_addSuggestion(suggestions, value, score, keywords);
			});
	}

	private void _addHighlightFieldSuggestions(
		List<SuggestionResponse<String>> suggestions,
		Map<String, HighlightField> highlightFieldsMap, float score,
		String keywords, String sanitizerRegexp, String stopwordLanguageId,
		int wordCount) {

		Set<Map.Entry<String, HighlightField>> entrySet =
			highlightFieldsMap.entrySet();

		entrySet.forEach(
			entry -> _addHighlightFieldSuggestion(
				suggestions, entry.getValue(), score, keywords, sanitizerRegexp,
				stopwordLanguageId, wordCount));
	}

	private void _addNestedFieldSuggestions(
		List<SuggestionResponse<String>> suggestions, SearchHit searchHit,
		Map<String, Map<String, Float>> nestedFieldMap, String keywords,
		String sanitizerRegexp, String stopwordLanguageId, int wordCount) {

		Set<Map.Entry<String, Map<String, Float>>> entrySet =
			nestedFieldMap.entrySet();

		entrySet.forEach(
			entry -> _addNestedPathSuggestions(
				suggestions, searchHit, entry.getValue(), keywords,
				sanitizerRegexp, stopwordLanguageId, wordCount));
	}

	private void _addNestedPathSuggestions(
		List<SuggestionResponse<String>> suggestions, SearchHit searchHit,
		Map<String, Float> nestedFieldMap, String keywords,
		String sanitizerRegexp, String stopwordLanguageId, int wordCount) {

		Document document = searchHit.getDocument();

		Set<String> keySet = nestedFieldMap.keySet();

		keySet.forEach(
			fieldName -> _addDocumentFieldSuggestion(
				suggestions, _getNestedFieldValue(document, fieldName),
				searchHit.getScore(), keywords, sanitizerRegexp,
				stopwordLanguageId, wordCount));
	}

	private void _addSuggestion(
		List<SuggestionResponse<String>> suggestions, String value, float score,
		String keywords) {

		String cleanedValue = _postSanitize(value);

		Stream<SuggestionResponse<String>> stream = suggestions.stream();

		if (stream.anyMatch(
				suggestion -> _payloadEquals(
					suggestion.getPayload(), cleanedValue))) {

			return;
		}

		suggestions.add(
			new SuggestionResponse<String>(
				cleanedValue, _getFinalScore(score, cleanedValue, keywords)));
	}

	private String _findDocumentFieldMatch(String fieldValue, String keywords) {
		int idx = fieldValue.indexOf(keywords);

		if (idx >= 0) {
			return fieldValue.substring(idx);
		}

		idx = fieldValue.indexOf(keywords + " ");

		if (idx >= 0) {
			return fieldValue.substring(idx);
		}

		String[] keywordArr = keywords.split(_TOKEN_SPLITTER);

		for (String keyword : keywordArr) {
			if (keyword.length() < 2) {
				continue;
			}

			idx = fieldValue.indexOf(keyword);

			if (idx < 0) {
				continue;
			}

			return fieldValue.substring(idx);
		}

		return null;
	}

	private DataProviderSettings _getDataProviderSettings(
		SuggestionAttributes suggestionAttributes) {

		return suggestionAttributes.getDataProviderSettings(_key);
	}

	private String _getDisplayField(DataProviderSettings dataProviderSettings) {
		if (dataProviderSettings == null) {
			return null;
		}

		return GetterUtil.getString(
			dataProviderSettings.getAttribute(
				SuggestionConstants.DISPLAY_FIELD));
	}

	private List<SuggestionResponse<String>> _getDisplayFieldTypeSuggestions(
		List<SearchHit> hits, DataProviderSettings dataProviderSettings,
		Map<String, Map<String, Float>> nestedFieldMap, String keywords,
		String sanitizerRegexp) {

		List<SuggestionResponse<String>> suggestions = new ArrayList<>();

		String displayField = _getDisplayField(dataProviderSettings);

		if (Validator.isBlank(displayField)) {
			_log.error("displayField has to be set");

			return suggestions;
		}

		hits.forEach(
			searchHit -> _addDisplayFieldSuggestion(
				suggestions, searchHit, nestedFieldMap, displayField, keywords,
				sanitizerRegexp));

		return suggestions;
	}

	private Map<String, Float> _getFieldMap(
		DataProviderSettings dataProviderSettings) {

		return (Map<String, Float>)dataProviderSettings.getAttribute(
			SuggestionConstants.FIELD_MAP);
	}

	private List<SuggestionResponse<String>> _getFieldMatchTypeSuggestions(
		List<SearchHit> hits, Map<String, Float> fieldMap,
		Map<String, Map<String, Float>> nestedFieldMap, String keywords,
		String sanitizerRegexp, String stopwordLanguageId, int wordCount) {

		List<SuggestionResponse<String>> suggestions = new ArrayList<>();

		hits.forEach(
			searchHit -> _addFieldMatchSuggestions(
				suggestions, searchHit, fieldMap, nestedFieldMap, keywords,
				sanitizerRegexp, stopwordLanguageId, wordCount));

		return suggestions;
	}

	private float _getFinalScore(float score, String value, String keywords) {
		keywords = StringUtil.toLowerCase(keywords);

		float factorial = 1.0F;

		String paddedKeywords = " " + keywords + " ";
		String paddedRightKeywords = keywords + " ";

		if (value.equals(keywords)) {
			factorial = 2.5F;
		}
		else if (value.startsWith(paddedRightKeywords)) {
			factorial = 2.2F;
		}
		else if (value.contains(paddedKeywords)) {
			factorial = 1.6F;
		}
		else if (value.contains(keywords)) {
			factorial = 1.2F;
		}

		return score * factorial;
	}

	private List<SuggestionResponse<String>> _getHightlighterTypeSuggestions(
		List<SearchHit> hits, Map<String, Map<String, Float>> nestedFieldMap,
		String keywords, String sanitizerRegexp, String stopwordLanguageId,
		int wordCount) {

		List<SuggestionResponse<String>> suggestions = new ArrayList<>();

		hits.forEach(
			searchHit -> _addHighlighterSuggestions(
				suggestions, searchHit, nestedFieldMap, keywords,
				sanitizerRegexp, stopwordLanguageId, wordCount));

		return suggestions;
	}

	private Map<String, Map<String, Float>> _getNestedFieldMap(
		DataProviderSettings dataProviderSettings) {

		return (Map<String, Map<String, Float>>)
			dataProviderSettings.getAttribute(
				SuggestionConstants.NESTED_FIELD_MAP);
	}

	private String _getNestedFieldValue(Document document, String fieldName) {
		String[] parts = fieldName.split("\\.");

		Map<String, Field> nestedFieldMap =
			(Map<String, Field>)document.getValue(parts[0]);

		return GetterUtil.getString(nestedFieldMap.get(parts[1]));
	}

	private int _getOffset(DataProviderSettings dataProviderSettings) {
		if (dataProviderSettings != null) {
			return GetterUtil.getInteger(
				dataProviderSettings.getAttribute(SuggestionConstants.OFFSET),
				2);
		}

		return 2;
	}

	private List<SuggestionResponse<String>> _getResults(
		SearchHits searchHits, SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings, Map<String, Float> fieldMap,
		Map<String, Map<String, Float>> nestedFieldMap, String type) {

		if (searchHits.getTotalHits() == 0) {
			return new ArrayList<>();
		}

		String sanitizerRegExp = _getSanitizerRegExp(dataProviderSettings);

		String stopwordLanguageId = _getStopWordLanguageId(
			suggestionAttributes, dataProviderSettings);

		int wordCount = _getWordCount(
			suggestionAttributes, dataProviderSettings);

		List<SearchHit> hits = searchHits.getSearchHits();

		if (type.equals("displayField")) {
			return _getDisplayFieldTypeSuggestions(
				hits, dataProviderSettings, nestedFieldMap,
				suggestionAttributes.getKeywords(), sanitizerRegExp);
		}
		else if (type.equals("highlighter")) {
			return _getHightlighterTypeSuggestions(
				hits, nestedFieldMap, suggestionAttributes.getKeywords(),
				sanitizerRegExp, stopwordLanguageId, wordCount);
		}
		else {
			return _getFieldMatchTypeSuggestions(
				hits, fieldMap, nestedFieldMap,
				suggestionAttributes.getKeywords(), sanitizerRegExp,
				stopwordLanguageId, wordCount);
		}
	}

	private String _getSanitizerRegExp(
		DataProviderSettings dataProviderSettings) {

		if (dataProviderSettings == null) {
			return _DEFAULT_PRE_SANITIZER_REGEXP;
		}

		return GetterUtil.getString(
			dataProviderSettings.getAttribute(
				SuggestionConstants.SANITIZER_REGEXP),
			_DEFAULT_PRE_SANITIZER_REGEXP);
	}

	private String _getStopWordLanguageId(
		SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		boolean trimStopWords = GetterUtil.getBoolean(
			dataProviderSettings.getAttribute(
				SuggestionConstants.TRIM_STOPWORDS),
			_DEFAULT_TRIM_STOPWORDS);

		if (trimStopWords) {
			return LocaleUtil.toLanguageId(suggestionAttributes.getLocale());
		}

		return null;
	}

	private List<SuggestionResponse<String>> _getSuggestions(
		SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		String type = _getType(dataProviderSettings);

		Map<String, Float> fieldMap = _getFieldMap(dataProviderSettings);

		Map<String, Map<String, Float>> nestedFieldMap = _getNestedFieldMap(
			dataProviderSettings);

		SearchRequestBuilder searchRequestBuilder =
			_fieldTypeaheadDataProviderSearchHelper.getSearchRequestBuilder(
				suggestionAttributes, dataProviderSettings, fieldMap,
				nestedFieldMap, type);

		SearchResponse searchResponse =
			_fieldTypeaheadDataProviderSearchHelper.search(
				searchRequestBuilder);

		return _getResults(
			searchResponse.getSearchHits(), suggestionAttributes,
			dataProviderSettings, fieldMap, nestedFieldMap, type);
	}

	private String _getType(DataProviderSettings dataProviderSettings) {
		return GetterUtil.getString(
			dataProviderSettings.getAttribute(SuggestionConstants.TYPE),
			_DEFAULT_TYPE);
	}

	private int _getWordCount(
		SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		String keywords = suggestionAttributes.getKeywords();

		Stream<String> stream = Arrays.stream(keywords.split("\\s+"));

		int keywordCount = (int)stream.distinct(
		).count();

		int aheadCount = _getOffset(dataProviderSettings);

		return keywordCount + aheadCount;
	}

	private boolean _payloadEquals(String payload, String text) {
		return payload.equals(text);
	}

	private String _postSanitize(String s) {
		return s.replaceAll(_POST_SANITIZER_REGEXP, "");
	}

	private String _preSanitize(String s, String sanitizerRegexp) {
		int idx = s.indexOf("<hl>");

		if (idx >= 0) {
			s = s.substring(idx + 4);
		}

		s = StringUtil.removeSubstring(s, "<hl>");
		s = StringUtil.removeSubstring(s, "</hl>");
		s = s.replaceAll(sanitizerRegexp, " ");
		s = s.replaceAll(" +", " ");

		return StringUtil.toLowerCase(s);
	}

	private String _trim(
		String value, String stopWordLanguageId, int wordCount) {

		StringBundler sb = new StringBundler();

		if (!Validator.isBlank(value)) {
			String[] words = value.split(_TOKEN_SPLITTER);

			int counter = 0;

			for (int i = 0; i < words.length; i++) {
				if (words[i].length() < 2) {
					continue;
				}

				if (((counter == (wordCount - 1)) ||
					 (i == (words.length - 1))) &&
					(stopWordLanguageId != null) &&
					_stopwordHelper.isStopWord(words[i], stopWordLanguageId)) {

					continue;
				}

				if (i > 0) {
					sb.append(" ");
				}

				sb.append(words[i]);

				if (++counter == wordCount) {
					break;
				}
			}
		}

		return sb.toString();
	}

	private static final String _DEFAULT_PRE_SANITIZER_REGEXP =
		"[\"\\[\\]\\{\\}\\(\\),]|(\\. )";

	private static final boolean _DEFAULT_TRIM_STOPWORDS = true;

	private static final String _DEFAULT_TYPE = "highlighter";

	private static final String _POST_SANITIZER_REGEXP = "[\\:\\;\\-]$";

	private static final String _TOKEN_SPLITTER = "(\\s+)";

	private static final Log _log = LogFactoryUtil.getLog(
		FieldTypeaheadDataProvider.class);

	@Reference
	private FieldTypeaheadDataProviderSearchHelper
		_fieldTypeaheadDataProviderSearchHelper;

	private volatile String _key;

	@Reference
	private StopwordHelper _stopwordHelper;

}