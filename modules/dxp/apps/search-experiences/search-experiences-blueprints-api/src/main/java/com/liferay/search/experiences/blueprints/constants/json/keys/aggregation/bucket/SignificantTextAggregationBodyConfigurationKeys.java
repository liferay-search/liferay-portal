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

package com.liferay.search.experiences.blueprints.constants.json.keys.aggregation.bucket;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Petteri Karttunen
 */
public enum SignificantTextAggregationBodyConfigurationKeys {

	BACKGROUND_FILTER("background_filter"), CHI_SQUARE("chi_square"),
	EXCLUDE("exclude"), EXECUTION_HINT("execution_hint"), FIELD("field"),
	FILTER_DUPLICATE_TEXT("filter_duplicate_text"), GND("gnd"),
	INCLUDE("include"), JLH("jlh"), MIN_DOC_COUNT("min_doc_count"),
	MISSING("missing"), MUTUAL_INFORMATION("mutual_information"),
	PERCENTAGE("percentage"), SCRIPT("script"),
	SCRIPT_HEURISTIC("script_heuristic"),
	SHARD_MIN_DOC_COUNT("shard_min_doc_count"), SHARD_SIZE("shard_size"),
	SIZE("size");

	public static SignificantTextAggregationBodyConfigurationKeys findByJsonKey(
		String jsonKey) {

		Stream<SignificantTextAggregationBodyConfigurationKeys> stream =
			Arrays.stream(
				SignificantTextAggregationBodyConfigurationKeys.values());

		return stream.filter(
			value -> value._jsonKey.equals(jsonKey)
		).findFirst(
		).orElse(
			null
		);
	}

	public String getJsonKey() {
		return _jsonKey;
	}

	private SignificantTextAggregationBodyConfigurationKeys(String jsonKey) {
		_jsonKey = jsonKey;
	}

	private final String _jsonKey;

}