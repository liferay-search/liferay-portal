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

/**
 * @author André de Oliveira
 */
public interface ProblemBuilders {

	public ProblemBuilder invalidConfigurationValueError(
		String className, Throwable throwable, Object rootObject,
		String rootProperty, String rootValue);

	public ProblemBuilder unknownError(
		String className, Object rootObject, Throwable throwable);

	public ProblemBuilder warning(
		String className, String localizationKey, String message,
		Object rootObject, String rootProperty, String rootValue);

}