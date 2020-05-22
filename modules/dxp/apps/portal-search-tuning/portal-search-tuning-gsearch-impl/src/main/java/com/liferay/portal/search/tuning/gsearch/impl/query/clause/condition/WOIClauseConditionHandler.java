package com.liferay.portal.search.tuning.gsearch.impl.query.clause.condition;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.gsearch.impl.util.GSearchUtil;

import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.portal.search.tuning.gsearch.api.constants.ClauseConfigurationValues;
import com.liferay.portal.search.tuning.gsearch.api.constants.SessionAttributes;
import com.liferay.portal.search.tuning.gsearch.api.query.clause.ClauseConditionHandler;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;

/**
 * Processes WOI clause condition. 
 * 
 * Just a simple exists implemented currently.
 *
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, 
	service = ClauseConditionHandler.class
)
public class WOIClauseConditionHandler implements ClauseConditionHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canProcess(String handlerName) {
		if (handlerName.equals(_HANDLER_NAME)) {

			// TODO: POC TIME
			
			return false;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTrue(QueryContext queryContext, JSONObject configuration)
		throws Exception {

		String woiCondition = configuration.getString("woi_condition");
		
		if (Validator.isBlank(woiCondition)) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Cannot process the clause condition. Check the existence of 'woi_condition' property in the configuration.");
			}
		}
		
		woiCondition = woiCondition.toLowerCase();

		PortletRequest portletRequest = 
				GSearchUtil.getPortletRequestFromContext(queryContext);

		PortletSession session = portletRequest.getPortletSession();

		Map<String, Integer> previousKeywords = 
				(Map<String, Integer>)session.getAttribute(
				SessionAttributes.PREVIOUS_KEYWORDS, PortletSession.APPLICATION_SCOPE);
				
		switch(woiCondition) {

			case ClauseConfigurationValues.MATCH_NOT_NULL: 

				return previousKeywords != null;
			default:
				return false;
		}		
	}
	
	private static final String _HANDLER_NAME = "woi";

	private static final Logger _log = LoggerFactory.getLogger(
		WOIClauseConditionHandler.class);
}