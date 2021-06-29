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

package com.liferay.search.experiences.predict.suggestions.internal.wrapper;

import com.liferay.search.experiences.predict.suggestions.suggestion.Suggestion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Petteri Karttunen
 */
public class SuggestionsWrapper<T> {

	public void addScore(Suggestion<T> suggestion) {
		List<Float> scores = _payloadScoreMap.get(suggestion.getPayload());

		if (scores == null) {
			scores = new ArrayList<>();

			_payloadScoreMap.put(suggestion.getPayload(), scores);
		}

		scores.add(suggestion.getScore());
	}

	public void addSuggestion(Suggestion<T> suggestion) {
		_keyMap.put(suggestion.getPayload(), suggestion);
	}

	public boolean containsPayload(String payload) {
		return _keyMap.containsKey(payload);
	}

	public List<Float> getScores(T payload) {
		return _payloadScoreMap.get(payload);
	}

	public Suggestion<T> getSuggestion(String key) {
		return _keyMap.get(key);
	}

	public List<Suggestion<T>> getSuggestions() {
		Collection<Suggestion<T>> values = _keyMap.values();

		Stream<Suggestion<T>> stream = values.stream();

		return stream.collect(Collectors.toList());
	}

	private final Map<T, Suggestion<T>> _keyMap = new HashMap<>();
	private final Map<T, List<Float>> _payloadScoreMap = new HashMap<>();

}