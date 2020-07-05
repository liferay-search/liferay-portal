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

package com.liferay.portal.search.tuning.gsearch.configuration.constants.json.keys.query;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Petteri Karttunen
 */
public enum StringQueryConfigurationKeys {

	ALLOW_LEADING_WILDCARD("allow_leading_wildcard"),
	ANALYZE_WILDCARD("analyze_wildcard"), ANALYZER("analyzer"), BOOST("boos"),
	DEFAULT_OPERATOR("default_operator"),
	ENABLE_POSITION_INCREMENTS("enable_position_increments"),
	FUZZINESS("fuzziness"), FUZZY_MAX_EXPANSIONS("fuzzy_max_expansions"),
	FUZZY_PREFIX_LENGTH("fuzzy_prefix_length"), LENIENT("lenient"),
	MAX_DETERMINED_STATES("max_determined_states"), PHRASE_SLOP("phrase_slop"),
	QUERY("query"), QUOTE_ANALYZER("quote_analyzer"),
	QUOTE_FIELD_SUFFIX("quote_field_suffix");

	public static final StringQueryConfigurationKeys findByJsonKey(
		String jsonKey) {

		Stream<StringQueryConfigurationKeys>
			completionSuggesterConfigurationKeysStream = Arrays.stream(
				StringQueryConfigurationKeys.values());

		return completionSuggesterConfigurationKeysStream.filter(
			value -> value._jsonKey.equals(jsonKey)
		).findFirst(
		).orElse(
			null
		);
	}

	public String getJsonKey() {
		return _jsonKey;
	}

	private StringQueryConfigurationKeys(String jsonKey) {
		_jsonKey = jsonKey;
	}

	private final String _jsonKey;

}