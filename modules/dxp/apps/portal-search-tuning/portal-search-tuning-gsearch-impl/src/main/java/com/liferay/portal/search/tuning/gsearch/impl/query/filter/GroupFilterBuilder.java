
package com.liferay.portal.search.tuning.gsearch.impl.query.filter;

import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.TermQuery;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.portal.search.tuning.gsearch.api.constants.FilterConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.api.constants.ParameterNames;
import com.liferay.portal.search.tuning.gsearch.api.params.FilterParameter;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;
import com.liferay.portal.search.tuning.gsearch.api.query.filter.FilterBuilder;

/**
 * Group filter builder.
 *
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, 
	service = FilterBuilder.class
)
public class GroupFilterBuilder implements FilterBuilder {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFilters(QueryContext queryContext,
			BooleanQuery preFilterQuery, BooleanQuery postFilterQuery) 
					throws Exception {

		FilterParameter filter = queryContext.getFilterParameter(
			ParameterNames.GROUP_ID);

		if (filter == null) {
			return;
		}

		long[] groupIds = (long[])filter.getAttribute(FilterConfigurationKeys.VALUES);

		if ((groupIds != null) && (groupIds.length > 0)) {
			
			BooleanQuery query = _queries.booleanQuery();

			for (long groupId : groupIds) {
				TermQuery condition = _queries.term(
					Field.SCOPE_GROUP_ID, groupId);

				query.addShouldQueryClauses(condition);
			}
		}
	}

	@Reference
	private Queries _queries;	
}