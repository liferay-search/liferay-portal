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

package com.liferay.search.experiences.blueprints.engine.internal.sort.translator;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.sort.FieldSort;
import com.liferay.portal.search.sort.NestedSort;
import com.liferay.portal.search.sort.Sort;
import com.liferay.portal.search.sort.SortMode;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.search.experiences.blueprints.constants.json.keys.sort.SortConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.internal.clause.util.ClauseHelper;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataBuilder;
import com.liferay.search.experiences.blueprints.engine.spi.sort.SortTranslator;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.util.util.SetterHelper;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=field", service = SortTranslator.class
)
public class FieldSortTranslator implements SortTranslator {

	@Override
	public Optional<Sort> translate(
		JSONObject jsonObject, String field, SortOrder sortOrder,
		Messages messages) {

		FieldSort fieldSort = _sorts.field(field, sortOrder);

		_setterHelper.setObjectValue(
			jsonObject, SortConfigurationKeys.MISSING.getJsonKey(),
			fieldSort::setMissing);

		if (jsonObject.has(SortConfigurationKeys.MODE.getJsonKey())) {
			fieldSort.setSortMode(_getSortMode(jsonObject));
		}

		if (jsonObject.has(SortConfigurationKeys.NESTED.getJsonKey())) {
			fieldSort.setNestedSort(_getNestedSort(jsonObject, messages));
		}

		return Optional.of(fieldSort);
	}

	private NestedSort _getNestedSort(
		JSONObject jsonObject, Messages messages) {

		JSONObject nestedJSONObject = jsonObject.getJSONObject("nested");

		String path = nestedJSONObject.getString("path");

		NestedSort nestedSort = _sorts.nested(path);

		if (jsonObject.has("filter")) {
			Optional<Query> optional = _clauseHelper.getClause(
				jsonObject, _parameterDataBuilder.build(), messages);

			if (optional.isPresent()) {
				nestedSort.setFilterQuery(optional.get());
			}
		}

		if (nestedJSONObject.has("nested")) {
			nestedSort.setNestedSort(
				_getNestedSort(nestedJSONObject, messages));
		}

		return nestedSort;
	}

	private SortMode _getSortMode(JSONObject jsonObject) {
		String s = jsonObject.getString(
			SortConfigurationKeys.MODE.getJsonKey());

		return SortMode.valueOf(StringUtil.toUpperCase(s));
	}

	@Reference
	private ClauseHelper _clauseHelper;

	@Reference
	private ParameterDataBuilder _parameterDataBuilder;

	@Reference
	private SetterHelper _setterHelper;

	@Reference
	private Sorts _sorts;

}