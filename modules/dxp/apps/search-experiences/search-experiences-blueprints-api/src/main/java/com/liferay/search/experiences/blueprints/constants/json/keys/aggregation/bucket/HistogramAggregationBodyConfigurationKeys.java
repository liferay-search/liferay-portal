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
public enum HistogramAggregationBodyConfigurationKeys {

	FIELD("field"), INTERVAL("interval"), KEYED("keyed"),
	MIN_DOC_COUNT("min_doc_count"), MISSING("missing"), OFFSET("offset"),
	ORDER("order"), SCRIPT("script");

	public static HistogramAggregationBodyConfigurationKeys findByJsonKey(
		String jsonKey) {

		Stream<HistogramAggregationBodyConfigurationKeys> stream =
			Arrays.stream(HistogramAggregationBodyConfigurationKeys.values());

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

	private HistogramAggregationBodyConfigurationKeys(String jsonKey) {
		_jsonKey = jsonKey;
	}

	private final String _jsonKey;

}