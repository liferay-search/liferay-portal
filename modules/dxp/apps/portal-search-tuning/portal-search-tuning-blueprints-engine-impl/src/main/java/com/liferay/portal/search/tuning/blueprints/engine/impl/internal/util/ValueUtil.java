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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Petteri Karttunen
 */
public class ValueUtil {

	public static String mapToString(Map<?, ?> map) {
		String s = map.keySet(
		).stream(
		).map(
			key -> key + "=" + map.get(key)
		).collect(
			Collectors.joining(", ", "[", "]")
		);

		return s;
	}

	public static Optional<Double> stringToDoubleOptional(String s) {
		if (Validator.isNull(s)) {
			return Optional.empty();
		}

		try {
			double value = Double.valueOf(s);

			return Optional.of(value);
		}
		catch (NumberFormatException nfe) {
			_log.error(nfe, nfe);
		}

		return Optional.empty();
	}

	public static Optional<Float> stringToFloatOptional(String s) {
		if (Validator.isNull(s)) {
			return Optional.empty();
		}

		try {
			float value = Float.valueOf(s);

			return Optional.of(value);
		}
		catch (NumberFormatException nfe) {
			_log.error(nfe, nfe);
		}

		return Optional.empty();
	}

	public static Optional<Integer> stringToIntegerOptional(String s) {
		if (Validator.isNull(s)) {
			return Optional.empty();
		}

		try {
			int value = Integer.valueOf(s);

			return Optional.of(value);
		}
		catch (NumberFormatException nfe) {
			_log.error(nfe, nfe);
		}

		return Optional.empty();
	}

	public static Optional<Long> stringToLongOptional(String s) {
		if (Validator.isNull(s)) {
			return Optional.empty();
		}

		try {
			long value = Long.valueOf(s);

			return Optional.of(value);
		}
		catch (NumberFormatException nfe) {
			_log.error(nfe, nfe);
		}

		return Optional.empty();
	}

	public static String stripHTML(String string, int length) throws Exception {
		if (Validator.isBlank(string)) {
			return string;
		}

		// Replace other than highlight tags.

		string = string.replaceAll("<liferay-hl>", "---LR-HL-START---");
		string = string.replaceAll("</liferay-hl>", "---LR-HL-STOP---");
		string = HtmlUtil.stripHtml(string);
		string = string.replaceAll("---LR-HL-START---", "<liferay-hl>");
		string = string.replaceAll("---LR-HL-STOP---", "</liferay-hl>");

		if ((length > -1) && (string.length() > length)) {
			String temp = string.substring(0, length);

			// Check that we are not breaking the HTML.

			if (temp.lastIndexOf("<") > temp.lastIndexOf(">")) {
				temp = string.substring(
					0, 1 + string.indexOf('>', temp.lastIndexOf('<')));
			}

			string = temp.concat("...");
		}

		return string;
	}

	public static Optional<String> toStringOptional(String s) {
		s = StringUtil.trim(s);

		if (Validator.isBlank(s)) {
			return Optional.empty();
		}

		return Optional.of(s);
	}

	private static final Log _log = LogFactoryUtil.getLog(ValueUtil.class);

}