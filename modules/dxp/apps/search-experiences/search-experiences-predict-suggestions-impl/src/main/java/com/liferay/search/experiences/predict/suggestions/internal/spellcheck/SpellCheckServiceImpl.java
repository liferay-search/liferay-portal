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

package com.liferay.search.experiences.predict.suggestions.internal.spellcheck;

import com.liferay.search.experiences.blueprints.util.component.ServiceComponentReference;
import com.liferay.search.experiences.blueprints.util.component.ServiceComponentReferenceUtil;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionsAttributes;
import com.liferay.search.experiences.predict.suggestions.internal.util.SuggestionsUtil;
import com.liferay.search.experiences.predict.suggestions.spellcheck.SpellCheckService;
import com.liferay.search.experiences.predict.suggestions.spi.provider.SpellCheckDataProvider;
import com.liferay.search.experiences.predict.suggestions.suggestion.Suggestion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SpellCheckService.class)
public class SpellCheckServiceImpl implements SpellCheckService {

	public List<Suggestion> getSuggestions(
		SuggestionsAttributes suggestionsAttributes) {

		if (_spellCheckDataProviders.isEmpty()) {
			return new ArrayList<>();
		}

		return fetchSuggestions(suggestionsAttributes);
	}

	protected List<Suggestion> fetchSuggestions(
		SuggestionsAttributes suggestionsAttributes) {

		List<String> excludedProviders = SuggestionsUtil.getExcludedProviders(
			suggestionsAttributes);

		List<String> includedProviders = SuggestionsUtil.getIncludedProviders(
			suggestionsAttributes);

		Map<String, Suggestion> suggestions = new HashMap<>();

		for (Map.Entry
				<String, ServiceComponentReference<SpellCheckDataProvider>>
					entry : _spellCheckDataProviders.entrySet()) {

			if (!SuggestionsUtil.includeProvider(
					includedProviders, excludedProviders, entry.getKey())) {

				continue;
			}

			ServiceComponentReference<SpellCheckDataProvider> value =
				entry.getValue();

			SpellCheckDataProvider spellCheckDataProvider =
				value.getServiceComponent();

			List<Suggestion> providerSuggestions =
				spellCheckDataProvider.getSuggestions(suggestionsAttributes);

			if (providerSuggestions.isEmpty()) {
				continue;
			}

			SuggestionsUtil.combineResults(
				suggestions, providerSuggestions,
				spellCheckDataProvider.getWeight());
		}

		return SuggestionsUtil.toSortedList(suggestions, suggestionsAttributes);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void registerSpellCheckDataProvider(
		SpellCheckDataProvider spellCheckDataProvider,
		Map<String, Object> properties) {

		ServiceComponentReferenceUtil.addToMapByName(
			_spellCheckDataProviders, spellCheckDataProvider, properties);
	}

	protected void unregisterSpellCheckDataProvider(
		SpellCheckDataProvider spellCheckDataProvider,
		Map<String, Object> properties) {

		ServiceComponentReferenceUtil.removeFromMapByName(
			_spellCheckDataProviders, spellCheckDataProvider, properties);
	}

	private volatile Map
		<String, ServiceComponentReference<SpellCheckDataProvider>>
			_spellCheckDataProviders = new ConcurrentHashMap<>();

}