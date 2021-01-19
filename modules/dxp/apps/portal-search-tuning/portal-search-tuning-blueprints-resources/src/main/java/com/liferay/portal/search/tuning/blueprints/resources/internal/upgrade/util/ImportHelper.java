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

package com.liferay.portal.search.tuning.blueprints.resources.internal.upgrade.util;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.util.importer.BlueprintImporter;

import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ImportHelper.class)
public class ImportHelper {

	public void importDefaultBlueprints() throws PortalException {
		importDefaultBlueprints(-1L);
	}

	public void importDefaultBlueprints(long companyId) throws PortalException {
		_importBlueprints(_listBlueprints(), companyId);
	}

	private Bundle _getBundle() {
		return FrameworkUtil.getBundle(getClass());
	}

	private String _getInstalledVersion(long companyId) throws PortalException {
		PortalPreferences portalPreferences = _getPortalPreferences(companyId);

		return portalPreferences.getValue(
			_getPreferenceNameSpace(companyId), _VERSION_PREFERENCE);
	}

	private JSONObject _getJSONObject(URL url) throws JSONException {
		return _jsonFactory.createJSONObject(
			StringUtil.read(getClass(), "/" + url.getPath()));
	}

	private PortalPreferences _getPortalPreferences(long companyId)
		throws PortalException {

		return PortletPreferencesFactoryUtil.getPortalPreferences(
			_userLocalService.getDefaultUserId(companyId), true);
	}

	private String _getPreferenceNameSpace(long companyId) {
		StringBundler sb = new StringBundler(2);

		sb.append("blueprint-resources-");
		sb.append(companyId);

		return sb.toString();
	}

	private void _importBlueprints(
			Enumeration<URL> urlEnumeration, long companyId)
		throws PortalException {

		if (_log.isInfoEnabled()) {
			_log.info("Importing default Blueprints");
		}

		if ((urlEnumeration == null) || !urlEnumeration.hasMoreElements()) {
			return;
		}

		while (urlEnumeration.hasMoreElements()) {
			URL url = urlEnumeration.nextElement();

			JSONObject jsonObject = _getJSONObject(url);

			if (companyId > 0) {
				_importToCompany(jsonObject, companyId);
			}
			else {
				_importToAllCompanies(jsonObject);
			}
		}
	}

	private void _importToAllCompanies(JSONObject jsonObject) {
		List<Company> companies = _companyLocalService.getCompanies();

		Stream<Company> stream = companies.stream();

		stream.forEach(
			company -> _importToCompany(jsonObject, company.getCompanyId()));
	}

	private void _importToCompany(JSONObject jsonObject, long companyId) {
		Bundle bundle = _getBundle();

		Version version = bundle.getVersion();

		try {
			if (_needsInstall(companyId, version)) {
				_blueprintImporter.importBlueprint(companyId, jsonObject);
				_setInstalled(companyId, version);
			}
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	private Enumeration<URL> _listBlueprints() {
		Bundle bundle = _getBundle();

		return bundle.findEntries(_BLUEPRINTS_PATH, "*.json", false);
	}

	private boolean _needsInstall(long companyId, Version version)
		throws PortalException {

		String installedVersion = _getInstalledVersion(companyId);

		if (Validator.isBlank(installedVersion)) {
			return true;
		}

		String[] arr = installedVersion.split("\\.");

		if (arr.length != 3) {
			return true;
		}

		if (GetterUtil.getInteger(arr[0]) < version.getMajor()) {
			return true;
		}

		if (GetterUtil.getInteger(arr[2]) < version.getMicro()) {
			return true;
		}

		return false;
	}

	private void _setInstalled(long companyId, Version version)
		throws PortalException {

		PortalPreferences portalPreferences = _getPortalPreferences(companyId);

		String nameSpace = _getPreferenceNameSpace(companyId);

		portalPreferences.setValue(
			nameSpace, _VERSION_PREFERENCE, version.toString());
	}

	private static final String _BLUEPRINTS_PATH =
		"/META-INF/search/blueprints";

	private static final String _VERSION_PREFERENCE = "version";

	private static final Log _log = LogFactoryUtil.getLog(ImportHelper.class);

	@Reference
	private BlueprintImporter _blueprintImporter;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Portal _portal;

	@Reference
	private UserLocalService _userLocalService;

}