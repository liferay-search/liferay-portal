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
import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public class SuggestionAttributesImpl implements SuggestionAttributes {

	public SuggestionAttributesImpl(
		Map<String, Object> attributes, Long companyId,
		Map<String, DataProviderSettings> dataProviderSettings,
		List<String> excludedDataProviders, List<String> excludedPostProcessors,
		Long groupId, List<String> includedDataProviders,
		List<String> includedPostProcessors, String ipAddress, String keywords,
		Locale locale, long plid, int size, String timezoneId, Long userId) {

		_attributes = attributes;
		_companyId = companyId;
		_dataProviderSettings = dataProviderSettings;
		_excludedDataProviders = excludedDataProviders;
		_excludedPostProcessors = excludedPostProcessors;
		_groupId = groupId;
		_includedDataProviders = includedDataProviders;
		_includedPostProcessors = includedPostProcessors;
		_ipAddress = ipAddress;
		_keywords = keywords;
		_locale = locale;
		_plid = plid;
		_size = size;
		_timezoneId = timezoneId;
		_userId = userId;
	}

	@Override
	public Object getAttribute(String key) {
		if (_attributes == null) {
			return null;
		}

		return _attributes.get(key);
	}

	@Override
	public Map<String, Object> getAttributes() {
		return _attributes;
	}

	@Override
	public Long getCompanyId() {
		return _companyId;
	}

	@Override
	public DataProviderSettings getDataProviderSettings(String key) {
		if (_dataProviderSettings == null) {
			return null;
		}

		return _dataProviderSettings.get(key);
	}

	@Override
	public List<String> getExcludedDataProviders() {
		if (_excludedDataProviders == null) {
			return Collections.emptyList();
		}

		return _excludedDataProviders;
	}

	@Override
	public List<String> getExcludedPostProcessors() {
		if (_excludedPostProcessors == null) {
			return Collections.emptyList();
		}

		return _excludedPostProcessors;
	}

	@Override
	public Long getGroupId() {
		return _groupId;
	}

	@Override
	public List<String> getIncludedDataProviders() {
		if (_includedDataProviders == null) {
			return Collections.emptyList();
		}

		return _includedDataProviders;
	}

	@Override
	public List<String> getIncludedPostProcessors() {
		if (_includedPostProcessors == null) {
			return Collections.emptyList();
		}

		return _includedPostProcessors;
	}

	@Override
	public String getIPAddress() {
		return _ipAddress;
	}

	@Override
	public String getKeywords() {
		return _keywords;
	}

	@Override
	public Locale getLocale() {
		return _locale;
	}

	@Override
	public Long getPlid() {
		return _plid;
	}

	@Override
	public Integer getSize() {
		return _size;
	}

	@Override
	public String getTimezoneId() {
		return _timezoneId;
	}

	@Override
	public Long getUserId() {
		return _userId;
	}

	private final Map<String, Object> _attributes;
	private final Long _companyId;
	private final Map<String, DataProviderSettings> _dataProviderSettings;
	private final List<String> _excludedDataProviders;
	private final List<String> _excludedPostProcessors;
	private final Long _groupId;
	private final List<String> _includedDataProviders;
	private final List<String> _includedPostProcessors;
	private final String _ipAddress;
	private final String _keywords;
	private final Locale _locale;
	private final Long _plid;
	private final Integer _size;
	private final String _timezoneId;
	private final Long _userId;

}