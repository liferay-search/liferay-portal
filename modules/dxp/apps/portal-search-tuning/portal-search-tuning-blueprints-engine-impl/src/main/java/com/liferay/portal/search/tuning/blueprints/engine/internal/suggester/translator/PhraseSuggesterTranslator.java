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

package com.liferay.portal.search.tuning.blueprints.engine.internal.suggester.translator;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.suggest.PhraseSuggester;
import com.liferay.portal.kernel.search.suggest.Suggester;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.PhraseSuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.suggester.SuggesterTranslator;
import com.liferay.portal.search.tuning.blueprints.message.Messages;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "type=phrase",
	service = SuggesterTranslator.class
)
public class PhraseSuggesterTranslator
	extends BaseSuggesterTranslator implements SuggesterTranslator {

	@Override
	public Optional<Suggester> translate(
		ParameterData parameterData, Messages messages,
		JSONObject configurationJsonObject, String suggesterName) {

		if (!validateSuggesterConfiguration(
				messages, configurationJsonObject, suggesterName)) {

			return Optional.empty();
		}

		String field = configurationJsonObject.getString(
			PhraseSuggesterConfigurationKeys.FIELD.getJsonKey());

		PhraseSuggester phraseSuggester = new PhraseSuggester(
			suggesterName, field,
			getText(parameterData, configurationJsonObject));

		if (!configurationJsonObject.isNull(
				PhraseSuggesterConfigurationKeys.ANALYZER.getJsonKey())) {

			phraseSuggester.setAnalyzer(
				configurationJsonObject.getString(
					PhraseSuggesterConfigurationKeys.ANALYZER.getJsonKey()));
		}

		if (!configurationJsonObject.isNull(
				PhraseSuggesterConfigurationKeys.COLLATE.getJsonKey())) {

			_setCollate(phraseSuggester, configurationJsonObject);
		}

		if (!configurationJsonObject.isNull(
				PhraseSuggesterConfigurationKeys.CONFIDENCE.getJsonKey())) {

			phraseSuggester.setConfidence(
				GetterUtil.getFloat(
					configurationJsonObject.get(
						PhraseSuggesterConfigurationKeys.CONFIDENCE.
							getJsonKey())));
		}

		if (!configurationJsonObject.isNull(
				PhraseSuggesterConfigurationKeys.DIRECT_GENERATOR.
					getJsonKey())) {

			_setCandidateGenerator(phraseSuggester, configurationJsonObject);
		}

		if (!configurationJsonObject.isNull(
				PhraseSuggesterConfigurationKeys.FORCE_UNIGRAMS.getJsonKey())) {

			phraseSuggester.setForceUnigrams(
				configurationJsonObject.getBoolean(
					PhraseSuggesterConfigurationKeys.FORCE_UNIGRAMS.
						getJsonKey()));
		}

		if (!configurationJsonObject.isNull(
				PhraseSuggesterConfigurationKeys.GRAM_SIZE.getJsonKey())) {

			phraseSuggester.setGramSize(
				configurationJsonObject.getInt(
					PhraseSuggesterConfigurationKeys.GRAM_SIZE.getJsonKey()));
		}

		if (!configurationJsonObject.isNull(
				PhraseSuggesterConfigurationKeys.MAX_ERRORS.getJsonKey())) {

			phraseSuggester.setMaxErrors(
				GetterUtil.getFloat(
					configurationJsonObject.get(
						PhraseSuggesterConfigurationKeys.MAX_ERRORS.
							getJsonKey())));
		}

		if (!configurationJsonObject.isNull(
				PhraseSuggesterConfigurationKeys.REAL_WORLD_ERROR_LIKELIHOOD.
					getJsonKey())) {

			phraseSuggester.setRealWordErrorLikelihood(
				GetterUtil.getFloat(
					configurationJsonObject.get(
						PhraseSuggesterConfigurationKeys.
							REAL_WORLD_ERROR_LIKELIHOOD.getJsonKey())));
		}

		if (!configurationJsonObject.isNull(
				PhraseSuggesterConfigurationKeys.SEPARATOR.getJsonKey())) {

			phraseSuggester.setSeparator(
				configurationJsonObject.getString(
					PhraseSuggesterConfigurationKeys.SEPARATOR.getJsonKey()));
		}

		if (!configurationJsonObject.isNull(
				PhraseSuggesterConfigurationKeys.SHARD_SIZE.getJsonKey())) {

			phraseSuggester.setShardSize(
				configurationJsonObject.getInt(
					PhraseSuggesterConfigurationKeys.SHARD_SIZE.getJsonKey()));
		}

		if (!configurationJsonObject.isNull(
				PhraseSuggesterConfigurationKeys.SIZE.getJsonKey())) {

			phraseSuggester.setSize(
				configurationJsonObject.getInt(
					PhraseSuggesterConfigurationKeys.SIZE.getJsonKey()));
		}

		if (!configurationJsonObject.isNull(
				PhraseSuggesterConfigurationKeys.PRE_HIGHLIGHT_TAG.
					getJsonKey())) {

			phraseSuggester.setPreHighlightFilter(
				configurationJsonObject.getString(
					PhraseSuggesterConfigurationKeys.PRE_HIGHLIGHT_TAG.
						getJsonKey()));
		}

		if (!configurationJsonObject.isNull(
				PhraseSuggesterConfigurationKeys.POST_HIGHLIGHT_TAG.
					getJsonKey())) {

			phraseSuggester.setPostHighlightFilter(
				configurationJsonObject.getString(
					PhraseSuggesterConfigurationKeys.POST_HIGHLIGHT_TAG.
						getJsonKey()));
		}

		return Optional.of(phraseSuggester);
	}

	private void _setCandidateGenerator(
		PhraseSuggester phraseSuggester, JSONObject configurationJsonObject) {

		JSONArray directGeneratorJsonArray =
			configurationJsonObject.getJSONArray(
				PhraseSuggesterConfigurationKeys.DIRECT_GENERATOR.getJsonKey());

		if ((directGeneratorJsonArray == null) ||
			(directGeneratorJsonArray.length() == 0)) {

			return;
		}

		String field = configurationJsonObject.getString(
			PhraseSuggesterConfigurationKeys.FIELD.getJsonKey());

		PhraseSuggester.CandidateGenerator candidateGenerator =
			new PhraseSuggester.CandidateGenerator(field);

		// TODO: implement rest of the properties
		// https://www.elastic.co/guide/en/elasticsearch/
		// reference/6.8/search-suggesters-phrase.html

		phraseSuggester.addCandidateGenerator(candidateGenerator);
	}

	private void _setCollate(
		PhraseSuggester phraseSuggester, JSONObject configurationJsonObject) {

		JSONObject collateJsonObject = configurationJsonObject.getJSONObject(
			PhraseSuggesterConfigurationKeys.COLLATE.getJsonKey());

		if ((collateJsonObject == null) || (collateJsonObject.length() == 0)) {
		}

		// TODO: implement (using clause translators)
		// https://www.elastic.co/guide/en/elasticsearch/reference/6.8/
		// search-suggesters-phrase.html

		// Make SF happy for now

		phraseSuggester.getClass();
	}

}