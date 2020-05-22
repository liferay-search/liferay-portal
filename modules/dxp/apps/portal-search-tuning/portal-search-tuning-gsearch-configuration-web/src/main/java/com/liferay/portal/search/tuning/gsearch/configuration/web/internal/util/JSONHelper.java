package com.liferay.portal.search.tuning.gsearch.configuration.web.internal.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SearchConfiguration;

public class JSONHelper {

	public static String[] getConfigurationSection(
			SearchConfiguration searchConfiguration, String key)
		throws JSONException {

		if (searchConfiguration == null) {
			return new String[] {StringPool.BLANK};
		}

		String configuration = searchConfiguration.getConfiguration();

		if (Validator.isBlank(configuration)) {
			return new String[] {StringPool.BLANK};
		}

		JSONObject configurationJSON = JSONFactoryUtil.createJSONObject(
			configuration);

		if (configurationJSON == null) {
			return new String[] {StringPool.BLANK};
		}

		JSONArray section = configurationJSON.getJSONArray(key);

		String[] array = new String[section.length()];

		for (int i = 0; i < section.length(); i++) {
			array[i] = section.getString(i);
		}

		return array;
	}

}