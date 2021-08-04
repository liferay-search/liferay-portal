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

package com.liferay.search.experiences.blueprints.engine.internal.attributes.util;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.util.util.BlueprintValueUtil;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintsAttributeValuesHelper.class)
public class BlueprintsAttributeValuesHelper {

	public Optional<Boolean> getBooleanOptional(
		BlueprintsAttributes blueprintsAttributes, String key) {

		Optional<Object> valueOptional =
			blueprintsAttributes.getAttributeOptional(key);

		if (!valueOptional.isPresent()) {
			return Optional.empty();
		}

		return valueOptional.map(GetterUtil::getBoolean);
	}

	public Optional<Integer> getIntegerOptional(
		BlueprintsAttributes blueprintsAttributes, String key) {

		Optional<Object> valueOptional =
			blueprintsAttributes.getAttributeOptional(key);

		if (!valueOptional.isPresent()) {
			return Optional.empty();
		}

		return valueOptional.map(GetterUtil::getInteger);
	}

	public Optional<Long> getLongOptional(
		BlueprintsAttributes blueprintsAttributes, String key) {

		Optional<Object> valueOptional =
			blueprintsAttributes.getAttributeOptional(key);

		if (!valueOptional.isPresent()) {
			return Optional.empty();
		}

		return valueOptional.map(GetterUtil::getLong);
	}

	public Optional<String[]> getStringArrayOptional(
		BlueprintsAttributes blueprintsAttributes, String key) {

		Optional<Object> valueOptional =
			blueprintsAttributes.getAttributeOptional(key);

		if (!valueOptional.isPresent()) {
			return Optional.empty();
		}

		Object value = valueOptional.get();

		if (value instanceof String[]) {
			String[] arr = (String[])value;

			if (arr.length > 0) {
				return Optional.of(arr);
			}
		}

		return Optional.empty();
	}

	public Optional<String> getStringOptional(
		BlueprintsAttributes blueprintsAttributes, String key) {

		Optional<Object> optional = blueprintsAttributes.getAttributeOptional(
			key);

		if (!optional.isPresent()) {
			return Optional.empty();
		}

		return BlueprintValueUtil.toStringOptional(
			String.valueOf(optional.get()));
	}

}