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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.response.contributor;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.AggregationConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.aggregations.facet.FacetHandlerFactory;
import com.liferay.portal.search.tuning.blueprints.engine.spi.aggregation.facet.FacetHandler;
import com.liferay.portal.search.tuning.blueprints.engine.spi.response.ResponseContributor;

import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ResponseContributor.class)
public class FacetsResponseContributor implements ResponseContributor {

	@Override
	public void contribute(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse,
		Map<String, Object> responseAttributes, JSONObject responseJsonObject) {

		responseJsonObject.put(
			JSONResponseKeys.FACETS,
			_getFacets(searchRequestContext, searchResponse));
	}

	private JSONArray _getFacets(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse) {

		JSONArray facetsJsonArray = JSONFactoryUtil.createJSONArray();

		Optional<JSONArray> aggregationsConfigurationJsonArrayOptional =
			searchRequestContext.getAggregationConfiguration();

		Map<String, AggregationResult> aggregations =
			searchResponse.getAggregationResultsMap();

		if ((aggregations == null) ||
			!aggregationsConfigurationJsonArrayOptional.isPresent()) {

			return facetsJsonArray;
		}

		JSONArray configuration =
			aggregationsConfigurationJsonArrayOptional.get();

		for (int i = 0; i < configuration.length(); i++) {
			JSONObject aggregationJsonObject = configuration.getJSONObject(i);

			JSONObject facetConfigurationJsonObject =
				aggregationJsonObject.getJSONObject(
					AggregationConfigurationKeys.FACET.getJsonKey());

			if (facetConfigurationJsonObject == null) {
				continue;
			}

			String aggregationName = aggregationJsonObject.getString(
				AggregationConfigurationKeys.NAME.getJsonKey());

			String facetHandlerName = facetConfigurationJsonObject.getString(
				FacetConfigurationKeys.HANDLER.getJsonKey());

			for (Map.Entry<String, AggregationResult> entry :
					aggregations.entrySet()) {

				String aggregationResultName = entry.getKey();

				if (!aggregationResultName.equalsIgnoreCase(aggregationName)) {
					continue;
				}

				FacetHandler facetHandler = _facetHandlerFactory.getHandler(
					facetHandlerName);

				Optional<JSONObject> resultObject =
					facetHandler.getResultsObject(
						searchRequestContext, entry.getValue(),
						facetConfigurationJsonObject);

				if (resultObject.isPresent()) {
					facetsJsonArray.put(resultObject);
				}
			}
		}

		return facetsJsonArray;
	}

	@Reference
	private FacetHandlerFactory _facetHandlerFactory;

}