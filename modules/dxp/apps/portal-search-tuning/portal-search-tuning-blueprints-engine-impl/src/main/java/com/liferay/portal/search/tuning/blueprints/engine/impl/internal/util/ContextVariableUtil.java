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

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.parameter.visitor.ToConfigurationParameterStringVisitor;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;

/**
 * @author Petteri Karttunen
 */
public class ContextVariableUtil {

	public static JSONObject parseConfigurationVariables(
			SearchRequestContext searchRequestContext,
			JSONObject queryJsonObject)
		throws Exception {

		if (!searchRequestContext.getSearchParameterData(
			).hasParameters()) {

			return queryJsonObject;
		}

		String queryString = queryJsonObject.toString();

		boolean changed = false;

		ToConfigurationParameterStringVisitor
			toConfigurationParameterStringVisitor =
				new ToConfigurationParameterStringVisitor();

		for (Parameter parameter :
				searchRequestContext.getSearchParameterData(
				).getParameters()) {

			String variable = parameter.getConfigurationVariable();

			if (queryString.contains(variable)) {
				queryString = queryString.replace(
					variable,
					parameter.accept(toConfigurationParameterStringVisitor));
				changed = true;
			}
		}

		if (changed) {
			return JSONFactoryUtil.createJSONObject(queryString);
		}

		return queryJsonObject;
	}

}