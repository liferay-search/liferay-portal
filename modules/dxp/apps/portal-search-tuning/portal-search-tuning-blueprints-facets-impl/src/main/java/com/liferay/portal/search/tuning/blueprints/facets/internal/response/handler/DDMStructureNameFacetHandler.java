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

import com.liferay.dynamic.data.mapping.kernel.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
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
	immediate = true, property = "name=ddm_structure_name",
	service = FacetResponseHandler.class
)
public class DDMStructureNameFacetHandler
	extends BaseFacetResponseHandler implements FacetResponseHandler {

	@Override
	public Optional<JSONObject> getResultOptional(
		AggregationResult aggregationResult,
		BlueprintsAttributes blueprintsAttributes, Messages messages,
		JSONObject configurationJsonObject) {

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResult;

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (Bucket bucket : termsAggregationResult.getBuckets()) {
			try {
				JSONObject jsonObject = _getDDMStructureJSONObject(
					bucket, blueprintsAttributes.getLocale());

				jsonArray.put(jsonObject);
			}
			catch (PortalException portalException) {
				messages.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.ddm-structure-not-found",
						portalException.getMessage(), portalException,
						configurationJsonObject, null, null));

				if (_log.isWarnEnabled()) {
					_log.warn(portalException.getMessage(), portalException);
				}
			}
		}

		return createResultObject(jsonArray, configurationJsonObject);
	}

	private DDMStructure _getDDMStructure(String ddmStructureKey)
		throws PortalException {

		DynamicQuery dynamicQuery = _ddmStructureLocalService.dynamicQuery();

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq("structureKey", ddmStructureKey));

		List<DDMStructure> structures = _ddmStructureLocalService.dynamicQuery(
			dynamicQuery);

		return structures.get(0);
	}

	private JSONObject _getDDMStructureJSONObject(Bucket bucket, Locale locale)
		throws PortalException {

		DDMStructure ddmStructure = _getDDMStructure(bucket.getKey());

		Group group = _groupLocalService.getGroup(ddmStructure.getGroupId());

		return JSONUtil.put(
			FacetJSONResponseKeys.FREQUENCY, bucket.getDocCount()
		).put(
			FacetJSONResponseKeys.GROUP_NAME, group.getName(locale, true)
		).put(
			FacetJSONResponseKeys.NAME, ddmStructure.getName(locale, true)
		).put(
			FacetJSONResponseKeys.VALUE, bucket.getKey()
		);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DDMStructureNameFacetHandler.class);

	@Reference
	private DDMStructureLocalService _ddmStructureLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

}