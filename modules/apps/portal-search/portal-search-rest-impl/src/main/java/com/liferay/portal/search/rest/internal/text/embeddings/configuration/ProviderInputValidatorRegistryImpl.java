/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.rest.internal.text.embeddings.configuration;

import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerListFactory;
import com.liferay.portal.search.rest.text.embeddings.configuration.ProviderInputValidator;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Rodrigo Guedes de Souza
 */
@Component(service = ProviderInputValidatorRegistry.class)
public class ProviderInputValidatorRegistryImpl
	implements ProviderInputValidatorRegistry {

	@Override
	public Map<String, String> validate(
		String service, Object serviceSettings) {

		Map<String, Object> serviceSettingsMap = Collections.emptyMap();

		if (serviceSettings instanceof Map) {
			serviceSettingsMap = (Map<String, Object>)serviceSettings;
		}

		for (ProviderInputValidator providerInputValidator :
				_serviceTrackerList) {

			if (Objects.equals(providerInputValidator.getService(), service)) {
				return providerInputValidator.validate(serviceSettingsMap);
			}
		}

		return Collections.emptyMap();
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerList = ServiceTrackerListFactory.open(
			bundleContext, ProviderInputValidator.class);
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerList.close();
	}

	private ServiceTrackerList<ProviderInputValidator> _serviceTrackerList;

}