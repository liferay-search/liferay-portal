/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.blueprints.facets.internal.request.handler;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.search.experiences.blueprints.facets.spi.request.FacetRequestHandler;

import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "type=internal",
	service = FacetRequestHandlerFactory.class
)
public class FacetRequestHandlerFactoryImpl
	implements FacetRequestHandlerFactory {

	@Override
	public FacetRequestHandler getHandler(String name)
		throws IllegalArgumentException {

		FacetRequestHandler facetRequestHandler =
			_facetRequestHandlerServiceTrackerMap.getService(name);

		if (facetRequestHandler == null) {
			facetRequestHandler =
				_facetRequestHandlerServiceTrackerMap.getService("terms");

			if (_log.isWarnEnabled()) {
				StringBundler sb = new StringBundler(3);

				sb.append("No registered facet request handler for ");
				sb.append(name);
				sb.append(". Falling back to default");

				_log.warn(sb.toString());
			}
		}

		return facetRequestHandler;
	}

	@Override
	public String[] getHandlerNames() {
		Set<String> keySet = _facetRequestHandlerServiceTrackerMap.keySet();

		return keySet.toArray(new String[0]);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_facetRequestHandlerServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, FacetRequestHandler.class, "name");
	}

	@Deactivate
	protected void deactivate() {
		_facetRequestHandlerServiceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FacetRequestHandlerFactoryImpl.class);

	private ServiceTrackerMap<String, FacetRequestHandler>
		_facetRequestHandlerServiceTrackerMap;

}