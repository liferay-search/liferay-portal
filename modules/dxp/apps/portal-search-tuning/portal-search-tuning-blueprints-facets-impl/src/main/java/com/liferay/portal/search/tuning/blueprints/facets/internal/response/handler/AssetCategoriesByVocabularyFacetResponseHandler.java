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

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
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

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=asset_categories_by_vocabulary",
	service = FacetResponseHandler.class
)
public class AssetCategoriesByVocabularyFacetResponseHandler
	extends BaseFacetResponseHandler implements FacetResponseHandler {

	@Override
	public Optional<JSONObject> getResultOptional(
		AggregationResult aggregationResult,
		BlueprintsAttributes blueprintsAttributes, Messages messages,
		JSONObject configurationJsonObject) {

		JSONObject handlerParametersJsonObject =
			configurationJsonObject.getJSONObject(
				FacetConfigurationKeys.HANDLER_PARAMETERS.getJsonKey());

		if (handlerParametersJsonObject == null) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.facet-handler-parameters-missing",
					"Facet handler parameters are missing", new Throwable(),
					handlerParametersJsonObject,
					FacetConfigurationKeys.HANDLER_PARAMETERS.getJsonKey(),
					null));

			return Optional.empty();
		}

		Long vocabularyId = handlerParametersJsonObject.getLong(
			"vocabulary_id", -1);

		if (vocabularyId < 0) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.facet-asset-vocabulary-id-parameter-missing",
					"Facet asset vocabulary id is not defined", new Throwable(),
					handlerParametersJsonObject, "vocabulary_id", null));

			return Optional.empty();
		}

		JSONArray jsonArray = null;

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResult;

		try {
			jsonArray = _getCategoriesJSONArray(
				termsAggregationResult, vocabularyId,
				blueprintsAttributes.getLocale());
		}
		catch (PortalException portalException) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.asset-vocabulary-not-found",
					portalException.getMessage(), portalException,
					handlerParametersJsonObject, "vocabulary_id",
					String.valueOf(vocabularyId)));

			if (_log.isWarnEnabled()) {
				_log.warn("Asset vocabulary " + vocabularyId + " not found.");
			}

			return Optional.empty();
		}

		return createResultObject(jsonArray, configurationJsonObject);
	}

	private JSONArray _getCategoriesJSONArray(
			TermsAggregationResult termsAggregationResult, long vocabularyId,
			Locale locale)
		throws PortalException {

		AssetVocabulary assetVocabulary =
			_assetVocabularyLocalService.getAssetVocabulary(vocabularyId);

		List<AssetCategory> assetCategories = assetVocabulary.getCategories();

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (AssetCategory assetCategory : assetCategories) {
			for (Bucket bucket : termsAggregationResult.getBuckets()) {
				if (Long.valueOf(bucket.getKey()) ==
						assetCategory.getCategoryId()) {

					jsonArray.put(
						JSONUtil.put(
							FacetJSONResponseKeys.FREQUENCY,
							bucket.getDocCount()
						).put(
							FacetJSONResponseKeys.NAME,
							assetCategory.getTitle(locale)
						).put(
							FacetJSONResponseKeys.VALUE, bucket.getKey()
						));
				}
			}
		}

		return jsonArray;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AssetCategoriesByVocabularyFacetResponseHandler.class);

	@Reference
	private AssetVocabularyLocalService _assetVocabularyLocalService;

}