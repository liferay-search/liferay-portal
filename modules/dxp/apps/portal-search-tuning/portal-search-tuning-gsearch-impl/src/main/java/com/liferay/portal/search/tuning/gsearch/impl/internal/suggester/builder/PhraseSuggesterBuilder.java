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
import com.liferay.portal.search.tuning.gsearch.configuration.constants.suggester.PhraseSuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.suggester.SuggesterConfigurationKeys;
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
			SuggesterConfigurationKeys.FIELD);

		String text = configurationJsonObject.getString(
			SuggesterConfigurationKeys.TEXT,
			searchRequestContext.getKeywords());

		text = text.toLowerCase();

		PhraseSuggester phraseSuggester = new PhraseSuggester(
			createSuggesterName("phrase"), field, text);

		if (Validator.isNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.ANALYZER))) {

			phraseSuggester.setAnalyzer(
				configurationJsonObject.getString(
					PhraseSuggesterConfigurationKeys.ANALYZER));
		}

		if (Validator.isNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.COLLATE))) {

			_setCollate(phraseSuggester, configurationJsonObject);
		}

		if (Validator.isNotNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.CONFIDENCE))) {

			phraseSuggester.setConfidence(
				GetterUtil.getFloat(
					configurationJsonObject.get(
						PhraseSuggesterConfigurationKeys.CONFIDENCE)));
		}

		if (Validator.isNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.DIRECT_GENERATOR))) {

			_setCandidateGenerator(phraseSuggester, configurationJsonObject);
		}

		if (Validator.isNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.FORCE_UNIGRAMS))) {

			phraseSuggester.setForceUnigrams(
				configurationJsonObject.getBoolean(
					PhraseSuggesterConfigurationKeys.FORCE_UNIGRAMS));
		}

		if (Validator.isNotNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.GRAM_SIZE))) {

			phraseSuggester.setGramSize(
				configurationJsonObject.getInt(
					PhraseSuggesterConfigurationKeys.GRAM_SIZE));
		}

		if (Validator.isNotNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.MAX_ERRORS))) {

			phraseSuggester.setMaxErrors(
				GetterUtil.getFloat(
					configurationJsonObject.get(
						PhraseSuggesterConfigurationKeys.MAX_ERRORS)));
		}

		if (Validator.isNotNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.
						REAL_WORLD_ERROR_LIKELIHOOD))) {

			phraseSuggester.setRealWordErrorLikelihood(
				GetterUtil.getFloat(
					configurationJsonObject.get(
						PhraseSuggesterConfigurationKeys.
							REAL_WORLD_ERROR_LIKELIHOOD)));
		}

		if (Validator.isNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.SEPARATOR))) {

			phraseSuggester.setSeparator(
				configurationJsonObject.getString(
					PhraseSuggesterConfigurationKeys.SEPARATOR));
		}

		if (Validator.isNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.SHARD_SIZE))) {

			phraseSuggester.setShardSize(
				configurationJsonObject.getInt(
					PhraseSuggesterConfigurationKeys.SHARD_SIZE));
		}

		if (Validator.isNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.SIZE))) {

			phraseSuggester.setSize(
				configurationJsonObject.getInt(
					PhraseSuggesterConfigurationKeys.SIZE));
		}

		if (Validator.isNotNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.PRE_HIGHLIGHT_TAG))) {

			phraseSuggester.setPreHighlightFilter(
				configurationJsonObject.getString(
					PhraseSuggesterConfigurationKeys.PRE_HIGHLIGHT_TAG));
		}

		if (Validator.isNotNull(
				configurationJsonObject.get(
					PhraseSuggesterConfigurationKeys.POST_HIGHLIGHT_TAG))) {

			phraseSuggester.setPreHighlightFilter(
				configurationJsonObject.getString(
					PhraseSuggesterConfigurationKeys.POST_HIGHLIGHT_TAG));
		}

		return Optional.of(phraseSuggester);
	}

	private void _setCandidateGenerator(
		PhraseSuggester phraseSuggester, JSONObject configurationJsonObject) {

		JSONArray directGeneratorJsonArray =
			configurationJsonObject.getJSONArray(
				PhraseSuggesterConfigurationKeys.DIRECT_GENERATOR);

		if ((directGeneratorJsonArray == null) ||
			(directGeneratorJsonArray.length() == 0)) {

			return;
		}

		String field = configurationJsonObject.getString(
			SuggesterConfigurationKeys.FIELD);

		PhraseSuggester.CandidateGenerator candidateGenerator =
			new PhraseSuggester.CandidateGenerator(field);

		// TODO: implement rest of the properties
		// https://www.elastic.co/guide/en/elasticsearch/reference/6.8/search-suggesters-phrase.html

		phraseSuggester.addCandidateGenerator(candidateGenerator);
	}

	private void _setCollate(
		PhraseSuggester phraseSuggester, JSONObject configurationJsonObject) {

		JSONObject collateJsonObject = configurationJsonObject.getJSONObject(
			PhraseSuggesterConfigurationKeys.COLLATE);

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