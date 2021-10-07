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

package com.liferay.search.experiences.blueprints.engine.searcher;

import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;

/**
 * @author André de Oliveira
 */
public interface BlueprintToSearchRequestTranslator {

	public void translate(
		Blueprint blueprint, SearchRequestBuilder searchRequestBuilder,
		ParameterData parameterData, BlueprintsAttributes blueprintsAttributes);

}