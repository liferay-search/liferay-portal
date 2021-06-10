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

package com.liferay.search.experiences.blueprints.engine.internal.clause.util;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.query.Query;
import com.liferay.search.experiences.blueprints.engine.internal.clause.ClauseTranslatorFactory;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.clause.ClauseTranslator;
import com.liferay.search.experiences.blueprints.engine.template.variable.BlueprintTemplateVariableParser;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;

import java.util.Iterator;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ClauseHelper.class)
public class ClauseHelper {

	public Optional<Query> getClause(
		JSONObject jsonObject, ParameterData parameterData, Messages messages) {

		if (jsonObject == null) {
			return Optional.empty();
		}

		String type = _getType(jsonObject);

		try {
			ClauseTranslator clauseTranslator =
				_clauseTranslatorFactory.getTranslator(type);

			Optional<JSONObject> optional = _getParsedClause(
				jsonObject.getJSONObject(type), parameterData, messages);

			if (!optional.isPresent()) {
				return Optional.empty();
			}

			return clauseTranslator.translate(
				optional.get(), parameterData, messages);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			MessagesUtil.invalidConfigurationValueError(
				messages, getClass().getName(), illegalArgumentException, null,
				null, type);
		}

		return Optional.empty();
	}

	private Optional<JSONObject> _getParsedClause(
		JSONObject jsonObject, ParameterData parameterData, Messages messages) {

		return _blueprintTemplateVariableParser.parseObject(
			jsonObject, parameterData, messages);
	}

	private String _getType(JSONObject jsonObject) {
		Iterator<String> iterator = jsonObject.keys();

		if (!iterator.hasNext()) {
			return null;
		}

		return iterator.next();
	}

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private ClauseTranslatorFactory _clauseTranslatorFactory;

}