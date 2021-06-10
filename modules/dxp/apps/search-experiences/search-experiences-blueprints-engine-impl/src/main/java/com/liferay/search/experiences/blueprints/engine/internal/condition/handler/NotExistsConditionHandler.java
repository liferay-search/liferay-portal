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
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.clause.ConditionHandler;
import com.liferay.search.experiences.blueprints.message.Messages;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=not_exists",
	service = ConditionHandler.class
)
public class NotExistsConditionHandler implements ConditionHandler {

	@Override
	public boolean isTrue(
		JSONObject jsonObject, ParameterData parameterData, Messages messages) {

		Optional<Parameter> optional = _conditionHelper.getParameterOptional(
			parameterData, jsonObject);

		return !optional.isPresent();
	}

	@Reference
	private ConditionHelper _conditionHelper;

}