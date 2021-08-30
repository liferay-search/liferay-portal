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

package com.liferay.search.experiences.blueprints.engine.internal.condition.visitor;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.search.experiences.blueprints.engine.exception.ParameterEvaluationException;
import com.liferay.search.experiences.blueprints.engine.parameter.BooleanParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.DateParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.DoubleParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.FloatParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.IntegerArrayParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.IntegerParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.LongArrayParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.LongParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.StringArrayParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.StringParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.visitor.EvaluationVisitor;
import com.liferay.search.experiences.problems.Problem;
import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemBuilders;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

/**
 * @author Petteri Karttunen
 */
public abstract class BaseEvaluationVisitor implements EvaluationVisitor {

	public BaseEvaluationVisitor(
		JSONObject conditionJSONObject, ProblemBuilders problemBuilders) {

		this.conditionJSONObject = conditionJSONObject;

		_problemBuilders = problemBuilders;
	}

	@Override
	public boolean visit(BooleanParameter parameter)
		throws ParameterEvaluationException {

		throw new UnsupportedOperationException();
	}

	@Override
	public boolean visit(DateParameter parameter)
		throws ParameterEvaluationException {

		throw new UnsupportedOperationException();
	}

	@Override
	public boolean visit(DoubleParameter parameter)
		throws ParameterEvaluationException {

		throw new UnsupportedOperationException();
	}

	@Override
	public boolean visit(FloatParameter parameter)
		throws ParameterEvaluationException {

		throw new UnsupportedOperationException();
	}

	@Override
	public boolean visit(IntegerArrayParameter parameter)
		throws ParameterEvaluationException {

		throw new UnsupportedOperationException();
	}

	@Override
	public boolean visit(IntegerParameter parameter)
		throws ParameterEvaluationException {

		throw new UnsupportedOperationException();
	}

	@Override
	public boolean visit(LongArrayParameter parameter)
		throws ParameterEvaluationException {

		throw new UnsupportedOperationException();
	}

	@Override
	public boolean visit(LongParameter parameter)
		throws ParameterEvaluationException {

		throw new UnsupportedOperationException();
	}

	@Override
	public boolean visit(StringArrayParameter parameter)
		throws ParameterEvaluationException {

		throw new UnsupportedOperationException();
	}

	@Override
	public boolean visit(StringParameter parameter)
		throws ParameterEvaluationException {

		throw new UnsupportedOperationException();
	}

	protected JSONArray getConditionValueJSONArray(
			JSONObject conditionJSONObject)
		throws ParameterEvaluationException {

		Object object = conditionJSONObject.get("value");

		if (!(object instanceof JSONArray)) {
			throw new ParameterEvaluationException(
				toErrorMessage(
					getClass().getName(),
					new Throwable("Match value has to be an array"),
					conditionJSONObject, "value", GetterUtil.getString(object),
					"core.error.match-value-has-to-be-an-array"));
		}

		return (JSONArray)object;
	}

	protected String getDateAsString(Date date, String dateFormatString)
		throws ParameterEvaluationException {

		if (Validator.isBlank(dateFormatString)) {
			throw new ParameterEvaluationException(
				toErrorMessage(
					getClass().getName(),
					new Throwable("Date format must be defined"),
					conditionJSONObject, "date_format", dateFormatString,
					"core.error.date-format-must-be-defined"));
		}

		try {
			DateFormat dateFormat = new SimpleDateFormat(dateFormatString);

			return dateFormat.format(date);
		}
		catch (Exception exception) {
			ProblemBuilder problemBuilder = _problemBuilders.error(
				BaseEvaluationVisitor.class.getName(),
				"core.error.clause-condition-date-parsing-error",
				conditionJSONObject, "value", GetterUtil.getString(date),
				exception);

			throw new ParameterEvaluationException(problemBuilder.build());
		}
	}

	protected Date getDateValue(JSONObject conditionJSONObject)
		throws ParameterEvaluationException {

		String dateString = conditionJSONObject.getString("value");

		String dateFormatString = conditionJSONObject.getString("date_format");

		if (Validator.isBlank(dateFormatString)) {
			ProblemBuilder problemBuilder = _problemBuilders.error(
				getClass().getName(), "core.error.date-format-must-be-defined",
				conditionJSONObject, "date_format", dateFormatString,
				new Throwable("Date format must be defined"));

			throw new ParameterEvaluationException(problemBuilder.build());
		}

		try {
			DateFormat dateFormat = new SimpleDateFormat(dateFormatString);

			return dateFormat.parse(dateString);
		}
		catch (Exception exception) {
			ProblemBuilder problemBuilder = _problemBuilders.error(
				BaseEvaluationVisitor.class.getName(),
				"core.error.clause-condition-date-parsing-error",
				conditionJSONObject, "value", dateString, exception);

			throw new ParameterEvaluationException(problemBuilder.build());
		}
	}

	protected Problem toErrorMessage(
		String className, Throwable throwable, Object rootObject,
		String rootProperty, String rootValue, String localizationKey) {

		ProblemBuilder problemBuilder = _problemBuilders.error(
			className, localizationKey, rootObject, rootProperty, rootValue,
			throwable);

		return problemBuilder.build();
	}

	protected final JSONObject conditionJSONObject;

	private final ProblemBuilders _problemBuilders;

}