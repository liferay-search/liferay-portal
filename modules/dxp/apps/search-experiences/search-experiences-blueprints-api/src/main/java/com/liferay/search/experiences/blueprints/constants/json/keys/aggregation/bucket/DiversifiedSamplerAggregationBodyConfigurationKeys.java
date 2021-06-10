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
public enum DiversifiedSamplerAggregationBodyConfigurationKeys {

	EXECUTION_HINT("execution_hint"), FIELD("field"),
	MAX_DOCS_PER_VALUE("max_docs_per_value"), MISSING("missing"),
	SCRIPT("script"), SHARD_SIZE("shard_size");

	public static DiversifiedSamplerAggregationBodyConfigurationKeys
		findByJsonKey(String jsonKey) {

		Stream<DiversifiedSamplerAggregationBodyConfigurationKeys> stream =
			Arrays.stream(
				DiversifiedSamplerAggregationBodyConfigurationKeys.values());

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

	private DiversifiedSamplerAggregationBodyConfigurationKeys(String jsonKey) {
		_jsonKey = jsonKey;
	}

	private final String _jsonKey;

}