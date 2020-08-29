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

package com.liferay.portal.search.tuning.blueprints.engine.internal.searchrequest;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public class SearchRequestContextBuilder {

	public SearchRequestContextBuilder attributes(
		Map<String, Object> attributes) {

		if ((attributes != null) && !attributes.isEmpty()) {
			_attributes.putAll(attributes);
		}

		return this;
	}

	public SearchRequestContextBuilder blueprint(
		JSONObject blueprintJsonObject) {

		_blueprintJsonObject = blueprintJsonObject;

		return this;
	}

	public SearchRequestContextBuilder blueprintId(long blueprintId) {
		_blueprintId = blueprintId;

		return this;
	}

	public SearchRequestContext build() {
		SearchRequestContext searchRequestContext =
			new SearchRequestContextImpl(
				_attributes, _blueprintJsonObject, _blueprintId, _companyId,
				_from, _initialKeywords, _keywords, _locale, _rawKeywords,
				_searchParameterData, _userId);

		_validateSearchRequestContext(searchRequestContext);

		return searchRequestContext;
	}

	public SearchRequestContextBuilder companyId(long companyId) {
		_companyId = companyId;

		return this;
	}

	public SearchRequestContextBuilder from(int from) {
		_from = from;

		return this;
	}

	public SearchRequestContextBuilder initialKeywords(String initialKeywords) {
		_initialKeywords = initialKeywords;

		return this;
	}

	public SearchRequestContextBuilder keywords(String keywords) {
		_keywords = keywords;

		return this;
	}

	public SearchRequestContextBuilder locale(Locale locale) {
		_locale = locale;

		return this;
	}

	public SearchRequestContextBuilder rawKeywords(String rawKeywords) {
		_rawKeywords = rawKeywords;

		return this;
	}

	public SearchRequestContextBuilder searchParameterData(
		SearchParameterData searchParameterData) {

		_searchParameterData = searchParameterData;

		return this;
	}

	public SearchRequestContextBuilder userId(long userId) {
		_userId = userId;

		return this;
	}

	private void _validateSearchRequestContext(
		SearchRequestContext queryContext) {

		if ((queryContext.getBlueprint() == null) ||
			(queryContext.getBlueprintId() == null) ||
			(queryContext.getCompanyId() == null) ||
			(queryContext.getLocale() == null) ||
			(queryContext.getUserId() == null)) {

			throw new IllegalStateException(
				"Blueprint ID, Company ID, locale, size" +
					" and user ID are mandatory");
		}
	}

	private final Map<String, Object> _attributes = new HashMap<>();
	private Long _blueprintId;
	private JSONObject _blueprintJsonObject;
	private Long _companyId;
	private int _from;
	private String _initialKeywords;
	private String _keywords;
	private Locale _locale;
	private String _rawKeywords;
	private SearchParameterData _searchParameterData;
	private Long _userId;

}