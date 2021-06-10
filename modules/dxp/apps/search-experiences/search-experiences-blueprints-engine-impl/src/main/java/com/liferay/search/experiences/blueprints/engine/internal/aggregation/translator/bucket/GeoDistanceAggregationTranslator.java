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

package com.liferay.search.experiences.blueprints.engine.internal.aggregation.translator.bucket;

import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.GeoDistanceAggregation;
import com.liferay.portal.search.geolocation.GeoBuilders;
import com.liferay.portal.search.geolocation.GeoDistanceType;
import com.liferay.portal.search.geolocation.GeoLocationPoint;
import com.liferay.search.experiences.blueprints.constants.json.keys.aggregation.bucket.GeoDistanceAggregationBodyConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.aggregation.AggregationWrapper;
import com.liferay.search.experiences.blueprints.engine.internal.aggregation.util.AggregationHelper;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.aggregation.AggregationTranslator;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.util.util.SetterHelper;

import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=geo_distance",
	service = AggregationTranslator.class
)
public class GeoDistanceAggregationTranslator implements AggregationTranslator {

	@Override
	public Optional<AggregationWrapper> translate(
		String aggregationName, JSONObject jsonObject,
		ParameterData parameterData, Messages messages) {

		GeoLocationPoint geoLocationPoint = _getGeoLocationPoint(jsonObject);

		if (geoLocationPoint == null) {
			return Optional.empty();
		}

		GeoDistanceAggregation aggregation = _aggregations.geoDistance(
			aggregationName,
			jsonObject.getString(
				GeoDistanceAggregationBodyConfigurationKeys.FIELD.getJsonKey()),
			geoLocationPoint);

		_setDistanceUnit(aggregation, jsonObject);

		_setDistanceType(aggregation, jsonObject);

		_setterHelper.setBooleanValue(
			jsonObject,
			GeoDistanceAggregationBodyConfigurationKeys.KEYED.getJsonKey(),
			aggregation::setKeyed);

		_aggregationHelper.setRanges(jsonObject, aggregation::addRange);

		_aggregationHelper.setScript(
			jsonObject, aggregation::setScript, messages);

		return _aggregationHelper.wrap(aggregation);
	}

	private List<String> _getCoordinates(String origin) {
		List<String> coordinates = StringUtil.split(origin, ',');

		if (coordinates.size() != 2) {
			return null;
		}

		return coordinates;
	}

	private GeoLocationPoint _getGeoLocationPoint(JSONObject jsonObject) {
		String origin = jsonObject.getString(
			GeoDistanceAggregationBodyConfigurationKeys.ORIGIN.getJsonKey());

		List<String> coordinates = _getCoordinates(origin);

		if (coordinates == null) {
			return null;
		}

		return _geoBuilders.geoLocationPoint(
			GetterUtil.getDouble(coordinates.get(0)),
			GetterUtil.getDouble(coordinates.get(1)));
	}

	private void _setDistanceType(
		GeoDistanceAggregation aggregation, JSONObject jsonObject) {

		String distanceType = jsonObject.getString(
			GeoDistanceAggregationBodyConfigurationKeys.DISTANCE_TYPE.
				getJsonKey());

		if (Validator.isBlank(distanceType)) {
			return;
		}

		aggregation.setGeoDistanceType(
			GeoDistanceType.valueOf(
				com.liferay.portal.kernel.util.StringUtil.toUpperCase(
					distanceType)));
	}

	private void _setDistanceUnit(
		GeoDistanceAggregation aggregation, JSONObject jsonObject) {

		String unit = jsonObject.getString(
			GeoDistanceAggregationBodyConfigurationKeys.UNIT.getJsonKey());

		if (Validator.isBlank(unit)) {
			return;
		}

		aggregation.setDistanceUnit(_aggregationHelper.getDistanceUnit(unit));
	}

	@Reference
	private AggregationHelper _aggregationHelper;

	@Reference
	private Aggregations _aggregations;

	@Reference
	private GeoBuilders _geoBuilders;

	@Reference
	private SetterHelper _setterHelper;

}