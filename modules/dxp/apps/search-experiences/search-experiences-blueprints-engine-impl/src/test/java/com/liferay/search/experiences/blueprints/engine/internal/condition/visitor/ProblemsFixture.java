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

package com.liferay.search.experiences.blueprints.engine.internal.condition.visitor;

import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemBuilders;

import org.mockito.Mockito;

/**
 * @author André de Oliveira
 */
public class ProblemsFixture {

	public ProblemsFixture() {
		_problemBuilders = createProblemBuilders();
	}

	public ProblemBuilders getProblemBuilders() {
		return _problemBuilders;
	}

	protected static ProblemBuilders createProblemBuilders() {
		ProblemBuilders problemBuilders = Mockito.mock(ProblemBuilders.class);

		Mockito.doReturn(
			Mockito.mock(ProblemBuilder.class)
		).when(
			problemBuilders
		).error(
			Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
			Mockito.any(), Mockito.any()
		);

		return problemBuilders;
	}

	private final ProblemBuilders _problemBuilders;

}