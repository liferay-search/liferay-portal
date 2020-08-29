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

package com.liferay.portal.search.tuning.blueprints.engine.internal.aggregation;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.engine.component.ServiceComponentReference;
import com.liferay.portal.search.tuning.blueprints.engine.spi.aggregation.AggregationRequestBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = AggregationRequestBuilderFactory.class)
public class AggregationRequestBuilderFactoryImpl
	implements AggregationRequestBuilderFactory {

	@Override
	public AggregationRequestBuilder getBuilder(String type)
		throws IllegalArgumentException {

		ServiceComponentReference<AggregationRequestBuilder>
			serviceComponentReference = _aggregationRequestBuilders.get(type);

		if (serviceComponentReference == null) {
			throw new IllegalArgumentException(
				"Unable to find aggregation builder for " + type);
		}

		return serviceComponentReference.getServiceComponent();
	}

	@Override
	public String[] getBuilderTypes() {
		return _aggregationRequestBuilders.keySet(
		).toArray(
			new String[0]
		);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void registerAggregationRequestBuilder(
		AggregationRequestBuilder aggregationRequestBuilder,
		Map<String, Object> properties) {

		String type = (String)properties.get("type");

		if (Validator.isBlank(type)) {
			if (_log.isWarnEnabled()) {
				Class<?> clazz = aggregationRequestBuilder.getClass();

				_log.warn(
					"Unable to add aggregation builder " + clazz.getName() +
						". Type property empty.");
			}

			return;
		}

		int serviceRanking = GetterUtil.get(
			properties.get("service.ranking"), 0);

		ServiceComponentReference<AggregationRequestBuilder>
			serviceComponentReference = new ServiceComponentReference<>(
				aggregationRequestBuilder, serviceRanking);

		if (_aggregationRequestBuilders.containsKey(type)) {
			ServiceComponentReference<AggregationRequestBuilder>
				previousReference = _aggregationRequestBuilders.get(type);

			if (previousReference.compareTo(serviceComponentReference) < 0) {
				_aggregationRequestBuilders.put(
					type, serviceComponentReference);
			}
		}
		else {
			_aggregationRequestBuilders.put(type, serviceComponentReference);
		}
	}

	protected void unregisterAggregationRequestBuilder(
		AggregationRequestBuilder aggregationRequestBuilder,
		Map<String, Object> properties) {

		String type = (String)properties.get("type");

		if (Validator.isBlank(type)) {
			return;
		}

		_aggregationRequestBuilders.remove(type);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AggregationRequestBuilderFactoryImpl.class);

	private volatile Map
		<String, ServiceComponentReference<AggregationRequestBuilder>>
			_aggregationRequestBuilders = new ConcurrentHashMap<>();

}