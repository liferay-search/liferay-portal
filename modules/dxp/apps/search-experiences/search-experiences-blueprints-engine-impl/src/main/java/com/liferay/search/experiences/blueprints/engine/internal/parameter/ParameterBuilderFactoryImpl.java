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

package com.liferay.search.experiences.blueprints.engine.internal.parameter;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.search.experiences.blueprints.engine.internal.parameter.builder.ParameterBuilder;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ParameterBuilderFactory.class)
public class ParameterBuilderFactoryImpl implements ParameterBuilderFactory {

	@Override
	public ParameterBuilder getBuilder(String name)
		throws IllegalArgumentException {

		ParameterBuilder parameterBuilder =
			_parameterBuilderServiceTrackerMap.getService(name);

		if (parameterBuilder == null) {
			throw new IllegalArgumentException(
				"No registered parameter builder " + name);
		}

		return parameterBuilder;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_parameterBuilderServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, ParameterBuilder.class, "name");
	}

	@Deactivate
	protected void deactivate() {
		_parameterBuilderServiceTrackerMap.close();
	}

	private ServiceTrackerMap<String, ParameterBuilder>
		_parameterBuilderServiceTrackerMap;

}