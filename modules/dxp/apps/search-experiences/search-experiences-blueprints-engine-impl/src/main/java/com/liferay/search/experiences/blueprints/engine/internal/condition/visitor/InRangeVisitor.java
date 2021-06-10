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
import com.liferay.search.experiences.blueprints.constants.json.keys.query.ConditionConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.exception.ParameterEvaluationException;
import com.liferay.search.experiences.blueprints.engine.parameter.DateParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.DoubleParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.FloatParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.IntegerParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.LongParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.visitor.EvaluationVisitor;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

/**
 * @author Petteri Karttunen
 */
public class InRangeVisitor
	extends BaseEvaluationVisitor implements EvaluationVisitor {

	public InRangeVisitor(JSONObject conditionJSONObject) {
		super(conditionJSONObject);
	}

	@Override
	public boolean visit(DateParameter parameter)
		throws ParameterEvaluationException {

		JSONArray jsonArray = getConditionValueJSONArray(conditionJSONObject);

		_checkRangeValue(jsonArray);

		String dateFormatString = conditionJSONObject.getString(
			ConditionConfigurationKeys.DATE_FORMAT.getJsonKey());

		String dateString = conditionJSONObject.getString(
			ConditionConfigurationKeys.VALUE.getJsonKey());

		if (Validator.isNull(dateFormatString)) {
			throw new ParameterEvaluationException(
				MessagesUtil.toErrorMessage(
					getClass().getName(),
					new Throwable("Date format must be defined"),
					conditionJSONObject,
					ConditionConfigurationKeys.DATE_FORMAT.getJsonKey(),
					dateFormatString,
					"core.error.date-format-must-be-defined"));
		}

		try {
			DateFormat dateFormat = new SimpleDateFormat(dateFormatString);

			String lowerBoundString = jsonArray.getString(0);
			String upperBoundString = jsonArray.getString(1);

			Date lowerBound = dateFormat.parse(lowerBoundString);
			Date upperBound = dateFormat.parse(upperBoundString);

			Date parameterValue = parameter.getValue();

			boolean inRange = false;

			if (parameterValue.after(lowerBound) &&
				parameterValue.before(upperBound)) {

				inRange = true;
			}

			return inRange;
		}
		catch (Exception exception) {
			throw new ParameterEvaluationException(
				MessagesUtil.toErrorMessage(
					getClass().getName(), exception, conditionJSONObject,
					ConditionConfigurationKeys.VALUE.getJsonKey(), dateString,
					"core.error.clause-condition-date-parsing-error"));
		}
	}

	@Override
	public boolean visit(DoubleParameter parameter)
		throws ParameterEvaluationException {

		JSONArray jsonArray = getConditionValueJSONArray(conditionJSONObject);

		_checkRangeValue(jsonArray);

		Double lowerBound = jsonArray.getDouble(0);
		Double upperBound = jsonArray.getDouble(1);

		Double parameterValue = parameter.getValue();

		boolean inRange = false;

		if ((parameterValue.compareTo(lowerBound) >= 0) &&
			(parameterValue.compareTo(upperBound) <= 0)) {

			inRange = true;
		}

		return inRange;
	}

	@Override
	public boolean visit(FloatParameter parameter)
		throws ParameterEvaluationException {

		JSONArray jsonArray = getConditionValueJSONArray(conditionJSONObject);

		_checkRangeValue(jsonArray);

		Float lowerBound = GetterUtil.getFloat(jsonArray.get(0));
		Float upperBound = GetterUtil.getFloat(jsonArray.get(1));

		Float parameterValue = parameter.getValue();

		boolean inRange = false;

		if ((parameterValue.compareTo(lowerBound) >= 0) &&
			(parameterValue.compareTo(upperBound) <= 0)) {

			inRange = true;
		}

		return inRange;
	}

	@Override
	public boolean visit(IntegerParameter parameter)
		throws ParameterEvaluationException {

		JSONArray jsonArray = getConditionValueJSONArray(conditionJSONObject);

		_checkRangeValue(jsonArray);

		Integer lowerBound = jsonArray.getInt(0);
		Integer upperBound = jsonArray.getInt(1);

		Integer parameterValue = parameter.getValue();

		boolean inRange = false;

		if ((parameterValue.compareTo(lowerBound) >= 0) &&
			(parameterValue.compareTo(upperBound) <= 0)) {

			inRange = true;
		}

		return inRange;
	}

	@Override
	public boolean visit(LongParameter parameter)
		throws ParameterEvaluationException {

		JSONArray jsonArray = getConditionValueJSONArray(conditionJSONObject);

		_checkRangeValue(jsonArray);

		Long parameterValue = parameter.getValue();

		boolean inRange = false;

		if ((parameterValue.compareTo(jsonArray.getLong(0)) >= 0) &&
			(parameterValue.compareTo(jsonArray.getLong(1)) <= 0)) {

			inRange = true;
		}

		return inRange;
	}

	private void _checkRangeValue(JSONArray jsonArray)
		throws ParameterEvaluationException {

		if (jsonArray.length() != 2) {
			throw new ParameterEvaluationException(
				MessagesUtil.toErrorMessage(
					getClass().getName(), new Throwable("Invalid range value"),
					conditionJSONObject,
					ConditionConfigurationKeys.VALUE.getJsonKey(),
					jsonArray.toString(), "core.error.invalid-range-value"));
		}
	}

}