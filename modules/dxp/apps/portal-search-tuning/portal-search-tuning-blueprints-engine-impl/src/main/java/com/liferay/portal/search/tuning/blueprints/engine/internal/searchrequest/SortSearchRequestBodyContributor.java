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
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.sort.Sort;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.sort.SortConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.internal.sort.SortTranslatorFactory;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.portal.search.tuning.blueprints.engine.spi.sort.SortTranslator;
import com.liferay.portal.search.tuning.blueprints.engine.util.BlueprintTemplateVariableParser;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
		SearchRequestBuilder searchRequestBuilder, Blueprint blueprint,
		ParameterData parameterData, Messages messages) {

		List<Sort> sorts = _getSortsFromParameters(
			parameterData, blueprint, messages);

		if (sorts.isEmpty()) {
			sorts = _getDefaultSorts(parameterData, blueprint, messages);
		}

		if (!sorts.isEmpty()) {
			Stream<Sort> stream = sorts.stream();

			stream.forEach(sort -> searchRequestBuilder.addSort(sort));
		}
	}

	private List<Sort> _getDefaultSorts(
		ParameterData parameterData, Blueprint blueprint, Messages messages) {

		List<Sort> sorts = new ArrayList<>();

		Optional<JSONArray> configurationJSONArrayOptional =
			_blueprintHelper.getDefaultSortConfigurationOptional(blueprint);

		if (!configurationJSONArrayOptional.isPresent()) {
			return sorts;
		}

		JSONArray configurationJSONArray = configurationJSONArrayOptional.get();

		for (int i = 0; i < configurationJSONArray.length(); i++) {
			JSONObject configurationJSONObject =
				configurationJSONArray.getJSONObject(i);

			if (!_validateConfiguration(configurationJSONObject, messages)) {
				continue;
			}

			JSONObject parsedConfigurationJSONObject =
				_getParsedConfigurationJSONObject(
					parameterData, messages, configurationJSONObject);

			if (parsedConfigurationJSONObject == null) {
				continue;
			}

			SortOrder sortOrder = _getSortOrder(
				parsedConfigurationJSONObject.getString(
					SortConfigurationKeys.ORDER.getJsonKey()),
				messages);

			if (sortOrder == null) {
				continue;
			}

			Optional<Sort> sortOptional = _getSort(
				parsedConfigurationJSONObject, sortOrder, messages);

			if (sortOptional.isPresent()) {
				sorts.add(sortOptional.get());
			}
		}

		return sorts;
	}

	private JSONObject _getParsedConfigurationJSONObject(
		ParameterData parameterData, Messages messages,
		JSONObject configurationJSONObject) {

		try {
			return _blueprintTemplateVariableParser.parse(
				configurationJSONObject, parameterData, messages);
		}
		catch (Exception exception) {
			messages.addMessage(
				new Message.Builder().className(
					getClass().getName()
				).localizationKey(
					"core.error.unknown-sort-configuration-error"
				).msg(
					exception.getMessage()
				).rootObject(
					configurationJSONObject
				).severity(
					Severity.ERROR
				).throwable(
					exception
				).build());

			_log.error(exception.getMessage(), exception);
		}

		return null;
	}

	private Optional<Sort> _getSort(
		JSONObject configurationJSONObject, SortOrder sortOrder,
		Messages messages) {

		String type = configurationJSONObject.getString("type", "field");

		try {
			SortTranslator sortTranslator =
				_sortTranslatorFactory.getTranslator(type);

			return sortTranslator.translate(
				configurationJSONObject, sortOrder, messages);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			messages.addMessage(
				new Message.Builder().className(
					getClass().getName()
				).localizationKey(
					"core.error.unknown-sort-type"
				).msg(
					illegalArgumentException.getMessage()
				).rootObject(
					configurationJSONObject
				).rootProperty(
					SortConfigurationKeys.TYPE.getJsonKey()
				).rootValue(
					type
				).severity(
					Severity.ERROR
				).throwable(
					illegalArgumentException
				).build());

			_log.error(
				illegalArgumentException.getMessage(),
				illegalArgumentException);
		}

		return Optional.empty();
	}

	private SortOrder _getSortOrder(String sortOrderString, Messages messages) {
		try {
			sortOrderString = StringUtil.toUpperCase(sortOrderString);

			return SortOrder.valueOf(sortOrderString);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			messages.addMessage(
				new Message.Builder().className(
					getClass().getName()
				).localizationKey(
					"core.error.unknown-sort-order"
				).msg(
					illegalArgumentException.getMessage()
				).rootValue(
					sortOrderString
				).severity(
					Severity.ERROR
				).throwable(
					illegalArgumentException
				).build());

			_log.error(
				illegalArgumentException.getMessage(),
				illegalArgumentException);
		}

		return null;
	}

	private SortOrder _getSortOrderFromParameter(
		ParameterData parameterData, JSONObject configurationJSONObject,
		Messages messages) {

		String parameterName = configurationJSONObject.getString(
			SortConfigurationKeys.PARAMETER_NAME.getJsonKey());

		if (Validator.isBlank(parameterName)) {
			return null;
		}

		Optional<Parameter> sortParameterOptional =
			parameterData.getByNameOptional(parameterName);

		if (!sortParameterOptional.isPresent()) {
			return null;
		}

		Parameter parameter = sortParameterOptional.get();

		String sortOrderString = GetterUtil.getString(parameter.getValue());

		return _getSortOrder(sortOrderString, messages);
	}

	private List<Sort> _getSortsFromParameters(
		ParameterData parameterData, Blueprint blueprint, Messages messages) {

		List<Sort> sorts = new ArrayList<>();

		Optional<JSONArray> configurationJSONArrayOptional =
			_blueprintHelper.getSortParameterConfigurationOptional(blueprint);

		if (!configurationJSONArrayOptional.isPresent()) {
			return sorts;
		}

		JSONArray configurationJSONArray = configurationJSONArrayOptional.get();

		for (int i = 0; i < configurationJSONArray.length(); i++) {
			JSONObject configurationJSONObject =
				configurationJSONArray.getJSONObject(i);

			if (!_validateConfiguration(configurationJSONObject, messages)) {
				continue;
			}

			JSONObject parsedConfigurationJSONObject =
				_getParsedConfigurationJSONObject(
					parameterData, messages, configurationJSONObject);

			if (parsedConfigurationJSONObject == null) {
				continue;
			}

			SortOrder sortOrder = _getSortOrderFromParameter(
				parameterData, parsedConfigurationJSONObject, messages);

			if (sortOrder == null) {
				continue;
			}

			Optional<Sort> sort = _getSort(
				parsedConfigurationJSONObject, sortOrder, messages);

			if (sort.isPresent()) {
				sorts.add(sort.get());
			}
		}

		return sorts;
	}

	private boolean _validateConfiguration(
		JSONObject configurationJSONObject, Messages messages) {

		boolean valid = true;

		if (configurationJSONObject.isNull(
				SortConfigurationKeys.FIELD.getJsonKey())) {

			messages.addMessage(
				new Message.Builder().className(
					getClass().getName()
				).localizationKey(
					"core.error.undefined-sort-field"
				).msg(
					"Sort field is not defined"
				).rootObject(
					configurationJSONObject
				).rootProperty(
					SortConfigurationKeys.FIELD.getJsonKey()
				).severity(
					Severity.ERROR
				).build());

			valid = false;

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Sort field is not defined [ " + configurationJSONObject +
						"]");
			}
		}

		return valid;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SortSearchRequestBodyContributor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private SortTranslatorFactory _sortTranslatorFactory;

}