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

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.search.experiences.blueprints.engine.exception.ParameterEvaluationException;
import com.liferay.search.experiences.blueprints.engine.parameter.DateParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.DoubleParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.FloatParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.IntegerParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.LongParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.visitor.EvaluationVisitor;
import com.liferay.search.experiences.problems.ProblemBuilders;

import java.util.Date;

/**
 * @author Petteri Karttunen
 */
public class GreaterThanVisitor
	extends BaseEvaluationVisitor implements EvaluationVisitor {

	public GreaterThanVisitor(
		JSONObject conditionJSONObject, ProblemBuilders problemBuilders,
		boolean closedRange) {

		super(conditionJSONObject, problemBuilders);

		_closedRange = closedRange;
	}

	@Override
	public boolean visit(DateParameter parameter)
		throws ParameterEvaluationException {

		Date parameterValue = parameter.getValue();

		return parameterValue.after(getDateValue(conditionJSONObject));
	}

	@Override
	public boolean visit(DoubleParameter parameter)
		throws ParameterEvaluationException {

		Double value = conditionJSONObject.getDouble("value");

		Double parameterValue = parameter.getValue();

		boolean greaterThan = false;

		if (_closedRange) {
			if (parameterValue.compareTo(value) >= 0) {
				greaterThan = true;
			}
		}
		else if (parameterValue.compareTo(value) > 0) {
			greaterThan = true;
		}

		return greaterThan;
	}

	@Override
	public boolean visit(FloatParameter parameter)
		throws ParameterEvaluationException {

		Float value = GetterUtil.getFloat(conditionJSONObject.get("value"));

		Float parameterValue = parameter.getValue();

		boolean greaterThan = false;

		if (_closedRange) {
			if (parameterValue.compareTo(value) >= 0) {
				greaterThan = true;
			}
		}
		else if (parameterValue.compareTo(value) > 0) {
			greaterThan = true;
		}

		return greaterThan;
	}

	@Override
	public boolean visit(IntegerParameter parameter)
		throws ParameterEvaluationException {

		Integer value = conditionJSONObject.getInt("value");

		Integer parameterValue = parameter.getValue();

		boolean greaterThan = false;

		if (_closedRange) {
			if (parameterValue.compareTo(value) >= 0) {
				greaterThan = true;
			}
		}
		else if (parameterValue.compareTo(value) > 0) {
			greaterThan = true;
		}

		return greaterThan;
	}

	@Override
	public boolean visit(LongParameter parameter)
		throws ParameterEvaluationException {

		Long value = conditionJSONObject.getLong("value");

		Long parameterValue = parameter.getValue();

		boolean greaterThan = false;

		if (_closedRange) {
			if (parameterValue.compareTo(value) >= 0) {
				greaterThan = true;
			}
		}
		else if (parameterValue.compareTo(value) > 0) {
			greaterThan = true;
		}

		return greaterThan;
	}

	private final boolean _closedRange;

}