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
public class InvalidConfigurationValueException extends RuntimeException {

	public static InvalidConfigurationValueException ofClassAndType(
		String className, String type, Throwable throwable) {

		InvalidConfigurationValueException invalidConfigurationValueException =
			new InvalidConfigurationValueException(throwable);

		invalidConfigurationValueException._className = className;
		invalidConfigurationValueException._type = type;

		return invalidConfigurationValueException;
	}

	public String getClassName() {
		return _className;
	}

	public String getType() {
		return _type;
	}

	private InvalidConfigurationValueException(Throwable throwable) {
		super(throwable);
	}

	private String _className;
	private String _type;

}