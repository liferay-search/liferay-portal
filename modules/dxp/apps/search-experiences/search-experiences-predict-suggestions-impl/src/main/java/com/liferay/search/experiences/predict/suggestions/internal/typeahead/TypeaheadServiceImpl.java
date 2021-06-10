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

package com.liferay.search.experiences.predict.suggestions.internal.typeahead;

import com.liferay.search.experiences.blueprints.util.component.ServiceComponentReference;
import com.liferay.search.experiences.blueprints.util.component.ServiceComponentReferenceUtil;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionsAttributes;
import com.liferay.search.experiences.predict.suggestions.internal.util.SuggestionsUtil;
import com.liferay.search.experiences.predict.suggestions.spi.provider.TypeaheadDataProvider;
import com.liferay.search.experiences.predict.suggestions.suggestion.Suggestion;
import com.liferay.search.experiences.predict.suggestions.typeahead.TypeaheadService;

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
@Component(immediate = true, service = TypeaheadService.class)
public class TypeaheadServiceImpl implements TypeaheadService {

	public List<Suggestion> getSuggestions(
		SuggestionsAttributes suggestionsAttributes) {

		if (_typeaheadDataProviders.isEmpty()) {
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

		for (Map.Entry<String, ServiceComponentReference<TypeaheadDataProvider>>
				entry : _typeaheadDataProviders.entrySet()) {

			if (!SuggestionsUtil.includeProvider(
					includedProviders, excludedProviders, entry.getKey())) {

				continue;
			}

			ServiceComponentReference<TypeaheadDataProvider> value =
				entry.getValue();

			TypeaheadDataProvider typeaheadDataProvider =
				value.getServiceComponent();

			List<Suggestion> providerSuggestions =
				typeaheadDataProvider.getSuggestions(suggestionsAttributes);

			if (providerSuggestions.isEmpty()) {
				continue;
			}

			SuggestionsUtil.combineResults(
				suggestions, providerSuggestions,
				typeaheadDataProvider.getWeight());
		}

		return SuggestionsUtil.toSortedList(suggestions, suggestionsAttributes);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void registerTypeaheadDataProvider(
		TypeaheadDataProvider typeaheadDataProvider,
		Map<String, Object> properties) {

		ServiceComponentReferenceUtil.addToMapByName(
			_typeaheadDataProviders, typeaheadDataProvider, properties);
	}

	protected void unregisterTypeaheadDataProvider(
		TypeaheadDataProvider typeaheadDataProvider,
		Map<String, Object> properties) {

		ServiceComponentReferenceUtil.removeFromMapByName(
			_typeaheadDataProviders, typeaheadDataProvider, properties);
	}

	private volatile Map
		<String, ServiceComponentReference<TypeaheadDataProvider>>
			_typeaheadDataProviders = new ConcurrentHashMap<>();

}