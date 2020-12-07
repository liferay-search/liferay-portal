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

package com.liferay.portal.search.tuning.blueprints.engine.internal.sort.translator;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.geolocation.DistanceUnit;
import com.liferay.portal.search.geolocation.GeoBuilders;
import com.liferay.portal.search.geolocation.GeoDistanceType;
import com.liferay.portal.search.sort.GeoDistanceSort;
import com.liferay.portal.search.sort.Sort;
import com.liferay.portal.search.sort.SortMode;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.sort.SortConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.spi.sort.SortTranslator;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "type=geo_distance",
	service = SortTranslator.class
)
public class GeoDistanceSortTranslator implements SortTranslator {

	@Override
	public Optional<Sort> translate(
			JSONObject configurationJsonObject, SortOrder sortOrder, Messages messages) {
		
		String field = configurationJsonObject.getString(
				SortConfigurationKeys.FIELD.getJsonKey());

		if (!configurationJsonObject.has(SortConfigurationKeys.CONFIGURATION.getJsonKey())) {
			return Optional.empty();
		}

		JSONObject sortConfigurationJsonObject = configurationJsonObject.getJSONObject(
				SortConfigurationKeys.CONFIGURATION.getJsonKey());

		if (!sortConfigurationJsonObject.has("locations")) {
			return Optional.empty();
		}

		GeoDistanceSort geoDistanceSort = _sorts.geoDistance(field);
		
		geoDistanceSort.setSortOrder(sortOrder);

		try {
			_setLocations(geoDistanceSort, sortConfigurationJsonObject);

			_setDistanceUnit(geoDistanceSort, sortConfigurationJsonObject);
			
			_setGeoDistanceType(geoDistanceSort, sortConfigurationJsonObject);

			_setSortMode(geoDistanceSort, sortConfigurationJsonObject);
			
			return Optional.of(geoDistanceSort);

		} catch (IllegalArgumentException illegalArgumentException) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.unknown-sort-configuration-error",
					illegalArgumentException.getMessage(), illegalArgumentException,
					configurationJsonObject, null, null));

			_log.error(illegalArgumentException.getMessage(), illegalArgumentException);
		}
		
		return Optional.empty();
	}

	private void _setGeoDistanceType(GeoDistanceSort geoDistanceSort, JSONObject configurationJsonObject) {

		String geoDistanceTypeString = configurationJsonObject.getString(
				"distance_type", GeoDistanceType.ARC.name());
	
		if (!Validator.isBlank(geoDistanceTypeString)) {
	
			geoDistanceSort.setGeoDistanceType(
					GeoDistanceType.valueOf(StringUtil.toUpperCase(geoDistanceTypeString)));
		}
	}
	
	private void _setDistanceUnit(GeoDistanceSort geoDistanceSort, JSONObject configurationJsonObject) {

		String geoDistanceUnitString = configurationJsonObject.getString(
				"unit");

		if (!Validator.isBlank(geoDistanceUnitString)) {

			geoDistanceUnitString = StringUtil.toLowerCase(geoDistanceUnitString);
			
			for (DistanceUnit distanceUnit : DistanceUnit.values()) {
				
				if (distanceUnit.getUnit().equals(geoDistanceUnitString)) {
					geoDistanceSort.setDistanceUnit(distanceUnit);
				}
			}
		}		
	}
	
	private void _setLocations(GeoDistanceSort geoDistanceSort, JSONObject configurationJsonObject) {

		JSONArray locationsJsonArray = configurationJsonObject.getJSONArray(
				"locations");
		
		for (int i = 0; i < locationsJsonArray.length(); i++) {
		
			JSONArray locationJsonArray = locationsJsonArray.getJSONArray(i);
			
			if (locationJsonArray.length() != 2) {
				continue;
			}
			
			Double latitude = locationJsonArray.getDouble(0);
			
			Double longitude = locationJsonArray.getDouble(1);
	
			geoDistanceSort.addGeoLocationPoints(
					_geoBuilders.geoLocationPoint(latitude, longitude));
		}
	}
	
	private void _setSortMode(GeoDistanceSort geoDistanceSort, JSONObject configurationJsonObject) {
		String sortModeString = configurationJsonObject.getString("mode");
		
		if (!Validator.isBlank(sortModeString)) {
	
			geoDistanceSort.setSortMode(
					SortMode.valueOf(StringUtil.toUpperCase(sortModeString)));
		}
	}
	
	private static final Log _log = LogFactoryUtil.getLog(
			GeoDistanceSortTranslator.class);
	
	@Reference
	private GeoBuilders _geoBuilders;

	@Reference
	private Sorts _sorts;

}
