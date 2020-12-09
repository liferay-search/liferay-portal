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

	public Message(Message message) {
		_className = message._className;
		_localizationKey = message._localizationKey;
		_msg = message._msg;
		_throwable = message._throwable;
		_rootObject = message._rootObject;
		_rootProperty = message._rootProperty;
		_rootValue = message._rootValue;
		_severity = message._severity;
	}

	public String getClassName() {
		return _className;
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

	public Throwable getThrowable() {
		return _throwable;
	}

	public static class Builder {

		public Builder() {
			_message = new Message();
		}

		public Builder(Message message) {
			_message = message;
		}

		public Message build() {
			return new Message(_message);
		}

		public Builder className(String className) {
			_message._className = className;

			return this;
		}

		public Builder localizationKey(String localizationKey) {
			_message._localizationKey = localizationKey;

			return this;
		}

		public Builder msg(String msg) {
			_message._msg = msg;

			return this;
		}

		public Builder rootObject(Object object) {
			_message._rootObject = object;

			return this;
		}

		public Builder rootProperty(String rootProperty) {
			_message._rootValue = rootProperty;

			return this;
		}

		public Builder rootValue(String rootValue) {
			_message._rootValue = rootValue;

			return this;
		}

		public Builder severity(Severity severity) {
			_message._severity = severity;

			return this;
		}

		public Builder throwable(Throwable throwable) {
			_message._throwable = throwable;

			return this;
		}

		private final Message _message;

	}

	private Message() {
	}

	private static final long serialVersionUID = 1L;

	private String _className;
	private String _localizationKey;
	private String _msg;
	private Object _rootObject;
	private String _rootProperty;
	private String _rootValue;
	private Severity _severity;
	private Throwable _throwable;

}