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

package com.liferay.search.experiences.blueprints.engine.attributes;

import java.util.Optional;

/**
 * @author André de Oliveira
 */
public interface BlueprintsAttributeValuesHelper {

	public Optional<Boolean> getBooleanOptional(
		BlueprintsAttributes blueprintsAttributes, String key);

	public Optional<Double> getDoubleOptional(
		BlueprintsAttributes blueprintsAttributes, String key);

	public Optional<Float> getFloatOptional(
		BlueprintsAttributes blueprintsAttributes, String key);

	public Optional<Integer[]> getIntegerArrayOptional(
		BlueprintsAttributes blueprintsAttributes, String key);

	public Optional<Integer> getIntegerOptional(
		BlueprintsAttributes blueprintsAttributes, String key);

	public Optional<Long[]> getLongArrayOptional(
		BlueprintsAttributes blueprintsAttributes, String key);

	public Optional<Long> getLongOptional(
		BlueprintsAttributes blueprintsAttributes, String key);

	public Optional<String[]> getStringArrayOptional(
		BlueprintsAttributes blueprintsAttributes, String key);

	public Optional<String> getStringOptional(
		BlueprintsAttributes blueprintsAttributes, String key);

}