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

import com.liferay.bookmarks.model.BookmarksFolder;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
import com.liferay.journal.model.JournalFolder;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=folder",
	service = FacetResponseHandler.class
)
public class FolderFacetResponseHandler
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
				JSONObject jsonObject = _getFolderJSONObject(
					bucket, resourceBundle, blueprintsAttributes);

				if (jsonObject != null) {
					jsonArray.put(jsonObject);
				}
			}
			catch (PortalException portalException) {
				messages.addMessage(
					new Message(
						Severity.ERROR, "core", "core.error.folder-not-found",
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

	private Document _getDocument(
		long folderId, BlueprintsAttributes blueprintsAttributes) {

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder(
			).addComplexQueryPart(
				_complexQueryPartBuilderFactory.builder(
				).query(
					_queries.term(Field.ENTRY_CLASS_PK, folderId)
				).occur(
					"filter"
				).build()
			).companyId(
				blueprintsAttributes.getCompanyId()
			).modelIndexerClasses(
				BookmarksFolder.class, DLFolder.class, JournalFolder.class
			).emptySearchEnabled(
				true
			).highlightEnabled(
				false
			).locale(
				blueprintsAttributes.getLocale()
			).size(
				1
			).from(
				0
			);

		SearchResponse searchResponse = _searcher.search(
			searchRequestBuilder.build());

		SearchHits searchHits = searchResponse.getSearchHits();

		List<SearchHit> hits = searchHits.getSearchHits();

		if (hits.size() == 1) {
			return hits.get(
				0
			).getDocument();
		}

		return null;
	}

	private JSONObject _getFolderJSONObject(
			Bucket bucket, ResourceBundle resourceBundle,
			BlueprintsAttributes blueprintsAttributes)
		throws PortalException {

		String value = bucket.getKey();

		long folderId = GetterUtil.getLong(value);

		if (folderId == 0) {
			return null;
		}

		Document document = _getDocument(folderId, blueprintsAttributes);

		if (document == null) {
			return null;
		}

		long groupId = document.getLong(Field.GROUP_ID);

		Group group = _groupLocalService.getGroup(groupId);

		long frequency = bucket.getDocCount();

		JSONObject jsonObject = JSONUtil.put(
			FacetJSONResponseKeys.FREQUENCY, frequency);

		Locale locale = blueprintsAttributes.getLocale();

		String name = document.getString(
			"localized_title_" + locale.toString());

		jsonObject.put(
			FacetJSONResponseKeys.GROUP_NAME, group.getName(locale, true)
		).put(
			FacetJSONResponseKeys.NAME, name
		).put(
			FacetJSONResponseKeys.TEXT, getText(name, frequency, null)
		).put(
			FacetJSONResponseKeys.VALUE, folderId
		);

		return jsonObject;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FolderFacetResponseHandler.class);

	@Reference
	ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Reference
	private DLFolderLocalService _dlFolderLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Queries _queries;

	@Reference
	private Searcher _searcher;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

}