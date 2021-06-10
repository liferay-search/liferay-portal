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

package com.liferay.search.experiences.blueprints.resources.internal.util;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.search.experiences.blueprints.importer.BlueprintsImporter;

import java.net.URL;

import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ImportHelper.class)
public class ImportHelper {

	public void importDefaultResources(
		long companyId, long groupId, long userId) {

		if (_log.isInfoEnabled()) {
			_log.info("Importing default Blueprints and Elements");
		}

		_processBlueprintResources(companyId, groupId, userId);

		_processElementResources(companyId, groupId, userId);

		if (_log.isInfoEnabled()) {
			_log.info("Import done");
		}
	}

	private Bundle _getBundle() {
		return FrameworkUtil.getBundle(getClass());
	}

	private JSONObject _getJSONObject(URL url) throws JSONException {
		return _jsonFactory.createJSONObject(
			StringUtil.read(getClass(), "/" + url.getPath()));
	}

	private void _importBlueprint(
		long companyId, long groupId, long userId, JSONObject jsonObject) {

		try {
			_blueprintsImporter.importBlueprint(
				companyId, groupId, userId, jsonObject);
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	private void _importElement(
		long companyId, long groupId, long userId, JSONObject jsonObject) {

		try {
			_blueprintsImporter.importElement(
				companyId, groupId, userId, jsonObject, true);
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	private Enumeration<URL> _listBlueprints() {
		Bundle bundle = _getBundle();

		return bundle.findEntries(_BLUEPRINTS_PATH, "*.json", false);
	}

	private Enumeration<URL> _listElements() {
		Bundle bundle = _getBundle();

		return bundle.findEntries(_ELEMENTS_PATH, "*.json", false);
	}

	private void _processBlueprintResources(
		long companyId, long groupId, long userId) {

		Enumeration<URL> urlEnumeration = _listBlueprints();

		if ((urlEnumeration == null) || !urlEnumeration.hasMoreElements()) {
			return;
		}

		try {
			while (urlEnumeration.hasMoreElements()) {
				URL url = urlEnumeration.nextElement();

				_importBlueprint(
					companyId, groupId, userId, _getJSONObject(url));
			}
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);
		}
	}

	private void _processElementResources(
		long companyId, long groupId, long userId) {

		Enumeration<URL> urlEnumeration = _listElements();

		if ((urlEnumeration == null) || !urlEnumeration.hasMoreElements()) {
			return;
		}

		try {
			while (urlEnumeration.hasMoreElements()) {
				URL url = urlEnumeration.nextElement();

				_importElement(companyId, groupId, userId, _getJSONObject(url));
			}
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);
		}
	}

	private static final String _BLUEPRINTS_PATH =
		"/META-INF/search/blueprints";

	private static final String _ELEMENTS_PATH = "/META-INF/search/elements";

	private static final Log _log = LogFactoryUtil.getLog(ImportHelper.class);

	@Reference
	private BlueprintsImporter _blueprintsImporter;

	@Reference
	private JSONFactory _jsonFactory;

}