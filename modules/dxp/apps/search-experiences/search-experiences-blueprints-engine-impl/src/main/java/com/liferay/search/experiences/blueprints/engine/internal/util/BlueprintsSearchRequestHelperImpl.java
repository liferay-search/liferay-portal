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
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.engine.exception.BlueprintsEngineException;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataCreator;
import com.liferay.search.experiences.blueprints.engine.searcher.BlueprintsSearchRequestHelper;
import com.liferay.search.experiences.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.search.experiences.blueprints.engine.template.variable.BlueprintTemplateVariableParser;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.problems.Problem;
import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemsHolder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;
import com.liferay.search.experiences.problems.Severity;

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
public class BlueprintsSearchRequestHelperImpl
	implements BlueprintsSearchRequestHelper {

	@Override
	public void checkEngineErrors(
		long blueprintId, ProblemsHolder problemsHolder) {

		if (problemsHolder.hasErrors()) {
			List<Problem> problems = problemsHolder.getProblemsBySeverity(
				Severity.ERROR);

			throw new BlueprintsEngineException(
				StringBundler.concat(
					"There were ", problems.size(),
					" error(s) in processing Blueprint ", blueprintId,
					". See messages for details."),
				problems);
		}
	}

	@Override
	public void executeSearchRequestBodyContributors(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, ProblemsHolderBuilder problemsHolderBuilder) {

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
						problemsHolderBuilder);
				}
				catch (Exception exception) {
					_log.error(exception);

					ProblemBuilder problemBuilder =
						_problemBuilders.unknownError(
							getClass().getName(), null, exception);

					problemsHolderBuilder.addProblem(problemBuilder.build());
				}
			}
		);
	}

	@Override
	public void setFieldRetrieval(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<JSONObject> optional =
			_blueprintHelper.getAdvancedConfigurationOptional(blueprint);

		JSONObject advancedConfigurationJSONObject = optional.get();

		_setSource(
			searchRequestBuilder,
			advancedConfigurationJSONObject.getJSONObject("source"),
			parameterData, problemsHolderBuilder);

		_setStoredFields(
			searchRequestBuilder,
			advancedConfigurationJSONObject.getJSONArray("stored_fields"),
			parameterData, problemsHolderBuilder);
	}

	@Override
	public void setPreview(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint) {

		Optional<Parameter> optional = parameterData.getByNameOptional(
			"preview");

		if (!optional.isPresent() || (blueprint.getBlueprintId() > 0)) {
			return;
		}

		searchRequestBuilder.withSearchContext(
			searchContext -> searchContext.setAttribute(
				"search.experiences.blueprint.preview", true));
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
				"excluded_search_request_body_contributors");

		if (!excludedSearchRequestBodyContributorsOptional.isPresent()) {
			return new ArrayList<>();
		}

		Object object = excludedSearchRequestBodyContributorsOptional.get();

		if (object instanceof String[]) {
			return Arrays.asList((String[])object);
		}

		return new ArrayList<>();
	}

	private void _setSource(
		SearchRequestBuilder searchRequestBuilder, JSONObject jsonObject,
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		if (jsonObject == null) {
			return;
		}

		Optional<JSONObject> optional =
			_blueprintTemplateVariableParser.parseObject(
				jsonObject, parameterData, problemsHolderBuilder);

		if (!optional.isPresent()) {
			return;
		}

		JSONObject parsedJSONObject = optional.get();

		if (parsedJSONObject.has("fetch_source")) {
			searchRequestBuilder.fetchSource(
				parsedJSONObject.getBoolean("fetch_source"));
		}

		JSONArray excludesJSONArray = parsedJSONObject.getJSONArray(
			"source_excludes");

		if ((excludesJSONArray != null) && (excludesJSONArray.length() > 0)) {
			searchRequestBuilder.fetchSourceExcludes(
				JSONUtil.toStringArray(excludesJSONArray));
		}

		JSONArray includesJSONArray = parsedJSONObject.getJSONArray(
			"source_includes");

		if ((includesJSONArray != null) && (includesJSONArray.length() > 0)) {
			searchRequestBuilder.fetchSourceIncludes(
				JSONUtil.toStringArray(includesJSONArray));
		}
	}

	private void _setStoredFields(
		SearchRequestBuilder searchRequestBuilder, JSONArray jsonArray,
		ParameterData parameterData,
		ProblemsHolderBuilder problemsHolderBuilder) {

		if (jsonArray == null) {
			return;
		}

		Optional<JSONArray> optional =
			_blueprintTemplateVariableParser.parseArray(
				jsonArray, parameterData, problemsHolderBuilder);

		if (!optional.isPresent()) {
			return;
		}

		JSONArray parsedJSONArray = optional.get();

		if ((parsedJSONArray != null) && (parsedJSONArray.length() > 0)) {
			searchRequestBuilder.fields(
				BlueprintJSONUtil.toStringArray(parsedJSONArray));
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintsSearchRequestHelperImpl.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private ParameterDataCreator _parameterDataCreator;

	@Reference
	private ProblemBuilders _problemBuilders;

	private ServiceTrackerMap<String, SearchRequestBodyContributor>
		_searchRequestBodyContributorServiceTrackerMap;

}