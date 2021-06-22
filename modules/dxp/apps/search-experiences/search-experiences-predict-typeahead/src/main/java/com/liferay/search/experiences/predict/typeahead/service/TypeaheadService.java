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

package com.liferay.search.experiences.predict.typeahead.service;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributes;
import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;
import com.liferay.search.experiences.predict.suggestions.service.SuggestionService;
import com.liferay.search.experiences.predict.suggestions.spi.post.processor.TypeaheadPostProcessor;
import com.liferay.search.experiences.predict.suggestions.spi.provider.TypeaheadDataProvider;
import com.liferay.search.experiences.predict.suggestions.suggestion.Suggestion;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	immediate = true, property = "suggestion.type=typeahead",
	service = SuggestionService.class
)
public class TypeaheadService implements SuggestionService {

	@Override
	public List<Suggestion<String>> getSuggestions(
		SuggestionAttributes suggestionAttributes) {

		List<Suggestion<String>> suggestions = _getSuggestions(
			suggestionAttributes);

		if (!suggestions.isEmpty()) {
			_postProcess(suggestions, suggestionAttributes);
		}

		return suggestions;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_dataProviderServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, TypeaheadDataProvider.class,
				"data.provider.key");

		_postProcessorServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, TypeaheadPostProcessor.class,
				"post.processor.key");
	}

	@Deactivate
	protected void deactivate() {
		_dataProviderServiceTrackerMap.close();
		_postProcessorServiceTrackerMap.close();
	}

	private void _combineProviderResults(
		String key, Map<String, Suggestion<String>> suggestions,
		SuggestionAttributes suggestionAttributes) {

		List<Suggestion<String>> providerSuggestions =
			_getDataProviderSuggestions(key, suggestionAttributes);

		if (ListUtil.isEmpty(providerSuggestions)) {
			return;
		}

		Stream<Suggestion<String>> stream = providerSuggestions.stream();

		float providerWeight = _getDataProviderWeight(
			key, suggestionAttributes);

		stream.forEach(
			suggestion -> _mergeResult(
				suggestions, suggestion, key, providerWeight));
	}

	private void _executePostProcessor(
		TypeaheadPostProcessor typeaheadPostProcessor,
		List<Suggestion<String>> suggestions,
		SuggestionAttributes suggestionAttributes) {

		typeaheadPostProcessor.process(suggestions, suggestionAttributes);
	}

	private List<Suggestion<String>> _getDataProviderSuggestions(
		String key, SuggestionAttributes suggestionAttributes) {

		TypeaheadDataProvider typeaheadDataProvider =
			_dataProviderServiceTrackerMap.getService(key);

		return typeaheadDataProvider.getSuggestions(suggestionAttributes);
	}

	private float _getDataProviderWeight(
		String key, SuggestionAttributes suggestionAttributes) {

		DataProviderSettings dataProviderSettings =
			suggestionAttributes.getDataProviderSettings(key);

		if ((dataProviderSettings == null) ||
			(dataProviderSettings.getWeight() == null)) {

			return 1.0F;
		}

		return dataProviderSettings.getWeight();
	}

	private float _getScore(
		Suggestion<String> suggestion, float providerWeight) {

		return suggestion.getScore() * providerWeight;
	}

	private List<Suggestion<String>> _getSuggestions(
		SuggestionAttributes suggestionAttributes) {

		Map<String, Suggestion<String>> suggestions = new HashMap<>();

		Set<String> keySet = _dataProviderServiceTrackerMap.keySet();

		Stream<String> stream = keySet.stream();

		stream.filter(
			key -> _includeComponent(
				suggestionAttributes.getIncludedDataProviders(),
				suggestionAttributes.getExcludedDataProviders(), key)
		).forEach(
			key -> _combineProviderResults(
				key, suggestions, suggestionAttributes)
		);

		return _toSortedList(suggestions, suggestionAttributes);
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

	private void _mergeResult(
		Map<String, Suggestion<String>> allSuggestions,
		Suggestion<String> suggestion, String provider, float providerWeight) {

		suggestion.setProvider(provider);

		float score = _getScore(suggestion, providerWeight);

		String payload = StringUtil.toLowerCase(suggestion.getPayload());

		if (!allSuggestions.containsKey(payload)) {
			suggestion.setScore(score);
			suggestion.setPayload(payload);

			allSuggestions.put(payload, suggestion);
		}
		else {
			Suggestion<String> existingSuggestion = allSuggestions.get(payload);

			if (existingSuggestion.getScore() < score) {
				allSuggestions.put(payload, suggestion);
			}
		}
	}

	private void _postProcess(
		List<Suggestion<String>> suggestions,
		SuggestionAttributes suggestionAttributes) {

		Set<String> keySet = _postProcessorServiceTrackerMap.keySet();

		Stream<String> stream = keySet.stream();

		stream.filter(
			key -> _includeComponent(
				suggestionAttributes.getIncludedPostProcessors(),
				suggestionAttributes.getExcludedPostProcessors(), key)
		).forEach(
			key -> _executePostProcessor(
				_postProcessorServiceTrackerMap.getService(key), suggestions,
				suggestionAttributes)
		);
	}

	private List<Suggestion<String>> _toSortedList(
		Map<String, Suggestion<String>> suggestions,
		SuggestionAttributes suggestionAttributes) {

		Collection<Suggestion<String>> entrySet = suggestions.values();

		Stream<Suggestion<String>> stream = entrySet.stream();

		return stream.sorted(
			Comparator.comparing(
				Suggestion::getScore, Comparator.reverseOrder())
		).limit(
			suggestionAttributes.getSize()
		).collect(
			Collectors.toList()
		);
	}

	private ServiceTrackerMap<String, TypeaheadDataProvider>
		_dataProviderServiceTrackerMap;
	private ServiceTrackerMap<String, TypeaheadPostProcessor>
		_postProcessorServiceTrackerMap;

}