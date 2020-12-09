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

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.suggest.Suggester;
import com.liferay.portal.kernel.search.suggest.TermSuggester;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.TermSuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.suggester.SuggesterTranslator;
import com.liferay.portal.search.tuning.blueprints.message.Messages;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "type=term",
	service = SuggesterTranslator.class
)
public class TermSuggesterTranslator
	extends BaseSuggesterTranslator implements SuggesterTranslator {

	@Override
	public Optional<Suggester> translate(
		String suggesterName, JSONObject configurationJSONObject,
		ParameterData parameterData, Messages messages) {

		if (!validateSuggesterConfiguration(
				messages, configurationJSONObject, suggesterName)) {

			return Optional.empty();
		}

		String field = configurationJSONObject.getString(
			TermSuggesterConfigurationKeys.FIELD.getJsonKey());

		TermSuggester termSuggester = new TermSuggester(
			suggesterName, field,
			getText(parameterData, configurationJSONObject));

		if (!configurationJSONObject.isNull(
				TermSuggesterConfigurationKeys.ACCURACY.getJsonKey())) {

			termSuggester.setAccuracy(
				GetterUtil.getFloat(
					configurationJSONObject.get(
						TermSuggesterConfigurationKeys.ACCURACY.getJsonKey())));
		}

		if (!configurationJSONObject.isNull(
				TermSuggesterConfigurationKeys.ANALYZER.getJsonKey())) {

			termSuggester.setAccuracy(
				GetterUtil.getFloat(
					configurationJSONObject.get(
						TermSuggesterConfigurationKeys.ANALYZER.getJsonKey())));
		}

		// TODO: implement the rest of the properties
		// https://www.elastic.co/guide/en/elasticsearch/reference/
		// current/search-suggesters.html#term-suggester

		return Optional.of(termSuggester);
	}

}