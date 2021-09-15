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
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;
import com.liferay.search.experiences.searchresponse.json.translator.SearchResponseJSONTranslator;
import com.liferay.search.experiences.searchresponse.json.translator.spi.contributor.JSONTranslationContributor;

import java.util.Collection;
import java.util.ResourceBundle;

import java.beans.ExceptionListener;

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
	public String translate(
		SearchResponse searchResponse, Blueprint blueprint,
		BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle, ExceptionListener exceptionListener,
		ProblemsHolderBuilder problemsHolderBuilder) {

		JSONObject jsonObject = _jsonFactory.createJSONObject();

		for (JSONTranslationContributor jsonTranslationContributor :
				getJSONTranslationContributors()) {

			try {
				jsonTranslationContributor.contribute(
					jsonObject, searchResponse, blueprint, blueprintsAttributes,
					resourceBundle, problemsHolderBuilder);
			}
			catch (Exception exception) {
				if (exceptionListener != null) {
					exceptionListener.exceptionThrown(exception);
				}
			}
		}

		return jsonObject.toString();
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

	protected Collection<JSONTranslationContributor>
		getJSONTranslationContributors() {

		if (_jsonTranslationContributors != null) {
			return _jsonTranslationContributors;
		}

		return _jsonTranslationContributorServiceTrackerMap.values();
	}

	@Reference(unbind = "-")
	protected void setJSONFactory(JSONFactory jsonFactory) {
		_jsonFactory = jsonFactory;
	}

	protected void setJSONTranslationContributors(
		Collection<JSONTranslationContributor> jsonTranslationContributors) {

		_jsonTranslationContributors = jsonTranslationContributors;
	}

	private JSONFactory _jsonFactory;
	private Collection<JSONTranslationContributor> _jsonTranslationContributors;
	private ServiceTrackerMap<String, JSONTranslationContributor>
		_jsonTranslationContributorServiceTrackerMap;

}