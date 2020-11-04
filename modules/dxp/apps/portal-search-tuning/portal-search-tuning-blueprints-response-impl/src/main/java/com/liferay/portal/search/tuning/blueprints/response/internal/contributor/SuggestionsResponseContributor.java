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

package com.liferay.portal.search.tuning.blueprints.response.internal.contributor;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.response.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.response.spi.contributor.ResponseContributor;

import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=suggestions",
	service = ResponseContributor.class
)
public class SuggestionsResponseContributor implements ResponseContributor {

	@Override
	public void contribute(
		JSONObject responseJsonObject, SearchResponse searchResponse,
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle,	
		Messages messages) {

		// TODO: Waiting for suggest support in SearchRequest & SearchResponse

		responseJsonObject.put(
			JSONResponseKeys.SUGGESTIONS, JSONFactoryUtil.createJSONObject());
	}

}