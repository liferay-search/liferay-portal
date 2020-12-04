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

package com.liferay.portal.search.tuning.blueprints.engine.internal.searchrequest;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.sort.Sort;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.sort.SortConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.internal.util.BlueprintValueUtil;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.portal.search.tuning.blueprints.engine.util.BlueprintTemplateVariableParser;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=sort",
	service = SearchRequestBodyContributor.class
)
public class SortSearchRequestBodyContributor
	implements SearchRequestBodyContributor {

	@Override
	public void contribute(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, Messages messages) {

		Optional<JSONArray> configurationJsonArrayOptional =
			_blueprintHelper.getSortParameterConfigurationOptional(blueprint);

		List<Sort> sorts = new ArrayList<>();

		if (configurationJsonArrayOptional.isPresent()) {
			JSONArray configurationJsonArray =
				configurationJsonArrayOptional.get();

			sorts = _getSortsFromParameters(
				parameterData, messages, configurationJsonArray);
		}

		if (sorts.isEmpty()) {
			sorts = _getDefaultSorts(parameterData, blueprint, messages);
		}

		if (!sorts.isEmpty()) {
			for (Sort sort : sorts) {
				searchRequestBuilder.addSort(sort);
			}
		}
	}

	private List<Sort> _getDefaultSorts(
		ParameterData parameterData, Blueprint blueprint, Messages messages) {

		List<Sort> sorts = new ArrayList<>();

		Optional<JSONArray> configurationJsonArrayOptional =
			_blueprintHelper.getDefaultSortConfigurationOptional(blueprint);

		if (!configurationJsonArrayOptional.isPresent()) {
			return sorts;
		}

		JSONArray configurationJsonArray = configurationJsonArrayOptional.get();

		for (int i = 0; i < configurationJsonArray.length(); i++) {
			JSONObject configurationJsonObject =
				configurationJsonArray.getJSONObject(i);

			try {
				JSONObject parsedConfigurationJsonObject =
					_blueprintTemplateVariableParser.parse(
						parameterData, messages, configurationJsonObject);

				String field = parsedConfigurationJsonObject.getString("field");
				String orderString = parsedConfigurationJsonObject.getString(
					"order");

				if (Validator.isBlank(field) ||
					Validator.isBlank(orderString)) {

					continue;
				}

				Sort sort = _getSort(messages, field, orderString);

				if (sort != null) {
					sorts.add(sort);
				}
			}
			catch (Exception exception) {
				messages.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.unknown-default-sort-configuration-error",
						exception.getMessage(), exception,
						configurationJsonObject, null, null));

				_log.error(exception.getMessage(), exception);
			}
		}

		return sorts;
	}

	private Sort _getSort(Messages messages, String field, String orderString) {
		if (Validator.isBlank(field)) {
			return _sorts.score();
		}

		try {
			return _sorts.field(
				field, BlueprintValueUtil.getSortOrder(orderString));
		}
		catch (IllegalArgumentException illegalArgumentException) {
			messages.addMessage(
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
		ParameterData parameterData, Messages messages,
		JSONArray configurationJsonArray) {

		List<Sort> sorts = new ArrayList<>();

		for (int i = 0; i < configurationJsonArray.length(); i++) {
			JSONObject configurationJsonObject =
				configurationJsonArray.getJSONObject(i);

			JSONObject parsedConfigurationJsonObject = null;

			try {
			
				parsedConfigurationJsonObject =
					_blueprintTemplateVariableParser.parse(
						parameterData, messages, configurationJsonObject);

			} catch (Exception exception) {
				_log.error(exception.getMessage(), exception);
				continue;
			}
			String parameterName = parsedConfigurationJsonObject.getString(
				SortConfigurationKeys.PARAMETER_NAME.getJsonKey());

			if (Validator.isBlank(parameterName)) {
				continue;
			}

			Optional<Parameter> sortParameterOptional =
				parameterData.getByNameOptional(parameterName);

			if (sortParameterOptional.isPresent()) {
				Parameter parameter = sortParameterOptional.get();

				String field = parsedConfigurationJsonObject.getString(
					SortConfigurationKeys.FIELD.getJsonKey());

				String orderString = GetterUtil.getString(parameter.getValue());

				Sort sort = _getSort(messages, field, orderString);

				if (sort != null) {
					sorts.add(sort);
				}
			}
		}

		return sorts;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SortSearchRequestBodyContributor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private Sorts _sorts;

}