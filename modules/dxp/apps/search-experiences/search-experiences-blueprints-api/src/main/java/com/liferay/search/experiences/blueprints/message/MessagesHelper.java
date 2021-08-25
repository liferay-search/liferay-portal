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

package com.liferay.search.experiences.blueprints.message;

/**
 * @author André de Oliveira
 */
public interface MessagesHelper {

	public void error(
		Messages messages, String className, Throwable throwable,
		Object rootObject, String rootProperty, String rootValue,
		String localizationKey);

	public void invalidConfigurationValueError(
		Messages messages, String className, Throwable throwable,
		Object rootObject, String rootProperty, String rootValue);

	public void invalidConfigurationValueTypeError(
		Messages messages, String className, String correctType,
		Object rootObject, String rootProperty, String rootValue);

	public void requiredFieldMissingError(
		Messages messages, String className, Object rootObject, String field);

	public Message toErrorMessage(
		String className, Throwable throwable, Object rootObject,
		String rootProperty, String rootValue, String localizationKey);

	public void unknownError(
		Messages messages, String className, Throwable throwable,
		Object rootObject, String rootProperty, String rootValue);

	public void warning(
		Messages messages, String className, String message, Object rootObject,
		String rootProperty, String rootValue, String localizationKey);

	public void warning(
		Messages messages, String className, Throwable throwable,
		Object rootObject, String rootProperty, String rootValue,
		String localizationKey);

}