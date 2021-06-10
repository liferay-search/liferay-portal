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

package com.liferay.search.experiences.blueprints.engine.internal.searchrequest;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.highlight.Highlight;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.blueprints.engine.internal.util.HighlightHelper;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.search.experiences.blueprints.engine.template.variable.BlueprintTemplateVariableParser;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=highlight",
	service = SearchRequestBodyContributor.class
)
public class HighlightSearchRequestBodyContributor
	implements SearchRequestBodyContributor {

	@Override
	public void contribute(
		SearchRequestBuilder searchRequestBuilder, Blueprint blueprint,
		ParameterData parameterData, Messages messages) {

		Optional<JSONObject> optional =
			_blueprintHelper.getHighlightConfigurationOptional(blueprint);

		if (!optional.isPresent()) {
			return;
		}

		_contribute(
			searchRequestBuilder, optional.get(), parameterData, messages);
	}

	private void _contribute(
		SearchRequestBuilder searchRequestBuilder, JSONObject jsonObject,
		ParameterData parameterData, Messages messages) {

		Optional<JSONObject> optional1 =
			_blueprintTemplateVariableParser.parseObject(
				jsonObject, parameterData, messages);

		if (!optional1.isPresent()) {
			return;
		}

		Optional<Highlight> optional2 = _highlightHelper.getHighlight(
			optional1.get(), parameterData, messages);

		if (optional2.isPresent()) {
			searchRequestBuilder.highlight(optional2.get());
		}
	}

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private HighlightHelper _highlightHelper;

}