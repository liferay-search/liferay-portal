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

package com.liferay.search.experiences.blueprints.engine.internal.searchrequest;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.suggest.Suggester;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.blueprints.constants.json.keys.aggregation.AggregationConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.internal.suggester.SuggesterTranslatorFactory;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.search.experiences.blueprints.engine.spi.suggester.SuggesterTranslator;
import com.liferay.search.experiences.blueprints.engine.template.variable.BlueprintTemplateVariableParser;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.blueprints.util.util.BlueprintJSONUtil;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;

import java.util.Optional;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=suggest",
	service = SearchRequestBodyContributor.class
)
public class SuggestSearchRequestBodyContributor
	implements SearchRequestBodyContributor {

	@Override
	public void contribute(
		SearchRequestBuilder searchRequestBuilder, Blueprint blueprint,
		ParameterData parameterData, Messages messages) {

		Optional<JSONObject> optional =
			_blueprintHelper.getSuggestConfigurationOptional(blueprint);

		if (!optional.isPresent()) {
			return;
		}

		_processSuggesters(
			searchRequestBuilder, optional.get(), parameterData, messages);
	}

	private Optional<Suggester> _getSuggesterOptional(
		String name, String type, JSONObject jsonObject,
		ParameterData parameterData, Messages messages) {

		if (!_isEnabled(jsonObject)) {
			return Optional.empty();
		}

		try {
			Optional<JSONObject> optional =
				_blueprintTemplateVariableParser.parseObject(
					jsonObject, parameterData, messages);

			if (!optional.isPresent()) {
				return Optional.empty();
			}

			SuggesterTranslator suggesterTranslator =
				_suggesterTranslatorFactory.getTranslator(type);

			return suggesterTranslator.translate(
				name, optional.get(), parameterData, messages);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			MessagesUtil.invalidConfigurationValueError(
				messages, getClass().getName(), illegalArgumentException, null,
				null, type);
		}

		return Optional.empty();
	}

	private boolean _isEnabled(JSONObject jsonObject) {
		return jsonObject.getBoolean(
			AggregationConfigurationKeys.ENABLED.getJsonKey(), true);
	}

	private void _processSuggester(
		SearchRequestBuilder searchRequestBuilder, String suggesterName,
		JSONObject jsonObject, ParameterData parameterData, Messages messages) {

		JSONObject nameJSONObject = jsonObject.getJSONObject(suggesterName);

		Optional<String> typeOptional = BlueprintJSONUtil.getFirstKeyOptional(
			nameJSONObject);

		if (!typeOptional.isPresent()) {
			return;
		}

		String type = typeOptional.get();

		JSONObject typeJSONObject = nameJSONObject.getJSONObject(type);

		Optional<Suggester> suggesterOptional = _getSuggesterOptional(
			suggesterName, type, typeJSONObject, parameterData, messages);

		if (!suggesterOptional.isPresent()) {
			return;
		}

		// TODO: https://issues.liferay.com/browse/LPS-120711

		searchRequestBuilder.getClass();
	}

	private void _processSuggesters(
		SearchRequestBuilder searchRequestBuilder, JSONObject jsonObject,
		ParameterData parameterData, Messages messages) {

		Set<String> keySet = jsonObject.keySet();

		keySet.forEach(
			suggesterName -> _processSuggester(
				searchRequestBuilder, suggesterName, jsonObject, parameterData,
				messages));
	}

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private SuggesterTranslatorFactory _suggesterTranslatorFactory;

}