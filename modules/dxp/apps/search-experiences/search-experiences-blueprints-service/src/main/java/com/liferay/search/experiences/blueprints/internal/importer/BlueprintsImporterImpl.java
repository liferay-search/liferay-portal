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

package com.liferay.search.experiences.blueprints.internal.importer;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.SecureRandomUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.search.experiences.blueprints.exception.BlueprintValidationException;
import com.liferay.search.experiences.blueprints.exception.ElementValidationException;
import com.liferay.search.experiences.blueprints.importer.BlueprintsImporter;
import com.liferay.search.experiences.blueprints.service.BlueprintLocalService;
import com.liferay.search.experiences.blueprints.service.ElementLocalService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintsImporter.class)
public class BlueprintsImporterImpl implements BlueprintsImporter {

	@Override
	public void importBlueprint(
			long companyId, long groupId, long userId, JSONObject jsonObject)
		throws PortalException {

		if (!_validateBlueprint(jsonObject)) {
			throw new BlueprintValidationException("Invalid Blueprint syntax");
		}

		_addBlueprint(
			jsonObject,
			_createServiceContext(companyId, groupId, userId, false));
	}

	@Override
	public void importElement(
			long companyId, long groupId, long userId, JSONObject jsonObject,
			boolean readOnly)
		throws PortalException {

		if (!_validateElement(jsonObject)) {
			throw new ElementValidationException("Invalid Element syntax");
		}

		_addElement(
			jsonObject,
			_createServiceContext(companyId, groupId, userId, readOnly));
	}

	private void _addBlueprint(
			JSONObject jsonObject, ServiceContext serviceContext)
		throws PortalException {

		JSONObject payloadJSONObject = jsonObject.getJSONObject(
			"blueprint-payload");

		_saveBlueprint(
			_getTitleMap(payloadJSONObject),
			_getDescriptionMap(payloadJSONObject),
			_getConfiguration(payloadJSONObject),
			_getSelectedElements(payloadJSONObject), serviceContext);
	}

	private void _addElement(
			JSONObject jsonObject, ServiceContext serviceContext)
		throws PortalException {

		JSONObject payloadJSONObject = jsonObject.getJSONObject(
			"element-payload");

		_saveElement(
			_getTitleMap(payloadJSONObject),
			_getDescriptionMap(payloadJSONObject),
			_getConfiguration(payloadJSONObject), jsonObject.getInt("type"),
			serviceContext);
	}

	private ServiceContext _createServiceContext(
			long companyId, long groupId, long userId, boolean readOnly)
		throws PortalException {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);
		serviceContext.setAttribute("read-only", readOnly);
		serviceContext.setCompanyId(companyId);
		serviceContext.setScopeGroupId(groupId);
		serviceContext.setUserId(userId);
		serviceContext.setUuid(_createUUID());

		return serviceContext;
	}

	private String _createUUID() {
		UUID uuid = new UUID(
			SecureRandomUtil.nextLong(), SecureRandomUtil.nextLong());

		return uuid.toString();
	}

	private String _getConfiguration(JSONObject jsonObject) {
		JSONObject configurationJSONObject = jsonObject.getJSONObject(
			"configuration");

		return configurationJSONObject.toJSONString();
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

	private String _getSelectedElements(JSONObject jsonObject) {
		JSONObject configurationJSONObject = jsonObject.getJSONObject(
			"selectedElements");

		return configurationJSONObject.toJSONString();
	}

	private Map<Locale, String> _getTitleMap(JSONObject jsonObject) {
		JSONObject titleJSONObject = jsonObject.getJSONObject("title");

		if (titleJSONObject == null) {
			return null;
		}

		return _getLocalizationMap(titleJSONObject);
	}

	private void _saveBlueprint(
			Map<Locale, String> titleMap, Map<Locale, String> descriptionMap,
			String configuration, String selectedElements,
			ServiceContext serviceContext)
		throws PortalException {

		_blueprintLocalService.addBlueprint(
			serviceContext.getUserId(), serviceContext.getScopeGroupId(),
			titleMap, descriptionMap, configuration, selectedElements,
			serviceContext);
	}

	private void _saveElement(
			Map<Locale, String> titleMap, Map<Locale, String> descriptionMap,
			String configuration, int type, ServiceContext serviceContext)
		throws PortalException {

		_elementLocalService.addElement(
			serviceContext.getUserId(), serviceContext.getScopeGroupId(),
			titleMap, descriptionMap, configuration, type, serviceContext);
	}

	private boolean _validateBlueprint(JSONObject jsonObject) {
		JSONObject payloadJSONObject = jsonObject.getJSONObject(
			"blueprint-payload");

		if (payloadJSONObject == null) {
			_log.error("Blueprint payload not found");
		}

		if (_validateTitle(payloadJSONObject) &&
			_validateConfiguration(payloadJSONObject) &&
			_validateSelectedElements(payloadJSONObject)) {

			return true;
		}

		return false;
	}

	private boolean _validateConfiguration(JSONObject payloadJSONObject) {
		if (!payloadJSONObject.has("configuration")) {
			_log.error("Missing configuration object");

			return false;
		}

		return true;
	}

	private boolean _validateElement(JSONObject jsonObject) {
		JSONObject payloadJSONObject = jsonObject.getJSONObject(
			"element-payload");

		if (payloadJSONObject == null) {
			_log.error("Element payload not found");
		}

		if (!jsonObject.has("type")) {
			_log.error("Missing element type");

			return false;
		}

		if (_validateTitle(payloadJSONObject) &&
			_validateConfiguration(payloadJSONObject)) {

			return true;
		}

		return false;
	}

	private boolean _validateSelectedElements(JSONObject payloadJSONObject) {
		if (!payloadJSONObject.has("selectedElements")) {
			_log.error("Missing selected element object");

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
		BlueprintsImporterImpl.class);

	@Reference
	private BlueprintLocalService _blueprintLocalService;

	@Reference
	private ElementLocalService _elementLocalService;

	@Reference
	private JSONFactory _jsonFactory;

}