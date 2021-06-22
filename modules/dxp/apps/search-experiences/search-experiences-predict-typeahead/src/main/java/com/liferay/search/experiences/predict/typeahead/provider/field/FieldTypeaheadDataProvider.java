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

package com.liferay.search.experiences.predict.typeahead.provider.field;

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
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributes;
import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;
import com.liferay.search.experiences.predict.suggestions.spi.provider.TypeaheadDataProvider;
import com.liferay.search.experiences.predict.suggestions.suggestion.Suggestion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	public List<Suggestion<String>> getSuggestions(
		SuggestionAttributes suggestionAttributes) {

		DataProviderSettings dataProviderSettings = _getDataProviderSettings(
			suggestionAttributes);

		Map<String, Float> fieldMap = _getFieldMap(dataProviderSettings);

		if (fieldMap == null) {
			if (_log.isWarnEnabled()) {
				_log.warn("Fields have to be defined");
			}

			return new ArrayList<>();
		}

		Map<String, Map<String, Float>> nestedFieldMap = _getNestedFieldMap(
			dataProviderSettings);

		return _getSuggestions(
			suggestionAttributes, dataProviderSettings, fieldMap,
			nestedFieldMap);
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_key = GetterUtil.getString(properties.get("data.provider.key"));
	}

	private void _addDisplayFieldSuggestion(
		List<Suggestion<String>> suggestions, SearchHit searchHit,
		Map<String, Map<String, Float>> nestedFieldMap, String displayField,
		String keywords, String sanitizerRegexp) {

		String value = null;

		Document document = searchHit.getDocument();

		if ((nestedFieldMap != null) &&
			nestedFieldMap.containsKey(displayField)) {

			_getNestedFieldValue(document, displayField);
		}
		else {
			value = document.getString(displayField);
		}

		value = _sanitize(value, sanitizerRegexp);

		suggestions.add(
			new Suggestion<String>(
				value, _getFinalScore(searchHit.getScore(), value, keywords)));
	}

	private void _addDocumentFieldSuggestion(
		List<Suggestion<String>> suggestions, String value, float score,
		String keywords, String sanitizerRegexp, int wordCount) {

		if (Validator.isBlank(value)) {
			return;
		}

		value = _sanitize(value, sanitizerRegexp);

		value = _findDocumentFieldMatch(value, keywords);

		if (Validator.isBlank(value)) {
			return;
		}

		suggestions.add(
			new Suggestion<String>(
				_trim(value, wordCount),
				_getFinalScore(score, value, keywords)));
	}

	private void _addDocumentFieldSuggestions(
		List<Suggestion<String>> suggestions, SearchHit searchHit,
		Map<String, Float> fieldMap, String keywords, String sanitizerRegexp,
		int wordCount) {

		Document document = searchHit.getDocument();

		Set<String> keySet = fieldMap.keySet();

		keySet.forEach(
			fieldName -> _addDocumentFieldSuggestion(
				suggestions, document.getString(fieldName),
				searchHit.getScore(), keywords, sanitizerRegexp, wordCount));
	}

	private void _addFieldMatchSuggestions(
		List<Suggestion<String>> suggestions, SearchHit searchHit,
		Map<String, Float> fieldMap,
		Map<String, Map<String, Float>> nestedFieldMap, String keywords,
		String sanitizerRegexp, int wordCount) {

		_addDocumentFieldSuggestions(
			suggestions, searchHit, fieldMap, keywords, sanitizerRegexp,
			wordCount);

		if (!MapUtil.isEmpty(nestedFieldMap)) {
			_addNestedFieldSuggestions(
				suggestions, searchHit, nestedFieldMap, keywords,
				sanitizerRegexp, wordCount);
		}
	}

	private void _addHighlighterSuggestions(
		List<Suggestion<String>> suggestions, SearchHit searchHit,
		Map<String, Map<String, Float>> nestedFieldMap, String keywords,
		String sanitizerRegexp, int wordCount) {

		Map<String, HighlightField> highlightFieldsMap =
			searchHit.getHighlightFieldsMap();

		if (MapUtil.isNotEmpty(highlightFieldsMap)) {
			_addHighlightFieldSuggestions(
				suggestions, highlightFieldsMap, searchHit.getScore(), keywords,
				sanitizerRegexp, wordCount);
		}

		if (!MapUtil.isEmpty(nestedFieldMap)) {
			_addNestedFieldSuggestions(
				suggestions, searchHit, nestedFieldMap, keywords,
				sanitizerRegexp, wordCount);
		}
	}

	private void _addHighlightFieldSuggestion(
		List<Suggestion<String>> suggestions, HighlightField highlightField,
		float score, String keywords, String sanitizerRegexp, int wordCount) {

		List<String> fragments = highlightField.getFragments();

		fragments.forEach(
			fragment -> {
				String value = _trim(
					_sanitize(fragment, sanitizerRegexp), wordCount);

				suggestions.add(
					new Suggestion<String>(
						value, _getFinalScore(score, value, keywords)));
			});
	}

	private void _addHighlightFieldSuggestions(
		List<Suggestion<String>> suggestions,
		Map<String, HighlightField> highlightFieldsMap, float score,
		String keywords, String sanitizerRegexp, int wordCount) {

		Set<Entry<String, HighlightField>> entrySet =
			highlightFieldsMap.entrySet();

		entrySet.forEach(
			entry -> _addHighlightFieldSuggestion(
				suggestions, entry.getValue(), score, keywords, sanitizerRegexp,
				wordCount));
	}

	private void _addNestedFieldSuggestions(
		List<Suggestion<String>> suggestions, SearchHit searchHit,
		Map<String, Map<String, Float>> nestedFieldMap, String keywords,
		String sanitizerRegexp, int wordCount) {

		Set<Entry<String, Map<String, Float>>> entrySet =
			nestedFieldMap.entrySet();

		entrySet.forEach(
			entry -> _addNestedPathSuggestions(
				suggestions, searchHit, entry.getValue(), keywords,
				sanitizerRegexp, wordCount));
	}

	private void _addNestedPathSuggestions(
		List<Suggestion<String>> suggestions, SearchHit searchHit,
		Map<String, Float> nestedFieldMap, String keywords,
		String sanitizerRegexp, int wordCount) {

		Document document = searchHit.getDocument();

		Set<String> keySet = nestedFieldMap.keySet();

		keySet.forEach(
			fieldName -> _addDocumentFieldSuggestion(
				suggestions, _getNestedFieldValue(document, fieldName),
				searchHit.getScore(), keywords, sanitizerRegexp, wordCount));
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

		String[] keywordArr = keywords.split(_tokenSplitter);

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

	private List<Suggestion<String>> _getSuggestions(
		SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings, Map<String, Float> fieldMap,
		Map<String, Map<String, Float>> nestedFieldMap) {

		String type = _getType(dataProviderSettings);

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

	private DataProviderSettings _getDataProviderSettings(
		SuggestionAttributes suggestionAttributes) {

		return suggestionAttributes.getDataProviderSettings(_key);
	}

	private String _getDisplayField(DataProviderSettings dataProviderSettings) {
		if (dataProviderSettings == null) {
			return null;
		}

		return GetterUtil.getString(
			dataProviderSettings.getAttribute("displayField"));
	}

	private List<Suggestion<String>> _getDisplayFieldTypeSuggestions(
		List<SearchHit> hits, DataProviderSettings dataProviderSettings,
		Map<String, Map<String, Float>> nestedFieldMap, String keywords,
		String sanitizerRegexp) {

		List<Suggestion<String>> suggestions = new ArrayList<>();

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

		if (dataProviderSettings == null) {
			return null;
		}

		return (Map<String, Float>)dataProviderSettings.getAttribute(
			"fieldMap");
	}

	private List<Suggestion<String>> _getFieldMatchTypeSuggestions(
		List<SearchHit> hits, Map<String, Float> fieldMap,
		Map<String, Map<String, Float>> nestedFieldMap, String keywords,
		String sanitizerRegexp, int wordCount) {

		List<Suggestion<String>> suggestions = new ArrayList<>();

		hits.forEach(
			searchHit -> _addFieldMatchSuggestions(
				suggestions, searchHit, fieldMap, nestedFieldMap, keywords,
				sanitizerRegexp, wordCount));

		return suggestions;
	}

	private float _getFinalScore(float score, String value, String keywords) {
		keywords = StringUtil.toLowerCase(keywords);

		float factorial = 1.0F;

		String paddedKeywords = " " + keywords + " ";
		String paddedRightKeywords = keywords + " ";

		if (value.equals(keywords)) {
			factorial = 2.0F;
		}
		else if (value.startsWith(paddedRightKeywords)) {
			factorial = 1.8F;
		}
		else if (value.contains(paddedKeywords)) {
			factorial = 1.6F;
		}
		else if (value.contains(keywords)) {
			factorial = 1.2F;
		}

		return score * factorial;
	}

	private List<Suggestion<String>> _getHightlighterTypeSuggestions(
		List<SearchHit> hits, Map<String, Map<String, Float>> nestedFieldMap,
		String keywords, String sanitizerRegexp, int wordCount) {

		List<Suggestion<String>> suggestions = new ArrayList<>();

		hits.forEach(
			searchHit -> _addHighlighterSuggestions(
				suggestions, searchHit, nestedFieldMap, keywords,
				sanitizerRegexp, wordCount));

		return suggestions;
	}

	private Map<String, Map<String, Float>> _getNestedFieldMap(
		DataProviderSettings dataProviderSettings) {

		if (dataProviderSettings == null) {
			return null;
		}

		return (Map<String, Map<String, Float>>)
			dataProviderSettings.getAttribute("nestedFieldMap");
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
				dataProviderSettings.getAttribute("offset"), 2);
		}

		return 2;
	}

	private List<Suggestion<String>> _getResults(
		SearchHits searchHits, SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings, Map<String, Float> fieldMap,
		Map<String, Map<String, Float>> nestedFieldMap, String type) {

		if (searchHits.getTotalHits() == 0) {
			return new ArrayList<>();
		}

		String sanitizerRegExp = _getSanitizerRegExp(dataProviderSettings);

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
				sanitizerRegExp, wordCount);
		}
		else {
			return _getFieldMatchTypeSuggestions(
				hits, fieldMap, nestedFieldMap,
				suggestionAttributes.getKeywords(), sanitizerRegExp, wordCount);
		}
	}

	private String _getSanitizerRegExp(
		DataProviderSettings dataProviderSettings) {

		if (dataProviderSettings == null) {
			return _defaultSanitizerRegExp;
		}

		return GetterUtil.getString(
			dataProviderSettings.getAttribute("sanitizerRegExp"),
			_defaultSanitizerRegExp);
	}

	private String _getType(DataProviderSettings dataProviderSettings) {
		if (dataProviderSettings == null) {
			return "highlighter";
		}

		return GetterUtil.getString(dataProviderSettings.getAttribute("type"));
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

	private String _sanitize(String s, String sanitizerRegexp) {
		s = s.replaceAll(sanitizerRegexp, " ");

		return StringUtil.toLowerCase(s);
	}

	private String _trim(String value, int wordCount) {
		StringBundler sb = new StringBundler();

		if (!Validator.isBlank(value)) {
			String[] words = value.split(_tokenSplitter);

			if (words.length > wordCount) {
				int counter = 0;

				for (int i = 0; (i < words.length) && (counter <= wordCount);
					 i++) {

					if (words[i].length() < 2) {
						continue;
					}

					if (i > 0) {
						sb.append(" ");
					}

					sb.append(words[i]);
					counter++;
				}
			}

			else

			sb.append(value);
		}

		return sb.toString();
	}

	private static String _defaultSanitizerRegExp =
		"[\"\\[\\]\\{\\}\\(\\),(\\. )]";
	private static String _tokenSplitter = "(\\s+)";

	private String _key;

	private static final Log _log = LogFactoryUtil.getLog(
		FieldTypeaheadDataProvider.class);

	@Reference
	private FieldTypeaheadDataProviderSearchHelper
		_fieldTypeaheadDataProviderSearchHelper;

}