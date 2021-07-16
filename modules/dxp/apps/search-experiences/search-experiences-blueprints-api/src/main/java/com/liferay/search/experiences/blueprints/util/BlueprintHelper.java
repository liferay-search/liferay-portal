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

package com.liferay.search.experiences.blueprints.util;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.search.experiences.blueprints.model.Blueprint;

import java.util.Optional;

/**
 * @author Petteri Karttunen
 */
public interface BlueprintHelper {

	public Optional<JSONObject> getAdvancedConfigurationOptional(
		Blueprint blueprint);

	public Optional<JSONObject> getAggsConfigurationOptional(
		Blueprint blueprint);

	public Optional<JSONArray> getCustomParameterConfigurationOptional(
		Blueprint blueprint);

	public int getDefaultSize(Blueprint blueprint);

	public Optional<JSONArray> getDefaultSortConfigurationOptional(
		Blueprint blueprint);

	public Optional<JSONObject> getHighlightConfigurationOptional(
		Blueprint blueprint);

	public Optional<JSONArray> getJSONArrayConfigurationOptional(
		Blueprint blueprint, String... paths);

	public Optional<JSONObject> getJSONObjectConfigurationOptional(
		Blueprint blueprint, String... paths);

	public String getKeywordsParameterName(Blueprint blueprint);

	public String getPageParameterName(Blueprint blueprint);

	public Optional<JSONObject> getParameterConfigurationOptional(
		Blueprint blueprint);

	public Optional<JSONArray> getQueryConfigurationOptional(
		Blueprint blueprint);

	public String getSizeParameterName(Blueprint blueprint);

	public Optional<JSONObject> getSortConfigurationOptional(
		Blueprint blueprint);

	public Optional<JSONObject> getSortParameterConfigurationOptional(
		Blueprint blueprint);

	public Optional<JSONObject> getSuggestConfigurationOptional(
		Blueprint blueprint);

}