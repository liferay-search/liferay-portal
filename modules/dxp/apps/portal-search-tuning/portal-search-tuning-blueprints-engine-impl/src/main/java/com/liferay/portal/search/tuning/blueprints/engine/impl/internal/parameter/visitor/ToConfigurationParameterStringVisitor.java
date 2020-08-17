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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.parameter.visitor;

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
import java.util.stream.Collectors;

/**
 * @author Petteri Karttunen
 */
public class ToConfigurationParameterStringVisitor implements ToStringVisitor {

	public String visit(BooleanParameter parameter) {
		return String.valueOf(parameter.getValue());
	}

	public String visit(DateParameter parameter, String outputDateFormat)
		throws Exception {

		DateFormat dateFormat = new SimpleDateFormat(outputDateFormat);

		return dateFormat.format(parameter.getValue());
	}

	public String visit(DoubleParameter parameter) throws Exception {
		return String.valueOf(parameter.getValue());
	}

	public String visit(FloatParameter parameter) {
		return String.valueOf(parameter.getValue());
	}

	public String visit(IntegerArrayParameter parameter) {
		return Arrays.toString(parameter.getValue());
	}

	public String visit(IntegerParameter parameter) {
		return String.valueOf(parameter.getValue());
	}

	public String visit(LongArrayParameter parameter) {
		return Arrays.toString(parameter.getValue());
	}

	public String visit(LongParameter parameter) {
		return String.valueOf(parameter.getValue());
	}

	public String visit(StringArrayParameter parameter) {
		return Arrays.asList(
			parameter.getValue()
		).stream(
		).collect(
			Collectors.joining(",", "[", "]")
		);
	}

	@Override
	public String visit(StringParameter parameter) throws Exception {
		return parameter.getValue();
	}

}