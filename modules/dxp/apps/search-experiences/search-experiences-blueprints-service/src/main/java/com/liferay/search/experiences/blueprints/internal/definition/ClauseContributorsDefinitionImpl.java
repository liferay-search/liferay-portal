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

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.search.experiences.blueprints.definition.ClauseContributorsDefinition;
import com.liferay.search.experiences.blueprints.util.util.BlueprintJSONUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * @author André de Oliveira
 */
public class ClauseContributorsDefinitionImpl
	implements ClauseContributorsDefinition {

	public ClauseContributorsDefinitionImpl(JSONObject jsonObject) {
		_jsonObject = jsonObject;
	}

	@Override
	public Collection<String> getExcludes() {
		return Collections.emptySet();
	}

	@Override
	public Collection<String> getIncludes() {
		HashSet<String> includes = new HashSet<>();

		List<String> sections = Arrays.asList(
			"KeywordQueryContributor", "ModelPrefilterContributor",
			"QueryPrefilterContributor");

		sections.forEach(section -> addIncludes(section, includes));

		return includes;
	}

	protected void addIncludes(String section, HashSet<String> includes) {
		Optional<JSONObject> optional = BlueprintJSONUtil.getJSONObjectOptional(
			_jsonObject, "JSONObject/" + section);

		optional.ifPresent(
			jsonObject -> {
				for (String key : jsonObject.keySet()) {
					if (jsonObject.getBoolean(key)) {
						includes.add(key);
					}
				}
			});
	}

	private final JSONObject _jsonObject;

}