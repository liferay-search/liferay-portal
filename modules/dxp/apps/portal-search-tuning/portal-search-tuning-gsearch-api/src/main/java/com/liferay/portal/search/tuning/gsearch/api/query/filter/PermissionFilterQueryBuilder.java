
package com.liferay.portal.search.tuning.gsearch.api.query.filter;

import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;

/**
 * Builds permission filter query.
 *
 * This interface is separated from FilterBuilder so
 * that overriding it would be easier.
 *
 * @author Petteri Karttunen
 */
public interface PermissionFilterQueryBuilder {

	/**
	 * Builds the permission query filter.
	 *
	 * @param queryContext
	 * @return Query object
	 * @throws Exception
	 */
	public Query buildPermissionQuery(QueryContext queryContext)
		throws Exception;

}