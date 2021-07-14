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

package com.liferay.search.experiences.blueprints.engine.internal.util;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.blueprints.constants.json.keys.advanced.AdvancedConfigurationKeys;
import com.liferay.search.experiences.blueprints.constants.json.keys.advanced.SourceConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.search.experiences.blueprints.engine.exception.BlueprintsEngineException;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataCreator;
import com.liferay.search.experiences.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.search.experiences.blueprints.engine.template.variable.BlueprintTemplateVariableParser;
import com.liferay.search.experiences.blueprints.message.Message;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.message.Severity;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.service.BlueprintService;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintsSearchRequestHelper.class)
public class BlueprintsSearchRequestHelper {

	public void checkEngineErrors(long blueprintId, Messages messages) {
		if (messages.hasErrors()) {
			List<Message> errors = messages.getMessagesBySeverity(
				Severity.ERROR);

			StringBundler sb = new StringBundler(5);

			sb.append("There were ");
			sb.append(errors.size());
			sb.append(" error(s) in processing Blueprint ");
			sb.append(blueprintId);
			sb.append(". See messages for details.");

			throw new BlueprintsEngineException(sb.toString(), errors);
		}
	}

	public void executeSearchRequestBodyContributors(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, Messages messages) {

		if (_log.isDebugEnabled()) {
			_log.debug("Executing search request body contributors");
		}

		List<String> excludedSearchRequestBodyContributors =
			_getExcludedSearchRequestBodyContributors(parameterData);

		Set<String> keySet =
			_searchRequestBodyContributorServiceTrackerMap.keySet();

		Stream<String> stream = keySet.stream();

		stream.filter(
			name -> !excludedSearchRequestBodyContributors.contains(name)
		).forEach(
			name -> {
				SearchRequestBodyContributor searchRequestBodyContributor =
					_searchRequestBodyContributorServiceTrackerMap.getService(
						name);

				try {
					searchRequestBodyContributor.contribute(
						searchRequestBuilder, blueprint, parameterData,
						messages);
				}
				catch (Exception exception) {
					MessagesUtil.error(
						messages, getClass().getName(), exception, null, null,
						null, "core.error.unknown-error");
				}
			}
		);
	}

	public void setSource(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, Messages messages) {

		Optional<JSONObject> optional1 =
			_blueprintHelper.getAdvancedConfigurationOptional(blueprint);

		JSONObject advancedConfigurationJSONObject = optional1.get();

		JSONObject sourceJSONObject =
			advancedConfigurationJSONObject.getJSONObject(
				AdvancedConfigurationKeys.SOURCE.getJsonKey());

		if (sourceJSONObject == null) {
			return;
		}

		Optional<JSONObject> optional2 =
			_blueprintTemplateVariableParser.parseObject(
				sourceJSONObject, parameterData, messages);

		if (!optional2.isPresent()) {
			return;
		}

		JSONObject parsedSourceJSONObject = optional2.get();

		if (parsedSourceJSONObject.has(
				SourceConfigurationKeys.FETCH_SOURCE.getJsonKey())) {

			searchRequestBuilder.fetchSource(
				parsedSourceJSONObject.getBoolean(
					SourceConfigurationKeys.FETCH_SOURCE.getJsonKey()));
		}

		JSONArray excludesJSONArray = parsedSourceJSONObject.getJSONArray(
			SourceConfigurationKeys.SOURCE_EXCLUDES.getJsonKey());

		if ((excludesJSONArray != null) && (excludesJSONArray.length() > 0)) {
			searchRequestBuilder.fetchSourceExcludes(
				JSONUtil.toStringArray(excludesJSONArray));
		}

		JSONArray includesJSONArray = parsedSourceJSONObject.getJSONArray(
			SourceConfigurationKeys.SOURCE_INCLUDES.getJsonKey());

		if ((includesJSONArray != null) && (includesJSONArray.length() > 0)) {
			searchRequestBuilder.fetchSourceIncludes(
				JSONUtil.toStringArray(includesJSONArray));
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_searchRequestBodyContributorServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, SearchRequestBodyContributor.class, "name");
	}

	@Deactivate
	protected void deactivate() {
		_searchRequestBodyContributorServiceTrackerMap.close();
	}

	private List<String> _getExcludedSearchRequestBodyContributors(
		ParameterData parameterData) {

		Optional<Parameter> excludedSearchRequestBodyContributorsOptional =
			parameterData.getByNameOptional(
				ReservedParameterNames.
					EXCLUDED_SEARCH_REQUEST_BODY_CONTRIBUTORS.getKey());

		if (!excludedSearchRequestBodyContributorsOptional.isPresent()) {
			return new ArrayList<>();
		}

		Object object = excludedSearchRequestBodyContributorsOptional.get();

		if (object instanceof String[]) {
			return Arrays.asList((String[])object);
		}

		return new ArrayList<>();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintsSearchRequestHelper.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintService _blueprintService;

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private ParameterDataCreator _parameterDataCreator;

	private ServiceTrackerMap<String, SearchRequestBodyContributor>
		_searchRequestBodyContributorServiceTrackerMap;

}