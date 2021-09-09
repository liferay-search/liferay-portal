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

import com.liferay.search.experiences.problems.Problem;
import com.liferay.search.experiences.problems.ProblemsHolder;
import com.liferay.search.experiences.problems.Severity;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Petteri Karttunen
 */
public class ProblemsHolderImpl implements ProblemsHolder {

	public ProblemsHolderImpl(List<Problem> problems) {
		_problems = problems;
	}

	@Override
	public List<Problem> getAllProblems() {
		return _problems;
	}

	@Override
	public List<Problem> getProblemsBySeverity(Severity severity) {
		Stream<Problem> stream = _problems.stream();

		return stream.filter(
			bySeverity(severity)
		).collect(
			Collectors.toList()
		);
	}

	@Override
	public boolean hasErrors() {
		Stream<Problem> stream = _problems.stream();

		return stream.anyMatch(bySeverity(Severity.ERROR));
	}

	protected Predicate<? super Problem> bySeverity(Severity severity) {
		return problem -> Objects.equals(problem.getSeverity(), severity);
	}

	private final List<Problem> _problems;

}