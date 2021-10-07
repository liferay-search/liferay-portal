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

import com.liferay.search.experiences.predict.suggestions.constants.CombineScoreStrategy;
import com.liferay.search.experiences.predict.suggestions.constants.SortStrategy;
import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public interface SuggestionAttributes {

	public Object getAttribute(String key);

	public Map<String, Object> getAttributes();

	public CombineScoreStrategy getCombineScoreStrategy();

	public Long getCompanyId();

	public DataProviderSettings getDataProviderSettings(String key);

	public List<String> getExcludedDataProviders();

	public List<String> getExcludedPostProcessors();

	public Long getGroupId();

	public List<String> getIncludedDataProviders();

	public List<String> getIncludedPostProcessors();

	public String getIPAddress();

	public String getKeywords();

	public Locale getLocale();

	public Long getPlid();

	public Integer getSize();

	public SortStrategy getSortStrategy();

	public String getTimezoneId();

	public Long getUserId();

}