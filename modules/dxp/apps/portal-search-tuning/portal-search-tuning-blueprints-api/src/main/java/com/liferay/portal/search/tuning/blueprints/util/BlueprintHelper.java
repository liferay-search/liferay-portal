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

package com.liferay.portal.search.tuning.blueprints.util;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;

import java.util.List;
import java.util.Optional;

/**
 * @author Petteri Karttunen
 */
public interface BlueprintHelper {

	public Optional<JSONArray> getAggregationConfigurationOptional(
		JSONObject blueprintJsonObject);

	public Optional<JSONArray> getClauseConfigurationOptional(
		JSONObject blueprintJsonObject);

	public Optional<JSONArray> getDefaultSortConfigurationOptional(
		JSONObject blueprintJsonObject);

	public Optional<List<String>> getExcludedQueryContributorsOptional(
		JSONObject blueprintJsonObject);

	public Optional<List<String>> getExcludedQueryPostProcessorsOptional(
		JSONObject blueprintJsonObject);

	public Optional<JSONObject> getHighlightConfigurationOptional(
		JSONObject blueprintJsonObject);

	public Optional<String[]> getIndexNamesOptional(
		JSONObject blueprintJsonObject);

	public Optional<JSONObject> getKeywordIndexingConfigurationOptional(
		JSONObject blueprintJsonObject);

	public Optional<String> getKeywordParameterNameOptional(
		JSONObject blueprintJsonObject);

	public Optional<JSONArray> getKeywordSuggestersOptional(
		JSONObject blueprintJsonObject);

	public Optional<String> getPagingParameterNameOptional(
		JSONObject blueprintJsonObject);

	public Optional<JSONObject> getRequestParameterConfigurationOptional(
		JSONObject blueprintJsonObject);

	public int getSize(JSONObject blueprintJsonObject);

	public Optional<JSONArray> getSortParameterConfigurationOptional(
		JSONObject blueprintJsonObject);

	public Optional<JSONObject> getSpellCheckingConfigurationOptional(
		JSONObject blueprintJsonObject);

	public Optional<JSONArray> getSpellCheckingSuggestersOptional(
		JSONObject blueprintJsonObject);

}