/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.settings;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.spi.index.configuration.contributor.helper.SettingsHelper;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * @author Gustavo Lima
 */
public class SettingsHelperImpl implements SettingsHelper {

	public String get(String key) {
		return _settings.get(key);
	}

	public Map<String, String> getSettings() {
		return _settings;
	}

	@Override
	public void loadFromSource(String source) {
	}

	@Override
	public void put(String key, String value) {
		if (!StringUtils.isBlank(value)) {
			_settings.put(key, value);
		}
	}

	public void putAll(JSONObject jsonObject) {
		for (String key : jsonObject.keySet()) {
			_settings.put(key, jsonObject.getString(key));
		}
	}

	private final Map<String, String> _settings = new HashMap<>();

}