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

package com.liferay.portal.search.tuning.blueprints.engine.internal.request.parameter.contributor;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.requestparameter.CommonRequestParameterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.DateParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "type=date",
	service = RequestParameterContributor.class
)
public class DateParameterContributor implements RequestParameterContributor {

	@Override
	public void contribute(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData,
		JSONObject configurationJsonObject) {

		if (!_validateConfiguration(
				searchParameterData, configurationJsonObject)) {

			return;
		}

		String parameterName = configurationJsonObject.getString(
			CommonRequestParameterConfigurationKeys.PARAMETER_NAME.
				getJsonKey());

		String dateString = ParamUtil.getString(
			httpServletRequest, parameterName);

		if (Validator.isBlank(dateString)) {
			return;
		}

		Date date = _getDate(
			httpServletRequest, configurationJsonObject, dateString);

		if (date != null) {
			searchParameterData.addParameter(
				new DateParameter(
					parameterName, "${request." + parameterName + "}", date));
		}
	}

	private Date _getDate(
		HttpServletRequest httpServletRequest,
		JSONObject configurationJsonObject, String dateString) {

		if (Validator.isBlank(dateString)) {
			return null;
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		String dateFormat = configurationJsonObject.getString(
			CommonRequestParameterConfigurationKeys.DATE_FORMAT.getJsonKey());

		try {
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
				dateFormat);

			LocalDate localDate = LocalDate.parse(
				dateString, dateTimeFormatter);

			TimeZone timeZone = themeDisplay.getTimeZone();

			GregorianCalendar calendar = GregorianCalendar.from(
				localDate.atStartOfDay(timeZone.toZoneId()));

			return calendar.getTime();
		}
		catch (Exception exception) {
			_log.error(
				String.format(
					"Cannot parse date from string '%s'", dateString,
					exception));
		}

		return null;
	}

	private boolean _validateConfiguration(
		SearchParameterData searchParameterData,
		JSONObject configurationJsonObject) {

		boolean valid = true;

		if (Validator.isNull(
				configurationJsonObject.getString(
					CommonRequestParameterConfigurationKeys.DATE_FORMAT.
						getJsonKey()))) {

			searchParameterData.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.undefined-date-format",
					null, null, configurationJsonObject,
					CommonRequestParameterConfigurationKeys.DATE_FORMAT.
						getJsonKey(),
					null));
			valid = false;

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Undefined date format [ " + configurationJsonObject +
						" ].");
			}
		}

		return valid;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DateParameterContributor.class);

}