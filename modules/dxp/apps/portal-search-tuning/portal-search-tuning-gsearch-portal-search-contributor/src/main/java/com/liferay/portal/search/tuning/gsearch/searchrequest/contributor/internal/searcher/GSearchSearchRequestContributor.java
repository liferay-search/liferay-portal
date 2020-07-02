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

package com.liferay.portal.search.tuning.gsearch.searchrequest.contributor.internal.searcher;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.spi.searcher.SearchRequestContributor;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationLocalService;
import com.liferay.portal.search.tuning.gsearch.constants.SearchContextAttributeKeys;
import com.liferay.portal.search.tuning.gsearch.context.SearchRequestContext;
import com.liferay.portal.search.tuning.gsearch.searchrequest.SearchRequestData;
import com.liferay.portal.search.tuning.gsearch.util.SearchClientHelper;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = "search.request.contributor.id=com.liferay.portal.search.gsearch",
	service = SearchRequestContributor.class
)
public class GSearchSearchRequestContributor
	implements SearchRequestContributor {

	@Override
	public SearchRequest contribute(SearchRequest searchRequest) {
		String keywords = searchRequest.getQueryString();

		if (!Validator.isBlank(keywords)) {
			return _build(searchRequest);
		}

		return searchRequest;
	}

	private SearchRequest _build(SearchRequest searchRequest) {
		SearchContext searchContext = _getSearchContext(searchRequest);

		int searchConfigurationId = GetterUtil.getInteger(
			searchContext.getAttribute(
				SearchContextAttributeKeys.SEARCH_CONFIGURATION_ID));

		long userId = GetterUtil.getLong(
			searchContext.getAttribute(SearchContextAttributeKeys.USER_ID));

		if ((searchConfigurationId == 0) || (userId == 0)) {
			return searchRequest;
		}

		try {
			SearchRequestContext searchRequestContext =
				_searchClientHelper.getSearchRequestContext(
					searchContext, searchConfigurationId);

			SearchRequestData searchRequestData =
				_searchClientHelper.getSearchRequestData(searchRequestContext);

			Optional<BooleanQuery> queryOptional =
				searchRequestData.getQueryOptional();

			if (!queryOptional.isPresent()) {
				return searchRequest;
			}

			return _searchRequestBuilderFactory.builder(
				searchRequest
			).query(
				queryOptional.get()
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
		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder(searchRequest);

		AtomicReference<SearchContext> searchContextReference =
			new AtomicReference<>();

		searchRequestBuilder.withSearchContext(
			searchContext -> searchContextReference.set(searchContext));

		return searchContextReference.get();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		GSearchSearchRequestContributor.class);

	@Reference
	private SearchClientHelper _searchClientHelper;

	@Reference
	private SearchConfigurationLocalService _searchConfigurationLocalService;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Reference
	private UserLocalService _userLocalService;

}