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

package com.liferay.search.experiences.problems.internal;

import com.liferay.portal.kernel.util.Validator;
import com.liferay.search.experiences.problems.InvalidConfigurationValueException;
import com.liferay.search.experiences.problems.Problem;
import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemBuilderFactory;
import com.liferay.search.experiences.problems.ProblemBuilders;
import com.liferay.search.experiences.problems.ProblemsHolder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;
import com.liferay.search.experiences.problems.Severity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Petteri Karttunen
 */
public class ProblemsHolderBuilderImpl implements ProblemsHolderBuilder {

	public ProblemsHolderBuilderImpl(
		ProblemBuilderFactory problemBuilderFactory,
		ProblemBuilders problemBuilders) {

		_problemBuilderFactory = problemBuilderFactory;
		_problemBuilders = problemBuilders;
	}

	@Override
	public ProblemsHolderBuilder addExceptions(Exception... exception) {
		_addThrowables(exception);

		return this;
	}

	@Override
	public ProblemsHolderBuilder addProblem(Problem problem) {
		_addProblem(problem);

		return this;
	}

	@Override
	public ProblemsHolder build() {
		return new ProblemsHolderImpl(_problems);
	}

	@Override
	public void setElementId(String elementId) {
		_elementId = elementId;
	}

	@Override
	public void unsetElementId() {
		_elementId = null;
	}

	private void _addProblem(Problem problem) {
		if (problem == null) {
			return;
		}

		_problems.add(
			_problemBuilderFactory.builder(
				problem
			).elementId(
				_elementId
			).build());
	}

	private void _addThrowables(Throwable... throwables) {
		for (Throwable throwable : throwables) {
			_addProblem(_toProblem(throwable));

			_addThrowables(throwable.getSuppressed());
		}
	}

	private Problem _toProblem(Throwable throwable) {
		if (throwable instanceof InvalidConfigurationValueException) {
			InvalidConfigurationValueException
				invalidConfigurationValueException =
					(InvalidConfigurationValueException)throwable;

			ProblemBuilder problemBuilder = _problemBuilders.error(
				invalidConfigurationValueException.getClassName(),
				"core.error.invalid-configuration-value", null, null,
				invalidConfigurationValueException.getType(),
				throwable.getCause());

			return problemBuilder.build();
		}

		if ((throwable.getClass() == RuntimeException.class) &&
			Validator.isBlank(throwable.getMessage())) {

			return null;
		}

		return _problemBuilderFactory.builder(
		).severity(
			Severity.ERROR
		).throwable(
			throwable
		).build();
	}

	private String _elementId;
	private final ProblemBuilderFactory _problemBuilderFactory;
	private final ProblemBuilders _problemBuilders;
	private final List<Problem> _problems = new ArrayList<>();

}