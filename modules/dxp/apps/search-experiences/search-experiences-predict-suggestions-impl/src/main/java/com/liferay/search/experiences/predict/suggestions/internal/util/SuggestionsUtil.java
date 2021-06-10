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

package com.liferay.search.experiences.predict.suggestions.internal.util;

import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionsAttributes;
import com.liferay.search.experiences.predict.suggestions.constants.SuggestionsConstants;
import com.liferay.search.experiences.predict.suggestions.suggestion.Suggestion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Petteri Karttunen
 */
public class SuggestionsUtil {

	public static void combineResults(
		Map<String, Suggestion> suggestions,
		List<Suggestion> providerSuggestions, int weight) {

		Stream<Suggestion> stream = providerSuggestions.stream();

		stream.forEach(
			suggestion -> {
				float score = suggestion.getScore() * weight;
				String text = suggestion.getText();

				if (!suggestions.containsKey(text)) {
					suggestion.setScore(score);
					suggestion.setText(StringUtil.toLowerCase(text));

					suggestions.put(text, suggestion);
				}
				else {
					Suggestion existingSuggestion = suggestions.get(text);

					if (existingSuggestion.getScore() < score) {
						suggestions.put(text, suggestion);
					}
				}
			});
	}

	public static List<String> getExcludedProviders(
		SuggestionsAttributes suggestionsAttributes) {

		return _toProvidersList(
			suggestionsAttributes.getAttributeOptional(
				SuggestionsConstants.EXCLUDED_PROVIDERS));
	}

	public static List<String> getIncludedProviders(
		SuggestionsAttributes suggestionsAttributes) {

		return _toProvidersList(
			suggestionsAttributes.getAttributeOptional(
				SuggestionsConstants.INCLUDED_PROVIDERS));
	}

	public static boolean includeProvider(
		List<String> includedProviders, List<String> excludedProviders,
		String provider) {

		if (includedProviders.contains(provider)) {
			return true;
		}
		else if (excludedProviders.contains(provider)) {
			return false;
		}

		return true;
	}

	public static List<Suggestion> toSortedList(
		Map<String, Suggestion> suggestions,
		SuggestionsAttributes suggestionsAttributes) {

		Collection<Suggestion> entrySet = suggestions.values();

		Stream<Suggestion> stream = entrySet.stream();

		return stream.sorted(
			Comparator.comparing(
				Suggestion::getScore, Comparator.reverseOrder())
		).limit(
			suggestionsAttributes.getSize()
		).collect(
			Collectors.toList()
		);
	}

	private static List<String> _toProvidersList(Optional<Object> optional) {
		if (!optional.isPresent()) {
			return new ArrayList<>();
		}

		Object object = optional.get();

		if (!List.class.isAssignableFrom(object.getClass())) {
			return new ArrayList<>();
		}

		return (List<String>)object;
	}

}