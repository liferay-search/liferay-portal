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
public class Suggestion<T> {

	public Suggestion(T payload, float score, String provider) {
		_payload = payload;
		_score = score;
		_provider = provider;
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

	public String getProvider() {
		return _provider;
	}

	public float getScore() {
		return _score;
	}

	public void setScore(float score) {
		_score = score;
	}

	private Map<String, String> _attributes;
	private final T _payload;
	private final String _provider;
	private float _score;

}