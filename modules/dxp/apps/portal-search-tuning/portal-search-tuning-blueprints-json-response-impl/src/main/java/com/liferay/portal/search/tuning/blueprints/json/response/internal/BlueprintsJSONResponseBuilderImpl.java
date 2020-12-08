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

package com.liferay.portal.search.tuning.blueprints.json.response.internal;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.component.ServiceComponentReference;
import com.liferay.portal.search.tuning.blueprints.json.response.BlueprintsJSONResponseBuilder;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.response.spi.contributor.ResponseContributor;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintsJSONResponseBuilder.class)
public class BlueprintsJSONResponseBuilderImpl
	implements BlueprintsJSONResponseBuilder {

	@Override
	public JSONObject buildJSONObject(
			SearchResponse searchResponse,
			BlueprintsAttributes blueprintsAttributes, 
			ResourceBundle resourceBundle,
			Messages messages,
			long blueprintId)
		throws PortalException {

		long startTime = System.currentTimeMillis();

		Blueprint blueprint = _blueprintService.getBlueprint(blueprintId);
		
		JSONObject responseJsonObject = JSONFactoryUtil.createJSONObject();

		for (Map.Entry<String, ServiceComponentReference<ResponseContributor>>
				entry : _responseContributors.entrySet()) {

			ServiceComponentReference<ResponseContributor> value =
				entry.getValue();

			ResponseContributor responseContributor =
				value.getServiceComponent();

			responseContributor.contribute(
				responseJsonObject, searchResponse, blueprint,
				blueprintsAttributes, resourceBundle, messages);
		}

		if (_log.isDebugEnabled()) {
			_log.debug(
				"Building response took took: " +
					(System.currentTimeMillis() - startTime));
		}

		return responseJsonObject;
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void registerResponseContributor(
		ResponseContributor responseContributor,
		Map<String, Object> properties) {

		String name = (String)properties.get("name");

		if (Validator.isBlank(name)) {
			if (_log.isWarnEnabled()) {
				Class<?> clazz = responseContributor.getClass();

				_log.warn(
					"Unable to add response contributor " + clazz.getName() +
						". Name property empty.");
			}

			return;
		}

		int serviceRanking = GetterUtil.get(
			properties.get("service.ranking"), 0);

		ServiceComponentReference<ResponseContributor>
			serviceComponentReference = new ServiceComponentReference<>(
				responseContributor, serviceRanking);

		if (_responseContributors.containsKey(name)) {
			ServiceComponentReference<ResponseContributor> previousReference =
				_responseContributors.get(name);

			if (previousReference.compareTo(serviceComponentReference) < 0) {
				_responseContributors.put(name, serviceComponentReference);
			}
		}
		else {
			_responseContributors.put(name, serviceComponentReference);
		}
	}

	protected void unregisterResponseContributor(
		ResponseContributor responseContributor,
		Map<String, Object> properties) {

		String name = (String)properties.get("name");

		if (Validator.isBlank(name)) {
			return;
		}

		_responseContributors.remove(name);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintsJSONResponseBuilderImpl.class);

	@Reference
	private BlueprintService _blueprintService;
	
	private volatile Map<String, ServiceComponentReference<ResponseContributor>>
		_responseContributors = new ConcurrentHashMap<>();

}