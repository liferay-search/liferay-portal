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

package com.liferay.portal.search.tuning.blueprints.json.response.internal.contributor;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.json.response.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.json.response.internal.util.ResponseUtil;
import com.liferay.portal.search.tuning.blueprints.json.response.spi.contributor.ResponseContributor;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=paging",
	service = ResponseContributor.class
)
public class PagingResponseContributor implements ResponseContributor {

	@Override
	public void contribute(
		JSONObject responseJSONObject, SearchResponse searchResponse,
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle, Messages messages) {

		responseJSONObject.put(
			JSONResponseKeys.PAGINATION,
			_getPagingJSONObject(searchResponse, blueprint));
	}

	private JSONObject _getPagingJSONObject(
		SearchResponse searchResponse, Blueprint blueprint) {

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		SearchHits searchHits = searchResponse.getSearchHits();

		try {
			int totalHits = Math.toIntExact(searchHits.getTotalHits());

			if (totalHits == 0) {
				return jsonObject;
			}

			int pageSize = _blueprintHelper.getSize(blueprint);

			SearchRequest searchRequest = searchResponse.getRequest();

			int start = ResponseUtil.getStart(
				totalHits, pageSize, searchRequest.getFrom());

			int pageCount = (int)Math.ceil(totalHits * 1.0 / pageSize);

			int currentPage = (int)Math.floor((start + 1) / pageSize) + 1;

			jsonObject.put(
				JSONResponseKeys.ACTIVE_PAGE, currentPage
			).put(
				JSONResponseKeys.TOTAL_PAGES, pageCount
			);
		}
		catch (ArithmeticException arithmeticException) {
			_log.error(arithmeticException.getMessage(), arithmeticException);
		}

		return jsonObject;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		PagingResponseContributor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

}