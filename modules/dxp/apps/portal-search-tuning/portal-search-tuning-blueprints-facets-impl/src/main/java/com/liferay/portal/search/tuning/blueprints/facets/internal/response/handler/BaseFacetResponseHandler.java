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

package com.liferay.portal.search.tuning.blueprints.facets.internal.response.handler;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetJSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.facets.spi.response.FacetResponseHandler;
import com.liferay.portal.search.tuning.blueprints.message.Messages;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Petteri Karttunen
 */
public abstract class BaseFacetResponseHandler implements FacetResponseHandler {

	@Override
	public Optional<JSONObject> getResultOptional(
		AggregationResult aggregationResult,
		BlueprintsAttributes blueprintsAttributes, Messages messages,
		JSONObject configurationJsonObject) {

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResult;

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (Bucket bucket : termsAggregationResult.getBuckets()) {
			JSONObject jsonObject = JSONUtil.put(
				FacetJSONResponseKeys.FREQUENCY, bucket.getDocCount());

			jsonObject.put(FacetJSONResponseKeys.VALUE, bucket.getKey());

			jsonArray.put(jsonObject);
		}

		if (jsonArray.length() == 0) {
			return Optional.empty();
		}

		return createResultObject(jsonArray, configurationJsonObject);
	}

	protected Optional<JSONObject> createResultObject(
		JSONArray jsonArray, JSONObject configurationJsonObject) {

		return Optional.of(
			JSONUtil.put(
				FacetJSONResponseKeys.PARAMETER_NAME,
				configurationJsonObject.getString(
					FacetConfigurationKeys.PARAMETER_NAME.getJsonKey())
			).put(
				FacetJSONResponseKeys.VALUES, jsonArray
			));
	}

	protected FacetCollector getFacetCollector(
		Collection<Facet> facets, String fieldName) {

		for (Facet facet : facets) {
			if (facet.isStatic()) {
				continue;
			}

			String facetFieldName = facet.getFieldName();

			if (StringUtil.equals(facetFieldName, fieldName)) {
				return facet.getFacetCollector();
			}
		}

		return null;
	}

}