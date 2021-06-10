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

package com.liferay.search.experiences.blueprints.facets.internal.response.handler;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.facets.constants.FacetsJSONResponseKeys;
import com.liferay.search.experiences.blueprints.facets.internal.util.FacetConfigurationUtil;
import com.liferay.search.experiences.blueprints.facets.spi.response.FacetResponseHandler;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Petteri Karttunen
 */
public abstract class BaseTermsFacetResponseHandler
	implements FacetResponseHandler {

	@Override
	public Optional<JSONObject> getResultOptional(
		AggregationResult aggregationResult, String type,
		BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle, Messages messages,
		JSONObject jsonObject) {

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResult;

		Collection<Bucket> buckets = termsAggregationResult.getBuckets();

		if (buckets.isEmpty()) {
			return Optional.empty();
		}

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		List<String> excludeValues = FacetConfigurationUtil.getExcludeValues(
			jsonObject);
		List<String> includeValues = FacetConfigurationUtil.getIncludeValues(
			jsonObject);

		for (Bucket bucket : buckets) {
			if (!FacetConfigurationUtil.includeValue(
					bucket.getKey(), includeValues, excludeValues)) {

				continue;
			}

			try {
				JSONObject bucketJSONObject = createBucketJSONObject(
					bucket, blueprintsAttributes, resourceBundle, messages);

				if (bucketJSONObject != null) {
					jsonArray.put(bucketJSONObject);
				}
			}
			catch (Exception exception) {
				MessagesUtil.error(
					messages, getClass().getName(), exception, jsonObject, null,
					null, "facets.error.could-not-create-bucket-object");
			}
		}

		if (jsonArray.length() == 0) {
			return Optional.empty();
		}

		return createResultObject(jsonArray, type, jsonObject, resourceBundle);
	}

	protected JSONObject createBucketJSONObject(
			Bucket bucket, BlueprintsAttributes blueprintsAttributes,
			ResourceBundle resourceBundle, Messages messages)
		throws Exception {

		String value = bucket.getKey();

		long frequency = bucket.getDocCount();

		return JSONUtil.put(
			FacetsJSONResponseKeys.FREQUENCY, frequency
		).put(
			FacetsJSONResponseKeys.TERM_NAME, value
		).put(
			FacetsJSONResponseKeys.TEXT,
			getText(value, frequency, resourceBundle)
		).put(
			FacetsJSONResponseKeys.VALUE, value
		);
	}

	protected JSONObject createBucketJSONObject(
		long frequency, String groupName, String termName, String text,
		Object value) {

		JSONObject jsonObject = JSONUtil.put(
			FacetsJSONResponseKeys.FREQUENCY, frequency
		).put(
			FacetsJSONResponseKeys.TERM_NAME, termName
		).put(
			FacetsJSONResponseKeys.TEXT, text
		).put(
			FacetsJSONResponseKeys.VALUE, value
		);

		if (Validator.isBlank(groupName)) {
			jsonObject.put(FacetsJSONResponseKeys.GROUP_NAME, groupName);
		}

		return jsonObject;
	}

	protected Optional<JSONObject> createResultObject(
		JSONArray jsonArray, String type, JSONObject jsonObject,
		ResourceBundle resourceBundle) {

		if (jsonArray.length() == 0) {
			return Optional.empty();
		}

		return Optional.of(
			JSONUtil.put(
				FacetsJSONResponseKeys.LABEL,
				LanguageUtil.get(
					resourceBundle, FacetConfigurationUtil.getLabel(jsonObject))
			).put(
				FacetsJSONResponseKeys.PARAMETER_NAME,
				FacetConfigurationUtil.getParameterName(jsonObject)
			).put(
				FacetsJSONResponseKeys.TYPE, type
			).put(
				FacetsJSONResponseKeys.VALUES, jsonArray
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