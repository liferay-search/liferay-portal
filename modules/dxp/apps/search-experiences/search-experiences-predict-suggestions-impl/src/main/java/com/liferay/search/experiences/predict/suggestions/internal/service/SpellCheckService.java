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

package com.liferay.search.experiences.predict.suggestions.internal.service;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributes;
import com.liferay.search.experiences.predict.suggestions.constants.CombineScoreStrategy;
import com.liferay.search.experiences.predict.suggestions.constants.SortStrategy;
import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;
import com.liferay.search.experiences.predict.suggestions.internal.wrapper.SuggestionsWrapper;
import com.liferay.search.experiences.predict.suggestions.service.SuggestionService;
import com.liferay.search.experiences.predict.suggestions.spi.provider.SpellCheckDataProvider;
import com.liferay.search.experiences.predict.suggestions.suggestion.Suggestion;
import com.liferay.search.experiences.predict.suggestions.suggestion.SuggestionResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "suggestion.type=spell_check",
	service = SuggestionService.class
)
public class SpellCheckService implements SuggestionService {

	@Override
	public List<Suggestion<String>> getSuggestions(
		SuggestionAttributes suggestionAttributes) {

		return _getSuggestions(suggestionAttributes);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_dataProviderServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, SpellCheckDataProvider.class,
				"data.provider.key");
	}

	@Deactivate
	protected void deactivate() {
		_dataProviderServiceTrackerMap.close();
	}

	private void _addDataProviderResponses(
		String provider, SuggestionsWrapper<String> suggestionsWrapper,
		SuggestionAttributes suggestionAttributes) {

		List<SuggestionResponse<String>> dataProviderResponses =
			_getDataProviderResponses(provider, suggestionAttributes);

		if (ListUtil.isEmpty(dataProviderResponses)) {
			return;
		}

		float providerWeight = _getDataProviderWeight(
			provider, suggestionAttributes);

		CombineScoreStrategy combineScoreStrategy =
			suggestionAttributes.getCombineScoreStrategy();

		dataProviderResponses.forEach(
			suggestionResponse -> _addSuggestion(
				suggestionsWrapper, suggestionResponse, provider,
				providerWeight, combineScoreStrategy));
	}

	private void _addSuggestion(
		SuggestionsWrapper<String> suggestionsWrapper,
		SuggestionResponse<String> suggestionResponse, String provider,
		float providerWeight, CombineScoreStrategy combineScoreStrategy) {

		float score = _getProviderScore(
			suggestionResponse.getScore(), providerWeight);

		String payload = StringUtil.toLowerCase(
			suggestionResponse.getPayload());

		Suggestion<String> suggestion = new Suggestion<>(
			payload, score, provider);

		if (!suggestionsWrapper.containsPayload(payload)) {
			suggestionsWrapper.addSuggestion(suggestion);
		}
		else {
			Suggestion<String> existingSuggestion =
				suggestionsWrapper.getSuggestion(payload);

			if (((combineScoreStrategy == null) ||
				 combineScoreStrategy.equals(CombineScoreStrategy.MAX)) &&
				(existingSuggestion.getScore() < score)) {

				suggestionsWrapper.addSuggestion(suggestion);
			}
			else if (((combineScoreStrategy == null) ||
					  combineScoreStrategy.equals(CombineScoreStrategy.MIN)) &&
					 (existingSuggestion.getScore() > score)) {

				suggestionsWrapper.addSuggestion(suggestion);
			}
		}

		suggestionsWrapper.addScore(suggestion);
	}

	private List<Suggestion<String>> _combineScores(
		SuggestionsWrapper<String> suggestionsWrapper,
		SuggestionAttributes suggestionAttributes) {

		CombineScoreStrategy combineScoreStrategy =
			suggestionAttributes.getCombineScoreStrategy();

		List<Suggestion<String>> suggestions =
			suggestionsWrapper.getSuggestions();

		suggestions.forEach(
			suggestion -> _scoreSuggestion(
				suggestion,
				suggestionsWrapper.getScores(suggestion.getPayload()),
				combineScoreStrategy));

		return suggestions;
	}

	private List<SuggestionResponse<String>> _getDataProviderResponses(
		String provider, SuggestionAttributes suggestionAttributes) {

		SpellCheckDataProvider spellCheckDataProvider =
			_dataProviderServiceTrackerMap.getService(provider);

		return spellCheckDataProvider.getSuggestions(suggestionAttributes);
	}

	private float _getDataProviderWeight(
		String key, SuggestionAttributes suggestionAttributes) {

		DataProviderSettings dataProviderSettings =
			suggestionAttributes.getDataProviderSettings(key);

		if ((dataProviderSettings == null) ||
			(dataProviderSettings.getWeight() == null)) {

			return _DEFAULT_DATA_PROVIDER_WEIGHT;
		}

		return dataProviderSettings.getWeight();
	}

	private float _getProviderScore(float score, float providerWeight) {
		return score * providerWeight;
	}

	private List<Suggestion<String>> _getSuggestions(
		SuggestionAttributes suggestionAttributes) {

		SuggestionsWrapper<String> suggestionsWrapper =
			new SuggestionsWrapper<>();

		Set<String> keySet = _dataProviderServiceTrackerMap.keySet();

		Stream<String> stream = keySet.stream();

		stream.filter(
			key -> _includeComponent(
				suggestionAttributes.getIncludedDataProviders(),
				suggestionAttributes.getExcludedDataProviders(), key)
		).forEach(
			key -> _addDataProviderResponses(
				key, suggestionsWrapper, suggestionAttributes)
		);

		return _toSortedList(
			_combineScores(suggestionsWrapper, suggestionAttributes),
			suggestionAttributes);
	}

	private boolean _includeComponent(
		List<String> includedClassNames, List<String> excludedClassNames,
		String provider) {

		if (includedClassNames.contains(provider)) {
			return true;
		}
		else if (excludedClassNames.contains(provider)) {
			return false;
		}

		return true;
	}

	private void _scoreSuggestion(
		Suggestion<String> suggestion, List<Float> scores,
		CombineScoreStrategy combineScoreStrategy) {

		if ((combineScoreStrategy == null) ||
			combineScoreStrategy.equals(CombineScoreStrategy.MIN) ||
			combineScoreStrategy.equals(CombineScoreStrategy.MAX)) {

			return;
		}

		Double combinedScore = 0.0;

		Stream<Float> stream = scores.stream();

		if (combineScoreStrategy.equals(CombineScoreStrategy.AVG)) {
			combinedScore = stream.mapToDouble(
				score -> score
			).average(
			).orElse(
				0.0
			);
		}
		else if (combineScoreStrategy.equals(CombineScoreStrategy.SUM)) {
			combinedScore = stream.mapToDouble(
				score -> score
			).sum();
		}

		suggestion.setScore(combinedScore.floatValue());
	}

	private List<Suggestion<String>> _toScoreSortedList(
		List<Suggestion<String>> suggestions,
		SuggestionAttributes suggestionAttributes) {

		Stream<Suggestion<String>> stream = suggestions.stream();

		return stream.sorted(
			Comparator.comparing(
				Suggestion::getScore, Comparator.reverseOrder())
		).limit(
			suggestionAttributes.getSize()
		).collect(
			Collectors.toList()
		);
	}

	private List<Suggestion<String>> _toSortedList(
		List<Suggestion<String>> suggestions,
		SuggestionAttributes suggestionAttributes) {

		SortStrategy sortStrategy = suggestionAttributes.getSortStrategy();

		if ((sortStrategy != null) && sortStrategy.equals(SortStrategy.NAN)) {
			return suggestions;
		}

		return _toScoreSortedList(suggestions, suggestionAttributes);
	}

	private static final float _DEFAULT_DATA_PROVIDER_WEIGHT = 1.0F;

	private ServiceTrackerMap<String, SpellCheckDataProvider>
		_dataProviderServiceTrackerMap;

}