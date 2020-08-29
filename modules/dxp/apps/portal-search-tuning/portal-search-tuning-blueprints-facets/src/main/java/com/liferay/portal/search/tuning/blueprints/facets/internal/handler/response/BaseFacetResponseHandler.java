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

package com.liferay.portal.search.tuning.blueprints.facets.internal.handler.response;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.spi.facet.FacetResponseHandler;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetJSONResponseKeys;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Petteri Karttunen
 */
public abstract class BaseFacetResponseHandler implements FacetResponseHandler {

	@Override
	public Optional<JSONObject> getResultObject(
		SearchRequestContext searchRequestContext,
		AggregationResult aggregationResult,
		JSONObject configurationJsonObject) {

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResult;

		JSONArray termsArray = JSONFactoryUtil.createJSONArray();

		for (Bucket bucket : termsAggregationResult.getBuckets()) {
			JSONObject item = JSONFactoryUtil.createJSONObject();

			item.put(FacetJSONResponseKeys.FREQUENCY, bucket.getDocCount());
			item.put(FacetJSONResponseKeys.VALUE, bucket.getKey());

			termsArray.put(item);
		}

		return createResultObject(termsArray, configurationJsonObject);
	}

	protected Optional<JSONObject> createResultObject(
		JSONArray termsArray, JSONObject configurationJsonObject) {

		if ((termsArray == null) || (termsArray.length() == 0)) {
			return Optional.empty();
		}

		JSONObject resultObject = JSONFactoryUtil.createJSONObject();

		resultObject.put(
			FacetJSONResponseKeys.PARAMETER_NAME,
			configurationJsonObject.getString(
				FacetConfigurationKeys.PARAMETER_NAME.getJsonKey()));

		resultObject.put(FacetJSONResponseKeys.VALUES, termsArray);

		return Optional.of(resultObject);
	}

	protected FacetCollector getFacetCollector(
		Collection<Facet> facets, String fieldName) {

		for (Facet facet : facets) {
			if (facet.isStatic()) {
				continue;
			}

			if (facet.getFieldName(
				).equals(
					fieldName
				)) {

				return facet.getFacetCollector();
			}
		}

		return null;
	}

}