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

package com.liferay.search.experiences.blueprints.facets.internal.response;

import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.facets.constants.FacetsBlueprintKeys;
import com.liferay.search.experiences.blueprints.facets.constants.FacetsJSONResponseKeys;
import com.liferay.search.experiences.blueprints.facets.internal.response.handler.FacetResponseHandlerFactory;
import com.liferay.search.experiences.blueprints.facets.internal.util.FacetConfigurationUtil;
import com.liferay.search.experiences.blueprints.facets.spi.response.FacetResponseHandler;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.blueprints.util.util.BlueprintJSONUtil;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;
import com.liferay.search.experiences.searchresponse.json.translator.spi.contributor.JSONTranslationContributor;

import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=facets",
	service = JSONTranslationContributor.class
)
public class FacetsJSONTranslationContributor
	implements JSONTranslationContributor {

	@Override
	public void contribute(
		JSONObject responseJSONObject, SearchResponse searchResponse,
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle, Messages messages) {

		Optional<JSONObject> optional =
			_blueprintHelper.getJSONObjectConfigurationOptional(
				blueprint,
				"JSONObject/" + FacetsBlueprintKeys.CONFIGURATION_SECTION);

		Map<String, AggregationResult> aggregations =
			searchResponse.getAggregationResultsMap();

		if (aggregations.isEmpty() || !optional.isPresent()) {
			return;
		}

		responseJSONObject.put(
			FacetsJSONResponseKeys.FACETS,
			_getFacetsJSONObject(
				aggregations, optional.get(), blueprintsAttributes,
				resourceBundle, messages));
	}

	private void _addResult(
		JSONObject responseJSONObject, String facetName, String type,
		JSONObject typeJSONObject, AggregationResult aggregationResult,
		BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle, Messages messages) {

		try {
			FacetResponseHandler facetResponseHandler =
				_facetResponseHandlerFactory.getHandler(type);

			Optional<JSONObject> resultOptional =
				facetResponseHandler.getResultOptional(
					aggregationResult, type, blueprintsAttributes,
					resourceBundle, messages, typeJSONObject);

			if (resultOptional.isPresent()) {
				responseJSONObject.put(facetName, resultOptional.get());
			}
		}
		catch (IllegalArgumentException illegalArgumentException) {
			MessagesUtil.invalidConfigurationValueError(
				messages, getClass().getName(), illegalArgumentException, null,
				null, type);
		}
	}

	private JSONObject _getFacetsJSONObject(
		Map<String, AggregationResult> aggregations, JSONObject jsonObject,
		BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle, Messages messages) {

		JSONObject responseJSONObject = _jsonFactory.createJSONObject();

		Set<String> keySet = jsonObject.keySet();

		keySet.forEach(
			facetName -> _processFacet(
				responseJSONObject, aggregations, facetName, jsonObject,
				blueprintsAttributes, resourceBundle, messages));

		return responseJSONObject;
	}

	private void _processFacet(
		JSONObject responseJSONObject,
		Map<String, AggregationResult> aggregations, String facetName,
		JSONObject configurationJSONObject,
		BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle, Messages messages) {

		JSONObject nameJSONObject = configurationJSONObject.getJSONObject(
			facetName);

		Optional<String> optional1 = BlueprintJSONUtil.getFirstKeyOptional(
			nameJSONObject);

		if (!optional1.isPresent()) {
			return;
		}

		String type = optional1.get();

		JSONObject typeJSONObject = nameJSONObject.getJSONObject(type);

		if (!FacetConfigurationUtil.isEnabled(typeJSONObject)) {
			return;
		}

		String aggregationName = FacetConfigurationUtil.getAggregationName(
			typeJSONObject);

		for (Map.Entry<String, AggregationResult> entry :
				aggregations.entrySet()) {

			if (!StringUtil.equalsIgnoreCase(entry.getKey(), aggregationName)) {
				continue;
			}

			_addResult(
				responseJSONObject, facetName, type, typeJSONObject,
				entry.getValue(), blueprintsAttributes, resourceBundle,
				messages);
		}
	}

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference(target = "(type=internal)")
	private FacetResponseHandlerFactory _facetResponseHandlerFactory;

	@Reference
	private JSONFactory _jsonFactory;

}