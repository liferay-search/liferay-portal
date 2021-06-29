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

package com.liferay.search.experiences.searchresponse.json.translator.internal.result;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.search.experiences.searchresponse.json.translator.internal.result.builder.DefaultResultBuilder;
import com.liferay.search.experiences.searchresponse.json.translator.spi.result.ResultBuilder;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ResultBuilderFactory.class)
public class ResultBuilderFactoryImpl implements ResultBuilderFactory {

	@Override
	public ResultBuilder getBuilder(String className) {
		ResultBuilder resultBuilder =
			_resultBuilderServiceTrackerMap.getService(className);

		if (resultBuilder == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to find result builder for " + className +
						". Falling back to default.");
			}

			return new DefaultResultBuilder();
		}

		return resultBuilder;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_resultBuilderServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, ResultBuilder.class, "model.class.name");
	}

	@Deactivate
	protected void deactivate() {
		_resultBuilderServiceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ResultBuilderFactoryImpl.class);

	private ServiceTrackerMap<String, ResultBuilder>
		_resultBuilderServiceTrackerMap;

}