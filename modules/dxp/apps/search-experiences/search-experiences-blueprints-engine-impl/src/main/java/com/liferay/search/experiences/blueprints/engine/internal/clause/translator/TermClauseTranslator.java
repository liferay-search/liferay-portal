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

package com.liferay.search.experiences.blueprints.engine.internal.clause.translator;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.query.TermQuery;
import com.liferay.search.experiences.blueprints.constants.json.keys.query.TermQueryConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.clause.ClauseTranslator;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.util.util.BlueprintJSONUtil;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=term", service = ClauseTranslator.class
)
public class TermClauseTranslator implements ClauseTranslator {

	@Override
	public Optional<Query> translate(
		JSONObject jsonObject, ParameterData parameterData, Messages messages) {

		Optional<String> optional = BlueprintJSONUtil.getFirstKeyOptional(
			jsonObject);

		if (!optional.isPresent()) {
			return Optional.empty();
		}

		String field = optional.get();

		Object object = jsonObject.get(field);

		String keywords = null;

		Float boost = null;

		if (_isShortForm(object)) {
			keywords = (String)object;
		}
		else {
			JSONObject fieldJSONObject = (JSONObject)object;

			keywords = fieldJSONObject.getString(
				TermQueryConfigurationKeys.VALUE.getJsonKey());

			boost = GetterUtil.getFloat(
				fieldJSONObject.get(
					TermQueryConfigurationKeys.BOOST.getJsonKey()));
		}

		if (Validator.isBlank(keywords)) {
			return Optional.empty();
		}

		TermQuery termQuery = _queries.term(field, keywords);

		if (boost != null) {
			termQuery.setBoost(boost);
		}

		return Optional.of(termQuery);
	}

	private boolean _isShortForm(Object object) {
		return object instanceof String;
	}

	@Reference
	private Queries _queries;

}