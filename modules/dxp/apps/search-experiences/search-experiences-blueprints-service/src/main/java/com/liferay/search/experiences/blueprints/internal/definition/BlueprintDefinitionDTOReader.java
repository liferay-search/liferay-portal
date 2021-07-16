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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author André de Oliveira
 */
public class BlueprintDefinitionDTOReader {

	public BlueprintDefinitionDTO read(String json) {
		ObjectMapper m = new ObjectMapper();

		try {
			return m.readValue(json, BlueprintDefinitionDTO.class);
		}
		catch (JsonMappingException jsonMappingException) {
			throw new RuntimeException(jsonMappingException);
		}
		catch (JsonProcessingException jsonProcessingException) {
			throw new RuntimeException(jsonProcessingException);
		}
	}

}