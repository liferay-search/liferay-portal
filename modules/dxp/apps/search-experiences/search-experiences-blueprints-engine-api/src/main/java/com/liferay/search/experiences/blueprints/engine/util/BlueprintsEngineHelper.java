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

package com.liferay.search.experiences.blueprints.engine.util;

import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.model.Blueprint;

/**
 * @author Petteri Karttunen
 */
public interface BlueprintsEngineHelper {

	public SearchRequestBuilder getSearchRequestBuilder(
		long blueprintId, BlueprintsAttributes blueprintsAttributes,
		Messages messages);

	public SearchResponse search(
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		Messages messages);

	public SearchResponse search(
		long blueprintId, BlueprintsAttributes blueprintsAttributes,
		Messages messages);

}