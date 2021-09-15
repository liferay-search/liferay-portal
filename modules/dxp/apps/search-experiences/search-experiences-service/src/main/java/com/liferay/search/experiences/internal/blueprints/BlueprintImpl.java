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

package com.liferay.search.experiences.internal.blueprints;

import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.model.SXPBlueprint;

/**
 * @author André de Oliveira
 */
public class BlueprintImpl implements Blueprint {

	public BlueprintImpl(SXPBlueprint sxpBlueprint) {
		_sxpBlueprint = sxpBlueprint;
	}

	@Override
	public long getBlueprintId() {
		return _sxpBlueprint.getSXPBlueprintId();
	}

	@Override
	public String getConfiguration() {
		return _sxpBlueprint.getConfigurationJSON();
	}

	private final SXPBlueprint _sxpBlueprint;

}