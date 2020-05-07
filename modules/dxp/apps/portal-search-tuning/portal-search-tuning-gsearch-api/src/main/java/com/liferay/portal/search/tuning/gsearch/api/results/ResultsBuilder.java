
package com.liferay.portal.search.tuning.gsearch.api.results;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;

/**
 * Builds the search results object.
 *
 * @author Petteri Karttunen
 */
public interface ResultsBuilder {

	/**
	 * Builds search results.
	 *
	 * @param queryContext
	 * @param searchResponse
	 * @return
	 */
	public JSONObject buildResults(
		QueryContext queryContext, SearchSearchResponse searchResponse);

}