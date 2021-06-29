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

package com.liferay.search.experiences.searchresponse.json.translator.internal.aggregation;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.search.experiences.searchresponse.json.translator.spi.aggregation.AggregationJSONTranslator;

import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = AggregationJSONTranslatorFactory.class)
public class AggregationJSONTranslatorFactoryImpl
	implements AggregationJSONTranslatorFactory {

	@Override
	public AggregationJSONTranslator getTranslator(String name)
		throws IllegalArgumentException {

		AggregationJSONTranslator aggregationJSONTranslator =
			_aggregationJSONTranslatorServiceTrackerMap.getService(name);

		if (aggregationJSONTranslator == null) {
			aggregationJSONTranslator =
				_aggregationJSONTranslatorServiceTrackerMap.getService(
					"default");

			if (_log.isWarnEnabled()) {
				StringBundler sb = new StringBundler(3);

				sb.append("No registered handler for ");
				sb.append(name);
				sb.append(". Falling back to default");

				_log.warn(sb.toString());
			}
		}

		return aggregationJSONTranslator;
	}

	@Override
	public String[] getTranslatorNames() {
		Set<String> keySet =
			_aggregationJSONTranslatorServiceTrackerMap.keySet();

		return keySet.toArray(new String[0]);
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

	private static final Log _log = LogFactoryUtil.getLog(
		AggregationJSONTranslatorFactoryImpl.class);

	private ServiceTrackerMap<String, AggregationJSONTranslator>
		_aggregationJSONTranslatorServiceTrackerMap;

}