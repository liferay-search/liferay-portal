
package com.liferay.portal.search.tuning.gsearch.impl.configuration;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * Keywords suggester configuration helper / facade.
 *
 * @author Petteri Karttunen
 */
@Component(
	configurationPid = KeywordSuggesterConfigurationHelper.CONFIGURATION_PID,
	immediate = true, 
	service = KeywordSuggesterConfigurationHelper.class
)
public class KeywordSuggesterConfigurationHelper {

	public static final String CONFIGURATION_PID =
		"com.liferay.portal.search.tuning.gsearch.impl.configuration.KeywordSuggesterConfiguration";

	public String[] getKeywordSuggesters() {
		return _configuration.keywordSuggesters();
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_configuration = ConfigurableUtil.createConfigurable(
			KeywordSuggesterConfiguration.class, properties);
	}

	private volatile KeywordSuggesterConfiguration _configuration;

}