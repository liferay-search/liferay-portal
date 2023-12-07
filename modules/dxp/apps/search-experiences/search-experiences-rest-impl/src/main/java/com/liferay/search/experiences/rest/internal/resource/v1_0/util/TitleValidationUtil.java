/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.rest.internal.resource.v1_0.util;

import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.search.experiences.exception.SXPBlueprintTitleException;

import java.util.Locale;
import java.util.Map;

/**
 * @author Gustavo Lima
 */
public class TitleValidationUtil {

	public static void validateTitleI18n(Map<String, String> titleI18n)
		throws Exception {

		if (Validator.isBlank(titleI18n.get(
				LocaleUtil.getDefault(
				).toString())) &&
			Validator.isBlank(titleI18n.get(
				LocaleUtil.toBCP47LanguageId(LocaleUtil.getDefault())))) {

			throw new SXPBlueprintTitleException(
				"The title for the default locale " +
					LocaleUtil.toLanguageId(LocaleUtil.getDefault()) +
						" cannot be blank");
		}
	}

	public static void validateTitleMap(Map<Locale, String> titleMap)
		throws Exception {

		if (Validator.isBlank(titleMap.get(LocaleUtil.getDefault()))) {
			throw new SXPBlueprintTitleException(
				"The title for the default locale " +
					LocaleUtil.toLanguageId(LocaleUtil.getDefault()) +
						" cannot be blank");
		}
	}

}