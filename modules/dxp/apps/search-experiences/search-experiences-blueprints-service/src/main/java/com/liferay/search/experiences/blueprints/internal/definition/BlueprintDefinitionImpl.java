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

import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.search.experiences.blueprints.definition.BlueprintDefinition;
import com.liferay.search.experiences.blueprints.definition.FrameworkDefinition;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.util.util.BlueprintJSONUtil;

import java.util.Optional;

/**
 * @author André de Oliveira
 */
public class BlueprintDefinitionImpl implements BlueprintDefinition {

	public BlueprintDefinitionImpl(
		Blueprint blueprint, JSONFactory jsonFactory) {

		_jsonFactory = jsonFactory;

		_jsonObject = _createJSONObject(blueprint.getConfiguration());
	}

	@Override
	public FrameworkDefinition getFrameworkDefinition(
		BlueprintDefinition blueprintDefinition) {

		Optional<JSONObject> optional = BlueprintJSONUtil.getJSONObjectOptional(
			_jsonObject, "JSONObject/framework_configuration");

		return new FrameworkDefinitionImpl(optional.orElse(null));
	}

	private JSONObject _createJSONObject(String json) {
		try {
			return _jsonFactory.createJSONObject(json);
		}
		catch (JSONException jsonException) {
			throw new RuntimeException(jsonException);
		}
	}

	private final JSONFactory _jsonFactory;
	private final JSONObject _jsonObject;

}