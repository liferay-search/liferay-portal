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
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
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
import java.util.ResourceBundle;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=category_tree",
	service = FacetResponseHandler.class
)
public class CategoryTreeFacetResponseHandler
	extends BaseFacetResponseHandler implements FacetResponseHandler {

	@Override
	public Optional<JSONObject> getResultOptional(
		AggregationResult aggregationResult,
		BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle, Messages messages,
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
			"root_vocabulary_id", -1);

		if (vocabularyId < 0) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.facet-asset-vocabulary-id-parameter-missing",
					"Root vocabulary id is not defined", new Throwable(),
					handlerParametersJsonObject, "vocabulary_id", null));

			return Optional.empty();
		}

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResult;

		if (termsAggregationResult.getBuckets(
			).size() == 0) {

			return Optional.empty();
		}

		long frequencyThreshold = configurationJsonObject.getLong(
			FacetConfigurationKeys.FREQUENCY_THRESHOLD.getJsonKey(), 1);

		JSONArray jsonArray = null;

		try {
			jsonArray = _getCategoriesJSONArray(
				termsAggregationResult, vocabularyId, frequencyThreshold,
				blueprintsAttributes.getLocale(), messages);
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

		return createResultObject(
			jsonArray, configurationJsonObject, resourceBundle);
	}

	private void _addChildNode(
		JSONArray jsonArray, List<AssetCategory> assetCategories,
		AssetCategory assetCategory, long frequency, Locale locale,
		Messages messages) {

		long categoryId = assetCategory.getCategoryId();

		JSONObject parentJsonObject = _getParentNodeJsonObject(
			jsonArray, assetCategories, assetCategory, frequency, locale);

		if (parentJsonObject == null) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "facets",
					"facets.error.asset-category-not-found",
					"Asset category not found", null, null, null,
					String.valueOf(categoryId)));

			if (_log.isWarnEnabled()) {
				_log.warn("Asset category " + categoryId + " not found");
			}

			return;
		}

		JSONObject childJsonObject = _createNode(
			categoryId, frequency, assetCategory.getTitle(locale));

		_addToChildren(parentJsonObject, childJsonObject);
	}

	private JSONObject _addRootNodeJsonObject(
		JSONArray jsonArray, AssetCategory assetCategory, long frequency,
		Locale locale) {

		long categoryId = assetCategory.getCategoryId();

		JSONObject rootNodeJsonObject = _findCategoryJsonObject(
			jsonArray, categoryId, true);

		if (rootNodeJsonObject != null) {
			_updateFrequency(rootNodeJsonObject, frequency);

			return rootNodeJsonObject;
		}

		rootNodeJsonObject = _createNode(
			categoryId, frequency, assetCategory.getTitle(locale));
		jsonArray.put(rootNodeJsonObject);

		return rootNodeJsonObject;
	}

	private void _addToChildren(
		JSONObject parentJsonObject, JSONObject childJsonObject) {

		JSONArray jsonArray = null;

		if (parentJsonObject.has("children")) {
			jsonArray = parentJsonObject.getJSONArray("children");
		}
		else {
			jsonArray = JSONFactoryUtil.createJSONArray();
			parentJsonObject.put("children", jsonArray);
		}

		jsonArray.put(childJsonObject);
	}

	private JSONObject _createNode(long value, long frequency, String name) {
		return JSONUtil.put(
			FacetJSONResponseKeys.FREQUENCY, frequency
		).put(
			FacetJSONResponseKeys.NAME, name
		).put(
			FacetJSONResponseKeys.TEXT, getText(name, frequency, null)
		).put(
			FacetJSONResponseKeys.VALUE, value
		);
	}

	private void _createTree(
		JSONArray jsonArray, List<AssetCategory> assetCategories,
		long categoryId, long frequency, Locale locale, Messages messages) {

		Stream<AssetCategory> stream = assetCategories.stream();

		Optional<AssetCategory> optional = stream.filter(
			a -> a.getCategoryId() == categoryId
		).findAny();

		if (!optional.isPresent()) {
			return;
		}

		AssetCategory assetCategory = optional.get();

		if (assetCategory.getParentCategoryId() == 0) {
			_addRootNodeJsonObject(jsonArray, assetCategory, frequency, locale);

			return;
		}

		_addChildNode(
			jsonArray, assetCategories, assetCategory, frequency, locale,
			messages);
	}

	private JSONObject _findCategoryJsonObject(
		JSONArray jsonArray, long categoryId, boolean root) {

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject1 = jsonArray.getJSONObject(i);

			if (jsonObject1.getLong(FacetJSONResponseKeys.VALUE) ==
					categoryId) {

				return jsonObject1;
			}

			if (!root && jsonObject1.has("children")) {
				JSONObject jsonObject2 = _findCategoryJsonObject(
					jsonObject1.getJSONArray("children"), categoryId, false);

				if (jsonObject2 != null) {
					return jsonObject2;
				}
			}
		}

		return null;
	}

	private JSONArray _getCategoriesJSONArray(
			TermsAggregationResult termsAggregationResult, long vocabularyId,
			long frequencyThreshold, Locale locale, Messages messages)
		throws PortalException {

		AssetVocabulary assetVocabulary =
			_assetVocabularyLocalService.getVocabulary(vocabularyId);

		List<AssetCategory> assetCategories = assetVocabulary.getCategories();

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (Bucket bucket : termsAggregationResult.getBuckets()) {
			long frequency = bucket.getDocCount();

			if (frequency < frequencyThreshold) {
				continue;
			}

			long categoryId = Long.valueOf(bucket.getKey());

			_createTree(
				jsonArray, assetCategories, categoryId, frequency, locale,
				messages);
		}

		return jsonArray;
	}

	private JSONObject _getParentNodeJsonObject(
		JSONArray jsonArray, List<AssetCategory> assetCategories,
		AssetCategory assetCategory, long frequency, Locale locale) {

		String treePath = assetCategory.getTreePath();

		String[] ids = StringUtil.split(treePath, "/");

		JSONObject parentJsonObject = null;

		for (int i = 1; i < (ids.length - 1); i++) {
			Stream<AssetCategory> stream = assetCategories.stream();

			long categoryId = Long.valueOf(ids[i]);

			Optional<AssetCategory> optional = stream.filter(
				a -> a.getCategoryId() == categoryId
			).findAny();

			if (!optional.isPresent()) {
				continue;
			}

			parentJsonObject = _getTreeNodeJsonObject(
				jsonArray, optional.get(), frequency, locale);
		}

		return parentJsonObject;
	}

	private JSONObject _getTreeNodeJsonObject(
		JSONArray jsonArray, AssetCategory assetCategory, long frequency,
		Locale locale) {

		long categoryId = assetCategory.getCategoryId();

		long parentCategoryId = assetCategory.getParentCategoryId();

		String title = assetCategory.getTitle(locale);

		if (parentCategoryId == 0) {
			return _addRootNodeJsonObject(
				jsonArray, assetCategory, frequency, locale);
		}

		JSONObject childNodeJsonObject = _findCategoryJsonObject(
			jsonArray, categoryId, false);

		if (childNodeJsonObject != null) {
			_updateFrequency(childNodeJsonObject, frequency);

			return childNodeJsonObject;
		}

		JSONObject parentJsonObject = _findCategoryJsonObject(
			jsonArray, parentCategoryId, false);

		childNodeJsonObject = _createNode(categoryId, frequency, title);

		_addToChildren(parentJsonObject, childNodeJsonObject);

		return childNodeJsonObject;
	}

	private void _updateFrequency(JSONObject jsonObject, long count) {
		long frequency = jsonObject.getLong(FacetJSONResponseKeys.FREQUENCY);

		frequency += count;

		jsonObject.put(FacetJSONResponseKeys.FREQUENCY, frequency);

		String name = jsonObject.getString(FacetJSONResponseKeys.NAME);

		jsonObject.put(
			FacetJSONResponseKeys.TEXT, getText(name, frequency, null));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CategoryTreeFacetResponseHandler.class);

	@Reference
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Reference
	private AssetVocabularyLocalService _assetVocabularyLocalService;

}