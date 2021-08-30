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
import com.liferay.search.experiences.blueprints.engine.internal.util.BlueprintValueUtil;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

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

	public Optional<Double> getDoubleOptional(
		BlueprintsAttributes blueprintsAttributes, String key) {

		Optional<Object> valueOptional =
			blueprintsAttributes.getAttributeOptional(key);

		if (!valueOptional.isPresent()) {
			return Optional.empty();
		}

		return valueOptional.map(GetterUtil::getDouble);
	}

	public Optional<Float> getFloatOptional(
		BlueprintsAttributes blueprintsAttributes, String key) {

		Optional<Object> valueOptional =
			blueprintsAttributes.getAttributeOptional(key);

		if (!valueOptional.isPresent()) {
			return Optional.empty();
		}

		return valueOptional.map(GetterUtil::getFloat);
	}

	public Optional<Integer[]> getIntegerArrayOptional(
		BlueprintsAttributes blueprintsAttributes, String key) {

		Optional<Object> valueOptional =
			blueprintsAttributes.getAttributeOptional(key);

		if (!valueOptional.isPresent()) {
			return Optional.empty();
		}

		Object value = valueOptional.get();

		if (value instanceof Integer[]) {
			Integer[] arr = (Integer[])value;

			if (arr.length > 0) {
				return Optional.of(arr);
			}
		}

		if (value instanceof int[]) {
			int[] arr = (int[])value;

			if (arr.length > 0) {
				IntStream intStream = Arrays.stream(arr);

				Integer[] boxedArray = intStream.boxed(
				).toArray(
					Integer[]::new
				);

				return Optional.of(boxedArray);
			}
		}

		return Optional.empty();
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

	public Optional<Long[]> getLongArrayOptional(
		BlueprintsAttributes blueprintsAttributes, String key) {

		Optional<Object> valueOptional =
			blueprintsAttributes.getAttributeOptional(key);

		if (!valueOptional.isPresent()) {
			return Optional.empty();
		}

		Object value = valueOptional.get();

		if (value instanceof Long[]) {
			Long[] arr = (Long[])value;

			if (arr.length > 0) {
				return Optional.of(arr);
			}
		}

		if (value instanceof long[]) {
			long[] arr = (long[])value;

			if (arr.length > 0) {
				LongStream longStream = Arrays.stream(arr);

				Long[] boxedArray = longStream.boxed(
				).toArray(
					Long[]::new
				);

				return Optional.of(boxedArray);
			}
		}

		return Optional.empty();
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