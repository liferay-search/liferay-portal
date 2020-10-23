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

package com.liferay.portal.search.tuning.blueprints.engine.internal.searchrequest;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.suggest.Suggester;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.aggregation.AggregationConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.SuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.internal.suggester.SuggesterTranslatorFactory;
import com.liferay.portal.search.tuning.blueprints.engine.internal.util.BlueprintTemplateVariableUtil;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.portal.search.tuning.blueprints.engine.spi.suggester.SuggesterTranslator;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.Optional;

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
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, Messages messages) {

		Optional<JSONArray> configurationJsonArrayOptional =
			_blueprintHelper.getSuggestConfigurationOptional(blueprint);

		if (!configurationJsonArrayOptional.isPresent()) {
			return;
		}

		JSONArray configurationJsonArray = configurationJsonArrayOptional.get();

		for (int i = 0; i < configurationJsonArray.length(); i++) {
			JSONObject configurationJsonObject =
				configurationJsonArray.getJSONObject(i);

			if (!configurationJsonObject.getBoolean(
					SuggesterConfigurationKeys.ENABLED.getJsonKey(), false)) {

				continue;
			}

			String type = configurationJsonObject.getString(
				SuggesterConfigurationKeys.TYPE.getJsonKey());

			String name = configurationJsonObject.getString(
				SuggesterConfigurationKeys.NAME.getJsonKey());

			try {
				SuggesterTranslator suggesterTranslator =
					_suggesterTranslatorFactory.getTranslator(type);

				JSONObject suggestConfigurationJsonObject =
					BlueprintTemplateVariableUtil.parseTemplateVariables(
						parameterData, messages,
						configurationJsonObject.getJSONObject(
							SuggesterConfigurationKeys.CONFIGURATION.
								getJsonKey()));

				Optional<Suggester> suggesterOptional =
					suggesterTranslator.translate(
						parameterData, messages, suggestConfigurationJsonObject,
						name);

				if (suggesterOptional.isPresent()) {

					// TODO: waiting for support in SearchRequestBuilder

					// searchRequestBuilder.addSuggester();

				}
			}
			catch (IllegalArgumentException illegalArgumentException) {
				messages.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.unknown-suggester-type",
						illegalArgumentException.getMessage(),
						illegalArgumentException, configurationJsonObject,
						AggregationConfigurationKeys.TYPE.getJsonKey(), type));

				_log.error(
					illegalArgumentException.getMessage(),
					illegalArgumentException);
			}
			catch (Exception exception) {
				messages.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.unknown-suggester-configuration-error",
						exception.getMessage(), exception,
						configurationJsonObject, null, null));

				_log.error(exception.getMessage(), exception);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SuggestSearchRequestBodyContributor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private SuggesterTranslatorFactory _suggesterTranslatorFactory;

}