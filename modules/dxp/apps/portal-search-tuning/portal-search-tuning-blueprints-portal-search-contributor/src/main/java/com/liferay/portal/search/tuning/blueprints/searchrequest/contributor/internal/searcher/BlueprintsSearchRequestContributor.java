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

package com.liferay.portal.search.tuning.blueprints.searchrequest.contributor.internal.searcher;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.spi.searcher.SearchRequestContributor;
import com.liferay.portal.search.tuning.blueprints.engine.constants.SearchContextAttributeKeys;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.searchrequest.SearchRequestData;
import com.liferay.portal.search.tuning.blueprints.engine.util.SearchClientHelper;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = "search.request.contributor.id=com.liferay.portal.search.tunning.blueprints",
	service = SearchRequestContributor.class
)
public class BlueprintsSearchRequestContributor
	implements SearchRequestContributor {

	@Override
	public SearchRequest contribute(SearchRequest searchRequest) {
		String keywords = searchRequest.getQueryString();

		_log.debug("Executing Search Blueprints search request contributor.");
		
		if (!Validator.isBlank(keywords)) {
			return _build(searchRequest);
		}

		return searchRequest;
	}

	private SearchRequest _build(SearchRequest searchRequest) {
		SearchContext searchContext = _getSearchContext(searchRequest);

		int blueprintId = GetterUtil.getInteger(
			searchContext.getAttribute(
				SearchContextAttributeKeys.BLUEPRINT_ID), 0);

		_log.debug("Blueprint ID " + blueprintId);
		
		long userId = GetterUtil.getLong(
			searchContext.getAttribute(SearchContextAttributeKeys.USER_ID), 0L);

		_log.debug("User ID " + userId);

		if ((blueprintId == 0) || (userId == 0)) {

			_log.debug("Blueprint and user ID have to be set in search context.");

			return searchRequest;
		}

		try {
			SearchRequestContext searchRequestContext =
				_searchClientHelper.getSearchRequestContext(
					searchContext, blueprintId);

			SearchRequestData searchRequestData =
				_searchClientHelper.getSearchRequestData(searchRequestContext);

			BooleanQuery query = searchRequestData.getQuery();

			if (!query.hasClauses()) {
				return searchRequest;
			}

			return _searchRequestBuilderFactory.builder(
				searchRequest
			).query(
				query
			).indexes(
				searchRequestContext.getIndexNames()
			).build();
		}
		catch (Exception exception) {
			_log.error(exception.getMessage(), exception);

			return searchRequest;
		}
	}

	private SearchContext _getSearchContext(SearchRequest searchRequest) {
		return _searchRequestBuilderFactory.builder(
			searchRequest
		).withSearchContextGet(
			searchContext -> searchContext
		);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintsSearchRequestContributor.class);

	@Reference
	private SearchClientHelper _searchClientHelper;

	@Reference
	private BlueprintLocalService _blueprintLocalService;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Reference
	private UserLocalService _userLocalService;

}