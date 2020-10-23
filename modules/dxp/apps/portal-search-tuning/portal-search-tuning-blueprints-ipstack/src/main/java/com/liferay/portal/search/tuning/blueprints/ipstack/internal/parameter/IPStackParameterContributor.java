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

package com.liferay.portal.search.tuning.blueprints.ipstack.internal.parameter;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.DoubleParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterDataBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterDefinition;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.StringParameter;
import com.liferay.portal.search.tuning.blueprints.engine.spi.dataprovider.GeoLocationDataProvider;
import com.liferay.portal.search.tuning.blueprints.engine.spi.parameter.ParameterContributor;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=ipstack",
	service = ParameterContributor.class
)
public class IPStackParameterContributor implements ParameterContributor {

	@Override
	public void contribute(
		ParameterDataBuilder parameterDataBuilder, Blueprint blueprint,
		BlueprintsAttributes blueprintsAttributes, Messages messages) {

		String ipAddress = _getIpAddress(blueprintsAttributes);

		if (Validator.isBlank(ipAddress)) {
			messages.addMessage(
				new Message(
					Severity.INFO, "ipstack",
					"core.error.ip-parameter-not-found",
					"IP address not found in parameter data"));

			return;
		}

		_contribute(parameterDataBuilder, messages, ipAddress);
	}

	@Override
	public List<ParameterDefinition> getParameterDefinitions() {
		List<ParameterDefinition> parameterDefinitions = new ArrayList<>();

		parameterDefinitions.add(
			new ParameterDefinition(
				"${ipstack.city}", StringParameter.class.getName(),
				"parameter.ipstack.city"));
		parameterDefinitions.add(
			new ParameterDefinition(
				"${ipstack.continent_code}", StringParameter.class.getName(),
				"parameter.ipstack.continent-code"));
		parameterDefinitions.add(
			new ParameterDefinition(
				"${ipstack.continent_name}", StringParameter.class.getName(),
				"parameter.ipstack.continent-name"));
		parameterDefinitions.add(
			new ParameterDefinition(
				"${ipstack.country_code}", StringParameter.class.getName(),
				"parameter.ipstack.country-code"));
		parameterDefinitions.add(
			new ParameterDefinition(
				"${ipstack.country_name}", StringParameter.class.getName(),
				"parameter.ipstack.country-name"));
		parameterDefinitions.add(
			new ParameterDefinition(
				"${ipstack.latitude}", DoubleParameter.class.getName(),
				"parameter.ipstack.latitude"));
		parameterDefinitions.add(
			new ParameterDefinition(
				"${ipstack.longitude}", DoubleParameter.class.getName(),
				"parameter.ipstack.longitude"));
		parameterDefinitions.add(
			new ParameterDefinition(
				"${ipstack.region_code}", StringParameter.class.getName(),
				"parameter.ipstack.region-code"));
		parameterDefinitions.add(
			new ParameterDefinition(
				"${ipstack.region_name}", StringParameter.class.getName(),
				"parameter.ipstack.region-name"));
		parameterDefinitions.add(
			new ParameterDefinition(
				"${ipstack.zip}", StringParameter.class.getName(),
				"parameter.ipstack.zip"));

		return parameterDefinitions;
	}

	private void _contribute(
		ParameterDataBuilder parameterDataBuilder, Messages messages,
		String ipAddress) {

		JSONObject geoLocationJSONObject =
			_geoLocationDataProvider.getGeoLocationDataJSONObject(messages, ipAddress);

		if (geoLocationJSONObject == null) {
			return;
		}

		parameterDataBuilder.addParameter(
			new StringParameter(
				"ipstack.city", "${ipstack.city}",
				geoLocationJSONObject.getString("city")));

		parameterDataBuilder.addParameter(
			new StringParameter(
				"ipstack.continent_code", "${ipstack.continent_code}",
				geoLocationJSONObject.getString("continent_code")));

		parameterDataBuilder.addParameter(
			new StringParameter(
				"ipstack.continent_name", "${ipstack.continent_name}",
				geoLocationJSONObject.getString("continent_name")));

		parameterDataBuilder.addParameter(
			new StringParameter(
				"ipstack.country_code", "${ipstack.country_code}",
				geoLocationJSONObject.getString("country_code")));

		parameterDataBuilder.addParameter(
			new StringParameter(
				"ipstack.country_name", "${ipstack.country_name}",
				geoLocationJSONObject.getString("country_name")));

		parameterDataBuilder.addParameter(
			new DoubleParameter(
				"ipstack.latitude", "${ipstack.latitude}",
				geoLocationJSONObject.getDouble("latitude")));

		parameterDataBuilder.addParameter(
			new DoubleParameter(
				"ipstack.longitude", "${ipstack.longitude}",
				geoLocationJSONObject.getDouble("longitude")));

		parameterDataBuilder.addParameter(
			new StringParameter(
				"ipstack.region_code", "${ipstack.region_code}",
				geoLocationJSONObject.getString("region_code")));

		parameterDataBuilder.addParameter(
			new StringParameter(
				"ipstack.region_name", "${ipstack.region_name}",
				geoLocationJSONObject.getString("region_name")));

		parameterDataBuilder.addParameter(
			new StringParameter(
				"ipstack.zip", "${ipstack.zip}",
				geoLocationJSONObject.getString("zip")));
	}

	private String _getIpAddress(BlueprintsAttributes blueprintsAttributes) {
		Optional<Object> valueOptional =
			blueprintsAttributes.getAttributeOptional(
				ReservedParameterNames.IP_ADDRESS.getKey());

		return GetterUtil.getString(valueOptional.orElse(null));
	}

	@Reference
	private GeoLocationDataProvider _geoLocationDataProvider;

}