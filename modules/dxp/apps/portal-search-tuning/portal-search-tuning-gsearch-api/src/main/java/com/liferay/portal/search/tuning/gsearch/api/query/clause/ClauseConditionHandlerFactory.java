
package com.liferay.portal.search.tuning.gsearch.api.query.clause;

/**
 * Clause condition handler factory.
 *
 * @author Petteri Karttunen
 */
public interface ClauseConditionHandlerFactory {

	/**
	 * Gets the condition handler.
	 *
	 * @param handlerName
	 * @return
	 */
	public ClauseConditionHandler getHandler(String handlerName);

}