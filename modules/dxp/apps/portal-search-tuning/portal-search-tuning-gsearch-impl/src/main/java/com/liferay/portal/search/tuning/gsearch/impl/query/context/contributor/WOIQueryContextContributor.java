package com.liferay.portal.search.tuning.gsearch.impl.query.context.contributor;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.search.tuning.gsearch.impl.configuration.WOIConfiguration;
import com.liferay.portal.search.tuning.gsearch.impl.util.GSearchUtil;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import com.liferay.portal.search.tuning.gsearch.api.constants.ConfigurationVariables;
import com.liferay.portal.search.tuning.gsearch.api.constants.QueryContextContributorNames;
import com.liferay.portal.search.tuning.gsearch.api.constants.SessionAttributes;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;
import com.liferay.portal.search.tuning.gsearch.api.query.context.contributor.QueryContextContributor;

/**
 * WOI (Words Of Interest) contributor.
 * 
 * Identifies user's most frequent search words in the
 * current session and puts them available in the context
 * for clause conditions and query template variables.
 * 
 * Values are stored in a word->count and a reverse map
 * to make manipulation faster.
 * 
 * @author Petteri Karttunen
 */
@Component(
	configurationPid = "com.liferay.portal.search.tuning.gsearch.impl.configuration.WOIConfiguration",
	immediate = true, 
	service = QueryContextContributor.class
)
public class WOIQueryContextContributor implements QueryContextContributor {

	public void contribute(QueryContext queryContext) throws Exception {

		_contribute(queryContext);
	}
	
	@Override
	public String getName() {
		return _NAME;
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_woiConfiguration = ConfigurableUtil.createConfigurable(
			WOIConfiguration.class, properties);
	}
	
	/**
	 * Adds configuration variables and parameters to query context.
	 * Updates the session variable holding the previous keywords.
	 * 
	 * @param queryContext
	 * @throws Exception
	 */
	private void _contribute(QueryContext queryContext) throws Exception {

		PortletRequest portletRequest = 
				GSearchUtil.getPortletRequestFromContext(queryContext);
		
		PortletSession session = portletRequest.getPortletSession();
		
		Map<String, Integer> words = _getWordsOfInterest(session);

		if (words != null ) {
		
			_setConfigurationVariables(queryContext, words);
		}
	}
	
	/**
	 * Gets the WOI / previous keywords from session.
	 * 
	 * @param session
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Integer> _getWordsOfInterest(PortletSession session) {

		Map<String, Integer> previousKeywords = 
				(Map<String, Integer>)session.getAttribute(
				SessionAttributes.PREVIOUS_KEYWORDS, PortletSession.APPLICATION_SCOPE);

		if (previousKeywords != null) {
			return previousKeywords;
		}
		
		return null;
	}
	
	/**
	 * Sets configuration variable.
	 * 
	 * This sets a space separated string of the top n keywords
	 * to be used in the query configuration templates.
	 * 
	 * @param queryContext
	 * @param previousKeywords
	 */
	private void _setConfigurationVariables(
			QueryContext queryContext, Map<String, Integer> previousKeywords) {
				
		Map<String, Integer> sorted =
				previousKeywords.entrySet().stream()
			       .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			       .limit(_woiConfiguration.pickCount())
			       .collect(Collectors.toMap(
			          Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
				
		String keywords = sorted.keySet().stream().map(Object::toString).collect(Collectors.joining(" "));
		
		queryContext.addConfigurationVariable(ConfigurationVariables.WORDS_OF_INTEREST,
				keywords);
	}
	
	private static final String _NAME = 
			QueryContextContributorNames.WOI;

	private volatile WOIConfiguration
		_woiConfiguration;
}
