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

package com.liferay.search.experiences.predict.suggestions.internal.attributes;

import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributes;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributesBuilder;
import com.liferay.search.experiences.predict.suggestions.constants.CombineScoreStrategy;
import com.liferay.search.experiences.predict.suggestions.constants.SortStrategy;
import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public class SuggestionAttributesBuilderImpl
	implements SuggestionAttributesBuilder {

	@Override
	public SuggestionAttributesBuilder addAttribute(String key, Object value) {
		if (_attributes == null) {
			_attributes = new HashMap<>();
		}

		_attributes.putIfAbsent(key, value);

		return this;
	}

	@Override
	public SuggestionAttributesBuilder addDataProviderSettings(
		String key, DataProviderSettings dataProviderSettings) {

		if (_dataProviderSettings == null) {
			_dataProviderSettings = new HashMap<>();
		}

		_dataProviderSettings.putIfAbsent(key, dataProviderSettings);

		return this;
	}

	@Override
	public SuggestionAttributes build() {
		SuggestionAttributes suggestionAttributes =
			new SuggestionAttributesImpl(
				_attributes, _combineScoreStrategy, _companyId,
				_dataProviderSettings, _excludedDataProviders,
				_excludedPostProcessors, _groupId, _includedDataProviders,
				_includedPostProcessors, _ipAddress, _keywords, _locale, _plid,
				_size, _sortStrategy, _timezoneId, _userId);

		_validateSuggestionAttributes(suggestionAttributes);

		return suggestionAttributes;
	}

	@Override
	public SuggestionAttributesBuilder combineScoreStrategy(
		CombineScoreStrategy combineScoreStrategy) {

		_combineScoreStrategy = combineScoreStrategy;

		return this;
	}

	@Override
	public SuggestionAttributesBuilder companyId(long companyId) {
		_companyId = companyId;

		return this;
	}

	@Override
	public SuggestionAttributesBuilder excludedDataProviders(
		String... excludedDataProviders) {

		_excludedDataProviders = Arrays.asList(excludedDataProviders);

		return this;
	}

	@Override
	public SuggestionAttributesBuilder excludedPostProcessors(
		String... excludedPostProcessors) {

		_excludedPostProcessors = Arrays.asList(excludedPostProcessors);

		return this;
	}

	@Override
	public SuggestionAttributesBuilder groupId(long groupId) {
		_groupId = groupId;

		return this;
	}

	@Override
	public SuggestionAttributesBuilder includedDataProviders(
		String... includedDataProviders) {

		_includedDataProviders = Arrays.asList(includedDataProviders);

		return this;
	}

	@Override
	public SuggestionAttributesBuilder includedPostProcessors(
		String... includedPostProcessors) {

		_includedPostProcessors = Arrays.asList(includedPostProcessors);

		return this;
	}

	@Override
	public SuggestionAttributesBuilder ipAddress(String ipAddress) {
		_ipAddress = ipAddress;

		return this;
	}

	@Override
	public SuggestionAttributesBuilder keywords(String keywords) {
		_keywords = keywords;

		return this;
	}

	@Override
	public SuggestionAttributesBuilder locale(Locale locale) {
		_locale = locale;

		return this;
	}

	@Override
	public SuggestionAttributesBuilder plid(long plid) {
		_plid = plid;

		return this;
	}

	@Override
	public SuggestionAttributesBuilder size(int size) {
		_size = size;

		return this;
	}

	@Override
	public SuggestionAttributesBuilder sortStrategy(SortStrategy sortStrategy) {
		_sortStrategy = sortStrategy;

		return this;
	}

	@Override
	public SuggestionAttributesBuilder timezoneId(String timezoneId) {
		_timezoneId = timezoneId;

		return this;
	}

	@Override
	public SuggestionAttributesBuilder userId(long userId) {
		_userId = userId;

		return this;
	}

	private void _validateSuggestionAttributes(
		SuggestionAttributes typeaheadSuggestionAttributes) {

		if ((typeaheadSuggestionAttributes.getCompanyId() == null) ||
			(typeaheadSuggestionAttributes.getGroupId() == null) ||
			(typeaheadSuggestionAttributes.getKeywords() == null) ||
			(typeaheadSuggestionAttributes.getSize() == null)) {

			throw new IllegalStateException(
				"Company id, group id, keyword and size are mandatory " +
					"attributes");
		}
	}

	private Map<String, Object> _attributes;
	private CombineScoreStrategy _combineScoreStrategy;
	private Long _companyId;
	private Map<String, DataProviderSettings> _dataProviderSettings;
	private List<String> _excludedDataProviders;
	private List<String> _excludedPostProcessors;
	private Long _groupId;
	private List<String> _includedDataProviders;
	private List<String> _includedPostProcessors;
	private String _ipAddress;
	private String _keywords;
	private Locale _locale;
	private Long _plid;
	private Integer _size;
	private SortStrategy _sortStrategy;
	private String _timezoneId;
	private Long _userId;

}