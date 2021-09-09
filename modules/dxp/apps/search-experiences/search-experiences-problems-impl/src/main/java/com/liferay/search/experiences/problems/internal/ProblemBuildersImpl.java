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
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemBuilderFactory;
import com.liferay.search.experiences.problems.ProblemBuilders;
import com.liferay.search.experiences.problems.Severity;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ProblemBuilders.class)
public class ProblemBuildersImpl implements ProblemBuilders {

	@Override
	public ProblemBuilder error(
		String className, String localizationKey, Object rootObject,
		String rootProperty, String rootValue, Throwable throwable) {

		if (throwable != null) {
			_log.error(throwable.getMessage(), throwable);
		}

		StringBundler sb = new StringBundler();

		_addLogMessageDetails(
			new StringBundler(), className, rootObject, rootProperty,
			rootValue);

		_log.error(sb.toString());

		return _problemBuilderFactory.builder(
		).className(
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
		);
	}

	@Reference(unbind = "=")
	public void setMessageBuilderFactory(
		ProblemBuilderFactory problemBuilderFactory) {

		_problemBuilderFactory = problemBuilderFactory;
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
		ProblemBuildersImpl.class);

	private ProblemBuilderFactory _problemBuilderFactory;

}