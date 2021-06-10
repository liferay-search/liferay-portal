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

import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.service.DLFileEntryTypeService;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
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
	immediate = true, property = "name=document_type",
	service = FacetResponseHandler.class
)
public class DocumentTypeFacetResponseHandler
	extends BaseTermsFacetResponseHandler implements FacetResponseHandler {

	@Override
	protected JSONObject createBucketJSONObject(
			Bucket bucket, BlueprintsAttributes blueprintsAttributes,
			ResourceBundle resourceBundle, Messages messages)
		throws Exception {

		long frequency = bucket.getDocCount();

		long fileEntryTypeId = GetterUtil.getLong(bucket.getKey());

		// Fetching the basic document type causes a NoSuchGroupException when
		// checking permission.

		if (fileEntryTypeId == 0) {
			return createBucketJSONObject(
				bucket.getDocCount(), null, "basic-document",
				getText("basic-document", frequency, null), fileEntryTypeId);
		}

		DLFileEntryType dlFileEntryType =
			_dLFileEntryTypeService.getFileEntryType(fileEntryTypeId);

		Locale locale = blueprintsAttributes.getLocale();

		String name = dlFileEntryType.getName(locale, true);

		Group group = _groupLocalService.getGroup(dlFileEntryType.getGroupId());

		String groupName = group.getName(locale, true);

		return createBucketJSONObject(
			bucket.getDocCount(), groupName, "basic-document",
			getText(name, frequency, null), fileEntryTypeId);
	}

	@Reference
	private DLFileEntryTypeService _dLFileEntryTypeService;

	@Reference
	private GroupLocalService _groupLocalService;

}