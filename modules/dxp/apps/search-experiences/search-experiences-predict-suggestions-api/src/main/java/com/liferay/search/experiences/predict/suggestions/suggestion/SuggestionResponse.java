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

package com.liferay.search.experiences.predict.suggestions.suggestion;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public class SuggestionResponse<T> {

	public SuggestionResponse(T payload, float score) {
		_payload = payload;
		_score = score;
	}

	public void addAttribute(String key, String value) {
		if (_attributes == null) {
			_attributes = new HashMap<>();
		}

		_attributes.put(key, value);
	}

	public Object getAttribute(String key) {
		if (_attributes == null) {
			return null;
		}

		return _attributes.get(key);
	}

	public Map<String, String> getAttributes() {
		return _attributes;
	}

	public T getPayload() {
		return _payload;
	}

	public float getScore() {
		return _score;
	}

	private Map<String, String> _attributes;
	private final T _payload;
	private final float _score;

}