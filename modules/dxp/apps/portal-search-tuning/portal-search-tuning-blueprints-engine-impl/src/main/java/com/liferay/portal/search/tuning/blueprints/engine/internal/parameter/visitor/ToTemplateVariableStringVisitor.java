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

package com.liferay.portal.search.tuning.blueprints.engine.internal.parameter.visitor;

import com.liferay.portal.search.tuning.blueprints.engine.parameter.BooleanParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.DateParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.DoubleParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.FloatParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.IntegerArrayParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.IntegerParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.LongArrayParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.LongParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.StringArrayParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.StringParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ToStringVisitor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Petteri Karttunen
 */
public class ToTemplateVariableStringVisitor implements ToStringVisitor {

	public String visit(
		BooleanParameter parameter, Map<String, String> options) {

		return String.valueOf(parameter.getValue());
	}

	public String visit(DateParameter parameter, Map<String, String> options)
		throws Exception {

		Date date = parameter.getValue();

		if ((options == null) || (options.get("dateFormat") == null)) {
			return date.toString();
		}

		String dateFormatString = options.get("dateFormat");

		if (dateFormatString.equals("timestamp")) {
			return String.valueOf(date.getTime());
		}

		DateFormat dateFormat = new SimpleDateFormat(dateFormatString);

		return dateFormat.format(parameter.getValue());
	}

	public String visit(DoubleParameter parameter, Map<String, String> options)
		throws Exception {

		return String.valueOf(parameter.getValue());
	}

	public String visit(FloatParameter parameter, Map<String, String> options) {
		return String.valueOf(parameter.getValue());
	}

	public String visit(
		IntegerArrayParameter parameter, Map<String, String> options) {

		return Arrays.toString(parameter.getValue());
	}

	public String visit(
		IntegerParameter parameter, Map<String, String> options) {

		return String.valueOf(parameter.getValue());
	}

	public String visit(
		LongArrayParameter parameter, Map<String, String> options) {

		return Arrays.toString(parameter.getValue());
	}

	public String visit(LongParameter parameter, Map<String, String> options) {
		return String.valueOf(parameter.getValue());
	}

	public String visit(
		StringArrayParameter parameter, Map<String, String> options) {

		List<String> list = Arrays.asList(parameter.getValue());

		Stream<String> stream = list.stream();

		return stream.collect(Collectors.joining(",", "[", "]"));
	}

	@Override
	public String visit(StringParameter parameter, Map<String, String> options)
		throws Exception {

		return parameter.getValue();
	}

}