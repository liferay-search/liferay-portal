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

package com.liferay.portal.search.tuning.blueprints.suggestions.attributes;

/**
 * @author Petteri Karttunen
 */
public interface SuggestionsAttributesBuilder {

	public SuggestionsAttributesBuilder addAttribute(String key, Object value);

	public SuggestionsAttributes build();

	public SuggestionsAttributesBuilder companyId(long companyId);

	public SuggestionsAttributesBuilder groupIds(Long[] groupIds);

	public SuggestionsAttributesBuilder keywords(String keywords);

	public SuggestionsAttributesBuilder languageIds(String[] languageIds);

	public SuggestionsAttributesBuilder size(int size);

	public SuggestionsAttributesBuilder status(int size);

	public SuggestionsAttributesBuilder userId(long userId);

}