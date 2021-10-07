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

package com.liferay.search.experiences.blueprints.engine.internal.condition.handler;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.search.experiences.blueprints.engine.internal.condition.util.ConditionHelper;
import com.liferay.search.experiences.blueprints.engine.internal.condition.visitor.EqualsVisitor;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.parameter.visitor.EvaluationVisitor;
import com.liferay.search.experiences.blueprints.engine.spi.clause.ConditionHandler;
import com.liferay.search.experiences.problems.ProblemBuilders;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.util.function.Function;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=not_equals",
	service = ConditionHandler.class
)
public class NotEqualsConditionHandler implements ConditionHandler {

	@Override
	public boolean isTrue(
		JSONObject jsonObject, ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Function<JSONObject, EvaluationVisitor> function =
			conditionJSONObject -> new EqualsVisitor(
				conditionJSONObject, _problemBuilders);

		return _conditionHelper.evaluate(
			function, true, jsonObject, parameterData, problemsHolderBuilder);
	}

	@Reference
	private ConditionHelper _conditionHelper;

	@Reference
	private ProblemBuilders _problemBuilders;

}