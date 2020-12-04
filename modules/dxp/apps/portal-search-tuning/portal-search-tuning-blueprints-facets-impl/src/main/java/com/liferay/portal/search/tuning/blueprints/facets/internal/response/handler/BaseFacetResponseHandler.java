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

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
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
import java.util.ResourceBundle;

/**
 * @author Petteri Karttunen
 */
public abstract class BaseFacetResponseHandler implements FacetResponseHandler {

	@Override
	public Optional<JSONObject> getResultOptional(
		AggregationResult aggregationResult,
		BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle, Messages messages,
		JSONObject configurationJsonObject) {

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResult;

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		long frequencyThreshold = configurationJsonObject.getLong(
			FacetConfigurationKeys.FREQUENCY_THRESHOLD.getJsonKey(), 1);

		for (Bucket bucket : termsAggregationResult.getBuckets()) {
			long frequency = bucket.getDocCount();

			if (frequency < frequencyThreshold) {
				continue;
			}

			JSONObject jsonObject = JSONUtil.put(
				FacetJSONResponseKeys.FREQUENCY, frequency);

			String value = bucket.getKey();

			jsonObject.put(FacetJSONResponseKeys.VALUE, value);

			jsonObject.put(
				FacetJSONResponseKeys.TEXT,
				getText(value, frequency, resourceBundle));

			jsonArray.put(jsonObject);
		}

		if (jsonArray.length() == 0) {
			return Optional.empty();
		}

		return createResultObject(
			jsonArray, configurationJsonObject, resourceBundle);
	}

	protected Optional<JSONObject> createResultObject(
		JSONArray jsonArray, JSONObject configurationJsonObject,
		ResourceBundle resourceBundle) {

		if (jsonArray.length() == 0) {
			return Optional.empty();
		}

		String handlerName = configurationJsonObject.getString(
			FacetConfigurationKeys.HANDLER.getJsonKey(), "default");

		String parameterName = configurationJsonObject.getString(
			FacetConfigurationKeys.PARAMETER_NAME.getJsonKey());

		String label = configurationJsonObject.getString(
			FacetConfigurationKeys.LABEL.getJsonKey(), parameterName);

		return Optional.of(
			JSONUtil.put(
				FacetJSONResponseKeys.HANDER_NAME, handlerName
			).put(
				FacetJSONResponseKeys.LABEL,
				LanguageUtil.get(resourceBundle, label)
			).put(
				FacetJSONResponseKeys.PARAMETER_NAME, parameterName
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

	protected String getText(
		String value, long frequency, ResourceBundle resourceBundle) {

		value = StringUtil.toLowerCase(value);

		StringBundler sb = new StringBundler(4);

		if (resourceBundle == null) {
			sb.append(value);
		}
		else {
			sb.append(LanguageUtil.get(resourceBundle, value));
		}

		sb.append(" (");
		sb.append(String.valueOf(frequency));
		sb.append(")");

		return sb.toString();
	}

}