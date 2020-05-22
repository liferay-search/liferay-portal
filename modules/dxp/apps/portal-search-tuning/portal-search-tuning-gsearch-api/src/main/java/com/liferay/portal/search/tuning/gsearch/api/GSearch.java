
package com.liferay.portal.search.tuning.gsearch.api;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;

/**
 * GSearch service.
 *
 * @author Petteri Karttunen
 */
public interface GSearch {

	/**
	 * Gets search results.
	 *
	 * @param queryContext
	 * @return search results as JSON object
	 * @throws Exception
	 */
	public JSONObject getSearchResults(QueryContext queryContext)
		throws Exception;

	public Query getQuery(QueryContext queryContext) throws Exception;
}