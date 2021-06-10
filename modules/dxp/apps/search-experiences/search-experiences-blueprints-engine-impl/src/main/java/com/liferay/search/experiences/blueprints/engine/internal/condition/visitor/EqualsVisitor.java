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
import com.liferay.search.experiences.blueprints.constants.json.keys.query.ConditionConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.exception.ParameterEvaluationException;
import com.liferay.search.experiences.blueprints.engine.parameter.BooleanParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.DateParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.DoubleParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.FloatParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.IntegerParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.LongParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.StringParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.visitor.EvaluationVisitor;

import java.util.Date;

/**
 * @author Petteri Karttunen
 */
public class EqualsVisitor
	extends BaseEvaluationVisitor implements EvaluationVisitor {

	public EqualsVisitor(JSONObject conditionJSONObject) {
		super(conditionJSONObject);
	}

	@Override
	public boolean visit(BooleanParameter parameter)
		throws ParameterEvaluationException {

		Boolean value = conditionJSONObject.getBoolean(
			ConditionConfigurationKeys.VALUE.getJsonKey());

		Boolean parameterValue = parameter.getValue();

		if (value.booleanValue() == parameterValue.booleanValue()) {
			return true;
		}

		return false;
	}

	@Override
	public boolean visit(DateParameter parameter)
		throws ParameterEvaluationException {

		Date date = getDateValue(conditionJSONObject);

		Date parameterValue = parameter.getValue();

		return parameterValue.equals(date);
	}

	@Override
	public boolean visit(DoubleParameter parameter)
		throws ParameterEvaluationException {

		Double value = conditionJSONObject.getDouble(
			ConditionConfigurationKeys.VALUE.getJsonKey());

		return parameter.equalsTo(value);
	}

	@Override
	public boolean visit(FloatParameter parameter)
		throws ParameterEvaluationException {

		return parameter.equalsTo(
			GetterUtil.getFloat(
				conditionJSONObject.get(
					ConditionConfigurationKeys.VALUE.getJsonKey())));
	}

	@Override
	public boolean visit(IntegerParameter parameter)
		throws ParameterEvaluationException {

		return parameter.equalsTo(
			conditionJSONObject.getInt(
				ConditionConfigurationKeys.VALUE.getJsonKey()));
	}

	@Override
	public boolean visit(LongParameter parameter)
		throws ParameterEvaluationException {

		return parameter.equalsTo(
			conditionJSONObject.getLong(
				ConditionConfigurationKeys.VALUE.getJsonKey()));
	}

	@Override
	public boolean visit(StringParameter parameter)
		throws ParameterEvaluationException {

		String parameterValue = parameter.getValue();

		return parameterValue.equals(
			conditionJSONObject.getString(
				ConditionConfigurationKeys.VALUE.getJsonKey()));
	}

}