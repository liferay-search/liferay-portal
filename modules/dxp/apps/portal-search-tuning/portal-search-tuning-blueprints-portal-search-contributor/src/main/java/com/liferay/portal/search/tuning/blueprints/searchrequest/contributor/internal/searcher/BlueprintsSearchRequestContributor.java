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
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributesBuilder;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributesBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.portal.search.tuning.blueprints.engine.constants.SearchContextAttributeKeys;
import com.liferay.portal.search.tuning.blueprints.engine.util.BlueprintsEngineHelper;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintLocalService;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.Locale;

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
		long blueprintId = getBlueprintId(searchRequest);

		long userId = getUserId(searchRequest);

		if (_log.isDebugEnabled()) {
			_log.debug(
				"Executing Search Blueprints search request contributor");
			_log.debug("Blueprint ID " + blueprintId);
			_log.debug("User ID " + userId);
		}

		if ((blueprintId == 0) || (userId == 0)) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Blueprint and user ID have to be set in search context");
			}

			return searchRequest;
		}

		Messages messages = new Messages();
		
		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder(searchRequest);

		_blueprintsEngineHelper.combine(
			searchRequestBuilder, getBlueprintsAttributes(searchRequest),
			messages, getBlueprintId(searchRequest));

		return searchRequestBuilder.build();
	}

	protected long getBlueprintId(SearchRequest searchRequest) {
		return _searchRequestBuilderFactory.builder(
			searchRequest
		).withSearchContextGet(
			searchContext -> GetterUtil.getLong(
				searchContext.getAttribute(
					SearchContextAttributeKeys.BLUEPRINT_ID))
		);
	}

	protected BlueprintsAttributes getBlueprintsAttributes(
		SearchRequest searchRequest) {

		BlueprintsAttributesBuilder blueprintsAttributesBuilder =
			_blueprintsAttributesBuilderFactory.builder();
		
		return blueprintsAttributesBuilder.companyId(
			getCompanyId(searchRequest)
		).locale(
			getLocale(searchRequest)
		).userId(
			getUserId(searchRequest)
		).addAttribute(
			ReservedParameterNames.IP_ADDRESS.getKey(), getIpAddress(searchRequest)
		).addAttribute(
			ReservedParameterNames.PLID.getKey(), getPlid(searchRequest)
		).addAttribute(
			ReservedParameterNames.SCOPE_GROUP_ID.getKey(),
			getScopeGroupId(searchRequest)
		).addAttribute(
			ReservedParameterNames.TIMEZONE_ID.getKey(), getTimezoneId(searchRequest)
		).build();
	}

	protected long getCompanyId(SearchRequest searchRequest) {
		return _searchRequestBuilderFactory.builder(
			searchRequest
		).withSearchContextGet(
			searchContext -> searchContext.getCompanyId()
		);
	}

	protected String getIpAddress(SearchRequest searchRequest) {
		return _searchRequestBuilderFactory.builder(
			searchRequest
		).withSearchContextGet(
			searchContext -> GetterUtil.getString(
				searchContext.getAttribute(SearchContextAttributeKeys.IP_ADDRESS))
		);
	}

	protected Locale getLocale(SearchRequest searchRequest) {
		return _searchRequestBuilderFactory.builder(
			searchRequest
		).withSearchContextGet(
			searchContext -> searchContext.getLocale()
		);
	}

	protected long getPlid(SearchRequest searchRequest) {
		return _searchRequestBuilderFactory.builder(
			searchRequest
		).withSearchContextGet(
			searchContext -> searchContext.getLayout(
			).getPlid()
		);
	}
	
	protected long getScopeGroupId(SearchRequest searchRequest) {
		return _searchRequestBuilderFactory.builder(
			searchRequest
		).withSearchContextGet(
			searchContext -> searchContext.getLayout(
			).getGroupId()
		);
	}

	protected String getTimezoneId(SearchRequest searchRequest) {
		return _searchRequestBuilderFactory.builder(
			searchRequest
		).withSearchContextGet(
			searchContext -> searchContext.getTimeZone(
			).getID()
		);
	}

	protected long getUserId(SearchRequest searchRequest) {
		return _searchRequestBuilderFactory.builder(
			searchRequest
		).withSearchContextGet(
			searchContext -> searchContext.getUserId()
		);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintsSearchRequestContributor.class);

	@Reference
	private BlueprintLocalService _blueprintLocalService;

	@Reference
	private BlueprintsAttributesBuilderFactory
		_blueprintsAttributesBuilderFactory;

	@Reference
	private BlueprintHelper _blueprintHelper;
	
	@Reference
	private BlueprintsEngineHelper _blueprintsEngineHelper;

	@Reference
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Reference
	private UserLocalService _userLocalService;

}