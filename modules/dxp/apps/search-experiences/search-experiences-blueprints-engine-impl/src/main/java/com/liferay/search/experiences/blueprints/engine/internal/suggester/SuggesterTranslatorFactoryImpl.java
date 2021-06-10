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

package com.liferay.search.experiences.blueprints.engine.internal.suggester;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.search.experiences.blueprints.engine.spi.suggester.SuggesterTranslator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SuggesterTranslatorFactory.class)
public class SuggesterTranslatorFactoryImpl
	implements SuggesterTranslatorFactory {

	@Override
	public SuggesterTranslator getTranslator(String name)
		throws IllegalArgumentException {

		SuggesterTranslator suggesterTranslator = _suggesterTranslators.get(
			name);

		if (suggesterTranslator == null) {
			throw new IllegalArgumentException(
				"No registered translator " + name);
		}

		return suggesterTranslator;
	}

	@Override
	public String[] getTranslatorNames() {
		Set<String> set = _suggesterTranslators.keySet();

		return set.toArray(new String[0]);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void registerSuggesterBuilder(
		SuggesterTranslator suggesterTranslator,
		Map<String, Object> properties) {

		String name = (String)properties.get("name");

		if (Validator.isBlank(name)) {
			Class<?> clazz = suggesterTranslator.getClass();

			StringBundler sb = new StringBundler(3);

			sb.append("Unable to register translator ");
			sb.append(clazz.getName());
			sb.append(". Name property empty.");

			_log.error(sb.toString());

			return;
		}

		_suggesterTranslators.put(name, suggesterTranslator);
	}

	protected void unregisterSuggesterBuilder(
		SuggesterTranslator suggesterTranslator,
		Map<String, Object> properties) {

		String name = (String)properties.get("name");

		if (Validator.isBlank(name)) {
			return;
		}

		_suggesterTranslators.remove(name);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SuggesterTranslatorFactoryImpl.class);

	private volatile Map<String, SuggesterTranslator> _suggesterTranslators =
		new HashMap<>();

}