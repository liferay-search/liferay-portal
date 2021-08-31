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

package com.liferay.search.experiences.predict.typeahead.field.internal.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.Field;
import com.liferay.portal.search.highlight.HighlightField;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.search.experiences.predict.suggestions.suggestion.SuggestionResponse;
import com.liferay.search.experiences.predict.typeahead.field.internal.data.FieldTypeaheadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = FieldTypeaheadResponseHelper.class)
public class FieldTypeaheadResponseHelper {

	public List<SuggestionResponse<String>> buildResults(
		SearchHits searchHits, FieldTypeaheadContext typeaheadContext) {

		if (searchHits.getTotalHits() == 0) {
			return new ArrayList<>();
		}

		String type = typeaheadContext.getType();

		List<SearchHit> hits = searchHits.getSearchHits();

		if (type.equals("displayField")) {
			return _getDisplayFieldTypeSuggestions(hits, typeaheadContext);
		}
		else if (type.equals("highlighter")) {
			return _getHightlighterTypeSuggestions(hits, typeaheadContext);
		}
		else {
			return _getFieldMatchTypeSuggestions(hits, typeaheadContext);
		}
	}

	private void _addDisplayFieldSuggestion(
		List<SuggestionResponse<String>> suggestions, SearchHit searchHit,
		FieldTypeaheadContext typeaheadContext) {

		Document document = searchHit.getDocument();

		String displayField = typeaheadContext.getDisplayField();

		Set<String> nestedFieldNames = typeaheadContext.getNestedFieldNames();

		String value = null;

		if (nestedFieldNames.contains(displayField)) {
			value = _getNestedFieldValue(document, displayField);
		}
		else {
			value = document.getString(displayField);
		}

		if (Validator.isBlank(value)) {
			return;
		}

		_addSuggestion(
			suggestions,
			_preSanitize(value, typeaheadContext.getPreSanitizerRegExp()),
			searchHit.getScore(), typeaheadContext);
	}

	private void _addDocumentFieldSuggestion(
		List<SuggestionResponse<String>> suggestions, String value, float score,
		FieldTypeaheadContext typeaheadContext) {

		if (Validator.isBlank(value)) {
			return;
		}

		value = _findDocumentFieldMatch(
			value, typeaheadContext.getKeywords(),
			typeaheadContext.getTokenSplitter());

		if (Validator.isBlank(value)) {
			return;
		}

		_addSuggestion(
			suggestions, _trim(value, typeaheadContext), score,
			typeaheadContext);
	}

	private void _addDocumentFieldSuggestions(
		List<SuggestionResponse<String>> suggestions, SearchHit searchHit,
		FieldTypeaheadContext typeaheadContext) {

		Document document = searchHit.getDocument();

		Set<String> fieldNames = typeaheadContext.getFieldNames();

		fieldNames.forEach(
			fieldName -> _addDocumentFieldSuggestion(
				suggestions, _getFieldValue(document, fieldName),
				searchHit.getScore(), typeaheadContext));
	}

	private void _addFieldMatchSuggestions(
		List<SuggestionResponse<String>> suggestions, SearchHit searchHit,
		FieldTypeaheadContext typeaheadContext) {

		_addDocumentFieldSuggestions(suggestions, searchHit, typeaheadContext);

		if (!MapUtil.isEmpty(typeaheadContext.getNestedFieldMap())) {
			_addNestedFieldSuggestions(
				suggestions, searchHit, typeaheadContext);
		}
	}

	private void _addHighlighterSuggestions(
		List<SuggestionResponse<String>> suggestions, SearchHit searchHit,
		FieldTypeaheadContext typeaheadContext) {

		Map<String, HighlightField> highlightFieldsMap =
			searchHit.getHighlightFieldsMap();

		if (MapUtil.isNotEmpty(highlightFieldsMap)) {
			_addHighlightFieldSuggestions(
				suggestions, highlightFieldsMap, searchHit, typeaheadContext);
		}

		if (!MapUtil.isEmpty(typeaheadContext.getNestedFieldMap())) {
			_addNestedFieldSuggestions(
				suggestions, searchHit, typeaheadContext);
		}
	}

