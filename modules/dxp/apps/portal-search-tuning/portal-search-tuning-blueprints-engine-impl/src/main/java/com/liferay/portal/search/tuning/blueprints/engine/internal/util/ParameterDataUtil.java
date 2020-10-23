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

package com.liferay.portal.search.tuning.blueprints.engine.internal.util;

import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Petteri Karttunen
 */
public class ParameterDataUtil {

	public static Long getLongValueByName(
		ParameterData parameterData, String name) {

		Optional<Object> optional = getValueByNameOptional(parameterData, name);

		if (!optional.isPresent()) {
			return null;
		}

		return (Long)optional.get();
	}

	public static String getStringValueByName(
		ParameterData parameterData, String name) {

		Optional<Object> optional = getValueByNameOptional(parameterData, name);

		if (!optional.isPresent()) {
			return null;
		}

		return (String)optional.get();
	}

	public static Optional<Object> getValueByNameOptional(
		ParameterData parameterData, String name) {

		List<Parameter> parameters = parameterData.getParameters();

		Stream<Parameter> stream = parameters.stream();

		Optional<Parameter> parameterOptional = stream.filter(
			p -> name.equals(p.getName())
		).findFirst();

		if (parameterOptional.isPresent()) {
			Parameter parameter = parameterOptional.get();

			return Optional.of(parameter.getValue());
		}

		return Optional.empty();
	}

}