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

package com.liferay.portal.search.tuning.blueprints.message;

import java.io.Serializable;

/**
 * @author Petteri Karttunen
 */
public class Message implements Serializable {

	public Message(
		Severity severity, String sourceModule, String localizationKey,
		String msg) {

		this(
			severity, sourceModule, localizationKey, msg, null, null, null,
			null);
	}

	public Message(
		Severity severity, String sourceModule, String localizationKey,
		String msg, Object rootObject, String rootProperty, String rootValue) {

		this(
			severity, sourceModule, localizationKey, msg, null, rootObject,
			rootProperty, rootValue);
	}

	public Message(
		Severity severity, String sourceModule, String localizationKey,
		String msg, Throwable throwable, Object rootObject, String rootProperty,
		String rootValue) {

		_severity = severity;
		_sourceModule = sourceModule;
		_localizationKey = localizationKey;
		_msg = msg;
		_throwable = throwable;
		_rootObject = rootObject;
		_rootProperty = rootProperty;
		_rootValue = rootValue;
	}

	public String getLocalizationKey() {
		return _localizationKey;
	}

	public String getMsg() {
		return _msg;
	}

	public Object getRootObject() {
		return _rootObject;
	}

	public String getRootProperty() {
		return _rootProperty;
	}

	public String getRootValue() {
		return _rootValue;
	}

	public Severity getSeverity() {
		return _severity;
	}

	public String getSourceModule() {
		return _sourceModule;
	}

	public Throwable getThrowable() {
		return _throwable;
	}

	private static final long serialVersionUID = 1L;

	private String _localizationKey;
	private String _msg;
	private Object _rootObject;
	private String _rootProperty;
	private String _rootValue;
	private Severity _severity;
	private String _sourceModule;
	private Throwable _throwable;

}