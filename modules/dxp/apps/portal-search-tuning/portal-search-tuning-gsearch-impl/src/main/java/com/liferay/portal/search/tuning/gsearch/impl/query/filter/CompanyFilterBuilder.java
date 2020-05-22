
package com.liferay.portal.search.tuning.gsearch.impl.query.filter;

import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.TermQuery;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.portal.search.tuning.gsearch.api.constants.ParameterNames;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;
import com.liferay.portal.search.tuning.gsearch.api.query.filter.FilterBuilder;

/**
 * Company filter builder.
 *
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, 
	service = FilterBuilder.class
)
public class CompanyFilterBuilder implements FilterBuilder {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFilters(QueryContext queryContext,
			BooleanQuery preFilterQuery, BooleanQuery postFilterQuery) 
					throws Exception {

		long companyId = (long)queryContext.getParameter(
			ParameterNames.COMPANY_ID);

		TermQuery query = _queries.term(Field.COMPANY_ID, companyId);
		
		preFilterQuery.addMustQueryClauses(query);
	}

	@Reference
	private Portal _portal;
	
	@Reference
	Queries _queries;
}