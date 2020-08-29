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

package com.liferay.portal.search.tuning.blueprints.engine.internal.parameter;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.engine.component.ServiceComponentReference;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterDefinition;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.parameter.ParameterContributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ParameterContributors.class)
public class ParameterContributorsImpl implements ParameterContributors {

	public void contribute(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData) {

		for (Map.Entry<String, ServiceComponentReference<ParameterContributor>>
				entry : _parameterContributors.entrySet()) {

			ServiceComponentReference<ParameterContributor> value =
				entry.getValue();

			ParameterContributor parameterContributor =
				value.getServiceComponent();

			parameterContributor.contribute(
				httpServletRequest, searchParameterData);
		}

		_logParameters(searchParameterData);
	}

	public void contribute(
			HttpServletRequest httpServletRequest,
			SearchParameterData searchParameterData,
			String parameterContributorName)
		throws IllegalArgumentException {

		ParameterContributor parameterContributor = _getContributorByName(
			parameterContributorName);

		parameterContributor.contribute(
			httpServletRequest, searchParameterData);
	}

	public void contribute(
		SearchContext searchContext, SearchParameterData searchParameterData) {

		for (Map.Entry<String, ServiceComponentReference<ParameterContributor>>
				entry : _parameterContributors.entrySet()) {

			ServiceComponentReference<ParameterContributor> value =
				entry.getValue();

			ParameterContributor parameterContributor =
				value.getServiceComponent();

			parameterContributor.contribute(searchContext, searchParameterData);
		}

		_logParameters(searchParameterData);
	}

	public void contribute(
			SearchContext searchContext,
			SearchParameterData searchParameterData,
			String parameterContributorName)
		throws IllegalArgumentException {

		ParameterContributor parameterContributor = _getContributorByName(
			parameterContributorName);

		parameterContributor.contribute(searchContext, searchParameterData);
	}

	public ParameterDefinition[] getParameterDefinitions() {
		List<ParameterDefinition> parameterDefinitionList = new ArrayList<>();

		for (Map.Entry<String, ServiceComponentReference<ParameterContributor>>
				entry : _parameterContributors.entrySet()) {

			ServiceComponentReference<ParameterContributor> value =
				entry.getValue();

			ParameterContributor parameterContributor =
				value.getServiceComponent();

			parameterDefinitionList.addAll(
				parameterContributor.getParameterDefinitions());
		}

		return parameterDefinitionList.toArray(new ParameterDefinition[0]);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void registerContextParameterContributor(
		ParameterContributor parameterContributor,
		Map<String, Object> properties) {

		String name = (String)properties.get("name");

		if (Validator.isBlank(name)) {
			if (_log.isWarnEnabled()) {
				Class<?> clazz = parameterContributor.getClass();

				_log.warn(
					"Unable to add parameter contributor " + clazz.getName() +
						". Name property empty.");
			}

			return;
		}

		int serviceRanking = GetterUtil.get(
			properties.get("service.ranking"), 0);

		ServiceComponentReference<ParameterContributor>
			serviceComponentReference = new ServiceComponentReference<>(
				parameterContributor, serviceRanking);

		if (_parameterContributors.containsKey(name)) {
			ServiceComponentReference<ParameterContributor> previousReference =
				_parameterContributors.get(name);

			if (previousReference.compareTo(serviceComponentReference) < 0) {
				_parameterContributors.put(name, serviceComponentReference);
			}
		}
		else {
			_parameterContributors.put(name, serviceComponentReference);
		}
	}

	protected void unregisterContextParameterContributor(
		ParameterContributor parameterContributor,
		Map<String, Object> properties) {

		String name = (String)properties.get("name");

		if (Validator.isBlank(name)) {
			return;
		}

		_parameterContributors.remove(name);
	}

	private ParameterContributor _getContributorByName(String name)
		throws IllegalArgumentException {

		ServiceComponentReference<ParameterContributor>
			serviceComponentReference = _parameterContributors.get(name);

		if (serviceComponentReference == null) {
			throw new IllegalArgumentException(
				"Unable to find parameter contributor " + name);
		}

		return serviceComponentReference.getServiceComponent();
	}

	private void _logParameters(SearchParameterData searchParameterData) {
		if (_log.isDebugEnabled()) {
			_log.debug(
				"Available template variables after parameter contributions:");

			if (searchParameterData.hasParameters()) {
				for (Parameter parameter :
						searchParameterData.getParameters()) {

					_log.debug(
						parameter.getTemplateVariable() + ":" +
							parameter.getValue());
				}
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ParameterContributorsImpl.class);

	private volatile Map
		<String, ServiceComponentReference<ParameterContributor>>
			_parameterContributors = new ConcurrentHashMap<>();

}