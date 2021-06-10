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

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.facets.constants.FacetConfigurationKeys;
import com.liferay.search.experiences.blueprints.facets.constants.FacetsJSONResponseKeys;
import com.liferay.search.experiences.blueprints.facets.internal.util.FacetConfigurationUtil;
import com.liferay.search.experiences.blueprints.facets.spi.response.FacetResponseHandler;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;

import java.util.Collection;
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
	extends BaseTermsFacetResponseHandler implements FacetResponseHandler {

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

		JSONObject parametersJSONObject = jsonObject.getJSONObject(
			FacetConfigurationKeys.PARAMETERS.getJsonKey());

		long frequencyThreshold = jsonObject.getLong(
			FacetConfigurationKeys.MIN_DOC_COUNT.getJsonKey(), 1);

		JSONArray jsonArray = null;

		try {
			jsonArray = _getCategoriesJSONArray(
				buckets, parametersJSONObject.getLong("root_vocabulary_id"),
				frequencyThreshold, blueprintsAttributes.getLocale(), messages,
				jsonObject);
		}
		catch (PortalException portalException) {
			MessagesUtil.error(
				messages, getClass().getName(), portalException, jsonObject,
				null, null, "facets.error.unknown-error");

			return Optional.empty();
		}

		return createResultObject(jsonArray, type, jsonObject, resourceBundle);
	}

	private void _addChildNode(
		JSONArray jsonArray, List<AssetCategory> assetCategories,
		AssetCategory assetCategory, long frequency, Locale locale,
		Messages messages) {

		long categoryId = assetCategory.getCategoryId();

		JSONObject parentJSONObject = _getParentNodeJSONObject(
			jsonArray, assetCategories, assetCategory, frequency, locale);

		if (parentJSONObject == null) {
			MessagesUtil.error(
				messages, getClass().getName(),
				new Throwable("Asset category not found"), null, null,
				String.valueOf(categoryId),
				"facets.error.asset-category-not-found");

			return;
		}

		JSONObject childJSONObject = _createNode(
			categoryId, frequency, assetCategory.getTitle(locale));

		_addToChildren(parentJSONObject, childJSONObject);
	}

	private JSONObject _addRootNodeJSONObject(
		JSONArray jsonArray, AssetCategory assetCategory, long frequency,
		Locale locale) {

		long categoryId = assetCategory.getCategoryId();

		JSONObject rootNodeJSONObject = _findCategoryJSONObject(
			jsonArray, categoryId, true);

		if (rootNodeJSONObject != null) {
			_updateFrequency(rootNodeJSONObject, frequency);

			return rootNodeJSONObject;
		}

		rootNodeJSONObject = _createNode(
			categoryId, frequency, assetCategory.getTitle(locale));

		jsonArray.put(rootNodeJSONObject);

		return rootNodeJSONObject;
	}

	private void _addToChildren(
		JSONObject parentJSONObject, JSONObject childJSONObject) {

		JSONArray jsonArray = null;

		if (parentJSONObject.has("children")) {
			jsonArray = parentJSONObject.getJSONArray("children");
		}
		else {
			jsonArray = JSONFactoryUtil.createJSONArray();

			parentJSONObject.put("children", jsonArray);
		}

		jsonArray.put(childJSONObject);
	}

	private JSONObject _createNode(long value, long frequency, String name) {
		return createBucketJSONObject(
			frequency, null, name, getText(name, frequency, null), value);
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
			_addRootNodeJSONObject(jsonArray, assetCategory, frequency, locale);

			return;
		}

		_addChildNode(
			jsonArray, assetCategories, assetCategory, frequency, locale,
			messages);
	}

	private JSONObject _findCategoryJSONObject(
		JSONArray jsonArray, long categoryId, boolean root) {

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject1 = jsonArray.getJSONObject(i);

			if (jsonObject1.getLong(FacetsJSONResponseKeys.VALUE) ==
					categoryId) {

				return jsonObject1;
			}

			if (!root && jsonObject1.has("children")) {
				JSONObject jsonObject2 = _findCategoryJSONObject(
					jsonObject1.getJSONArray("children"), categoryId, false);

				if (jsonObject2 != null) {
					return jsonObject2;
				}
			}
		}

		return null;
	}

	private JSONArray _getCategoriesJSONArray(
			Collection<Bucket> buckets, long vocabularyId,
			long frequencyThreshold, Locale locale, Messages messages,
			JSONObject jsonObject)
		throws PortalException {

		AssetVocabulary assetVocabulary =
			_assetVocabularyLocalService.getVocabulary(vocabularyId);

		List<AssetCategory> assetCategories = assetVocabulary.getCategories();

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		List<String> excludeValues = FacetConfigurationUtil.getExcludeValues(
			jsonObject);
		List<String> includeValues = FacetConfigurationUtil.getIncludeValues(
			jsonObject);

		for (Bucket bucket : buckets) {
			long frequency = bucket.getDocCount();

			if ((frequency < frequencyThreshold) ||
				!FacetConfigurationUtil.includeValue(
					bucket.getKey(), includeValues, excludeValues)) {

				continue;
			}

			_createTree(
				jsonArray, assetCategories, Long.valueOf(bucket.getKey()),
				frequency, locale, messages);
		}

		return jsonArray;
	}

	private JSONObject _getParentNodeJSONObject(
		JSONArray jsonArray, List<AssetCategory> assetCategories,
		AssetCategory assetCategory, long frequency, Locale locale) {

		String[] ids = StringUtil.split(assetCategory.getTreePath(), "/");

		JSONObject parentJSONObject = null;

		for (int i = 1; i < (ids.length - 1); i++) {
			Stream<AssetCategory> stream = assetCategories.stream();

			long categoryId = Long.valueOf(ids[i]);

			Optional<AssetCategory> optional = stream.filter(
				a -> a.getCategoryId() == categoryId
			).findAny();

			if (!optional.isPresent()) {
				continue;
			}

			parentJSONObject = _getTreeNodeJSONObject(
				jsonArray, optional.get(), frequency, locale);
		}

		return parentJSONObject;
	}

	private JSONObject _getTreeNodeJSONObject(
		JSONArray jsonArray, AssetCategory assetCategory, long frequency,
		Locale locale) {

		long categoryId = assetCategory.getCategoryId();

		long parentCategoryId = assetCategory.getParentCategoryId();

		String title = assetCategory.getTitle(locale);

		if (parentCategoryId == 0) {
			return _addRootNodeJSONObject(
				jsonArray, assetCategory, frequency, locale);
		}

		JSONObject childNodeJSONObject = _findCategoryJSONObject(
			jsonArray, categoryId, false);

		if (childNodeJSONObject != null) {
			_updateFrequency(childNodeJSONObject, frequency);

			return childNodeJSONObject;
		}

		JSONObject parentJSONObject = _findCategoryJSONObject(
			jsonArray, parentCategoryId, false);

		childNodeJSONObject = _createNode(categoryId, frequency, title);

		_addToChildren(parentJSONObject, childNodeJSONObject);

		return childNodeJSONObject;
	}

	private void _updateFrequency(JSONObject jsonObject, long count) {
		long frequency = jsonObject.getLong(FacetsJSONResponseKeys.FREQUENCY);

		frequency += count;

		jsonObject.put(FacetsJSONResponseKeys.FREQUENCY, frequency);

		String name = jsonObject.getString(FacetsJSONResponseKeys.TERM_NAME);

		jsonObject.put(
			FacetsJSONResponseKeys.TEXT, getText(name, frequency, null));
	}

	@Reference
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Reference
	private AssetVocabularyLocalService _assetVocabularyLocalService;

}