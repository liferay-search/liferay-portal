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

package com.liferay.portal.search.tuning.blueprints.facets.internal.response;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetJSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetsBlueprintContributorKeys;
import com.liferay.portal.search.tuning.blueprints.facets.internal.response.handler.FacetResponseHandlerFactory;
import com.liferay.portal.search.tuning.blueprints.facets.spi.response.FacetResponseHandler;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.response.spi.contributor.ResponseContributor;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, property = "name=facets", service = ResponseContributor.class)
public class FacetsResponseContributor implements ResponseContributor {

	@Override
	public void contribute(
		JSONObject responseJsonObject, SearchResponse searchResponse,
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		Messages messages) {

		responseJsonObject.put(
			FacetJSONResponseKeys.FACETS,
			_getFacetsJSONArray(
				searchResponse, blueprint, blueprintsAttributes, messages));
	}

	private JSONArray _getFacetsJSONArray(
		SearchResponse searchResponse, Blueprint blueprint,
		BlueprintsAttributes blueprintsAttributes, Messages messages) {

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		Optional<JSONArray> configurationJsonArrayOptional =
				_blueprintHelper.getJSONArrayConfigurationOptional(
					blueprint,
					"JSONArray/" +
						FacetsBlueprintContributorKeys.CONFIGURATION_SECTION);

		Map<String, AggregationResult> aggregationResultsMap =
			searchResponse.getAggregationResultsMap();

		if (aggregationResultsMap.isEmpty() ||
			!configurationJsonArrayOptional.isPresent()) {

			return jsonArray;
		}

		_processFacets(
			jsonArray, aggregationResultsMap, blueprintsAttributes, messages,
			configurationJsonArrayOptional.get());

		return jsonArray;
	}

	private void _processFacets(
		JSONArray jsonArray,
		Map<String, AggregationResult> aggregationResultsMap,
		BlueprintsAttributes blueprintsAttributes, Messages messages,
		JSONArray configurationJsonArray) {

		for (int i = 0; i < configurationJsonArray.length(); i++) {
			JSONObject configurationJsonObject =
				configurationJsonArray.getJSONObject(i);

			String responseHandlerName = configurationJsonObject.getString(
				FacetConfigurationKeys.HANDLER.getJsonKey(), "default");

			String aggregationName = configurationJsonObject.getString(
				FacetConfigurationKeys.FIELD.getJsonKey());

			for (Map.Entry<String, AggregationResult> entry :
					aggregationResultsMap.entrySet()) {

				String aggregationResultName = entry.getKey();

				if (!StringUtil.equalsIgnoreCase(
						aggregationResultName, aggregationName)) {

					continue;
				}

				FacetResponseHandler facetResponseHandler =
					_facetResponseHandlerFactory.getHandler(
						responseHandlerName);

				Optional<JSONObject> resultOptional =
					facetResponseHandler.getResultOptional(
						entry.getValue(), blueprintsAttributes, messages,
						configurationJsonObject);

				if (resultOptional.isPresent()) {
					jsonArray.put(resultOptional.get());
				}
			}
		}
	}

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private FacetResponseHandlerFactory _facetResponseHandlerFactory;

}