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
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.SuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.internal.suggester.SuggesterTranslatorFactory;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.portal.search.tuning.blueprints.engine.spi.suggester.SuggesterTranslator;
import com.liferay.portal.search.tuning.blueprints.engine.util.BlueprintTemplateVariableParser;
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
		SearchRequestBuilder searchRequestBuilder, Blueprint blueprint,
		ParameterData parameterData, Messages messages) {

		Optional<JSONArray> configurationJSONArrayOptional =
			_blueprintHelper.getSuggestConfigurationOptional(blueprint);

		if (!configurationJSONArrayOptional.isPresent()) {
			return;
		}

		JSONArray configurationJSONArray = configurationJSONArrayOptional.get();

		for (int i = 0; i < configurationJSONArray.length(); i++) {
			JSONObject configurationJSONObject =
				configurationJSONArray.getJSONObject(i);

			if (!configurationJSONObject.getBoolean(
					SuggesterConfigurationKeys.ENABLED.getJsonKey(), false)) {

				continue;
			}

			String type = configurationJSONObject.getString(
				SuggesterConfigurationKeys.TYPE.getJsonKey());

			String name = configurationJSONObject.getString(
				SuggesterConfigurationKeys.NAME.getJsonKey());

			try {
				SuggesterTranslator suggesterTranslator =
					_suggesterTranslatorFactory.getTranslator(type);

				JSONObject suggestConfigurationJSONObject =
					_blueprintTemplateVariableParser.parse(
						configurationJSONObject.getJSONObject(
							SuggesterConfigurationKeys.CONFIGURATION.
								getJsonKey()),
						parameterData, messages);

				Optional<Suggester> suggesterOptional =
					suggesterTranslator.translate(
						name, suggestConfigurationJSONObject, parameterData,
						messages);

				if (suggesterOptional.isPresent()) {

					// TODO: waiting for support in SearchRequestBuilder

					// searchRequestBuilder.addSuggester();

				}
			}
			catch (IllegalArgumentException illegalArgumentException) {
				messages.addMessage(
					new Message.Builder().className(
						getClass().getName()
					).localizationKey(
						"core.error.unknown-suggester-type"
					).msg(
						illegalArgumentException.getMessage()
					).rootObject(
						configurationJSONObject
					).rootProperty(
						SuggesterConfigurationKeys.TYPE.getJsonKey()
					).rootValue(
						type
					).severity(
						Severity.ERROR
					).throwable(
						illegalArgumentException
					).build());

				_log.error(
					illegalArgumentException.getMessage(),
					illegalArgumentException);
			}
			catch (Exception exception) {
				messages.addMessage(
					new Message.Builder().className(
						getClass().getName()
					).localizationKey(
						"core.error.unknown-suggester-configuration-error"
					).msg(
						exception.getMessage()
					).rootObject(
						configurationJSONObject
					).severity(
						Severity.ERROR
					).throwable(
						exception
					).build());

				_log.error(exception.getMessage(), exception);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SuggestSearchRequestBodyContributor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private SuggesterTranslatorFactory _suggesterTranslatorFactory;

}