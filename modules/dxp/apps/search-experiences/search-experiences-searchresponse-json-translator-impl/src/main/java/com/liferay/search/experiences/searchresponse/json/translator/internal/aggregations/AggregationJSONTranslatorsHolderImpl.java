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

package com.liferay.search.experiences.searchresponse.json.translator.internal.aggregations;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.search.experiences.searchresponse.json.translator.spi.aggregation.AggregationJSONTranslator;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = AggregationJSONTranslatorsHolder.class)
public class AggregationJSONTranslatorsHolderImpl
	implements AggregationJSONTranslatorsHolder {

	@Override
	public AggregationJSONTranslator getTranslator(String name) {
		if (name == null) {
			return null;
		}

		return _aggregationJSONTranslatorServiceTrackerMap.getService(name);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_aggregationJSONTranslatorServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, AggregationJSONTranslator.class, "name");
	}

	@Deactivate
	protected void deactivate() {
		_aggregationJSONTranslatorServiceTrackerMap.close();
	}

	private ServiceTrackerMap<String, AggregationJSONTranslator>
		_aggregationJSONTranslatorServiceTrackerMap;

}