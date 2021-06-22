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

package com.liferay.search.experiences.predict.suggestions.data.provider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public class DataProviderSettings {

	public void addAttribute(String key, Object value) {
		if (_attributes == null) {
			_attributes = new HashMap<>();
		}

		_attributes.putIfAbsent(key, value);
	}

	public Object getAttribute(String key) {
		if (_attributes == null) {
			return null;
		}

		return _attributes.get(key);
	}

	public Integer getTimeout() {
		return _timeout;
	}

	public Float getWeight() {
		return _weight;
	}

	public void setTimeout(int timeout) {
		_timeout = timeout;
	}

	public void setWeight(float weight) {
		_weight = weight;
	}

	private Map<String, Object> _attributes;
	private Integer _timeout;
	private Float _weight;

}