
package com.liferay.portal.search.tuning.gsearch.impl.configuration;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * Sort configuration helper / facade.
 *
 * @author Petteri Karttunen
 */
@Component(
	configurationPid = SortConfigurationHelper.CONFIGURATION_PID,
	immediate = true, 
	service = SortConfigurationHelper.class
)
public class SortConfigurationHelper {

	public static final String CONFIGURATION_PID =
		"com.liferay.portal.search.tuning.gsearch.impl.configuration.SortConfiguration";

	public String[] getSorts() {
		return _configuration.sorts();
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_configuration = ConfigurableUtil.createConfigurable(
			SortConfiguration.class, properties);
	}

	private volatile SortConfiguration _configuration;

}