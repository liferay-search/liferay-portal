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

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.search.experiences.problems.Problem;
import com.liferay.search.experiences.problems.ProblemBuilderFactory;
import com.liferay.search.experiences.problems.Severity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;

/**
 * @author Petteri Karttunen
 */
public abstract class BaseValidator {

	protected void addProblem(
		List<Problem> problems, ValidationException validationException) {

		String keyword = validationException.getKeyword();

		if (!isLoggedKeyword(keyword)) {
			return;
		}

		String rootProperty = validationException.getPointerToViolation();

		if (StringUtil.startsWith(rootProperty, "#") &&
			(rootProperty.length() > 1)) {

			rootProperty = rootProperty.substring(1);
		}

		ProblemBuilderFactory problemBuilderFactory =
			getProblemBuilderFactory();

		problems.add(
			problemBuilderFactory.builder(
			).className(
				getClass().getName()
			).localizationKey(
				_getMessageLocalizationRootKey(keyword)
			).msg(
				_getMsg(validationException, keyword)
			).rootConfiguration(
				_getRootConfiguration(validationException)
			).rootObject(
				null
			).rootProperty(
				rootProperty
			).severity(
				Severity.ERROR
			).throwable(
				validationException
			).build());
	}

	protected void addProblems(
		List<Problem> problems,
		List<ValidationException> validationExceptions) {

		for (ValidationException validationException : validationExceptions) {
			if (ListUtil.isEmpty(validationException.getCausingExceptions())) {
				addProblem(problems, validationException);
			}
			else {
				addProblems(problems, validationException);
			}
		}
	}

	protected void addProblems(
		List<Problem> problems, ValidationException validationException) {

		if (ListUtil.isEmpty(validationException.getCausingExceptions())) {
			addProblem(problems, validationException);
		}
		else {
			addProblems(problems, validationException.getCausingExceptions());
		}
	}

	protected abstract ProblemBuilderFactory getProblemBuilderFactory();

	protected boolean isLoggedKeyword(String keyword) {
		return !_excludedKeywords.contains(keyword);
	}

	protected void validateTitle(
		Map<Locale, String> titleMap, List<Problem> problems) {

		if (MapUtil.isEmpty(titleMap)) {
			ProblemBuilderFactory problemBuilderFactory =
				getProblemBuilderFactory();

			problems.add(
				problemBuilderFactory.builder(
				).msg(
					"Title empty"
				).localizationKey(
					"core.errors.title-empty"
				).severity(
					Severity.ERROR
				).build());
		}
	}

	private String _getExpectedValue(
		ValidationException validationException, String keyword) {

		try {
			Schema schema = validationException.getViolatedSchema();

			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
				schema.toString());

			return ". Expected value is " +
				jsonObject.get(
					keyword
				).toString();
		}
		catch (JSONException jsonException) {
			_log.error(jsonException.getMessage(), jsonException);
		}

		return "";
	}

	private String _getMessageLocalizationRootKey(String keyword) {
		return "json.validator.invalid." + keyword;
	}

	private String _getMsg(
		ValidationException validationException, String keyword) {

		StringBundler sb = new StringBundler(2);

		sb.append(validationException.getErrorMessage());

		if (keyword.equals("const") || keyword.equals("enum")) {
			sb.append(_getExpectedValue(validationException, keyword));
		}

		return sb.toString();
	}

	private String _getRootConfiguration(
		ValidationException validationException) {

		String pointer = validationException.getPointerToViolation();

		int end = pointer.indexOf("/", 2);

		if (StringUtil.startsWith(pointer, "#") && (end > 0)) {
			return pointer.substring(2, end);
		}

		return null;
	}

	private static final Log _log = LogFactoryUtil.getLog(BaseValidator.class);

	private static final List<String> _excludedKeywords = new ArrayList<>(
		Arrays.asList("allOf", "anyOf", "oneOf", "none"));

}