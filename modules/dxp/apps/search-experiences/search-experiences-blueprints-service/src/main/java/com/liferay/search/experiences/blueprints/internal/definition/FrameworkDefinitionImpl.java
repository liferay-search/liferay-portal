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

package com.liferay.search.experiences.blueprints.internal.definition;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.search.experiences.blueprints.definition.ClauseContributorsDefinition;
import com.liferay.search.experiences.blueprints.definition.FrameworkDefinition;
import com.liferay.search.experiences.blueprints.util.util.BlueprintJSONUtil;

import java.util.Optional;

/**
 * @author André de Oliveira
 */
public class FrameworkDefinitionImpl implements FrameworkDefinition {

	public FrameworkDefinitionImpl(JSONObject jsonObject) {
		_jsonObject = _getJSONObject(jsonObject);
	}

	@Override
	public Optional<ClauseContributorsDefinition>
		getClauseContributorsDefinitionOptional() {

		Optional<JSONObject> optional = BlueprintJSONUtil.getJSONObjectOptional(
			_jsonObject, "JSONObject/clause_contributors");

		return optional.map(ClauseContributorsDefinitionImpl::new);
	}

	@Override
	public String[] getSearchableAssetTypes() {
		Optional<JSONArray> optional = BlueprintJSONUtil.getJSONArrayOptional(
			_jsonObject, "JSONArray/searchable_asset_types");

		return optional.map(
			JSONUtil::toStringArray
		).orElse(
			new String[0]
		);
	}

	@Override
	public boolean isSuppressIndexerClauses() {
		return !_jsonObject.getBoolean("apply_indexer_clauses", true);
	}

	private JSONObject _getJSONObject(JSONObject jsonObject) {
		if (jsonObject != null) {
			return jsonObject;
		}

		return JSONUtil.put(
			"apply_indexer_clauses", true
		).put(
			"searchable_asset_types",
			JSONUtil.putAll(
				"com.liferay.journal.model.JournalArticle",
				"com.liferay.document.library.kernel.model.DLFileEntry")
		);
	}

	private final JSONObject _jsonObject;

}