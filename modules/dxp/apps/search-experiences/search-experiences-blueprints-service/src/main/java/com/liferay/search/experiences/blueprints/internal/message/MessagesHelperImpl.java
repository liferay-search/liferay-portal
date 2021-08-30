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

package com.liferay.search.experiences.blueprints.internal.message;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.search.experiences.blueprints.message.Message;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.message.MessagesHelper;
import com.liferay.search.experiences.blueprints.message.Severity;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = MessagesHelper.class)
public class MessagesHelperImpl implements MessagesHelper {

	@Override
	public void error(
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

	@Override
	public void invalidConfigurationValueError(
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

	@Override
	public void invalidConfigurationValueTypeError(
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

	@Override
	public void requiredFieldMissingError(
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

	@Override
	public Message toErrorMessage(
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

	@Override
	public void unknownError(
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

	@Override
	public void warning(
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

	@Override
	public void warning(
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

	private void _addLogMessageDetails(
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

	private String _getMsg(Throwable throwable, String className) {
		if (throwable != null) {
			return throwable.getMessage();
		}

		return className + " reported an error";
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MessagesHelperImpl.class);

}