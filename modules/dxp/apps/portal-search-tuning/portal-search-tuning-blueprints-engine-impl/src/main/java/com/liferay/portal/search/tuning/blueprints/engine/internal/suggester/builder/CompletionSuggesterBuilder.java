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

package com.liferay.portal.search.tuning.blueprints.engine.internal.suggester.builder;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.suggest.CompletionSuggester;
import com.liferay.portal.kernel.search.suggest.Suggester;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.CompletionSuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.spi.suggester.SuggesterBuilder;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "type=completion",
	service = SuggesterBuilder.class
)
public class CompletionSuggesterBuilder
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
			CompletionSuggesterConfigurationKeys.FIELD.getJsonKey());

		String text = configurationJsonObject.getString(
			CompletionSuggesterConfigurationKeys.TEXT.getJsonKey(),
			searchRequestContext.getKeywords());

		text = StringUtil.toLowerCase(text);

		CompletionSuggester completionSuggester = new CompletionSuggester(
			createSuggesterName("completion"), field, text);

		if (!configurationJsonObject.isNull(
				CompletionSuggesterConfigurationKeys.ANALYZER.getJsonKey())) {

			completionSuggester.setAnalyzer(
				configurationJsonObject.getString(
					CompletionSuggesterConfigurationKeys.ANALYZER.
						getJsonKey()));
		}

		if (!configurationJsonObject.isNull(
				CompletionSuggesterConfigurationKeys.SHARD_SIZE.getJsonKey())) {

			completionSuggester.setShardSize(
				configurationJsonObject.getInt(
					CompletionSuggesterConfigurationKeys.SHARD_SIZE.
						getJsonKey()));
		}

		if (!configurationJsonObject.isNull(
				CompletionSuggesterConfigurationKeys.SIZE.getJsonKey())) {

			completionSuggester.setSize(
				configurationJsonObject.getInt(
					CompletionSuggesterConfigurationKeys.SIZE.getJsonKey()));
		}

		return Optional.of(completionSuggester);
	}

}