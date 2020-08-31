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
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.spi.searcher.SearchRequestContributor;
import com.liferay.portal.search.tuning.blueprints.engine.constants.SearchContextAttributeKeys;
import com.liferay.portal.search.tuning.blueprints.engine.util.SearchClientHelper;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = "search.request.contributor.id=com.liferay.portal.search.tuning.blueprints",
	service = SearchRequestContributor.class
)
public class BlueprintsSearchRequestContributor
	implements SearchRequestContributor {

	@Override
	public SearchRequest contribute(SearchRequest searchRequest) {
		_log.debug("Executing Search Blueprints search request contributor.");

		int blueprintId = getBlueprintId(searchRequest);

		_log.debug("Blueprint ID " + blueprintId);

		long userId = getUserId(searchRequest);

		_log.debug("User ID " + userId);

		if ((blueprintId == 0) || (userId == 0)) {
			_log.debug(
				"Blueprint and user ID have to be set in search context.");

			return searchRequest;
		}

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder(searchRequest);

		_searchClientHelper.combine(searchRequestBuilder, blueprintId);

		return searchRequestBuilder.build();
	}

	protected long getUserId(SearchRequest searchRequest) {
		if (true) {
			return 1;
		}

		return _searchRequestBuilderFactory.builder(searchRequest)
		.withSearchContextGet(
			searchContext -> GetterUtil.getLong(
				searchContext.getAttribute(
					SearchContextAttributeKeys.USER_ID)));
	}

	protected int getBlueprintId(SearchRequest searchRequest) {
		return _searchRequestBuilderFactory.builder(searchRequest)
		.withSearchContextGet(
			searchContext -> GetterUtil.getInteger(
				searchContext.getAttribute(
					SearchContextAttributeKeys.BLUEPRINT_ID)));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintsSearchRequestContributor.class);

	@Reference
	private SearchClientHelper _searchClientHelper;

	@Reference
	private BlueprintLocalService _blueprintLocalService;

	@Reference
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Reference
	private UserLocalService _userLocalService;

}