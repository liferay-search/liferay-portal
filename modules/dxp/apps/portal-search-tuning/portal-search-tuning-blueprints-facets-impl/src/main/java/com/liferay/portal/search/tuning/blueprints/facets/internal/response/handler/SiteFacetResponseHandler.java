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

import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetJSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.facets.spi.response.FacetResponseHandler;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=site",
	service = FacetResponseHandler.class
)
public class SiteFacetResponseHandler
	extends BaseFacetResponseHandler implements FacetResponseHandler {

	@Override
	public Optional<JSONObject> getResultOptional(
		AggregationResult aggregationResult,
		BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle, Messages messages,
		JSONObject configurationJsonObject) {

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResult;

		if (termsAggregationResult.getBuckets(
			).size() == 0) {

			return Optional.empty();
		}

		long frequencyThreshold = configurationJsonObject.getLong(
			FacetConfigurationKeys.FREQUENCY_THRESHOLD.getJsonKey(), 1);

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (Bucket bucket : termsAggregationResult.getBuckets()) {
			long frequency = bucket.getDocCount();

			if (frequency < frequencyThreshold) {
				continue;
			}

			try {
				JSONObject jsonObject = _getGroupJsonObject(
					bucket, resourceBundle, blueprintsAttributes.getLocale());

				if (jsonObject != null) {
					jsonArray.put(jsonObject);
				}
			}
			catch (PortalException portalException) {
				messages.addMessage(
					new Message(
						Severity.ERROR, "core", "core.error.group-not-found",
						portalException.getMessage(), portalException,
						configurationJsonObject, null, null));

				if (_log.isWarnEnabled()) {
					_log.warn(portalException.getMessage(), portalException);
				}
			}
		}

		return createResultObject(
			jsonArray, configurationJsonObject, resourceBundle);
	}

	private JSONObject _getGroupJsonObject(
			Bucket bucket, ResourceBundle resourceBundle, Locale locale)
		throws PortalException {

		String value = bucket.getKey();

		long groupId = GetterUtil.getLong(value);

		if (groupId == 0) {
			return null;
		}

		long frequency = bucket.getDocCount();

		JSONObject jsonObject = JSONUtil.put(
			FacetJSONResponseKeys.FREQUENCY, frequency);

		Group group = _groupLocalService.getGroup(groupId);

		String name = group.getName(locale, true);

		jsonObject.put(
			FacetJSONResponseKeys.NAME, name
		).put(
			FacetJSONResponseKeys.TEXT, getText(name, frequency, null)
		).put(
			FacetJSONResponseKeys.VALUE, groupId
		);

		return jsonObject;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SiteFacetResponseHandler.class);

	@Reference
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Reference
	private AssetVocabularyLocalService _assetVocabularyLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

}