/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.ingest.web.internal.ingester;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Petteri Karttunen
 */
@Component(enabled = false, service = IngesterFactory.class)
public class IngesterFactoryImpl implements IngesterFactory {

	@Override
	public Ingester getIngester(String type) throws IllegalArgumentException {
		Ingester ingester = _ingesterServiceTrackerMap.getService(type);

		if (ingester == null) {
			throw new IllegalArgumentException(
				"Unable to find ingester for " + type);
		}

		return ingester;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_ingesterServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, Ingester.class, "type");
	}

	@Deactivate
	protected void deactivate() {
		_ingesterServiceTrackerMap.close();
	}

	private ServiceTrackerMap<String, Ingester> _ingesterServiceTrackerMap;

}