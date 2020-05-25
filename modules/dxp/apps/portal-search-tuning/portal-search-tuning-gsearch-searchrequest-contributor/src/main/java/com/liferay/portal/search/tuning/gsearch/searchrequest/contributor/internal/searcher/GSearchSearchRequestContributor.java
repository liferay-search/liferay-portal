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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.spi.searcher.SearchRequestContributor;
import com.liferay.portal.search.tuning.gsearch.api.GSearch;
import com.liferay.portal.search.tuning.gsearch.api.configuration.CoreConfigurationHelper;
import com.liferay.portal.search.tuning.gsearch.api.constants.ConfigurationNames;
import com.liferay.portal.search.tuning.gsearch.api.constants.ParameterNames;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContextBuilder;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SearchConfiguration;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationLocalService;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = "search.request.contributor.id=com.liferay.portal.search.gsearch.poc",
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
		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder(searchRequest);

		AtomicReference<SearchContext> searchContext1 = new AtomicReference<>();

		searchRequestBuilder.withSearchContext(
			searchContext -> searchContext1.set(searchContext));

		SearchContext searchContext2 = searchContext1.get();

		Query query = null;

		try {
			QueryContext queryContext = _buildQueryContext(
				searchRequest, searchContext2);

			query = _gSearch.getQuery(queryContext);
		}
		catch (Exception exception) {
			_log.error(exception.getMessage(), exception);

			return searchRequest;
		}

		searchRequestBuilder.query(
			query
		).from(
			searchContext2.getStart()
		).indexes(
			"liferay-" + searchContext2.getCompanyId()
		).size(
			searchContext2.getStart() + searchContext2.getEnd()
		);

		return searchRequestBuilder.build();
	}

	private QueryContext _buildQueryContext(
			SearchRequest searchRequest, SearchContext searchContext)
		throws Exception {

		String keywords = searchRequest.getQueryString();

		QueryContext queryContext = new QueryContext();

		int searchConfigurationId = GetterUtil.getInteger(
			searchContext.getAttribute("searchConfigurationId"));

		queryContext.setParameter(
			"searchConfigurationId", searchConfigurationId);

		long userId = GetterUtil.getLong(searchContext.getAttribute("userId"));

		queryContext.setParameter(
			ParameterNames.USER, _userLocalService.getUser(userId));

		queryContext.setParameter(
			ParameterNames.COMPANY_ID, searchContext.getCompanyId());
		queryContext.setKeywords(keywords);
		queryContext.setParameter(
			ParameterNames.LOCALE, searchContext.getLocale());
		queryContext.setConfiguration(
			ConfigurationNames.RESULT_DESCRIPTION_MAX_LENGTH, 300);
		queryContext.setPageSize(10);
		queryContext.setQueryPostProcessorsEnabled(false);

		queryContext.setConfiguration(
			ConfigurationNames.FILTER, _coreConfigurationHelper.getFilters());

		queryContext.setConfiguration(
			ConfigurationNames.CLAUSE,
			_getClauseConfiguration(searchConfigurationId));

		queryContext.setConfiguration(
			ConfigurationNames.FACET, _coreConfigurationHelper.getFacets());

		queryContext.setConfiguration(
			ConfigurationNames.SORT, _coreConfigurationHelper.getSorts());

		queryContext.setConfiguration(
			ConfigurationNames.RESCORE,
			_coreConfigurationHelper.getRescoreClauses());

		queryContext.setConfiguration(
			ConfigurationNames.SUGGESTER,
			_coreConfigurationHelper.getKeywordSuggesters());

		Map<String, Object> parameters = HashMapBuilder.<String, Object>put(
			ParameterNames.KEYWORDS, keywords
		).put(
			ParameterNames.LOCALE, searchContext.getLocale()
		).put(
			ParameterNames.SORT_DIRECTION, "asc"
		).put(
			ParameterNames.SORT_FIELD, "_score"
		).put(
			ParameterNames.START, searchContext.getStart()
		).build();

		_queryContextBuilder.parseParametersHeadless(queryContext, parameters);

		return queryContext;
	}

	private JSONArray _getClauseConfiguration(long searchConfigurationId)
		throws JSONException, PortalException {

		SearchConfiguration searchConfiguration =
			_searchConfigurationLocalService.getSearchConfiguration(
				searchConfigurationId);

		JSONObject configurationObject = JSONFactoryUtil.createJSONObject(
			searchConfiguration.getConfiguration());

		return configurationObject.getJSONArray("clause_configuration");
	}

	private static final Log _log = LogFactoryUtil.getLog(
		GSearchSearchRequestContributor.class);

	@Reference
	private CoreConfigurationHelper _coreConfigurationHelper;

	@Reference
	private GSearch _gSearch;

	@Reference
	private QueryContextBuilder _queryContextBuilder;

	@Reference
	private SearchConfigurationLocalService _searchConfigurationLocalService;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Reference
	private UserLocalService _userLocalService;

}