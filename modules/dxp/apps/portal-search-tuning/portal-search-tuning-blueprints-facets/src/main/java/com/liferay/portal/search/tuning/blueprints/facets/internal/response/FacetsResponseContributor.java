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
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.spi.facet.FacetResponseHandler;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetJSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.facets.internal.handler.response.FacetResponseHandlerFactory;
import com.liferay.portal.search.tuning.blueprints.poc.util.POCMockUtil;
import com.liferay.portal.search.tuning.blueprints.response.spi.contributor.ResponseContributor;

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
			FacetJSONResponseKeys.FACETS,
			_getFacets(searchRequestContext, searchResponse));
	}

	private JSONArray _getFacets(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse) {

		JSONArray facetsJsonArray = JSONFactoryUtil.createJSONArray();

		// TODO

		JSONArray facetsConfigurationJsonArray =
			POCMockUtil.getFacetsParameterConfigurationMock();

		Map<String, AggregationResult> aggregations =
			searchResponse.getAggregationResultsMap();

		if ((aggregations == null) || (facetsConfigurationJsonArray == null)) {
			return facetsJsonArray;
		}

		for (int i = 0; i < facetsConfigurationJsonArray.length(); i++) {
			JSONObject facetConfigurationJsonObject =
				facetsConfigurationJsonArray.getJSONObject(i);

			if (facetConfigurationJsonObject == null) {
				continue;
			}

			String facetResponseHandlerName =
				facetConfigurationJsonObject.getString(
					FacetConfigurationKeys.HANDLER.getJsonKey(), "default");

			String aggregationName = facetConfigurationJsonObject.getString(
				FacetConfigurationKeys.AGGREGATION_NAME.getJsonKey());

			for (Map.Entry<String, AggregationResult> entry :
					aggregations.entrySet()) {

				String aggregationResultName = entry.getKey();

				if (!aggregationResultName.equalsIgnoreCase(aggregationName)) {
					continue;
				}

				FacetResponseHandler facetResponseHandler =
					_facetResponseHandlerFactory.getHandler(
						facetResponseHandlerName);

				Optional<JSONObject> resultObject =
					facetResponseHandler.getResultObject(
						searchRequestContext, entry.getValue(),
						facetConfigurationJsonObject);

				if (resultObject.isPresent()) {
					facetsJsonArray.put(resultObject.get());
				}
			}
		}

		return facetsJsonArray;
	}

	@Reference
	private FacetResponseHandlerFactory _facetResponseHandlerFactory;

}