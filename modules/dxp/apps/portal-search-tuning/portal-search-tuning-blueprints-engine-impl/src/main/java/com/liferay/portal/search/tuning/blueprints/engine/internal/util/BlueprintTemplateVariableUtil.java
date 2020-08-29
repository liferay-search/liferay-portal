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

package com.liferay.portal.search.tuning.blueprints.engine.internal.util;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.internal.clause.condition.visitor.ToTemplateVariableStringVisitor;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;

/**
 * @author Petteri Karttunen
 */
public class BlueprintTemplateVariableUtil {

	public static JSONObject parseTemplateVariables(
			SearchRequestContext searchRequestContext,
			JSONObject queryJsonObject)
		throws Exception {

		SearchParameterData searchParameterData =
			searchRequestContext.getSearchParameterData();

		if (!searchParameterData.hasParameters()) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to find parameters in context");
			}

			return queryJsonObject;
		}

		String queryString = queryJsonObject.toString();

		boolean changed = false;

		ToTemplateVariableStringVisitor toConfigurationParameterStringVisitor =
			new ToTemplateVariableStringVisitor();

		for (Parameter parameter : searchParameterData.getParameters()) {
			String variable = parameter.getTemplateVariable();

			if (queryString.contains(variable)) {
				queryString = StringUtil.replace(
					queryString, variable,
					parameter.accept(toConfigurationParameterStringVisitor));
				changed = true;
			}
		}

		if (changed) {
			return JSONFactoryUtil.createJSONObject(queryString);
		}

		return queryJsonObject;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintTemplateVariableUtil.class);

}