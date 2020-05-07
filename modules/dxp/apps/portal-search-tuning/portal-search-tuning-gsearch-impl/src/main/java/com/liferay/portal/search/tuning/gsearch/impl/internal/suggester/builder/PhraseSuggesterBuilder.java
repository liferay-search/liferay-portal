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

package com.liferay.portal.search.tuning.gsearch.impl.internal.suggester.builder;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.suggest.PhraseSuggester;
import com.liferay.portal.kernel.search.suggest.Suggester;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.json.keys.suggester.CommonSuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.json.keys.suggester.PhraseSuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.context.SearchRequestContext;
import com.liferay.portal.search.tuning.gsearch.spi.suggester.SuggesterBuilder;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "type=phrase", service = SuggesterBuilder.class
)
public class PhraseSuggesterBuilder
	extends BaseSuggesterBuilder implements SuggesterBuilder {

	@Override
	public Optional<Suggester> build(
		SearchRequestContext searchRequestContext,
		JSONObject configurationJsonObject) {

		if (!validateSuggesterConfiguration(
				searchRequestContext, configurationJsonObject)) {

			return Optional.empty();
		}

		String field = configurationJsonObject.getString(
			CommonSuggesterConfigurationKeys.FIELD.getJsonKey());

		String text = configurationJsonObject.getString(
			CommonSuggesterConfigurationKeys.TEXT.getJsonKey(),
			searchRequestContext.getKeywords());

		text = text.toLowerCase();

		PhraseSuggester phraseSuggester = new PhraseSuggester(
			createSuggesterName("phrase"), field, text);

		if (Validator.isNull(
				configurationJsonObject.get(
					CommonSuggesterConfigurationKeys.ANALYZER.getJsonKey()))) {

			phraseSuggester.setAnalyzer(
				configurationJsonObject.getString(
					CommonSuggesterConfigurationKeys.ANALYZER.getJsonKey()));
		}

		if (Validator.isNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.COLLATE.getJsonKey()))) {

			_setCollate(phraseSuggester, configurationJsonObject);
		}

		if (Validator.isNotNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.CONFIDENCE.
						getJsonKey()))) {

			phraseSuggester.setConfidence(
				GetterUtil.getFloat(
					configurationJsonObject.get(
						PhraseSuggesterConfigurationKeys.CONFIDENCE.
							getJsonKey())));
		}

		if (Validator.isNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.DIRECT_GENERATOR.
						getJsonKey()))) {

			_setCandidateGenerator(phraseSuggester, configurationJsonObject);
		}

		if (Validator.isNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.FORCE_UNIGRAMS.
						getJsonKey()))) {

			phraseSuggester.setForceUnigrams(
				configurationJsonObject.getBoolean(
					PhraseSuggesterConfigurationKeys.FORCE_UNIGRAMS.
						getJsonKey()));
		}

		if (Validator.isNotNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.GRAM_SIZE.getJsonKey()))) {

			phraseSuggester.setGramSize(
				configurationJsonObject.getInt(
					PhraseSuggesterConfigurationKeys.GRAM_SIZE.getJsonKey()));
		}

		if (Validator.isNotNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.MAX_ERRORS.
						getJsonKey()))) {

			phraseSuggester.setMaxErrors(
				GetterUtil.getFloat(
					configurationJsonObject.get(
						PhraseSuggesterConfigurationKeys.MAX_ERRORS.
							getJsonKey())));
		}

		if (Validator.isNotNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.
						REAL_WORLD_ERROR_LIKELIHOOD.getJsonKey()))) {

			phraseSuggester.setRealWordErrorLikelihood(
				GetterUtil.getFloat(
					configurationJsonObject.get(
						PhraseSuggesterConfigurationKeys.
							REAL_WORLD_ERROR_LIKELIHOOD.getJsonKey())));
		}

		if (Validator.isNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.SEPARATOR.getJsonKey()))) {

			phraseSuggester.setSeparator(
				configurationJsonObject.getString(
					PhraseSuggesterConfigurationKeys.SEPARATOR.getJsonKey()));
		}

		if (Validator.isNull(
				configurationJsonObject.get(
					CommonSuggesterConfigurationKeys.SHARD_SIZE.
						getJsonKey()))) {

			phraseSuggester.setShardSize(
				configurationJsonObject.getInt(
					CommonSuggesterConfigurationKeys.SHARD_SIZE.getJsonKey()));
		}

		if (Validator.isNull(
				configurationJsonObject.get(
					CommonSuggesterConfigurationKeys.SIZE.getJsonKey()))) {

			phraseSuggester.setSize(
				configurationJsonObject.getInt(
					CommonSuggesterConfigurationKeys.SIZE.getJsonKey()));
		}

		if (Validator.isNotNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.PRE_HIGHLIGHT_TAG.
						getJsonKey()))) {

			phraseSuggester.setPreHighlightFilter(
				configurationJsonObject.getString(
					PhraseSuggesterConfigurationKeys.PRE_HIGHLIGHT_TAG.
						getJsonKey()));
		}

		if (Validator.isNotNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.POST_HIGHLIGHT_TAG.
						getJsonKey()))) {

			phraseSuggester.setPreHighlightFilter(
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
			CommonSuggesterConfigurationKeys.FIELD.getJsonKey());

		PhraseSuggester.CandidateGenerator candidateGenerator =
			new PhraseSuggester.CandidateGenerator(field);

		// TODO: implement rest of the properties
		// https://www.elastic.co/guide/en/elasticsearch/reference/6.8/search-suggesters-phrase.html

		phraseSuggester.addCandidateGenerator(candidateGenerator);
	}

	private void _setCollate(
		PhraseSuggester phraseSuggester, JSONObject configurationJsonObject) {

		JSONObject collateJsonObject = configurationJsonObject.getJSONObject(
			PhraseSuggesterConfigurationKeys.COLLATE.getJsonKey());

		if ((collateJsonObject == null) || (collateJsonObject.length() == 0)) {
			return;
		}

		// TODO: implement. Use clause builders.
		// https://www.elastic.co/guide/en/elasticsearch/reference/6.8/search-suggesters-phrase.html

		/*
		Collate collate = new PhraseSuggester.Collate(query);
		boolean prune = collateJsonObject.getBoolean(
				PhraseSuggesterConfigurationKeys.PRUNE);
		collate.setPrune(true);
		collate.addParams(key, value);
		phraseSuggester.setCollate(collate);
		*/

	}

}