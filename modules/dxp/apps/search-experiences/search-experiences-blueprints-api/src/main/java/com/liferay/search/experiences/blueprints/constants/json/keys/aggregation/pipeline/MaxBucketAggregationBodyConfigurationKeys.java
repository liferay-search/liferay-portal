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

package com.liferay.search.experiences.blueprints.constants.json.keys.aggregation.pipeline;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Petteri Karttunen
 */
public enum MaxBucketAggregationBodyConfigurationKeys {

	BUCKETS_PATH("buckets_path"), FORMAT("format"), GAP_POLICY("gap_policy");

	public static MaxBucketAggregationBodyConfigurationKeys findByJsonKey(
		String jsonKey) {

		Stream<MaxBucketAggregationBodyConfigurationKeys> stream =
			Arrays.stream(MaxBucketAggregationBodyConfigurationKeys.values());

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

	private MaxBucketAggregationBodyConfigurationKeys(String jsonKey) {
		_jsonKey = jsonKey;
	}

	private final String _jsonKey;

}