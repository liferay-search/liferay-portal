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
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.suggest.Suggester;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.engine.internal.suggester.SuggesterTranslatorFactory;
import com.liferay.search.experiences.blueprints.engine.internal.util.BlueprintJSONUtil;
import com.liferay.search.experiences.blueprints.engine.internal.util.ProblemBuilders;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.search.experiences.blueprints.engine.spi.suggester.SuggesterTranslator;
import com.liferay.search.experiences.blueprints.engine.template.variable.BlueprintTemplateVariableParser;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

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
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<JSONObject> optional =
			_blueprintHelper.getSuggestConfigurationOptional(blueprint);

		if (!optional.isPresent()) {
			return;
		}

		_processSuggesters(
			searchRequestBuilder, optional.get(), parameterData,
			problemsHolderBuilder);
	}

	private Optional<Suggester> _getSuggesterOptional(
		String name, String type, JSONObject jsonObject,
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		if (!_isEnabled(jsonObject)) {
			return Optional.empty();
		}

		try {
			Optional<JSONObject> optional =
				_blueprintTemplateVariableParser.parseObject(
					jsonObject, parameterData, problemsHolderBuilder);

			if (!optional.isPresent()) {
				return Optional.empty();
			}

			SuggesterTranslator suggesterTranslator =
				_suggesterTranslatorFactory.getTranslator(type);

			return suggesterTranslator.translate(
				name, optional.get(), parameterData, problemsHolderBuilder);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			_log.error(illegalArgumentException);

			ProblemBuilder problemBuilder =
				_problemBuilders.invalidConfigurationValueError(
					getClass().getName(), illegalArgumentException, null, null,
					type);

			problemsHolderBuilder.addProblem(problemBuilder.build());
		}

		return Optional.empty();
	}

	private boolean _isEnabled(JSONObject jsonObject) {
		return jsonObject.getBoolean("enabled", true);
	}

	private void _processSuggester(
		SearchRequestBuilder searchRequestBuilder, String suggesterName,
		JSONObject jsonObject, ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		JSONObject nameJSONObject = jsonObject.getJSONObject(suggesterName);

		Optional<String> typeOptional = BlueprintJSONUtil.getFirstKeyOptional(
			nameJSONObject);

		if (!typeOptional.isPresent()) {
			return;
		}

		String type = typeOptional.get();

		JSONObject typeJSONObject = nameJSONObject.getJSONObject(type);

		Optional<Suggester> suggesterOptional = _getSuggesterOptional(
			suggesterName, type, typeJSONObject, parameterData,
			problemsHolderBuilder);

		suggesterOptional.ifPresent(
			suggester -> searchRequestBuilder.addSelectedFieldNames(
				suggester.getName()));
	}

	private void _processSuggesters(
		SearchRequestBuilder searchRequestBuilder, JSONObject jsonObject,
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Set<String> keySet = jsonObject.keySet();

		keySet.forEach(
			suggesterName -> _processSuggester(
				searchRequestBuilder, suggesterName, jsonObject, parameterData,
				problemsHolderBuilder));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SuggestSearchRequestBodyContributor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private ProblemBuilders _problemBuilders;

	@Reference
	private SuggesterTranslatorFactory _suggesterTranslatorFactory;

}