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

package com.liferay.search.experiences.searchresponse.json.translator.internal.paging;

import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;
import com.liferay.search.experiences.searchresponse.json.translator.spi.contributor.JSONTranslationContributor;

import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=paging",
	service = JSONTranslationContributor.class
)
public class PagingJSONTranslationContributor
	implements JSONTranslationContributor {

	@Override
	public void contribute(
		JSONObject responseJSONObject, SearchResponse searchResponse,
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle,
		ProblemsHolderBuilder problemsHolderBuilder) {

		responseJSONObject.put(
			"pagination", _getPagingJSONObject(searchResponse));
	}

	@Reference(unbind = "-")
	public void setJSONFactory(JSONFactory jsonFactory) {
		_jsonFactory = jsonFactory;
	}

	private int _getFrom(SearchResponse searchResponse) {
		SearchRequest searchRequest = searchResponse.getRequest();

		if (searchRequest.getFrom() != null) {
			return searchRequest.getFrom();
		}

		return searchResponse.withSearchContextGet(
			searchContext -> searchContext.getStart());
	}

	private int _getPageSize(SearchResponse searchResponse) {
		SearchRequest searchRequest = searchResponse.getRequest();

		if (searchRequest.getSize() != null) {
			return searchRequest.getSize();
		}

		return searchResponse.withSearchContextGet(
			searchContext -> searchContext.getEnd() - searchContext.getStart());
	}

	private JSONObject _getPagingJSONObject(SearchResponse searchResponse) {
		JSONObject jsonObject = _jsonFactory.createJSONObject();

		SearchHits searchHits = searchResponse.getSearchHits();

		try {
			int totalHits = Math.toIntExact(searchHits.getTotalHits());

			if (totalHits == 0) {
				return jsonObject;
			}

			int from = _getFrom(searchResponse);

			int pageSize = _getPageSize(searchResponse);

			int start = _getStart(totalHits, pageSize, from);

			jsonObject.put(
				"activePage", (int)Math.floor((start + 1) / pageSize) + 1
			).put(
				"pageSize", pageSize
			).put(
				"totalPages", (int)Math.ceil(totalHits * 1.0 / pageSize)
			);
		}
		catch (ArithmeticException arithmeticException) {
			_log.error(arithmeticException.getMessage(), arithmeticException);
		}

		return jsonObject;
	}

	private int _getStart(int totalHits, int pageSize, int start)
		throws ArithmeticException {

		if (totalHits < start) {
			int pageCount = (int)Math.ceil(totalHits * 1.0 / pageSize);

			start = (pageCount - 1) * pageSize;

			if (start < 0) {
				start = 0;
			}
		}

		return start;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		PagingJSONTranslationContributor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	private JSONFactory _jsonFactory;

}