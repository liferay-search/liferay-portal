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

import com.liferay.search.experiences.problems.ProblemBuilderFactory;
import com.liferay.search.experiences.problems.ProblemBuilders;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilderFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author André de Oliveira
 */
@Component(immediate = true, service = ProblemsHolderBuilderFactory.class)
public class ProblemsHolderBuilderFactoryImpl
	implements ProblemsHolderBuilderFactory {

	@Override
	public ProblemsHolderBuilder builder() {
		return new ProblemsHolderBuilderImpl(
			_problemBuilderFactory, _problemBuilders);
	}

	@Reference(unbind = "-")
	public void setMessageBuilderFactory(
		ProblemBuilderFactory problemBuilderFactory) {

		_problemBuilderFactory = problemBuilderFactory;
	}

	@Reference(unbind = "-")
	public void setMessageBuilders(ProblemBuilders problemBuilders) {
		_problemBuilders = problemBuilders;
	}

	private ProblemBuilderFactory _problemBuilderFactory;
	private ProblemBuilders _problemBuilders;

}