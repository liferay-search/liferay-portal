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

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.suggest.Suggester;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.CompletionSuggesterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.suggester.SuggesterTranslator;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;

import java.util.Optional;

/**
 * @author Petteri Karttunen
 */
public abstract class BaseSuggesterTranslator implements SuggesterTranslator {

	@Override
	public abstract Optional<Suggester> translate(
		ParameterData parameterData, Messages messages,
		JSONObject configurationJsonObject, String suggesterName);

	protected String getText(
		ParameterData parameterData, JSONObject configurationJsonObject) {

		String text = configurationJsonObject.getString(
			CompletionSuggesterConfigurationKeys.TEXT.getJsonKey());

		if (Validator.isBlank(text)) {
			text = parameterData.getKeywords();
		}

		return StringUtil.toLowerCase(text);
	}

	protected boolean validateSuggesterConfiguration(
		Messages messages, JSONObject configurationJsonObject,
		String suggesterName) {

		if (_validateField(messages, configurationJsonObject) &&
			_validateName(messages, suggesterName)) {

			return true;
		}

		return false;
	}

	private boolean _validateField(
		Messages messages, JSONObject configurationJsonObject) {

		if (configurationJsonObject.isNull("field")) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-suggester-field",
					"Suggester field is not defined.", null,
					configurationJsonObject, "field", null));

			if (_log.isWarnEnabled()) {
				StringBundler sb = new StringBundler(3);

				sb.append("Suggester field is not defined [ ");
				sb.append(configurationJsonObject);
				sb.append(" ]");

				_log.warn(sb.toString());
			}

			return false;
		}

		return true;
	}

	private boolean _validateName(Messages messages, String suggesterName) {
		if (Validator.isBlank(suggesterName)) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-suggester-name",
					"Suggester name is not defined."));

			if (_log.isWarnEnabled()) {
				_log.warn("Suggester name is not defined");
			}

			return false;
		}

		return true;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BaseSuggesterTranslator.class);

}