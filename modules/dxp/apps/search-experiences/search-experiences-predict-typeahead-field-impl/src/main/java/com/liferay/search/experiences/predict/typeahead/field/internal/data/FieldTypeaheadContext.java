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

package com.liferay.search.experiences.predict.typeahead.field.internal.data;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.query.Operator;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributes;
import com.liferay.search.experiences.predict.suggestions.constants.SuggestionConstants;
import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;
import com.liferay.search.experiences.predict.typeahead.field.constants.FieldTypeaheadConstants;
import com.liferay.search.experiences.predict.typeahead.field.definition.FieldTypeaheadSourceDefinition;
import com.liferay.search.experiences.predict.typeahead.field.definition.FieldsSourceDefinition;
import com.liferay.search.experiences.predict.typeahead.field.definition.NestedFieldSourceDefinition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Petteri Karttunen
 */
public class FieldTypeaheadContext {

	public FieldTypeaheadContext(
		SuggestionAttributes suggestionAttributes, String key) {

		DataProviderSettings dataProviderSettings =
			suggestionAttributes.getDataProviderSettings(key);

		if (dataProviderSettings == null) {
			throw new IllegalStateException(
				"Data provider settings are required");
		}

		_sourceDefinitions = _getSourceDefinitions(dataProviderSettings);

		if (ListUtil.isEmpty(_sourceDefinitions)) {
			throw new IllegalStateException("No source definitions provided");
		}

		_companyId = suggestionAttributes.getCompanyId();
		_displayField = _getDisplayField(dataProviderSettings);
		_entryClassNames = _getEntryClassNames(dataProviderSettings);
		_excludeDDMStructureContentField = _excludeDDMStructureContentField(
			dataProviderSettings);
		_fuzziness = _getFuzziness(dataProviderSettings);
		_groupId = suggestionAttributes.getGroupId();
		_indices = _getIndices(dataProviderSettings);
		_keywords = suggestionAttributes.getKeywords();
		_locale = suggestionAttributes.getLocale();
		_operator = _getOperator(dataProviderSettings);
		_prefixLength = _getPrefixLength(dataProviderSettings);
		_preSanitizerRegExp = _getPreSanitizerRegExp(dataProviderSettings);
		_size = suggestionAttributes.getSize();
		_sortFieldMap = _getSortFieldMap(dataProviderSettings);
		_sourceGroupIds = _getSourceGroupIds(dataProviderSettings);
		_stopWordLanguageId = _getStopWordLanguageId(
			suggestionAttributes, dataProviderSettings);
		_type = _getType(dataProviderSettings);
		_wordCount = _getWordCount(dataProviderSettings, _keywords);

		Set<String> fieldNames = new HashSet<>();

		Map<String, Set<String>> nestedFieldMap = new HashMap<>();

		Set<String> nestedFieldNames = new HashSet<>();

		_sourceDefinitions.forEach(
			sourceDefinition -> {
				if (sourceDefinition instanceof FieldsSourceDefinition) {
					FieldsSourceDefinition fieldsSourceDefinition =
						(FieldsSourceDefinition)sourceDefinition;

					Map<String, Float> fieldsBoosts =
						fieldsSourceDefinition.getFieldsBoosts();

					fieldNames.addAll(fieldsBoosts.keySet());
				}
				else if (sourceDefinition instanceof
							NestedFieldSourceDefinition) {

					NestedFieldSourceDefinition nestedFieldSourceDefinition =
						(NestedFieldSourceDefinition)sourceDefinition;

					Set<String> sourceFieldNames = new HashSet<>();

					sourceFieldNames.add(
						nestedFieldSourceDefinition.getValueFieldName());

					nestedFieldMap.put(
						nestedFieldSourceDefinition.getPath(),
						sourceFieldNames);

					nestedFieldNames.add(
						nestedFieldSourceDefinition.getValueFieldName());
				}
			});

		_fieldNames = fieldNames;

		_nestedFieldMap = nestedFieldMap;

		_nestedFieldNames = nestedFieldNames;

		_allFieldNames = _getAllFieldNames(_fieldNames, _nestedFieldNames);
	}

