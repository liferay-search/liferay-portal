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

package com.liferay.portal.search.tuning.blueprints.engine.internal.request.parameter;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.requestparameter.CustomRequestParameterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.requestparameter.KeywordsConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.requestparameter.PagingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.requestparameter.RequestParameterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.requestparameter.SortConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.internal.request.parameter.contributor.RequestParameterContributor;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.IntegerParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.StringParameter;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = RequestParameterBuilder.class)
public class RequestParameterBuilderImpl implements RequestParameterBuilder {

	public SearchParameterData build(
		HttpServletRequest httpServletRequest, JSONObject blueprintJsonObject) {

		SearchParameterData searchParameterData = new SearchParameterData();

		Optional<JSONObject> requestParameterConfigurationJsonObjectOptional =
			_blueprintHelper.getRequestParameterConfigurationOptional(
				blueprintJsonObject);

		if (!requestParameterConfigurationJsonObjectOptional.isPresent()) {
			return searchParameterData;
		}

		JSONObject requestParameterConfigurationJsonObject =
			requestParameterConfigurationJsonObjectOptional.get();

		_parseKeywordsConfiguration(
			httpServletRequest, searchParameterData,
			requestParameterConfigurationJsonObject.getJSONObject(
				RequestParameterConfigurationKeys.KEYWORDS.getJsonKey()));

		_parsePagingConfiguration(
			httpServletRequest, searchParameterData,
			requestParameterConfigurationJsonObject.getJSONObject(
				RequestParameterConfigurationKeys.PAGING.getJsonKey()));

		_parseSortParameterConfiguration(
			httpServletRequest, searchParameterData,
			requestParameterConfigurationJsonObject.getJSONArray(
				RequestParameterConfigurationKeys.SORTS.getJsonKey()));

		_parseCustomParameterConfiguration(
			httpServletRequest, searchParameterData,
			requestParameterConfigurationJsonObject.getJSONArray(
				RequestParameterConfigurationKeys.CUSTOM.getJsonKey()));

		return searchParameterData;
	}

