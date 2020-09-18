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

package com.liferay.portal.search.tuning.blueprints.engine.internal.query.postprocessor;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.query.QueryPostProcessor;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=keyword_indexer",
	service = QueryPostProcessor.class
)
public class KeywordIndexerPostProcessor implements QueryPostProcessor {

	@Override
	public boolean process(
		SearchResponse searchResponse, Blueprint blueprint,
		ParameterData parameterData, Messages messages) {

		Optional<JSONObject> configurationJsonObjectOptional =
			_blueprintHelper.getKeywordIndexingConfigurationOptional(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return true;
		}

		// TODO: waiting for suggestions module implementation
		// https://issues.liferay.com/browse/LPS-118888

		return true;
	}

	@Reference
	private BlueprintHelper _blueprintHelper;

}