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

package com.liferay.portal.search.tuning.blueprints.facets.internal.parameter;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterDefinition;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.facet.FacetRequestHandler;
import com.liferay.portal.search.tuning.blueprints.engine.spi.parameter.ParameterContributor;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.internal.handler.request.FacetRequestHandlerFactory;
import com.liferay.portal.search.tuning.blueprints.poc.util.POCMockUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=facets",
	service = ParameterContributor.class
)
public class FacetParameterContributor implements ParameterContributor {

	@Override
	public void contribute(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData) {

		_provide(httpServletRequest, searchParameterData);
	}

	@Override
	public void contribute(
		SearchContext searchContext, SearchParameterData searchParameterData) {

		return;
	}

	@Override
	public List<ParameterDefinition> getParameterDefinitions() {
		List<ParameterDefinition> parameterDefinitions = new ArrayList<>();

		return parameterDefinitions;
	}

	private void _parseFacetParameter(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData, JSONObject facetJsonObject) {

		String parameterName = facetJsonObject.getString(
			FacetConfigurationKeys.PARAMETER_NAME.getJsonKey());

		if (ParamUtil.getString(httpServletRequest, parameterName) == null) {
			return;
		}

		String handler = facetJsonObject.getString(
			FacetConfigurationKeys.HANDLER.getJsonKey(), "default");

		try {
			FacetRequestHandler facetRequestHandler =
				_facetRequestHandlerFactory.getHandler(handler);

			Optional<Parameter> parameter = facetRequestHandler.getParameter(
				httpServletRequest, searchParameterData, facetJsonObject);

			if (parameter.isPresent()) {
				searchParameterData.addParameter(parameter.get());
			}
		}
		catch (IllegalArgumentException iae) {
			searchParameterData.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.unknown-facet-handler",
					null, null, facetJsonObject,
					FacetConfigurationKeys.HANDLER.getJsonKey(), handler));
			_log.error(iae.getMessage(), iae);
		}
	}

	private void _provide(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData) {

		// TODO

		JSONArray facetsConfigurationJsonArray =
			POCMockUtil.getFacetsParameterConfigurationMock();

		for (int i = 0; i < facetsConfigurationJsonArray.length(); i++) {
			JSONObject facetConfigurationJsonObject =
				facetsConfigurationJsonArray.getJSONObject(i);

			if (_validateFacetParameterConfiguration(
					searchParameterData, facetConfigurationJsonObject)) {

				_parseFacetParameter(
					httpServletRequest, searchParameterData,
					facetConfigurationJsonObject);
			}
		}
	}

	private boolean _validateFacetParameterConfiguration(
		SearchParameterData searchParameterData,
		JSONObject facetConfigurationJsonObject) {

		boolean valid = true;

		if (facetConfigurationJsonObject.isNull(
				FacetConfigurationKeys.PARAMETER_NAME.getJsonKey())) {

			searchParameterData.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-parameter-name", null, null,
					facetConfigurationJsonObject,
					FacetConfigurationKeys.PARAMETER_NAME.getJsonKey(), null));
			valid = false;
		}

		return valid;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FacetParameterContributor.class);

	@Reference
	private FacetRequestHandlerFactory _facetRequestHandlerFactory;

}