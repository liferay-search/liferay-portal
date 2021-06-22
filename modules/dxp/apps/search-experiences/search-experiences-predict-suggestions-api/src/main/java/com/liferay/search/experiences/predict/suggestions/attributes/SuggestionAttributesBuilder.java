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

package com.liferay.search.experiences.predict.suggestions.attributes;

import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;

import java.util.Locale;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Petteri Karttunen
 */
@ProviderType
public interface SuggestionAttributesBuilder {

	public SuggestionAttributesBuilder addAttribute(String key, Object value);

	public SuggestionAttributesBuilder addDataProviderSettings(
		String key, DataProviderSettings dataProviderSettings);

	public SuggestionAttributes build();

	public SuggestionAttributesBuilder companyId(long companyId);

	public SuggestionAttributesBuilder excludedDataProviders(
		String... excludedDataProviders);

	public SuggestionAttributesBuilder excludedPostProcessors(
		String... excludedPostProcessors);

	public SuggestionAttributesBuilder groupId(long groupId);

	public SuggestionAttributesBuilder includedDataProviders(
		String... includedDataProviders);

	public SuggestionAttributesBuilder includedPostProcessors(
		String... includedPostProcessors);

	public SuggestionAttributesBuilder ipAddress(String ipAddress);

	public SuggestionAttributesBuilder keywords(String keywords);

	public SuggestionAttributesBuilder locale(Locale locale);

	public SuggestionAttributesBuilder plid(long plid);

	public SuggestionAttributesBuilder size(int size);

	public SuggestionAttributesBuilder timezoneId(String timeZoneId);

	public SuggestionAttributesBuilder userId(long userId);

}