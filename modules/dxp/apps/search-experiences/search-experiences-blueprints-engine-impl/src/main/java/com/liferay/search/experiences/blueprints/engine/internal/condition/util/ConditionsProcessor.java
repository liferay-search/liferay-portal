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

package com.liferay.search.experiences.blueprints.engine.internal.condition.util;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.search.experiences.blueprints.engine.internal.condition.ConditionHandlerFactory;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.clause.ConditionHandler;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ConditionsProcessor.class)
public class ConditionsProcessor {

	public boolean processConditions(
		JSONObject jsonObject, ParameterData parameterData,
		String groupCondition, Messages messages) {

		if ((jsonObject == null) || (jsonObject.length() == 0)) {
			return true;
		}

		Set<String> keySet = jsonObject.keySet();

		boolean childrenValid = _processDirectChildren(
			keySet, jsonObject, parameterData, groupCondition, messages);

		if (!childrenValid) {
			return false;
		}

		if (keySet.contains("any_of")) {
			Stream<String> stream = keySet.stream();

			boolean valid = stream.filter(
				key -> key.equals("any_of")
			).anyMatch(
				key -> processConditions(
					jsonObject.getJSONObject(key), parameterData, "any_of",
					messages)
			);

			if (!valid) {
				return false;
			}
		}

		if (keySet.contains("all_of")) {
			Stream<String> stream = keySet.stream();

			boolean valid = stream.filter(
				key -> key.equals("all_of")
			).allMatch(
				key -> processConditions(
					jsonObject.getJSONObject(key), parameterData, "all_of",
					messages)
			);

			if (!valid) {
				return false;
			}
		}

		return true;
	}

	private boolean _processCondition(
		String handler, JSONObject jsonObject, ParameterData parameterData,
		Messages messages) {

		try {
			ConditionHandler conditionHandler =
				_conditionHandlerFactory.getHandler(handler);

			return conditionHandler.isTrue(jsonObject, parameterData, messages);
		}
		catch (Exception exception) {
			MessagesUtil.unknownError(
				messages, getClass().getName(), exception, jsonObject, null,
				null);
		}

		return false;
	}

	private boolean _processDirectChildren(
		Set<String> keySet, JSONObject jsonObject, ParameterData parameterData,
		String groupCondition, Messages messages) {

		Stream<String> stream1 = keySet.stream();

		List<String> conditions = stream1.filter(
			key -> !key.equals("all_of") && !key.equals("any_of")
		).collect(
			Collectors.toList()
		);

		if (conditions.isEmpty()) {
			return true;
		}

		Stream<String> stream2 = conditions.stream();

		if ((groupCondition != null) && groupCondition.equals("any_of")) {
			return stream2.anyMatch(
				key -> _processCondition(
					key, jsonObject.getJSONObject(key), parameterData,
					messages));
		}

		return stream2.allMatch(
			key -> _processCondition(
				key, jsonObject.getJSONObject(key), parameterData, messages));
	}

	@Reference
	private ConditionHandlerFactory _conditionHandlerFactory;

}