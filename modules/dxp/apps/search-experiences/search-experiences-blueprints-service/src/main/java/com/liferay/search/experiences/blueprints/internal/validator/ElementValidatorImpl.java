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

package com.liferay.search.experiences.blueprints.internal.validator;

import com.liferay.portal.kernel.util.Validator;
import com.liferay.search.experiences.blueprints.exception.ElementValidationException;
import com.liferay.search.experiences.blueprints.internal.validator.util.BlueprintJSONValidatorUtil;
import com.liferay.search.experiences.blueprints.message.Message;
import com.liferay.search.experiences.blueprints.validator.ElementValidator;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.everit.json.schema.ValidationException;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ElementValidator.class)
public class ElementValidatorImpl
	extends BaseValidator implements ElementValidator {

	@Override
	public void validateConfiguration(String configuration, int type)
		throws ElementValidationException {

		try {
			_validateConfiguration(configuration, type);
		}
		catch (ValidationException validationException) {
			List<Message> messages = new ArrayList<>();

			addMessages(messages, validationException);

			throw new ElementValidationException(
				"There were (" + messages.size() + ") validation errors",
				messages);
		}
	}

	@Override
	public void validateElement(
			Map<Locale, String> titleMap, String configuration, int type)
		throws ElementValidationException {

		List<Message> messages = new ArrayList<>();

		validateTitle(titleMap, messages);

		try {
			_validateConfiguration(configuration, type);
		}
		catch (ValidationException validationException) {
			addMessages(messages, validationException);
		}

		if (!messages.isEmpty()) {
			throw new ElementValidationException(
				"There were (" + messages.size() + ") validation errors",
				messages);
		}
	}

	private void _validateConfiguration(String configuration, int type)
		throws ValidationException {

		if (Validator.isNull(configuration)) {
			return;
		}

		InputStream configurationJSONSchemaInputStream =
			BlueprintValidatorImpl.class.getResourceAsStream(
				"dependencies/element.schema.json");

		BlueprintJSONValidatorUtil.validate(
			_wrapConfiguration(configuration, type),
			configurationJSONSchemaInputStream);
	}

	private String _wrapConfiguration(String configuration, int type) {
		StringBuilder sb = new StringBuilder();

		if (type == 1) {
			sb.append("{\"aggregation_element\": ");
		}
		else if (type == 5) {
			sb.append("{\"facet_element\": ");
		}
		else if (type == 10) {
			sb.append("{\"query_element\": ");
		}
		else if (type == 15) {
			sb.append("{\"suggester_element\": ");
		}

		sb.append(configuration);
		sb.append("}");

		return sb.toString();
	}

}