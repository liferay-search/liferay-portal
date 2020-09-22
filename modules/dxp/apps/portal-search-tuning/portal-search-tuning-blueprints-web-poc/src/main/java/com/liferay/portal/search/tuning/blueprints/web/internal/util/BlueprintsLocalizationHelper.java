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

package com.liferay.portal.search.tuning.blueprints.web.internal.util;

import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.AggregateResourceBundle;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.BlueprintsWebPortletKeys;

import java.util.Locale;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = "javax.portlet.name=" + BlueprintsWebPortletKeys.BLUEPRINTS_WEB,
	service = BlueprintsLocalizationHelper.class
)
public class BlueprintsLocalizationHelper {

	public String get(Locale locale, String key) {
		return _language.get(_getResourceBundle(locale), key);
	}

	private ResourceBundle _getCommonResourceBundle(Locale locale) {
		ResourceBundle classResourceBundle = ResourceBundleUtil.getBundle(
			locale, "com.liferay.portal.search.tuning.blueprints.lang");

		return new AggregateResourceBundle(
			classResourceBundle, _portal.getResourceBundle(locale));
	}

	private ResourceBundle _getResourceBundle(Locale locale) {
		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			"content.Language", locale, getClass());

		return new AggregateResourceBundle(
			resourceBundle, _getCommonResourceBundle(locale));
	}

	@Reference
	private Language _language;

	@Reference
	private Portal _portal;

}