
package com.liferay.portal.search.tuning.gsearch.api.query.clause;

/**
 * Returns a clause builder specific for the query type (MatchQuery,
 * QueryStrinQuery etc.)
 *
 * @author Petteri Karttunen
 */
public interface ClauseBuilderFactory {

	/**
	 * Gets clause builder.
	 *
	 * @param queryType
	 * @return ClauseBuilder
	 */
	public ClauseBuilder getClauseBuilder(String queryType);

}