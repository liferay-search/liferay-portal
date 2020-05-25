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

package com.liferay.portal.search.tuning.gsearch.configuration.validator;

import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.gsearch.configuration.exception.SearchConfigurationValidationException;
import com.liferay.portal.search.tuning.gsearch.configuration.validation.SearchConfigurationValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SearchConfigurationValidator.class)
public class SearchConfigurationValidatorImpl
	implements SearchConfigurationValidator {

	public boolean isConfigurationValid(
			Map<Locale, String> titleMap, Map<Locale, String> descriptionMap,
			String configuration, List<String> errors)
		throws SearchConfigurationValidationException {

		boolean result = true;

		result &= isSearchConfigurationValid(configuration, errors);
		result &= isDescriptionValid(descriptionMap, errors);
		result &= isTitleValid(titleMap, errors);

		return result;
	}

	@Override
	public void validate(
			Map<Locale, String> titleMap, Map<Locale, String> descriptionMap,
			String configuration)
		throws SearchConfigurationValidationException {

		List<String> errors = new ArrayList<>();

		if (!isConfigurationValid(
				titleMap, descriptionMap, configuration, errors)) {

			throw new SearchConfigurationValidationException(errors);
		}
	}

	private boolean isDescriptionValid(
		final Map<Locale, String> descriptionMap, final List<String> errors) {

		boolean result = true;

		if (MapUtil.isEmpty(descriptionMap)) {
			errors.add("descriptionEmpty");
			result = false;
		}
		else {
			Locale defaultLocale = LocaleUtil.getSiteDefault();

			if (Validator.isBlank(descriptionMap.get(defaultLocale))) {
				errors.add("defaultLocaleDescriptionEmpty");
				result = false;
			}
		}

		return result;
	}

	private boolean isSearchConfigurationValid(
		String configuration, List<String> errors) {

		// TODO Auto-generated method stub

		return true;
	}

	private boolean isTitleValid(
		final Map<Locale, String> titleMap, final List<String> errors) {

		boolean result = true;

		if (MapUtil.isEmpty(titleMap)) {
			errors.add("titleEmpty");
			result = false;
		}
		else {
			Locale defaultLocale = LocaleUtil.getSiteDefault();

			if (Validator.isBlank(titleMap.get(defaultLocale))) {
				errors.add("defaultLocaleTitleEmpty");
				result = false;
			}
		}

		return result;
	}

}