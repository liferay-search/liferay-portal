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

import com.liferay.petra.string.StringBundler;
import com.liferay.search.experiences.problems.Problem;
import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.Severity;

import java.io.Serializable;

/**
 * @author Petteri Karttunen
 */
public class ProblemImpl implements Problem, Serializable {

	public static ProblemBuilder builder() {
		return new Builder();
	}

	public static ProblemBuilder builder(ProblemImpl problemImpl) {
		return new Builder(problemImpl);
	}

	@Override
	public String getClassName() {
		return _className;
	}

	@Override
	public String getElementId() {
		return _elementId;
	}

	@Override
	public String getLocalizationKey() {
		return _localizationKey;
	}

	@Override
	public String getMsg() {
		return _msg;
	}

	@Override
	public Object getRootConfiguration() {
		return _rootConfiguration;
	}

	@Override
	public Object getRootObject() {
		return _rootObject;
	}

	@Override
	public String getRootProperty() {
		return _rootProperty;
	}

	@Override
	public String getRootValue() {
		return _rootValue;
	}

	@Override
	public Severity getSeverity() {
		return _severity;
	}

	@Override
	public Throwable getThrowable() {
		return _throwable;
	}

	public void setElementId(String elementId) {
		_elementId = elementId;
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(21);

		sb.append("Message [_className=");
		sb.append(_className);
		sb.append(", _elementId=");
		sb.append(_elementId);
		sb.append(", _localizationKey=");
		sb.append(_localizationKey);
		sb.append(", _msg=");
		sb.append(_msg);
		sb.append(", _rootConfiguration=");
		sb.append(_rootConfiguration);
		sb.append(", _rootObject=");
		sb.append(_rootObject);
		sb.append(", _rootProperty=");
		sb.append(_rootProperty);
		sb.append(", _rootValue=");
		sb.append(_rootValue);
		sb.append(", _severity=");
		sb.append(_severity);
		sb.append(", _throwable=");
		sb.append(_throwable);
		sb.append("]");

		return sb.toString();
	}

	public static class Builder implements ProblemBuilder {

		@Override
		public Problem build() {
			return new ProblemImpl(_problemImpl);
		}

		@Override
		public ProblemBuilder className(String className) {
			_problemImpl._className = className;

			return this;
		}

		@Override
		public ProblemBuilder elementId(String elementId) {
			_problemImpl._elementId = elementId;

			return this;
		}

		@Override
		public ProblemBuilder localizationKey(String localizationKey) {
			_problemImpl._localizationKey = localizationKey;

			return this;
		}

		@Override
		public ProblemBuilder msg(String msg) {
			_problemImpl._msg = msg;

			return this;
		}

		@Override
		public ProblemBuilder rootConfiguration(String rootConfiguration) {
			_problemImpl._rootConfiguration = rootConfiguration;

			return this;
		}

		@Override
		public ProblemBuilder rootObject(Object object) {
			_problemImpl._rootObject = object;

			return this;
		}

		@Override
		public ProblemBuilder rootProperty(String rootProperty) {
			_problemImpl._rootProperty = rootProperty;

			return this;
		}

		@Override
		public ProblemBuilder rootValue(String rootValue) {
			_problemImpl._rootValue = rootValue;

			return this;
		}

		@Override
		public ProblemBuilder severity(Severity severity) {
			_problemImpl._severity = severity;

			return this;
		}

		@Override
		public ProblemBuilder throwable(Throwable throwable) {
			_problemImpl._throwable = throwable;

			return this;
		}

		private Builder() {
			_problemImpl = new ProblemImpl();
		}

		private Builder(ProblemImpl problemImpl) {
			_problemImpl = new ProblemImpl(problemImpl);
		}

		private final ProblemImpl _problemImpl;

	}

	private ProblemImpl() {
	}

	private ProblemImpl(ProblemImpl problemImpl) {
		_className = problemImpl._className;
		_elementId = problemImpl._elementId;
		_localizationKey = problemImpl._localizationKey;
		_msg = problemImpl._msg;
		_throwable = problemImpl._throwable;
		_rootConfiguration = problemImpl._rootConfiguration;
		_rootObject = problemImpl._rootObject;
		_rootProperty = problemImpl._rootProperty;
		_rootValue = problemImpl._rootValue;
		_severity = problemImpl._severity;
	}

	private static final long serialVersionUID = 1L;

	private String _className;
	private String _elementId;
	private String _localizationKey;
	private String _msg;
	private String _rootConfiguration;
	private Object _rootObject;
	private String _rootProperty;
	private String _rootValue;
	private Severity _severity;
	private Throwable _throwable;

}