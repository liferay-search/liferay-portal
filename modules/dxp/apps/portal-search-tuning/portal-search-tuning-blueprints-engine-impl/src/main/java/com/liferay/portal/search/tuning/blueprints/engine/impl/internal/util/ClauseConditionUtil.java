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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.util;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.ClauseConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.exception.ParameterEvaluationException;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;

/**
 * @author Petteri Karttunen
 */
public class ClauseConditionUtil {

	public static JSONArray getClauseConditionArrayValue(
			JSONObject conditionJsonObject)
		throws ParameterEvaluationException {

		Object object = conditionJsonObject.get(
			ClauseConfigurationKeys.MATCH_VALUE.getJsonKey());

		if (!(object instanceof JSONArray)) {
			throw new ParameterEvaluationException(
				new Message(
					Severity.ERROR, "core",
					"core.error.expected-clause-condition-array-match-value",
					null, null, conditionJsonObject,
					ClauseConfigurationKeys.MATCH_VALUE.getJsonKey(),
					object.toString()));
		}

		return (JSONArray)object;
	}

}