	private void _addHighlightFieldSuggestion(
		List<SuggestionResponse<String>> suggestions,
		HighlightField highlightField, float score,
		FieldTypeaheadContext typeaheadContext) {

		List<String> fragments = highlightField.getFragments();

		fragments.forEach(
			fragment -> {
				String value = _trim(fragment, typeaheadContext);

				if (Validator.isBlank(value)) {
					return;
				}

				_addSuggestion(suggestions, value, score, typeaheadContext);
			});
	}

	private void _addHighlightFieldSuggestions(
		List<SuggestionResponse<String>> suggestions,
		Map<String, HighlightField> highlightFieldsMap, SearchHit searchHit,
		FieldTypeaheadContext typeaheadContext) {

		Document document = searchHit.getDocument();

		String ddmStructureKey = document.getString("ddmStructureKey");

		String entryClassName = document.getString("entryClassName");

		float score = searchHit.getScore();

		Set<Map.Entry<String, HighlightField>> entrySet =
			highlightFieldsMap.entrySet();

		Stream<Map.Entry<String, HighlightField>> stream = entrySet.stream();

		if (entryClassName.equals("com.liferay.journal.model.JournalArticle") &&
			!ddmStructureKey.equals("BASIC-WEB-CONTENT") &&
			typeaheadContext.excludeDDMStructureContentField()) {

			stream.filter(
				entry -> !_isContentField(entry.getKey())
			).forEach(
				entry -> _addHighlightFieldSuggestion(
					suggestions, entry.getValue(), score, typeaheadContext)
			);
		}
		else {
			entrySet.forEach(
				entry -> _addHighlightFieldSuggestion(
					suggestions, entry.getValue(), score, typeaheadContext));
		}
	}

	private void _addNestedFieldSuggestions(
		List<SuggestionResponse<String>> suggestions, SearchHit searchHit,
		FieldTypeaheadContext typeaheadContext) {

		Map<String, Set<String>> nestedFieldMap =
			typeaheadContext.getNestedFieldMap();

		Set<Map.Entry<String, Set<String>>> entrySet =
			nestedFieldMap.entrySet();

		entrySet.forEach(
			entry -> _addNestedPathSuggestions(
				suggestions, searchHit, entry.getValue(), typeaheadContext));
	}

	private void _addNestedPathSuggestions(
		List<SuggestionResponse<String>> suggestions, SearchHit searchHit,
		Set<String> nestedFieldNames, FieldTypeaheadContext typeaheadContext) {

		Document document = searchHit.getDocument();

		nestedFieldNames.forEach(
			fieldName -> {
				String value = _getNestedFieldValue(document, fieldName);

				if (Validator.isBlank(value)) {
					return;
				}

				_addDocumentFieldSuggestion(
					suggestions, value, searchHit.getScore(), typeaheadContext);
			});
	}

	private void _addSuggestion(
		List<SuggestionResponse<String>> suggestions, String value, float score,
		FieldTypeaheadContext typeaheadContext) {

		String cleanedValue = _postSanitize(
			value, typeaheadContext.getPostSanitizerRegExp());

		Stream<SuggestionResponse<String>> stream = suggestions.stream();

		if (stream.anyMatch(
				suggestion -> _payloadEquals(
					suggestion.getPayload(), cleanedValue))) {

			return;
		}

		suggestions.add(
			new SuggestionResponse<String>(
				cleanedValue,
				_getFinalScore(
					score, cleanedValue, typeaheadContext.getKeywords())));
	}

