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

package com.liferay.portal.search.tuning.blueprints.engine.internal.searchrequest.data.contributor;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.sort.Sort;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.requestparameter.SortConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.internal.util.BlueprintValueUtil;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.searchrequest.SearchRequestData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.searchrequest.SearchRequestDataContributor;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SearchRequestDataContributor.class)
public class SortsSearchRequestDataContributor
	implements SearchRequestDataContributor {

	@Override
	public void contribute(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData) {

		Optional<JSONArray> sortParameterConfigurationOptional =
			_blueprintHelper.getSortParameterConfigurationOptional(
				searchRequestContext.getBlueprint());

		List<Sort> sorts = new ArrayList<>();

		if (sortParameterConfigurationOptional.isPresent()) {
			JSONArray sortParameterConfigurationJsonArray =
				sortParameterConfigurationOptional.get();

			sorts = _getSortsFromParameters(
				searchRequestContext, sortParameterConfigurationJsonArray);
		}

		if (sorts.isEmpty()) {
			sorts = _getDefaultSorts(searchRequestContext);
		}

		if (!sorts.isEmpty()) {
			searchRequestData.setSorts(sorts);
		}
	}

	private List<Sort> _getDefaultSorts(
		SearchRequestContext searchRequestContext) {

		List<Sort> sorts = new ArrayList<>();

		Optional<JSONArray> defaultSortConfigurationOptional =
			_blueprintHelper.getDefaultSortConfigurationOptional(
				searchRequestContext.getBlueprint());

		if (!defaultSortConfigurationOptional.isPresent()) {
			return sorts;
		}

		JSONArray defaultSortConfigurationJsonArray =
			defaultSortConfigurationOptional.get();

		for (int i = 0; i < defaultSortConfigurationJsonArray.length(); i++) {
			JSONObject sortJsonObject =
				defaultSortConfigurationJsonArray.getJSONObject(i);

			String field = sortJsonObject.getString("field");
			String orderString = sortJsonObject.getString("order");

			if (Validator.isBlank(field) || Validator.isBlank(orderString)) {
				continue;
			}

			Sort sort = _getSort(searchRequestContext, field, orderString);

			if (sort != null) {
				sorts.add(sort);
			}
		}

		return sorts;
	}

	private Sort _getSort(
		SearchRequestContext searchRequestContext, String field,
		String orderString) {

		try {
			return _sorts.field(
				field, BlueprintValueUtil.getSortOrder(orderString));
		}
		catch (IllegalArgumentException illegalArgumentException) {
			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.unknown-sort-order",
					illegalArgumentException.getMessage(),
					illegalArgumentException, null, null, orderString));
			_log.error(
				illegalArgumentException.getMessage(),
				illegalArgumentException);

			return null;
		}
	}

	private List<Sort> _getSortsFromParameters(
		SearchRequestContext searchRequestContext,
		JSONArray sortParameterConfigurationJsonArray) {

		List<Sort> sorts = new ArrayList<>();

		for (int i = 0; i < sortParameterConfigurationJsonArray.length(); i++) {
			JSONObject sortConfigurationJsonObject =
				sortParameterConfigurationJsonArray.getJSONObject(i);

			String parameterName = sortConfigurationJsonObject.getString(
				SortConfigurationKeys.PARAMETER_NAME.getJsonKey());

			String field = sortConfigurationJsonObject.getString(
				SortConfigurationKeys.FIELD.getJsonKey());

			if (Validator.isBlank(parameterName) || Validator.isBlank(field)) {
				continue;
			}

			SearchParameterData searchParameterData =
				searchRequestContext.getSearchParameterData();

			Optional<Parameter> sortParameterOptional =
				searchParameterData.getByName(parameterName);

			if (sortParameterOptional.isPresent()) {
				String orderString = (String)sortParameterOptional.get(
				).getValue();

				Sort sort = _getSort(searchRequestContext, field, orderString);

				if (sort != null) {
					sorts.add(sort);
				}
			}
		}

		return sorts;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SortsSearchRequestDataContributor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private Sorts _sorts;

}