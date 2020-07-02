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

package com.liferay.portal.search.tuning.gsearch.impl.internal.request.parameter;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.AggregationConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.ParameterConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SearchConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.impl.internal.aggregations.facet.FacetHandlerFactory;
import com.liferay.portal.search.tuning.gsearch.impl.internal.parameter.SearchParameterDataImpl;
import com.liferay.portal.search.tuning.gsearch.impl.internal.request.parameter.contributor.RequestParameterContributor;
import com.liferay.portal.search.tuning.gsearch.message.Message;
import com.liferay.portal.search.tuning.gsearch.message.Severity;
import com.liferay.portal.search.tuning.gsearch.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.gsearch.spi.aggregation.facet.FacetHandler;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = RequestParameterBuilder.class)
public class RequestParameterBuilderImpl implements RequestParameterBuilder {

	public SearchParameterData build(
		HttpServletRequest httpServletRequest,
		JSONObject searchConfigurationJsonObject) {

		SearchParameterData searchParameterData = new SearchParameterDataImpl();

		_parseParameterConfiguration(
			httpServletRequest, searchParameterData,
			searchConfigurationJsonObject);

		_parseAggregationsConfiguration(
			httpServletRequest, searchParameterData,
			searchConfigurationJsonObject);

		return searchParameterData;
	}

	private void _parseAggregationsConfiguration(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData,
		JSONObject searchConfigurationJsonObject) {

		JSONArray aggregationConfigurationJsonArray =
			searchConfigurationJsonObject.getJSONArray(
				AggregationConfigurationKeys.AGGREGATION_CONFIGURATION);

		if ((aggregationConfigurationJsonArray == null) ||
			(aggregationConfigurationJsonArray.length() == 0)) {

			return;
		}

		for (int i = 0; i < aggregationConfigurationJsonArray.length(); i++) {
			JSONObject aggregationJsonObject =
				aggregationConfigurationJsonArray.getJSONObject(i);

			JSONObject facetJsonObject = aggregationJsonObject.getJSONObject(
				FacetConfigurationKeys.FACET);

			if (facetJsonObject == null) {
				continue;
			}

			boolean enabled = facetJsonObject.getBoolean(
				FacetConfigurationKeys.ENABLED);

			if (!enabled) {
				continue;
			}

			if (_validateFacetParameter(searchParameterData, facetJsonObject)) {
				_parseFacetParameter(
					httpServletRequest, searchParameterData, facetJsonObject);
			}
		}
	}

	private void _parseFacetParameter(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData, JSONObject facetJsonObject) {

		String parameterName = facetJsonObject.getString(
			SearchConfigurationKeys.PARAMETER_NAME);

		if (ParamUtil.getString(httpServletRequest, parameterName) == null) {
			return;
		}

		try {
			String handler = facetJsonObject.getString(
				FacetConfigurationKeys.HANDLER, "default");

			FacetHandler facetHandler = _facetHandlerFactory.getHandler(
				handler);

			facetHandler.addSearchParameter(
				httpServletRequest, searchParameterData, facetJsonObject);
		}
		catch (IllegalArgumentException iae) {
			searchParameterData.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.unknown-facet-handler",
					null, null, facetJsonObject,
					ParameterConfigurationKeys.TYPE, null));
			_log.error(iae.getMessage(), iae);
		}
	}

	private void _parseParameter(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData,
		JSONObject parameterJsonObject) {

		String type = parameterJsonObject.getString(
			ParameterConfigurationKeys.TYPE);

		try {
			RequestParameterContributor requestParameterContributor =
				_requestParameterContributorFactory.getContributor(type);

			requestParameterContributor.contribute(
				httpServletRequest, searchParameterData, parameterJsonObject);
		}
		catch (IllegalArgumentException iae) {
			searchParameterData.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.unknown-parameter-type",
					null, null, parameterJsonObject,
					ParameterConfigurationKeys.TYPE, null));
			_log.error(iae.getMessage(), iae);
		}
	}

	private void _parseParameterConfiguration(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData,
		JSONObject searchConfigurationJsonObject) {

		JSONArray parameterConfigurationJsonArray =
			searchConfigurationJsonObject.getJSONArray(
				ParameterConfigurationKeys.PARAMETER_CONFIGURATION);

		if ((parameterConfigurationJsonArray == null) ||
			(parameterConfigurationJsonArray.length() == 0)) {

			return;
		}

		for (int i = 0; i < parameterConfigurationJsonArray.length(); i++) {
			JSONObject parameterJsonObject =
				parameterConfigurationJsonArray.getJSONObject(i);

			if (_validateParameter(searchParameterData, parameterJsonObject)) {
				_parseParameter(
					httpServletRequest, searchParameterData,
					parameterJsonObject);
			}
		}
	}

	private boolean _validateFacetParameter(
		SearchParameterData searchParameterData,
		JSONObject configurationJsonObject) {

		boolean valid = true;

		if (Validator.isNull(
				configurationJsonObject.getString(
					ParameterConfigurationKeys.PARAMETER_NAME))) {

			searchParameterData.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-parameter-name", null, null,
					configurationJsonObject,
					ParameterConfigurationKeys.PARAMETER_NAME, null));
			valid = false;
		}

		return valid;
	}

	private boolean _validateParameter(
		SearchParameterData searchParameterData,
		JSONObject configurationJsonObject) {

		boolean valid = true;

		if (Validator.isNull(
				configurationJsonObject.getString(
					ParameterConfigurationKeys.PARAMETER_NAME))) {

			searchParameterData.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-parameter-name", null, null,
					configurationJsonObject,
					ParameterConfigurationKeys.PARAMETER_NAME, null));
			valid = false;
		}

		if (Validator.isNull(
				configurationJsonObject.getString(
					ParameterConfigurationKeys.TYPE))) {

			searchParameterData.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-parameter-type", null, null,
					configurationJsonObject, ParameterConfigurationKeys.TYPE,
					null));
			valid = false;
		}

		return valid;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		RequestParameterBuilderImpl.class);

	@Reference
	private FacetHandlerFactory _facetHandlerFactory;

	@Reference
	private RequestParameterContributorFactory
		_requestParameterContributorFactory;

}