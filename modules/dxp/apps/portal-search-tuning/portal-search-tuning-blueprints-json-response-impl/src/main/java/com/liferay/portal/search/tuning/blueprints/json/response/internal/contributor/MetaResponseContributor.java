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

package com.liferay.portal.search.tuning.blueprints.json.response.internal.contributor;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.portal.search.tuning.blueprints.json.response.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.json.response.spi.contributor.ResponseContributor;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;

import java.util.IllegalFormatException;
import java.util.Optional;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=meta",
	service = ResponseContributor.class
)
public class MetaResponseContributor implements ResponseContributor {

	@Override
	public void contribute(
		JSONObject responseJSONObject, SearchResponse searchResponse,
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle, Messages messages) {

		responseJSONObject.put(
			JSONResponseKeys.META,
			_getMetaJSONObject(searchResponse, blueprintsAttributes));
	}

	private JSONObject _getMetaJSONObject(
		SearchResponse searchResponse,
		BlueprintsAttributes blueprintsAttributes) {

		SearchHits searchHits = searchResponse.getSearchHits();

		JSONObject jsonObject = JSONUtil.put(
			JSONResponseKeys.TOTAL_HITS, searchHits.getTotalHits());

		jsonObject.put(
			JSONResponseKeys.KEYWORDS, blueprintsAttributes.getKeywords());

		_setExecutionTime(jsonObject, searchResponse);

		_setShowingInsteadOf(jsonObject, blueprintsAttributes);

		return jsonObject;
	}

	private void _setExecutionTime(
		JSONObject jsonObject, SearchResponse searchResponse) {

		searchResponse.withHits(
			hits -> {
				try {
					jsonObject.put(
						JSONResponseKeys.EXECUTION_TIME,
						String.format("%.3f", hits.getSearchTime()));
				}
				catch (IllegalFormatException illegalFormatException) {
					_log.error(
						illegalFormatException.getMessage(),
						illegalFormatException);
				}
			});
	}

	private void _setShowingInsteadOf(
		JSONObject jsonObject, BlueprintsAttributes blueprintsAttributes) {

		Optional<Object> showingInsteadOfOptional =
			blueprintsAttributes.getAttributeOptional(
				ReservedParameterNames.SHOWING_INSTEAD_OF.getKey());

		if (showingInsteadOfOptional.isPresent()) {
			jsonObject.put(
				JSONResponseKeys.SHOWING_INSTEAD_OF,
				showingInsteadOfOptional.get());
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MetaResponseContributor.class);

}