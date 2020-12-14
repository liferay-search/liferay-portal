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

package com.liferay.portal.search.tuning.blueprints.util.internal.importer;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.search.tuning.blueprints.constants.BlueprintTypes;
import com.liferay.portal.search.tuning.blueprints.exception.BlueprintValidationException;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;
import com.liferay.portal.search.tuning.blueprints.util.importer.BlueprintImporter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.portlet.PortletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintImporter.class)
public class BlueprintImporterImpl implements BlueprintImporter {

	@Override
	public void importBlueprint(
			PortletRequest portletRequest, InputStream inputStream)
		throws PortalException, UncheckedIOException {

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
			_getRawText(inputStream));

		if (!_validate(jsonObject)) {
			throw new BlueprintValidationException("Invalid Blueprint syntax");
		}

		_addBlueprint(jsonObject, portletRequest);
	}

	private void _addBlueprint(
			JSONObject jsonObject, PortletRequest portletRequest)
		throws PortalException {

		int type = jsonObject.getInt("type");

		JSONObject payloadJSONObject = jsonObject.getJSONObject("payload");

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			Blueprint.class.getName(), portletRequest);

		if (type == BlueprintTypes.BLUEPRINT) {
			_addBlueprint(payloadJSONObject, serviceContext);
		}
		else if (type == BlueprintTypes.QUERY_FRAGMENT) {
			_addQueryFragment(payloadJSONObject, serviceContext);
		}
	}

	private void _addBlueprint(
			JSONObject payloadJSONObject, ServiceContext serviceContext)
		throws PortalException {

		Map<Locale, String> titleMap = _getTitleMap(payloadJSONObject);

		Map<Locale, String> descriptionMap = _getDescriptionMap(
			payloadJSONObject);

		String configuration = payloadJSONObject.getJSONObject(
			"configuration"
		).toJSONString();

		String selectedFragments = payloadJSONObject.getJSONObject(
			"selected_fragments"
		).toJSONString();

		_save(
			titleMap, descriptionMap, configuration, selectedFragments,
			BlueprintTypes.BLUEPRINT, serviceContext);
	}

	private void _addQueryFragment(
		JSONObject payloadJSONObject, ServiceContext serviceContext) {

		// TODO

		_log.error("Not implemented");
	}

	private Map<Locale, String> _getDescriptionMap(JSONObject jsonObject) {
		JSONObject descriptionJSONObject = jsonObject.getJSONObject(
			"description");

		if (descriptionJSONObject == null) {
			return null;
		}

		return _getLocalizationMap(descriptionJSONObject);
	}

	private Map<Locale, String> _getLocalizationMap(JSONObject jsonObject) {
		Map<Locale, String> map = new HashMap<>();

		Set<String> languageIds = jsonObject.keySet();

		Stream<String> stream = languageIds.stream();

		stream.forEach(
			s -> {
				if ((s != null) && (s.length() == 5) && s.contains("_")) {
					String[] arr = s.split("_");

					map.put(
						new Locale(arr[0], arr[1]), jsonObject.getString(s));
				}
			});

		return map;
	}

	private String _getRawText(InputStream inputStream)
		throws UncheckedIOException {

		BufferedReader bufferedReader = new BufferedReader(
			new InputStreamReader(inputStream, StandardCharsets.UTF_8));

		Stream<String> lines = bufferedReader.lines();

		return lines.collect(Collectors.joining(System.lineSeparator()));
	}

	private Map<Locale, String> _getTitleMap(JSONObject jsonObject) {
		JSONObject titleJSONObject = jsonObject.getJSONObject("title");

		if (titleJSONObject == null) {
			return null;
		}

		return _getLocalizationMap(titleJSONObject);
	}

	private void _save(
			Map<Locale, String> titleMap, Map<Locale, String> descriptionMap,
			String configuration, String selectedFragments, int type,
			ServiceContext serviceContext)
		throws PortalException {

		_blueprintService.addCompanyBlueprint(
			titleMap, descriptionMap, configuration, selectedFragments, type,
			serviceContext);
	}

	private boolean _validate(JSONObject jsonObject) {
		int type = jsonObject.getInt("type", 0);

		if (type == 0) {
			_log.error("Missing Blueprint type");

			return false;
		}

		return _validatePayload(jsonObject.getJSONObject("payload"), type);
	}

	private boolean _validateClauses(JSONObject payloadJSONObject) {
		JSONArray clausesJSONArray = payloadJSONObject.getJSONArray("clauses");

		if ((clausesJSONArray == null) || (clausesJSONArray.length() == 0)) {
			_log.error("Missing clauses object");

			return false;
		}

		return true;
	}

	private boolean _validateConfiguration(JSONObject payloadJSONObject) {
		if (!payloadJSONObject.has("configuration")) {
			_log.error("Missing configuration object");

			return false;
		}

		return true;
	}

	private boolean _validatePayload(JSONObject payloadJSONObject, int type) {
		if ((payloadJSONObject == null) || (payloadJSONObject.length() == 0)) {
			_log.error("Missing data payload");

			return false;
		}

		if (type == BlueprintTypes.BLUEPRINT) {
			if (_validateTitle(payloadJSONObject) &&
				_validateConfiguration(payloadJSONObject) &&
				_validateSelectedFragments(payloadJSONObject)) {

				return true;
			}

			return false;
		}
		else if (type == BlueprintTypes.QUERY_FRAGMENT) {
			if (_validateTitle(payloadJSONObject) &&
				_validateClauses(payloadJSONObject)) {

				// TODO: add support for fragments
				// https://issues.liferay.com/browse/LPS-114089

				return false;
			}

			return false;
		}

		_log.error("Unsupported import type " + type);

		return false;
	}

	private boolean _validateSelectedFragments(JSONObject payloadJSONObject) {
		if (!payloadJSONObject.has("selected_fragments")) {
			_log.error("Missing selected fragments object");

			return false;
		}

		return true;
	}

	private boolean _validateTitle(JSONObject payloadJSONObject) {
		if (!payloadJSONObject.has("title")) {
			_log.error("Missing title object");

			return false;
		}

		return true;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintImporterImpl.class);

	@Reference
	private BlueprintService _blueprintService;

}