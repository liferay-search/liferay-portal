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

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.suggest.Suggester;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.suggester.SuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.context.SearchRequestContext;
import com.liferay.portal.search.tuning.gsearch.message.Message;
import com.liferay.portal.search.tuning.gsearch.message.Severity;
import com.liferay.portal.search.tuning.gsearch.spi.suggester.SuggesterBuilder;

import java.util.Optional;
import java.util.Random;

/**
 * @author Petteri Karttunen
 */
public abstract class BaseSuggesterBuilder implements SuggesterBuilder {

	@Override
	public abstract Optional<Suggester> build(
		SearchRequestContext queryContext, JSONObject configurationJsonObject);

	protected String createSuggesterName(String type) {
		StringBundler sb = new StringBundler(3);

		sb.append(type);
		sb.append("-suggester-");
		sb.append(new Random().nextInt(1000));

		return sb.toString();
	}

	protected boolean validateSuggesterConfiguration(
		SearchRequestContext searchRequestContext,
		JSONObject configurationJsonObject) {

		boolean isValid = true;

		if (Validator.isBlank(
				configurationJsonObject.getString(
					SuggesterConfigurationKeys.FIELD))) {

			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.suggester-field-empty",
					null, null, configurationJsonObject,
					SuggesterConfigurationKeys.FIELD, null));
			isValid = false;
		}

		if (Validator.isBlank(
				configurationJsonObject.getString(
					SuggesterConfigurationKeys.TEXT,
					searchRequestContext.getKeywords()))) {

			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.suggester-text-empty",
					null, null, configurationJsonObject,
					SuggesterConfigurationKeys.TEXT, null));
			isValid = false;
		}

		return isValid;
	}

}