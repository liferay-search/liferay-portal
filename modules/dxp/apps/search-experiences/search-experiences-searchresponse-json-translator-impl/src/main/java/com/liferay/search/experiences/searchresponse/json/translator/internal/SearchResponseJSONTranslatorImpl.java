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

package com.liferay.search.experiences.searchresponse.json.translator.internal;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.message.Message;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.service.BlueprintService;
import com.liferay.search.experiences.searchresponse.json.translator.SearchResponseJSONTranslator;
import com.liferay.search.experiences.searchresponse.json.translator.constants.JSONKeys;
import com.liferay.search.experiences.searchresponse.json.translator.spi.contributor.JSONTranslationContributor;

import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SearchResponseJSONTranslator.class)
public class SearchResponseJSONTranslatorImpl
	implements SearchResponseJSONTranslator {

	@Override
	public JSONObject translate(
			SearchResponse searchResponse, Blueprint blueprint,
			BlueprintsAttributes blueprintsAttributes,
			ResourceBundle resourceBundle, Messages messages)
		throws PortalException {

		JSONObject responseJSONObject = _jsonFactory.createJSONObject();

		if (searchResponse == null) {
			return responseJSONObject;
		}

		Set<String> keySet =
			_jsonTranslationContributorServiceTrackerMap.keySet();

		keySet.forEach(
			key -> {
				JSONTranslationContributor jsonTranslationContributor =
					_jsonTranslationContributorServiceTrackerMap.getService(
						key);

				jsonTranslationContributor.contribute(
					responseJSONObject, searchResponse, blueprint,
					blueprintsAttributes, resourceBundle, messages);
			});

		return responseJSONObject;
	}

	@Override
	public JSONObject translateErrorMessages(
		List<Message> errorMessages, ResourceBundle resourceBundle) {

		JSONObject responseJSONObject = _jsonFactory.createJSONObject();

		JSONArray errorMessagesJSONArray = _jsonFactory.createJSONArray();

		for (Message message : errorMessages) {
			JSONObject errorMessageJSONObject = _jsonFactory.createJSONObject();

			errorMessageJSONObject.put(
				"className", message.getClassName()
			).put(
				"elementId", message.getElementId()
			).put(
				"localizedMessage",
				_language.get(resourceBundle, message.getLocalizationKey())
			).put(
				"msg", message.getMsg()
			).put(
				"rootConfiguration", message.getRootConfiguration()
			).put(
				"rootObject", message.getRootObject()
			).put(
				"rootProperty", message.getRootProperty()
			).put(
				"rootValue", message.getRootValue()
			).put(
				"severity", message.getSeverity()
			).put(
				"throwable", message.getThrowable()
			);

			errorMessagesJSONArray.put(errorMessageJSONObject);
		}

		responseJSONObject.put(JSONKeys.ERRORS, errorMessagesJSONArray);

		return responseJSONObject;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_jsonTranslationContributorServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, JSONTranslationContributor.class, "name");
	}

	@Deactivate
	protected void deactivate() {
		_jsonTranslationContributorServiceTrackerMap.close();
	}

	@Reference
	private BlueprintService _blueprintService;

	@Reference
	private JSONFactory _jsonFactory;

	private ServiceTrackerMap<String, JSONTranslationContributor>
		_jsonTranslationContributorServiceTrackerMap;

	@Reference
	private Language _language;

}