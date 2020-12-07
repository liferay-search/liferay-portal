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

		List<Sort> sorts = _getSortsFromParameters(parameterData, blueprint, messages);
	
		if (sorts.isEmpty()) {
			sorts = _getDefaultSorts(parameterData, blueprint, messages);
		}

		if (!sorts.isEmpty()) {
			sorts.stream().forEach(sort->searchRequestBuilder.addSort(sort));
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

			if (!_validateConfiguration(configurationJsonObject, messages)) {
				continue;
			}

			JSONObject parsedConfigurationJsonObject = 
					_getParsedConfigurationJsonObject(
							parameterData, messages, configurationJsonObject);
				
			if (parsedConfigurationJsonObject == null) {
				continue;
			}

			SortOrder sortOrder = _getSortOrder(
					parsedConfigurationJsonObject.getString(
							SortConfigurationKeys.ORDER.getJsonKey()), messages);

			if (sortOrder == null) {
				continue;
			}
			
			Optional<Sort> sortOptional = _getSort(
					parsedConfigurationJsonObject, sortOrder, messages);
			
			if (sortOptional.isPresent()) {
				sorts.add(sortOptional.get());
			}
		}

		return sorts;
	}
			
	private JSONObject _getParsedConfigurationJsonObject(ParameterData parameterData, Messages messages, JSONObject configurationJsonObject) {

		try {
			return 
					_blueprintTemplateVariableParser.parse(
						parameterData, messages, configurationJsonObject);
		}
		catch (Exception exception) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.unknown-sort-configuration-error",
					exception.getMessage(), exception,
					configurationJsonObject, null, null));

			_log.error(exception.getMessage(), exception);
		}
		
		return null;
	}
	
	private Optional<Sort> _getSort(JSONObject configurationJsonObject, SortOrder sortOrder, Messages messages) {
		
		String type = configurationJsonObject.getString("type", "field");

		try {
			SortTranslator sortTranslator =
					_sortTranslatorFactory.getTranslator(type);
			
			return 	sortTranslator.translate(
					configurationJsonObject, sortOrder, messages);

		}
		catch (IllegalArgumentException illegalArgumentException) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.unknown-sort-type",
					illegalArgumentException.getMessage(),
					illegalArgumentException, configurationJsonObject,
					SortConfigurationKeys.TYPE.getJsonKey(), type));

			_log.error(
				illegalArgumentException.getMessage(),
				illegalArgumentException);
		}
		
		return Optional.empty();
	}
	
	private SortOrder _getSortOrder(
			String sortOrderString,  Messages messages) {

		try {
			sortOrderString = StringUtil.toUpperCase(sortOrderString);

			return SortOrder.valueOf(sortOrderString);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.unknown-sort-order",
					illegalArgumentException.getMessage(),
					illegalArgumentException, null, null, sortOrderString));
			_log.error(
				illegalArgumentException.getMessage(),
				illegalArgumentException);

		}
		return null;
	}
	
	private SortOrder _getSortOrderFromParameter(ParameterData parameterData, 
			JSONObject configurationJsonObject, Messages messages) {
		
		String parameterName = configurationJsonObject.getString(
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

			Optional<JSONArray> configurationJsonArrayOptional =
					_blueprintHelper.getSortParameterConfigurationOptional(blueprint);

			if (!configurationJsonArrayOptional.isPresent()) {
				return sorts;
			}

			JSONArray configurationJsonArray =
					configurationJsonArrayOptional.get();

			for (int i = 0; i < configurationJsonArray.length(); i++) {
				JSONObject configurationJsonObject =
					configurationJsonArray.getJSONObject(i);

				if (!_validateConfiguration(configurationJsonObject, messages)) {
					continue;
				}

				JSONObject parsedConfigurationJsonObject = 
						_getParsedConfigurationJsonObject(
								parameterData, messages, configurationJsonObject);

				if (parsedConfigurationJsonObject == null) {
					continue;
				}

				SortOrder sortOrder = _getSortOrderFromParameter(
							parameterData, parsedConfigurationJsonObject, messages);
					
				if (sortOrder == null) {
					continue;
				}

				Optional<Sort> sort = _getSort(parsedConfigurationJsonObject, sortOrder, messages);

				if (sort.isPresent()) {
					sorts.add(sort.get());
				}
			}

			return sorts;
		}
	
	private boolean _validateConfiguration(JSONObject configurationJsonObject, Messages messages) {

		boolean valid = true;

		if (configurationJsonObject.isNull(
				SortConfigurationKeys.FIELD.getJsonKey())) {

			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-sort-field",
					"Sort field is not defined", null,
					configurationJsonObject,
					SortConfigurationKeys.FIELD.getJsonKey(), null));
			valid = false;

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Sort field is not defined [ " +
						configurationJsonObject + "]");
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