	public boolean excludeDDMStructureContentField() {
		return _excludeDDMStructureContentField;
	}

	public String[] getAllFieldNames() {
		return _allFieldNames;
	}

	public long getCompanyId() {
		return _companyId;
	}

	public String getDisplayField() {
		return _displayField;
	}

	public String[] getEntryClassNames() {
		return _entryClassNames;
	}

	public Set<String> getFieldNames() {
		return _fieldNames;
	}

	public String getFuzziness() {
		return _fuzziness;
	}

	public long getGroupId() {
		return _groupId;
	}

	public String[] getIndices() {
		return _indices;
	}

	public String getKeywords() {
		return _keywords;
	}

	public Locale getLocale() {
		return _locale;
	}

	public Map<String, Set<String>> getNestedFieldMap() {
		return _nestedFieldMap;
	}

	public Set<String> getNestedFieldNames() {
		return _nestedFieldNames;
	}

	public Operator getOperator() {
		return _operator;
	}

	public String getPostSanitizerRegExp() {
		return _DEFAULT_POST_SANITIZER_REGEXP;
	}

	public int getPrefixLength() {
		return _prefixLength;
	}

	public String getPreSanitizerRegExp() {
		return _preSanitizerRegExp;
	}

	public int getSize() {
		return _size;
	}

	public Map<String, String> getSortFieldMap() {
		return _sortFieldMap;
	}

	public List<FieldTypeaheadSourceDefinition> getSourceDefinitions() {
		return _sourceDefinitions;
	}

	public long[] getSourceGroupIds() {
		return _sourceGroupIds;
	}

	public String getStopWordLanguageId() {
		return _stopWordLanguageId;
	}

	public String getTokenSplitter() {
		return _TOKEN_SPLITTER;
	}

	public String getType() {
		return _type;
	}

	public int getWordCount() {
		return _wordCount;
	}

	private boolean _excludeDDMStructureContentField(
		DataProviderSettings dataProviderSettings) {

		return GetterUtil.getBoolean(
			dataProviderSettings.getAttribute(
				FieldTypeaheadConstants.EXCLUDE_DDM_STRUCTURE_CONTENT_FIELD),
			true);
	}

	private String[] _getAllFieldNames(
		Set<String> fieldNames, Set<String> nestedFieldNames) {

		Set<String> allFieldNames = new HashSet<>();

		allFieldNames.addAll(fieldNames);
		allFieldNames.addAll(nestedFieldNames);
		allFieldNames.addAll(_fixedFieldNames);

		return allFieldNames.toArray(new String[0]);
	}

	private String _getDisplayField(DataProviderSettings dataProviderSettings) {
		return GetterUtil.getString(
			dataProviderSettings.getAttribute(
				FieldTypeaheadConstants.DISPLAY_FIELD));
	}

	private String[] _getEntryClassNames(
		DataProviderSettings dataProviderSettings) {

		return GetterUtil.getStringValues(
			dataProviderSettings.getAttribute(
				FieldTypeaheadConstants.ENTRY_CLASS_NAMES));
	}

	private String _getFuzziness(DataProviderSettings dataProviderSettings) {
		return GetterUtil.getString(
			dataProviderSettings.getAttribute(SuggestionConstants.FUZZINESS),
			_DEFAULT_FUZZINESS);
	}

	private String[] _getIndices(DataProviderSettings dataProviderSettings) {
		return GetterUtil.getStringValues(
			dataProviderSettings.getAttribute(FieldTypeaheadConstants.INDICES));
	}

	private int _getOffset(DataProviderSettings dataProviderSettings) {
		return GetterUtil.getInteger(
			dataProviderSettings.getAttribute(FieldTypeaheadConstants.OFFSET),
			_DEFAULT_OFFSET);
	}

	private Operator _getOperator(DataProviderSettings dataProviderSettings) {
		String operator = GetterUtil.getString(
			dataProviderSettings.getAttribute(SuggestionConstants.OPERATOR));

		if (Validator.isBlank(operator)) {
			return _DEFAULT_OPERATOR;
		}

		return Operator.valueOf(StringUtil.toUpperCase(operator));
	}

