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

package com.liferay.search.experiences.problems;

/**
 * @author André de Oliveira
 */
public interface ProblemBuilder {

	public Problem build();

	public ProblemBuilder className(String className);

	public ProblemBuilder elementId(String elementId);

	public ProblemBuilder localizationKey(String localizationKey);

	public ProblemBuilder msg(String msg);

	public ProblemBuilder rootConfiguration(String rootConfiguration);

	public ProblemBuilder rootObject(Object object);

	public ProblemBuilder rootProperty(String rootProperty);

	public ProblemBuilder rootValue(String rootValue);

	public ProblemBuilder severity(Severity severity);

	public ProblemBuilder throwable(Throwable throwable);

}