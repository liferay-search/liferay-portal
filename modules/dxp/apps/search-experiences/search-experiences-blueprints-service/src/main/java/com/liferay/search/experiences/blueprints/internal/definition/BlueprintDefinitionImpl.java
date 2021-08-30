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

import com.liferay.search.experiences.blueprints.definition.AdvancedDefinition;
import com.liferay.search.experiences.blueprints.definition.BlueprintDefinition;
import com.liferay.search.experiences.blueprints.definition.FrameworkDefinition;
import com.liferay.search.experiences.blueprints.model.Blueprint;

/**
 * @author André de Oliveira
 */
public class BlueprintDefinitionImpl implements BlueprintDefinition {

	public BlueprintDefinitionImpl(Blueprint blueprint) {
		_blueprintDefinitionDTO = _getBlueprintDefinitionDTO(blueprint);
	}

	@Override
	public AdvancedDefinition getAdvancedDefinition() {
		return new AdvancedDefinitionImpl(
			_blueprintDefinitionDTO.advancedDefinitionDTO);
	}

	@Override
	public FrameworkDefinition getFrameworkDefinition() {
		return new FrameworkDefinitionImpl(
			_blueprintDefinitionDTO.frameworkDefinitionDTO);
	}

	private BlueprintDefinitionDTO _getBlueprintDefinitionDTO(
		Blueprint blueprint) {

		BlueprintDefinitionDTOReader blueprintDefinitionDTOReader =
			new BlueprintDefinitionDTOReader();

		return blueprintDefinitionDTOReader.read(blueprint.getConfiguration());
	}

	private final BlueprintDefinitionDTO _blueprintDefinitionDTO;

}