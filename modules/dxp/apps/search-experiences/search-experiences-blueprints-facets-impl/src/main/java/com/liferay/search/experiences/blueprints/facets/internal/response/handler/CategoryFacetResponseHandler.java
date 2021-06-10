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
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.facets.constants.FacetsJSONResponseKeys;
import com.liferay.search.experiences.blueprints.facets.spi.response.FacetResponseHandler;
import com.liferay.search.experiences.blueprints.message.Messages;

import java.util.Locale;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=category",
	service = FacetResponseHandler.class
)
public class CategoryFacetResponseHandler
	extends BaseTermsFacetResponseHandler implements FacetResponseHandler {

	@Override
	protected JSONObject createBucketJSONObject(
			Bucket bucket, BlueprintsAttributes blueprintsAttributes,
			ResourceBundle resourceBundle, Messages messages)
		throws Exception {

		Locale locale = blueprintsAttributes.getLocale();

		long frequency = bucket.getDocCount();

		long assetCategoryId = GetterUtil.getLong(bucket.getKey());

		AssetCategory assetCategory =
			_assetCategoryLocalService.getAssetCategory(assetCategoryId);

		String name = assetCategory.getTitle(locale, true);

		Group group = _groupLocalService.getGroup(assetCategory.getGroupId());

		return JSONUtil.put(
			FacetsJSONResponseKeys.FREQUENCY, frequency
		).put(
			FacetsJSONResponseKeys.GROUP_NAME, group.getName(locale, true)
		).put(
			FacetsJSONResponseKeys.TERM_NAME, name
		).put(
			FacetsJSONResponseKeys.TEXT, getText(name, frequency, null)
		).put(
			FacetsJSONResponseKeys.VALUE, assetCategoryId
		);
	}

	@Reference
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

}