	private String _findDocumentFieldMatch(
		String fieldValue, String keywords, String tokenSplitter) {

		fieldValue = StringUtil.toLowerCase(fieldValue);

		if (fieldValue.startsWith(keywords)) {
			return fieldValue;
		}

		String s = _findSubstring(fieldValue, keywords);

		if (!Validator.isBlank(s)) {
			return s;
		}

		String[] keywordArr = keywords.split(tokenSplitter);

		for (String keyword : keywordArr) {
			if (keyword.length() < 2) {
				continue;
			}

			if (!Validator.isBlank(s)) {
				return s;
			}
		}

		return null;
	}

	private String _findSubstring(String value, String match) {
		if (value.startsWith(match)) {
			return value;
		}

		int idx = value.indexOf(" " + match + " ");

		if (idx >= 0) {
			return value.substring(idx + 1);
		}

		idx = value.indexOf(" " + match);

		if (idx >= 0) {
			return value.substring(idx + 1);
		}

		return null;
	}

	private List<SuggestionResponse<String>> _getDisplayFieldTypeSuggestions(
		List<SearchHit> hits, FieldTypeaheadContext typeaheadContext) {

		List<SuggestionResponse<String>> suggestions = new ArrayList<>();

		if (Validator.isBlank(typeaheadContext.getDisplayField())) {
			_log.error("displayField has to be set");

			return suggestions;
		}

		hits.forEach(
			searchHit -> _addDisplayFieldSuggestion(
				suggestions, searchHit, typeaheadContext));

		return suggestions;
	}

	private List<SuggestionResponse<String>> _getFieldMatchTypeSuggestions(
		List<SearchHit> hits, FieldTypeaheadContext typeaheadContext) {

		List<SuggestionResponse<String>> suggestions = new ArrayList<>();

		hits.forEach(
			searchHit -> _addFieldMatchSuggestions(
				suggestions, searchHit, typeaheadContext));

		return suggestions;
	}

	private String _getFieldValue(Document document, String fieldName) {
		return document.getString(fieldName);
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
		List<SearchHit> hits, FieldTypeaheadContext typeaheadContext) {

		List<SuggestionResponse<String>> suggestions = new ArrayList<>();

		hits.forEach(
			searchHit -> _addHighlighterSuggestions(
				suggestions, searchHit, typeaheadContext));

		return suggestions;
	}

	private String _getNestedFieldValue(Document document, String fieldName) {
		String[] parts = fieldName.split("\\.");

		Map<String, Field> nestedFieldMap =
			(Map<String, Field>)document.getValue(parts[0]);

		if (nestedFieldMap == null) {
			return null;
		}

		return GetterUtil.getString(nestedFieldMap.get(parts[1]));
	}

	private boolean _isContentField(String fieldName) {
		return fieldName.startsWith("content_");
	}

	private boolean _payloadEquals(String payload, String text) {
		return payload.equals(text);
	}

	private String _postSanitize(String s, String regExp) {
		return s.replaceAll(regExp, "");
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

	private String _trim(String value, FieldTypeaheadContext typeaheadContext) {
		value = _preSanitize(value, typeaheadContext.getPreSanitizerRegExp());

		StringBundler sb = new StringBundler();

		if (!Validator.isBlank(value)) {
			String stopWordLanguageId =
				typeaheadContext.getStopWordLanguageId();

			int wordCount = typeaheadContext.getWordCount();

			String[] words = value.split(typeaheadContext.getTokenSplitter());

			int counter = 0;

			for (int i = 0; i < words.length; i++) {
				String s = words[i];

				if (s.length() < 2) {
					continue;
				}

				if (((counter == (wordCount - 1)) ||
					 (i == (words.length - 1))) &&
					(stopWordLanguageId != null) &&
					_stopwordHelper.isStopWord(s, stopWordLanguageId)) {

					continue;
				}

				if (i > 0) {
					sb.append(" ");
				}

				sb.append(s);

				if (++counter == wordCount) {
					break;
				}
			}
		}

		return sb.toString();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FieldTypeaheadResponseHelper.class);

	@Reference
	private StopwordHelper _stopwordHelper;

}