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

package com.liferay.search.experiences.blueprints.internal.lookup;

import com.liferay.search.experiences.blueprints.Blueprint;

/**
 * @author André de Oliveira
 */
public class BlueprintImpl implements Blueprint {

	public BlueprintImpl(
		com.liferay.search.experiences.blueprints.model.Blueprint
			sxpBlueprint) {

		_blueprint = sxpBlueprint;
	}

	@Override
	public long getBlueprintId() {
		return _blueprint.getBlueprintId();
	}

	@Override
	public String getConfiguration() {
		return _blueprint.getConfiguration();
	}

	private final com.liferay.search.experiences.blueprints.model.Blueprint
		_blueprint;

}