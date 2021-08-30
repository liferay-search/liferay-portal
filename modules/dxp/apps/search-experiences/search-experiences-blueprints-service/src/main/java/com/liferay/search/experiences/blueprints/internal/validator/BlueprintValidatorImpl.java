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
import com.liferay.search.experiences.blueprints.exception.BlueprintValidationException;
import com.liferay.search.experiences.blueprints.internal.validator.util.BlueprintJSONValidatorUtil;
import com.liferay.search.experiences.blueprints.validator.BlueprintValidator;
import com.liferay.search.experiences.problems.Problem;
import com.liferay.search.experiences.problems.ProblemBuilderFactory;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.everit.json.schema.ValidationException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintValidator.class)
public class BlueprintValidatorImpl
	extends BaseValidator implements BlueprintValidator {

	@Override
	public void validateBlueprint(
			Map<Locale, String> titleMap, String configuration)
		throws BlueprintValidationException {

		List<Problem> problems = new ArrayList<>();

		validateTitle(titleMap, problems);

		try {
			_validateConfiguration(configuration);
		}
		catch (ValidationException validationException) {
			addProblems(problems, validationException);
		}

		if (!problems.isEmpty()) {
			throw new BlueprintValidationException(
				"There were (" + problems.size() + ") validation errors",
				problems);
		}
	}

	@Override
	public void validateConfiguration(String configuration)
		throws BlueprintValidationException {

		try {
			_validateConfiguration(configuration);
		}
		catch (ValidationException validationException) {
			List<Problem> problems = new ArrayList<>();

			addProblems(problems, validationException);

			throw new BlueprintValidationException(
				"There were (" + problems.size() + ") validation errors",
				problems);
		}
	}

	@Override
	protected ProblemBuilderFactory getProblemBuilderFactory() {
		return _problemBuilderFactory;
	}

	@Reference(unbind = "-")
	protected void setProblemBuilderFactory(
		ProblemBuilderFactory problemBuilderFactory) {

		_problemBuilderFactory = problemBuilderFactory;
	}

	private void _validateConfiguration(String configuration)
		throws ValidationException {

		if (Validator.isNull(configuration)) {
			return;
		}

		InputStream configurationJSONSchemaInputStream =
			BlueprintValidatorImpl.class.getResourceAsStream(
				"dependencies/blueprint.schema.json");

		BlueprintJSONValidatorUtil.validate(
			configuration, configurationJSONSchemaInputStream);
	}

	private ProblemBuilderFactory _problemBuilderFactory;

}