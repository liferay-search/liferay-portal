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

package com.liferay.portal.search.tuning.blueprints.engine.internal.suggester.keyword;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.internal.suggester.SuggesterHelper;
import com.liferay.portal.search.tuning.blueprints.engine.suggester.Suggester;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "type=keyword", service = Suggester.class
)
public class KeywordSuggester implements Suggester {

	@Override
	public List<String> getSuggestions(
		SearchRequestContext searchRequestContext) {

		Optional<JSONArray> configurationJsonArrayOptional =
			_blueprintHelper.getKeywordSuggestersOptional(
				searchRequestContext.getBlueprint());

		if (!configurationJsonArrayOptional.isPresent()) {
			return new ArrayList<>();
		}

		JSONArray configurationJsonArray = configurationJsonArrayOptional.get();

		List<com.liferay.portal.kernel.search.suggest.Suggester> suggesters =
			_suggesterHelper.getSuggesters(
				searchRequestContext, configurationJsonArray);

		if (suggesters.isEmpty()) {
			return new ArrayList<>();
		}

		return _suggesterHelper.getSuggestions(
			searchRequestContext, suggesters);
	}

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private SuggesterHelper _suggesterHelper;

}