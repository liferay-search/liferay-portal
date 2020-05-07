package com.liferay.portal.search.tuning.gsearch.api.query.context.contributor;

import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;

/**
 * API for contributing to query context.
 * 
 * @author liferay
 *
 */
public interface QueryContextContributor {

	/**
	 * Contributes to the query context.
	 * 
	 * @param queryContext
	 * @throws Exception
	 */
	public void contribute(QueryContext queryContext) throws Exception;

	/**
	 * Gets contributor name.
	 * 
	 * @param name
	 * @return
	 */
	public String getName();
}
