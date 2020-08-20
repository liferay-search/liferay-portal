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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.response.contributor;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.tuning.blueprints.engine.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.util.ResponseUtil;
import com.liferay.portal.search.tuning.blueprints.engine.spi.response.ResponseContributor;

import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ResponseContributor.class)
public class PagingResponseContributor implements ResponseContributor {

	@Override
	public void contribute(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse,
		Map<String, Object> responseAttributes, JSONObject responseJsonObject) {

		responseJsonObject.put(
			JSONResponseKeys.PAGINATION,
			_getPaging(searchRequestContext, searchResponse));
	}

	protected JSONObject _getPaging(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse) {

		JSONObject pagingJsonObject = JSONFactoryUtil.createJSONObject();

		SearchHits searchHits = searchResponse.getSearchHits();

		try {
			int totalHits = Math.toIntExact(searchHits.getTotalHits());

			if (totalHits == 0) {
				return pagingJsonObject;
			}

			int pageSize = searchRequestContext.getSize();
			int start = ResponseUtil.getStart(searchRequestContext, searchHits);
			int pageCount = (int)Math.ceil(
				totalHits * 1.0 / searchRequestContext.getSize());

			int currentPage = (int)Math.floor((start + 1) / pageSize) + 1;
			pagingJsonObject.put(JSONResponseKeys.ACTIVE_PAGE, currentPage);
			pagingJsonObject.put(JSONResponseKeys.TOTAL_PAGES, pageCount);
		}
		catch (ArithmeticException arithmeticException) {
			_log.error(arithmeticException.getMessage(), arithmeticException);
		}

		return pagingJsonObject;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		PagingResponseContributor.class);

}