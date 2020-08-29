
package com.liferay.portal.search.tuning.blueprints.facets.internal.handler.response;

import com.liferay.dynamic.data.mapping.kernel.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.spi.facet.FacetResponseHandler;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetJSONResponseKeys;

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
	public Optional<JSONObject> getResultObject(
		SearchRequestContext searchRequestContext,
		AggregationResult aggregationResult,
		JSONObject configurationJsonObject) {

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResult;

		Locale locale = searchRequestContext.getLocale();

		JSONArray termsArray = JSONFactoryUtil.createJSONArray();

		for (Bucket bucket : termsAggregationResult.getBuckets()) {
			try {
				JSONObject jsonObject = _getDDMStructureObject(bucket, locale);

				termsArray.put(jsonObject);
			}
			catch (PortalException portalException) {
				searchRequestContext.addMessage(
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

		return createResultObject(termsArray, configurationJsonObject);
	}

	private DDMStructure _getDDMStructure(String ddmStructureKey)
		throws PortalException {

		DynamicQuery structureQuery = _ddmStructureLocalService.dynamicQuery();

		structureQuery.add(
			RestrictionsFactoryUtil.eq("structureKey", ddmStructureKey));

		List<DDMStructure> structures = _ddmStructureLocalService.dynamicQuery(
			structureQuery);

		return structures.get(0);
	}

	private JSONObject _getDDMStructureObject(Bucket bucket, Locale locale)
		throws PortalException {

		DDMStructure structure = _getDDMStructure(bucket.getKey());

		JSONObject item = JSONFactoryUtil.createJSONObject();

		item.put(FacetJSONResponseKeys.FREQUENCY, bucket.getDocCount());
		item.put(
			FacetJSONResponseKeys.GROUP_NAME,
			_groupLocalService.getGroup(
				structure.getGroupId()
			).getName(
				locale, true
			));
		item.put(FacetJSONResponseKeys.NAME, structure.getName(locale, true));
		item.put(FacetJSONResponseKeys.VALUE, bucket.getKey());

		return item;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DDMStructureNameFacetHandler.class);

	@Reference
	private DDMStructureLocalService _ddmStructureLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

}