	private int _getPrefixLength(DataProviderSettings dataProviderSettings) {
		return GetterUtil.getInteger(
			dataProviderSettings.getAttribute(
				SuggestionConstants.PREFIX_LENGTH),
			_DEFAULT_PREFIX_LENGTH);
	}

	private String _getPreSanitizerRegExp(
		DataProviderSettings dataProviderSettings) {

		return GetterUtil.getString(
			dataProviderSettings.getAttribute(
				FieldTypeaheadConstants.PRE_SANITIZER_REGEXP),
			_DEFAULT_PRE_SANITIZER_REGEXP);
	}

	private Map<String, String> _getSortFieldMap(
		DataProviderSettings dataProviderSettings) {

		return (Map<String, String>)dataProviderSettings.getAttribute(
			FieldTypeaheadConstants.SORT_FIELD_MAP);
	}

	private List<FieldTypeaheadSourceDefinition> _getSourceDefinitions(
		DataProviderSettings dataProviderSettings) {

		return (List<FieldTypeaheadSourceDefinition>)
			dataProviderSettings.getAttribute(
				FieldTypeaheadConstants.SOURCE_DEFINITIONS);
	}

	private long[] _getSourceGroupIds(
		DataProviderSettings dataProviderSettings) {

		return GetterUtil.getLongValues(
			dataProviderSettings.getAttribute(
				SuggestionConstants.SOURCE_GROUP_IDS));
	}

	private String _getStopWordLanguageId(
		SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		boolean trimStopWords = GetterUtil.getBoolean(
			dataProviderSettings.getAttribute(
				FieldTypeaheadConstants.TRIM_STOPWORDS),
			_DEFAULT_TRIM_STOPWORDS);

		if (trimStopWords) {
			return LocaleUtil.toLanguageId(suggestionAttributes.getLocale());
		}

		return null;
	}

	private String _getType(DataProviderSettings dataProviderSettings) {
		return GetterUtil.getString(
			dataProviderSettings.getAttribute(FieldTypeaheadConstants.TYPE),
			_DEFAULT_TYPE);
	}

	private int _getWordCount(
		DataProviderSettings dataProviderSettings, String keywords) {

		Stream<String> stream = Arrays.stream(keywords.split("\\s+"));

		int keywordCount = (int)stream.distinct(
		).count();

		int aheadCount = _getOffset(dataProviderSettings);

		return keywordCount + aheadCount;
	}

	private static final String _DEFAULT_FUZZINESS = "1";

	private static final int _DEFAULT_OFFSET = 1;

	private static final Operator _DEFAULT_OPERATOR = Operator.AND;

	private static final String _DEFAULT_POST_SANITIZER_REGEXP =
		"[ \\:\\;\\-\\.]$";

	private static final String _DEFAULT_PRE_SANITIZER_REGEXP =
		"[\\?;\"\\[\\]\\{\\}\\(\\),]|(\\. )";

	private static final int _DEFAULT_PREFIX_LENGTH = 2;

	private static final boolean _DEFAULT_TRIM_STOPWORDS = true;

	private static final String _DEFAULT_TYPE = "highlighter";

	private static final String _TOKEN_SPLITTER = "(\\s+)";

	private static final Set<String> _fixedFieldNames = new HashSet<String>() {
		{
			add("ddmStructureKey");
			add("entryClassName");
		}
	};

	private final String[] _allFieldNames;
	private final long _companyId;
	private final String _displayField;
	private final String[] _entryClassNames;
	private final boolean _excludeDDMStructureContentField;
	private final Set<String> _fieldNames;
	private final String _fuzziness;
	private final long _groupId;
	private final String[] _indices;
	private final String _keywords;
	private final Locale _locale;
	private final Map<String, Set<String>> _nestedFieldMap;
	private final Set<String> _nestedFieldNames;
	private final Operator _operator;
	private final int _prefixLength;
	private final String _preSanitizerRegExp;
	private final int _size;
	private final Map<String, String> _sortFieldMap;
	private final List<FieldTypeaheadSourceDefinition> _sourceDefinitions;
	private final long[] _sourceGroupIds;
	private final String _stopWordLanguageId;
	private final String _type;
	private final int _wordCount;

}