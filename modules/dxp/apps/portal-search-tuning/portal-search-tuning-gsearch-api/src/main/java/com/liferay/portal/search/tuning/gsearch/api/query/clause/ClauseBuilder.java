
package com.liferay.portal.search.tuning.gsearch.api.query.clause;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;

/**
 * Builds a single query clause (Match, QueryStringQuery etc).
 *
 * @author Petteri Karttunen
 */
public interface ClauseBuilder {

	/**
	 * Builds clause.
	 *
	 * @param queryContext
	 * @param configuration
	 * @return Query object
	 * @throws Exception
	 */
	public Query buildClause(
			QueryContext queryContext, JSONObject configuration)
		throws Exception;

	/**
	 * Checks if this builder can build the requested query type
	 *
	 * @param queryType
	 * @return
	 */
	public boolean canBuild(String queryType);

}