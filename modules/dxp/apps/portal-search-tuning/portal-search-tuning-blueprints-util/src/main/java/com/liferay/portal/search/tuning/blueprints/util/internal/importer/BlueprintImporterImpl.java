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
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.SecureRandomUtil;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.LocaleThreadLocal;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.tuning.blueprints.constants.BlueprintTypes;
import com.liferay.portal.search.tuning.blueprints.exception.BlueprintValidationException;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintLocalService;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;
import com.liferay.portal.search.tuning.blueprints.util.importer.BlueprintImporter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
	public void importBlueprint(long companyId, JSONObject jsonObject)
		throws PortalException {

		if (!_validate(jsonObject)) {
			throw new BlueprintValidationException("Invalid Blueprint syntax");
		}

		_add(jsonObject, _createServiceContext(companyId), true);
	}

	@Override
	public void importBlueprint(
			PortletRequest portletRequest, InputStream inputStream)
		throws IOException, PortalException {

		JSONObject jsonObject = _jsonFactory.createJSONObject(
			_getRawText(inputStream));

		if (!_validate(jsonObject)) {
			throw new BlueprintValidationException("Invalid Blueprint syntax");
		}

		_add(
			jsonObject,
			ServiceContextFactory.getInstance(
				Blueprint.class.getName(), portletRequest),
			false);
	}

	private void _add(
			JSONObject jsonObject, ServiceContext serviceContext,
			boolean privileged)
		throws PortalException {

		int type = jsonObject.getInt("type");

		JSONObject payloadJSONObject = jsonObject.getJSONObject("payload");

		if (type == BlueprintTypes.BLUEPRINT) {
			_addBlueprint(payloadJSONObject, serviceContext, privileged);
		}
		else if (type == BlueprintTypes.QUERY_FRAGMENT) {

			// TODO: https://issues.liferay.com/browse/LPS-114089

			_log.error("Not implemented");
		}
	}

	private void _addBlueprint(
			JSONObject payloadJSONObject, ServiceContext serviceContext,
			boolean privileged)
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
			BlueprintTypes.BLUEPRINT, serviceContext, privileged);
	}

	private ServiceContext _createServiceContext(long companyId)
		throws PortalException {

		User defaultUser = _userLocalService.getDefaultUser(companyId);

		PermissionThreadLocal.setPermissionChecker(
			_permissionCheckerFactory.create(defaultUser));

		LocaleThreadLocal.setDefaultLocale(
			LocaleUtil.fromLanguageId(defaultUser.getLanguageId()));

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);
		serviceContext.setCompanyId(companyId);
		serviceContext.setScopeGroupId(_getGlobalGroupId(companyId));
		serviceContext.setUuid(_createUUID());
		serviceContext.setUserId(defaultUser.getUserId());

		return serviceContext;
	}

	private String _createUUID() {
		UUID uuid = new UUID(
			SecureRandomUtil.nextLong(), SecureRandomUtil.nextLong());

		return uuid.toString();
	}

	private Map<Locale, String> _getDescriptionMap(JSONObject jsonObject) {
		JSONObject descriptionJSONObject = jsonObject.getJSONObject(
			"description");

		if (descriptionJSONObject == null) {
			return null;
		}

		return _getLocalizationMap(descriptionJSONObject);
	}

	private long _getGlobalGroupId(long companyId) throws PortalException {
		Group group = _groupLocalService.getCompanyGroup(companyId);

		return group.getGroupId();
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

	private String _getRawText(InputStream inputStream) throws IOException {
		return StringUtil.read(inputStream);
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
			ServiceContext serviceContext, boolean privileged)
		throws PortalException {

		if (privileged) {
			_blueprintLocalService.addBlueprint(
				serviceContext.getUserId(), serviceContext.getScopeGroupId(),
				titleMap, descriptionMap, configuration, selectedFragments,
				BlueprintTypes.BLUEPRINT, serviceContext);
		}
		else {
			_blueprintService.addCompanyBlueprint(
				titleMap, descriptionMap, configuration, selectedFragments,
				type, serviceContext);
		}
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

				// TODO: https://issues.liferay.com/browse/LPS-114089

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
	private BlueprintLocalService _blueprintLocalService;

	@Reference
	private BlueprintService _blueprintService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private PermissionCheckerFactory _permissionCheckerFactory;

	@Reference
	private Portal _portal;

	@Reference
	private UserLocalService _userLocalService;

}