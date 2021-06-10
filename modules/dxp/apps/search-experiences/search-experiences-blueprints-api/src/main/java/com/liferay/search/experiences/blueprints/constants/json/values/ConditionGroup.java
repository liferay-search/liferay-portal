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

package com.liferay.search.experiences.blueprints.constants.json.values;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Petteri Karttunen
 */
public enum ConditionGroup {

	ALL_OF("all_of"), ANY_OF("any_of");

	public static ConditionGroup findBy_jsonValue(String jsonValue) {
		Stream<ConditionGroup> stream = Arrays.stream(ConditionGroup.values());

		return stream.filter(
			value -> value._jsonValue.equals(jsonValue)
		).findFirst(
		).orElse(
			null
		);
	}

	public String getJsonValue() {
		return _jsonValue;
	}

	private ConditionGroup(String jsonValue) {
		_jsonValue = jsonValue;
	}

	private final String _jsonValue;

}