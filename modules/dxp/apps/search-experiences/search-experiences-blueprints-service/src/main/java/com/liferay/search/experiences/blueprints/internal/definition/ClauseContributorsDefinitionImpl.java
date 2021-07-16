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
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author André de Oliveira
 */
public class ClauseContributorsDefinitionImpl
	implements ClauseContributorsDefinition {

	public static final boolean LPS_129052_NEW_PAYLOAD_READY = false;

	public ClauseContributorsDefinitionImpl(JSONObject jsonObject) {
		_jsonObject = jsonObject;
	}

	@Override
	public Collection<String> getExcludes() {
		return getClauseContributorIds("excludes", Collections::emptySet);
	}

	@Override
	public Collection<String> getIncludes() {
		return getClauseContributorIds(
			"includes",
			LPS_129052_NEW_PAYLOAD_READY ? Collections::emptySet :
				this::_getIncludesFromSubsections);
	}

	protected Collection<String> getClauseContributorIds(
		String key, Supplier<? extends Collection<String>> supplier) {

		Optional<JSONObject> optional = getJSONObjectOptional(key);

		return optional.map(
			this::getJSONObjectKeys
		).orElseGet(
			supplier
		);
	}

	protected Collection<String> getJSONObjectKeys(JSONObject jsonObject) {
		return new HashSet<>(jsonObject.keySet());
	}

	protected Optional<JSONObject> getJSONObjectOptional(String key) {
		return BlueprintJSONUtil.getJSONObjectOptional(
			_jsonObject, "JSONObject/" + key);
	}

	private void _addIncludesFromSubsection(
		Collection<String> collection, String key) {

		Optional<JSONObject> optional = getJSONObjectOptional(key);

		optional.ifPresent(
			jsonObject -> _addJSONObjectKeysWithValueTrue(
				collection, jsonObject));
	}

	private void _addJSONObjectKeysWithValueTrue(
		Collection<String> collection, JSONObject jsonObject) {

		for (String key : jsonObject.keySet()) {
			if (jsonObject.getBoolean(key)) {
				collection.add(key);
			}
		}
	}

	private Collection<String> _getIncludesFromSubsections() {
		List<String> sections = Arrays.asList(
			"KeywordQueryContributor", "ModelPrefilterContributor",
			"QueryPrefilterContributor");

		Set<String> set = new HashSet<>();

		sections.forEach(key -> _addIncludesFromSubsection(set, key));

		return set;
	}

	private final JSONObject _jsonObject;

}