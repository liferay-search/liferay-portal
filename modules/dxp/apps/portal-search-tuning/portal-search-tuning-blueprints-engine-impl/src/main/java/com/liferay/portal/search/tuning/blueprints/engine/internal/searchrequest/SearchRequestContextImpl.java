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
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Petteri Karttunen
 */
public class SearchRequestContextImpl implements SearchRequestContext {

	public SearchRequestContextImpl(
		SearchRequestBuilder searchRequestBuilder,
		Map<String, Object> attributes, JSONObject blueprintJsonObject,
		long blueprintId, Long companyId, int from, String initialKeywords,
		String keywords, Locale locale, String rawKeywords,
		SearchParameterData searchParameterData, Long userId) {

		_searchRequestBuilder = searchRequestBuilder;
		_attributes = attributes;
		_blueprintJsonObject = blueprintJsonObject;
		_blueprintId = blueprintId;
		_companyId = companyId;
		_from = from;
		_initialKeywords = initialKeywords;
		_keywords = keywords;
		_locale = locale;
		_rawKeywords = rawKeywords;
		_searchParameterData = searchParameterData;
		_userId = userId;
	}

	@Override
	public void addMessage(Message message) {
		_messages.add(message);
	}

	@Override
	public Map<String, Object> getAttributes() {
		return _attributes;
	}

	@Override
	public JSONObject getBlueprint() {
		return _blueprintJsonObject;
	}

	@Override
	public Long getBlueprintId() {
		return _blueprintId;
	}

	@Override
	public Long getCompanyId() {
		return _companyId;
	}

	@Override
	public int getFrom() {
		return _from;
	}

	@Override
	public Optional<String> getInitialKeywords() {
		if (!Validator.isBlank(_initialKeywords)) {
			return Optional.of(_initialKeywords);
		}

		return Optional.empty();
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
	public List<Message> getMessages() {
		return _messages;
	}

	@Override
	public String getRawKeywords() {
		return _rawKeywords;
	}

	@Override
	public SearchParameterData getSearchParameterData() {
		return _searchParameterData;
	}

	@Override
	public SearchRequestBuilder getSearchRequestBuilder() {
		return _searchRequestBuilder;
	}

	@Override
	public Long getUserId() {
		return _userId;
	}

	@Override
	public boolean hasErrors() {
		Stream<Message> stream = _messages.stream();

		return stream.anyMatch(
			m -> m.getSeverity(
			).equals(
				Severity.ERROR
			));
	}

	private final Map<String, Object> _attributes;
	private final long _blueprintId;
	private final JSONObject _blueprintJsonObject;
	private final Long _companyId;
	private final int _from;
	private final String _initialKeywords;
	private final String _keywords;
	private final Locale _locale;
	private final List<Message> _messages = new ArrayList<>();
	private final String _rawKeywords;
	private final SearchParameterData _searchParameterData;
	private final SearchRequestBuilder _searchRequestBuilder;
	private final Long _userId;

}