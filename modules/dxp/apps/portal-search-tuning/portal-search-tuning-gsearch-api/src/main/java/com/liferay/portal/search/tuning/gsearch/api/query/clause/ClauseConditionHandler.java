
package com.liferay.portal.search.tuning.gsearch.api.query.clause;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;

/**
 * Checks clause's conditions.
 *
 * @author Petteri Karttunen
 */
public interface ClauseConditionHandler {

	/**
	 * Checks if this handler can process the requested type.
	 *
	 * @param handlerName
	 * @return
	 */
	public boolean canProcess(String handlerName);

	/**
	 * Is this condition true.
	 *
	 * @param queryContext
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	public boolean isTrue(QueryContext queryContext, JSONObject parameters)
		throws Exception;

}