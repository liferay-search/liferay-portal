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

package com.liferay.search.experiences.blueprints.engine.internal.util;

import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemBuilderFactory;
import com.liferay.search.experiences.problems.Severity;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author André de Oliveira
 */
@Component(service = ProblemBuilders.class)
public class ProblemBuildersImpl implements ProblemBuilders {

	@Override
	public ProblemBuilder invalidConfigurationValueError(
		String className, Throwable throwable, Object rootObject,
		String rootProperty, String rootValue) {

		return _problemBuilderFactory.builder(
		).className(
			className
		).localizationKey(
			"core.error.invalid-configuration-value"
		).msg(
			throwable.getMessage()
		).rootObject(
			rootObject
		).rootProperty(
			rootProperty
		).rootValue(
			rootValue
		).severity(
			Severity.ERROR
		).throwable(
			throwable
		);
	}

	@Reference(unbind = "=")
	public void setMessageBuilderFactory(
		ProblemBuilderFactory problemBuilderFactory) {

		_problemBuilderFactory = problemBuilderFactory;
	}

	@Override
	public ProblemBuilder unknownError(
		String className, Object rootObject, Throwable throwable) {

		return _problemBuilderFactory.builder(
		).className(
			className
		).localizationKey(
			"core.error.unknown-error"
		).msg(
			throwable.getMessage()
		).rootObject(
			rootObject
		).severity(
			Severity.ERROR
		).throwable(
			throwable
		);
	}

	@Override
	public ProblemBuilder warning(
		String className, String localizationKey, String message,
		Object rootObject, String rootProperty, String rootValue) {

		return _problemBuilderFactory.builder(
		).className(
			className
		).localizationKey(
			localizationKey
		).msg(
			message
		).rootObject(
			rootObject
		).rootProperty(
			rootProperty
		).rootValue(
			rootValue
		).severity(
			Severity.WARN
		);
	}

	private ProblemBuilderFactory _problemBuilderFactory;

}