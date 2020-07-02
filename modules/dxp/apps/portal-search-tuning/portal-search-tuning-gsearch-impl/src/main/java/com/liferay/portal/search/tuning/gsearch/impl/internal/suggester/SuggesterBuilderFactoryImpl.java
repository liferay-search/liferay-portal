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

package com.liferay.portal.search.tuning.gsearch.impl.internal.suggester;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.gsearch.spi.suggester.SuggesterBuilder;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SuggesterBuilderFactory.class)
public class SuggesterBuilderFactoryImpl implements SuggesterBuilderFactory {

	@Override
	public SuggesterBuilder getBuilder(String type)
		throws IllegalArgumentException {

		SuggesterBuilder suggesterBuilder = _suggesterBuilders.get(type);

		if (suggesterBuilder == null) {
			throw new IllegalArgumentException(
				"Unable to find suggester builder for type " + type);
		}

		return suggesterBuilder;
	}

	@Override
	public String[] getBuilderTypes() {
		return _suggesterBuilders.keySet(
		).toArray(
			new String[0]
		);
	}

	protected void addSuggesterBuilder(
		SuggesterBuilder suggesterBuilder, Map<String, Object> properties) {

		String type = (String)properties.get("type");

		if (Validator.isBlank(type)) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to add suggester builder " +
						suggesterBuilder.getClass(
						).getName() + ". Type property empty.");
			}
		}

		_suggesterBuilders.put(type, suggesterBuilder);
	}

	protected void removeSuggesterBuilder(
		SuggesterBuilder suggesterBuilder, Map<String, Object> properties) {

		String type = (String)properties.get("type");

		_suggesterBuilders.remove(type);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SuggesterBuilderFactoryImpl.class);

	@Reference(
		bind = "addSuggesterBuilder",
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC, service = SuggesterBuilder.class,
		unbind = "removeSuggesterBuilder"
	)
	private volatile Map<String, SuggesterBuilder> _suggesterBuilders =
		new HashMap<>();

}