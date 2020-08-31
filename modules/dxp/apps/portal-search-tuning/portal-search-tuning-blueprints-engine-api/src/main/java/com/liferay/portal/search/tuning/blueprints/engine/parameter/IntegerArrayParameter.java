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

package com.liferay.portal.search.tuning.blueprints.engine.parameter;

import com.liferay.portal.search.tuning.blueprints.constants.json.values.EvaluationType;
import com.liferay.portal.search.tuning.blueprints.engine.exception.ParameterEvaluationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public class IntegerArrayParameter implements Parameter {

	public IntegerArrayParameter(
		String name, String configurationVariable, Integer[] value) {

		_name = name;
		_configurationVariable = configurationVariable;
		_value = value;
	}

	@Override
	public boolean accept(EvaluationVisitor evaluationVisitor)
		throws ParameterEvaluationException {

		return evaluationVisitor.visit(this);
	}

	@Override
	public String accept(ToStringVisitor toStringVisitor, Map<String, String> options) throws Exception {
		return toStringVisitor.visit(this, options);
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public List<EvaluationType> getSupportedEvaluationTypes() {
		List<EvaluationType> evaluationTypes = new ArrayList<>();

		evaluationTypes.add(EvaluationType.CONTAINS);
		evaluationTypes.add(EvaluationType.NOT_CONTAINS);

		return evaluationTypes;
	}

	@Override
	public String getTemplateVariable() {
		return _configurationVariable;
	}

	@Override
	public Integer[] getValue() {
		return _value;
	}

	private final String _configurationVariable;
	private final String _name;
	private final Integer[] _value;

}