	private void _parseCustomParameter(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData,
		JSONObject parameterJsonObject) {

		String type = parameterJsonObject.getString(
			CustomRequestParameterConfigurationKeys.TYPE.getJsonKey());

		try {
			RequestParameterContributor requestParameterContributor =
				_requestParameterContributorFactory.getContributor(type);

			requestParameterContributor.contribute(
				httpServletRequest, searchParameterData, parameterJsonObject);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			searchParameterData.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.unknown-parameter-type",
					null, null, parameterJsonObject,
					CustomRequestParameterConfigurationKeys.TYPE.getJsonKey(),
					null));
			_log.error(
				illegalArgumentException.getMessage(),
				illegalArgumentException);
		}
	}

	private void _parseCustomParameterConfiguration(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData,
		JSONArray customParameterConfigurationJsonArray) {

		if ((customParameterConfigurationJsonArray == null) ||
			(customParameterConfigurationJsonArray.length() == 0)) {

			return;
		}

		for (int i = 0; i < customParameterConfigurationJsonArray.length();
			 i++) {

			JSONObject parameterJsonObject =
				customParameterConfigurationJsonArray.getJSONObject(i);

			if (_validateCustonParameterConfiguration(
					searchParameterData, parameterJsonObject)) {

				_parseCustomParameter(
					httpServletRequest, searchParameterData,
					parameterJsonObject);
			}
		}
	}

	private void _parseKeywordsConfiguration(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData,
		JSONObject keywordsConfigurationJsonObject) {

		if ((keywordsConfigurationJsonObject == null) ||
			keywordsConfigurationJsonObject.isNull(
				KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey())) {

			return;
		}

		String parameterName = keywordsConfigurationJsonObject.getString(
			KeywordsConfigurationKeys.PARAMETER_NAME.getJsonKey());

		String parameterValue = ParamUtil.getString(
			httpServletRequest, parameterName);

		// Keywords is always accessible with ${keywords}.

		searchParameterData.addParameter(
			new StringParameter("keywords", "${keywords}", parameterValue));

		searchParameterData.addParameter(
			new StringParameter(
				parameterName, "${request." + parameterName + "}",
				parameterValue));
	}

	private void _parsePagingConfiguration(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData,
		JSONObject pagingConfigurationJsonObject) {

		if ((pagingConfigurationJsonObject == null) ||
			pagingConfigurationJsonObject.isNull(
				PagingConfigurationKeys.PARAMETER_NAME.getJsonKey())) {

			return;
		}

		String parameterName = pagingConfigurationJsonObject.getString(
			PagingConfigurationKeys.PARAMETER_NAME.getJsonKey());

		int parameterValue = ParamUtil.getInteger(
			httpServletRequest, parameterName, 1);

		searchParameterData.addParameter(
			new IntegerParameter(
				parameterName, "${request." + parameterName + "}",
				parameterValue));
	}

	private void _parseSortParameterConfiguration(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData,
		JSONArray sortConfigurationJsonArray) {

		if ((sortConfigurationJsonArray == null) ||
			(sortConfigurationJsonArray.length() == 0)) {

			return;
		}

		for (int i = 0; i < sortConfigurationJsonArray.length(); i++) {
			JSONObject sortConfigurationJsonObject =
				sortConfigurationJsonArray.getJSONObject(i);

			if (_validateSortParameterConfiguration(
					searchParameterData, sortConfigurationJsonObject)) {

				RequestParameterContributor requestParameterContributor =
					_requestParameterContributorFactory.getContributor(
						"string");

				requestParameterContributor.contribute(
					httpServletRequest, searchParameterData,
					sortConfigurationJsonObject);
			}
		}
	}

	private boolean _validateCustonParameterConfiguration(
		SearchParameterData searchParameterData,
		JSONObject configurationJsonObject) {

		boolean valid = true;

		if (configurationJsonObject.isNull(
				CustomRequestParameterConfigurationKeys.PARAMETER_NAME.
					getJsonKey())) {

			searchParameterData.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-parameter-name", null, null,
					configurationJsonObject,
					CustomRequestParameterConfigurationKeys.PARAMETER_NAME.
						getJsonKey(),
					null));
			valid = false;

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Undefined parameter name [ " + configurationJsonObject +
						" ].");
			}
		}

		if (configurationJsonObject.isNull(
				CustomRequestParameterConfigurationKeys.TYPE.getJsonKey())) {

			searchParameterData.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-parameter-type", null, null,
					configurationJsonObject,
					CustomRequestParameterConfigurationKeys.TYPE.getJsonKey(),
					null));
			valid = false;

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Undefined parameter type [ " + configurationJsonObject +
						" ].");
			}
		}

		return valid;
	}

	private boolean _validateSortParameterConfiguration(
		SearchParameterData searchParameterData,
		JSONObject configurationJsonObject) {

		boolean valid = true;

		if (configurationJsonObject.isNull(
				SortConfigurationKeys.PARAMETER_NAME.getJsonKey())) {

			searchParameterData.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-parameter-name", null, null,
					configurationJsonObject,
					SortConfigurationKeys.PARAMETER_NAME.getJsonKey(), null));
			valid = false;

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Undefined parameter name [ " + configurationJsonObject +
						" ].");
			}
		}

		if (configurationJsonObject.isNull(
				SortConfigurationKeys.FIELD.getJsonKey())) {

			searchParameterData.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.undefined-field", null,
					null, configurationJsonObject,
					SortConfigurationKeys.FIELD.getJsonKey(), null));
			valid = false;

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Undefined parameter name [ " + configurationJsonObject +
						" ].");
			}
		}

		return valid;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		RequestParameterBuilderImpl.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private RequestParameterContributorFactory
		_requestParameterContributorFactory;

}