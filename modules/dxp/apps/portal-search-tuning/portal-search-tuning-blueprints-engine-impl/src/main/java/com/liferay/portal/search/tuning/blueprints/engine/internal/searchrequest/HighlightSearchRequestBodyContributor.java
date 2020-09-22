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
import com.liferay.portal.search.highlight.FieldConfigBuilder;
import com.liferay.portal.search.highlight.HighlightBuilder;
import com.liferay.portal.search.highlight.Highlights;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.HighlightingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=highlight",
	service = SearchRequestBodyContributor.class
)
public class HighlightSearchRequestBodyContributor
	implements SearchRequestBodyContributor {

	@Override
	public void contribute(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, Messages messages) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_blueprintHelper.getHighlightConfigurationOptional(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return;
		}

		JSONObject configurationJsonObject =
			configurationJsonObjectOptional.get();

		boolean enabled = configurationJsonObject.getBoolean(
			HighlightingConfigurationKeys.ENABLED.getJsonKey());

		if (!enabled) {
			searchRequestBuilder.highlightEnabled(false);

			return;
		}

		HighlightBuilder highlightBuilder = _highlights.builder();

		if (configurationJsonObject.has(
				HighlightingConfigurationKeys.FRAGMENT_SIZE.getJsonKey())) {

			highlightBuilder.fragmentSize(
				configurationJsonObject.getInt(
					HighlightingConfigurationKeys.FRAGMENT_SIZE.getJsonKey()));
		}

		if (configurationJsonObject.has(
				HighlightingConfigurationKeys.SNIPPET_SIZE.getJsonKey())) {

			highlightBuilder.numOfFragments(
				configurationJsonObject.getInt(
					HighlightingConfigurationKeys.SNIPPET_SIZE.getJsonKey()));
		}

		if (configurationJsonObject.has(
				HighlightingConfigurationKeys.REQUIRE_FIELD_MATCH.
					getJsonKey())) {

			highlightBuilder.requireFieldMatch(
				configurationJsonObject.getBoolean(
					HighlightingConfigurationKeys.REQUIRE_FIELD_MATCH.
						getJsonKey()));
		}

		if (configurationJsonObject.has(
				HighlightingConfigurationKeys.FIELDS.getJsonKey())) {

			JSONArray fieldsJsonArray = configurationJsonObject.getJSONArray(
				HighlightingConfigurationKeys.FIELDS.getJsonKey());

			_addFieldConfigs(highlightBuilder, fieldsJsonArray);
		}

		// TODO: waiting for support in SearchRequestBuilder

		// searchRequestBuilder.setHighlight(highlightBuilder.build());

	}

	private void _addFieldConfigs(
		HighlightBuilder highlightBuilder, JSONArray fieldsJsonArray) {

		for (int i = 0; i < fieldsJsonArray.length(); i++) {
			FieldConfigBuilder fieldConfigBuilder =
				_highlights.fieldConfigBuilder();

			fieldConfigBuilder.field(fieldsJsonArray.getString(i));

			highlightBuilder.addFieldConfig(fieldConfigBuilder.build());
		}
	}

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private Highlights _highlights;

}