package com.liferay.portal.search.tuning.gsearch.impl.configuration;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * Rescore clause configuration helper / facade.
 *
 * @author Petteri Karttunen
 */
@Component(
	configurationPid = RescoreClauseConfigurationHelper.CONFIGURATION_PID,
	immediate = true,
	service = RescoreClauseConfigurationHelper.class
)
public class RescoreClauseConfigurationHelper {

	public static final String CONFIGURATION_PID =
		"com.liferay.portal.search.tuning.gsearch.impl.configuration.RescoreClauseConfiguration";

	public String[] getRescoreClauses() {
		return _configuration.rescoreClauses();
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		
		_configuration = ConfigurableUtil.createConfigurable(
				RescoreClauseConfiguration.class, properties);
	}

	private volatile RescoreClauseConfiguration _configuration;

}