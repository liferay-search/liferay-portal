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

package com.liferay.search.experiences.blueprints.util.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.search.experiences.blueprints.message.Message;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.message.Severity;

/**
 * @author Petteri Karttunen
 */
public class MessagesUtil {

	public static void error(
		Messages messages, String className, Throwable throwable,
		Object rootObject, String rootProperty, String rootValue,
		String localizationKey) {

		messages.addMessage(
			new Message.Builder().className(
				className
			).localizationKey(
				localizationKey
			).msg(
				_getMsg(throwable, className)
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
			).build());

		if (throwable != null) {
			_log.error(throwable.getMessage(), throwable);
		}

		StringBundler sb = new StringBundler();

		_addLogMessageDetails(
			new StringBundler(), className, rootObject, rootProperty,
			rootValue);

		_log.error(sb.toString());
	}

	public static void invalidConfigurationValueError(
		Messages messages, String className, Throwable throwable,
		Object rootObject, String rootProperty, String rootValue) {

		messages.addMessage(
			new Message.Builder().className(
				className
			).localizationKey(
				"core.error.invalid-configuration-value"
			).msg(
				_getMsg(throwable, className)
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
			).build());

		if (throwable != null) {
			_log.error(throwable.getMessage(), throwable);
		}

		StringBundler sb = new StringBundler(7);

		sb.append("Invalid configuration value.");

		_addLogMessageDetails(
			sb, className, rootObject, rootProperty, rootValue);

		_log.error(sb.toString());
	}

	public static void invalidConfigurationValueTypeError(
		Messages messages, String className, String correctType,
		Object rootObject, String rootProperty, String rootValue) {

		StringBundler sb = new StringBundler();

		sb.append("[ ");
		sb.append(rootProperty);
		sb.append(" ] has to be of type [ ");
		sb.append(correctType);
		sb.append(" ] ");

		messages.addMessage(
			new Message.Builder().className(
				className
			).localizationKey(
				"core.error.invalid-value-type"
			).msg(
				sb.toString()
			).rootObject(
				rootObject
			).rootProperty(
				rootProperty
			).rootValue(
				rootValue
			).severity(
				Severity.ERROR
			).build());

		_addLogMessageDetails(
			sb, className, rootObject, rootProperty, rootValue);

		_log.error(sb.toString());
	}

	public static void requiredFieldMissingError(
		Messages messages, String className, Object rootObject, String field) {

		messages.addMessage(
			new Message.Builder().className(
				className
			).localizationKey(
				"core.error.required-field-missing"
			).msg(
				"[ " + field + " ] must be defined."
			).rootObject(
				rootObject
			).rootProperty(
				field
			).severity(
				Severity.ERROR
			).build());

		StringBundler sb = new StringBundler(5);

		sb.append("[ ");
		sb.append(field);
		sb.append(" ] must be defined ");

		_addLogMessageDetails(sb, className, rootObject, field, null);

		_log.error(sb.toString());
	}

	public static Message toErrorMessage(
		String className, Throwable throwable, Object rootObject,
		String rootProperty, String rootValue, String localizationKey) {

		return new Message.Builder().className(
			className
		).localizationKey(
			localizationKey
		).msg(
			_getMsg(throwable, className)
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
		).build();
	}

	public static void unknownError(
		Messages messages, String className, Throwable throwable,
		Object rootObject, String rootProperty, String rootValue) {

		messages.addMessage(
			new Message.Builder().className(
				className
			).localizationKey(
				"core.error.unknown-error"
			).msg(
				_getMsg(throwable, className)
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
			).build());

		if (throwable != null) {
			_log.error(throwable.getMessage(), throwable);
		}

		StringBundler sb = new StringBundler();

		_addLogMessageDetails(
			sb, className, rootObject, rootProperty, rootValue);

		_log.error(sb.toString());
	}

	public static void warning(
		Messages messages, String className, String message, Object rootObject,
		String rootProperty, String rootValue, String localizationKey) {

		messages.addMessage(
			new Message.Builder().className(
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
			).build());

		if (_log.isWarnEnabled()) {
			StringBundler sb = new StringBundler();

			sb.append("Warning: ");
			sb.append(message);

			_addLogMessageDetails(
				sb, className, rootObject, rootProperty, rootValue);

			_log.warn(sb.toString());
		}
	}

	public static void warning(
		Messages messages, String className, Throwable throwable,
		Object rootObject, String rootProperty, String rootValue,
		String localizationKey) {

		messages.addMessage(
			new Message.Builder().className(
				className
			).localizationKey(
				localizationKey
			).msg(
				_getMsg(throwable, className)
			).rootObject(
				rootObject
			).rootProperty(
				rootProperty
			).rootValue(
				rootValue
			).severity(
				Severity.WARN
			).throwable(
				throwable
			).build());

		if ((throwable != null) && _log.isWarnEnabled()) {
			_log.warn(throwable.getMessage(), throwable);
		}

		if (_log.isWarnEnabled()) {
			StringBundler sb = new StringBundler();

			_addLogMessageDetails(
				new StringBundler(), className, rootObject, rootProperty,
				rootValue);

			_log.warn(sb.toString());
		}
	}

	private static void _addLogMessageDetails(
		StringBundler sb, String className, Object rootObject,
		String rootProperty, String rootValue) {

		if (className != null) {
			sb.append(" Reporting class: [ ");
			sb.append(className);
			sb.append(" ]");
		}

		if (rootValue != null) {
			sb.append(" Root value: [ ");
			sb.append(rootValue);
			sb.append(" ]");
		}

		if (rootProperty != null) {
			sb.append(" Root property: [ ");
			sb.append(rootProperty);
			sb.append(" ]");
		}

		if (rootObject != null) {
			sb.append(" Root object: [ ");
			sb.append(rootObject);
			sb.append(" ]");
		}
	}

	private static String _getMsg(Throwable throwable, String className) {
		if (throwable != null) {
			return throwable.getMessage();
		}

		return className + " reported an error";
	}

	private static final Log _log = LogFactoryUtil.getLog(MessagesUtil.class);

}