package com.liferay.portal.search.tuning.gsearch.poc;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.query.Queries;
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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
	immediate = true, 
	property = "search.request.contributor.id=com.liferay.portal.search.gsearch.poc", 
	service = SearchRequestContributor.class
)
public class GSearchSearchRequestContributor implements SearchRequestContributor {

	@Override
	public SearchRequest contribute(SearchRequest searchRequest) {
		String keywords = searchRequest.getQueryString();

		if (!Validator.isBlank(keywords)) {
			return build(searchRequest);
		}

		return searchRequest;
	}
	
	
	private SearchRequest build(SearchRequest searchRequest) {
		
		SearchRequestBuilder b = 
				_searchRequestBuilderFactory.builder(searchRequest);

		AtomicReference<SearchContext> searchContext1 = new AtomicReference<SearchContext>();

		b.withSearchContext(
			searchContext -> searchContext1.set(searchContext)
		);
				
		Query query = null;
		try {
			if (searchContext1 != null ) {
			QueryContext queryContext = _buildQueryContext(searchRequest, searchContext1);
			query = _gSearch.getQuery(queryContext);
			}
		}
		catch (Exception e) {
			_log.error(e.getMessage(), e);
	
			return searchRequest;
		}
		
		b.query(
			query
		).from(
			searchContext1.get().getStart()
		).indexes(
			"liferay-" + searchContext1.get().getCompanyId()
		).size(
			(searchContext1.get().getStart() + searchContext1.get().getEnd())
		).companyId(
			searchContext1.get().getCompanyId()
		)
		.excludeContributors(
			"com.liferay.portal.search.ranking"
		).withSearchContext(
			searchContext -> searchContext.setEntryClassNames(new String[] {})
		);
				
		return b.build();
	}	

	/**
	 * Builds query context.
	 * 
	 * @param resourceRequest
	 * @return
	 * @throws Exception
	 */
	private QueryContext _buildQueryContext(SearchRequest searchRequest, AtomicReference<SearchContext> searchContext) throws Exception {
		
		String keywords = searchRequest.getQueryString();
		
		Locale locale = searchContext.get().getLocale();
		
		QueryContext queryContext = new QueryContext();
		
		// Yes!
		
		User user = UserLocalServiceUtil.getUser(20126);
		
		queryContext.setParameter(ParameterNames.USER, user);
		queryContext.setParameter(ParameterNames.COMPANY_ID, searchContext.get().getCompanyId());
		queryContext.setKeywords(keywords);
		queryContext.setParameter(ParameterNames.LOCALE, searchContext.get().getLocale());
		queryContext.setConfiguration(
				ConfigurationNames.RESULT_DESCRIPTION_MAX_LENGTH, 300);


		// Parse request parameters.
		
		Map<String, Object>parameters = new HashMap<String, Object>();
		
		parameters.put(ParameterNames.LOCALE, locale);
		parameters.put(ParameterNames.KEYWORDS, keywords);
		parameters.put(ParameterNames.START, searchContext.get().getStart());
		parameters.put(ParameterNames.SORT_FIELD, "_score");
		parameters.put(ParameterNames.SORT_DIRECTION, "asc");
		
		queryContext.setConfiguration(
			ConfigurationNames.FILTER, _coreConfigurationHelper.getFilters());

		queryContext.setConfiguration(
			ConfigurationNames.CLAUSE, _coreConfigurationHelper.getClauses());

		queryContext.setConfiguration(
			ConfigurationNames.FACET, _coreConfigurationHelper.getFacets());

		queryContext.setConfiguration(
				ConfigurationNames.SORT, _coreConfigurationHelper.getSorts());

		queryContext.setConfiguration(
				ConfigurationNames.RESCORE, _coreConfigurationHelper.getRescoreClauses());

		queryContext.setConfiguration(
			ConfigurationNames.SUGGESTER, _coreConfigurationHelper.getKeywordSuggesters());

		
		_queryContextBuilder.parseParametersHeadless(queryContext, parameters);
		
		queryContext.setPageSize(10);

		queryContext.setQueryPostProcessorsEnabled(false);		
		
		return queryContext;
	}
		
	private static final Log _log = LogFactoryUtil.getLog(
			GSearchSearchRequestContributor.class);

	@Reference
	private CoreConfigurationHelper _coreConfigurationHelper;
	
	@Reference
	private GSearch _gSearch;
	
	@Reference
	private Queries _queries;
	
	@Reference
	private QueryContextBuilder _queryContextBuilder;
	
	@Reference
	protected SearchRequestBuilderFactory _searchRequestBuilderFactory;

}
