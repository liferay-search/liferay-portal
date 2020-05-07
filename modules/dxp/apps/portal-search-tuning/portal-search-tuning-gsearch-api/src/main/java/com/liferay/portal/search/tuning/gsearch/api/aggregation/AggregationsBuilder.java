package com.liferay.portal.search.tuning.gsearch.api.aggregation;

import com.liferay.portal.search.aggregation.Aggregation;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;

import java.util.List;

/**
 * Aggregations builder service.
 *
 * @author Petteri Karttunen
 */
public interface AggregationsBuilder {

	/**
	 * Builds a collection of aggregations.
	 *
	 * @param queryContext
	 * @return
	 * @throws Exception
	 */
	public List<Aggregation> buildAggregation(QueryContext queryContext) 
			throws Exception;

}