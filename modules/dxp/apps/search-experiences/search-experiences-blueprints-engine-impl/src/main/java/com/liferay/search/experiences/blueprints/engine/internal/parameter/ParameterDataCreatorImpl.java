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

package com.liferay.search.experiences.blueprints.engine.internal.parameter;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.internal.attributes.util.BlueprintsAttributeValuesHelper;
import com.liferay.search.experiences.blueprints.engine.internal.parameter.builder.ParameterBuilder;
import com.liferay.search.experiences.blueprints.engine.internal.util.ProblemBuilders;
import com.liferay.search.experiences.blueprints.engine.parameter.IntegerParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataBuilder;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataCreator;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDefinition;
import com.liferay.search.experiences.blueprints.engine.parameter.StringParameter;
import com.liferay.search.experiences.blueprints.engine.spi.keywords.KeywordsProcessor;
import com.liferay.search.experiences.blueprints.engine.spi.parameter.ParameterContributor;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ParameterDataCreator.class)
public class ParameterDataCreatorImpl implements ParameterDataCreator {

	@Override
	public ParameterData create(
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<JSONObject> optional =
			_blueprintHelper.getParameterConfigurationOptional(blueprint);

		if (!optional.isPresent()) {
			return new ParameterDataImpl(StringPool.BLANK, new ArrayList<>());
		}

		ParameterDataBuilder parameterDataBuilder =
			new ParameterDataBuilderImpl();

		JSONObject jsonObject = optional.get();

		_addKeywordParameter(
			parameterDataBuilder, blueprint, blueprintsAttributes,
			problemsHolderBuilder);

		_addPagingParameter(
			parameterDataBuilder, blueprintsAttributes,
			jsonObject.getJSONObject("page"));

		_addSizeParameter(
			parameterDataBuilder, blueprintsAttributes,
			jsonObject.getJSONObject("size"), problemsHolderBuilder);

		_addSortParameters(
			parameterDataBuilder, blueprint, blueprintsAttributes,
			problemsHolderBuilder);

		_executeParameterContributors(
			parameterDataBuilder, blueprint, blueprintsAttributes,
			problemsHolderBuilder);

		_addCustomParameters(
			parameterDataBuilder, blueprintsAttributes,
			jsonObject.getJSONArray("custom"), problemsHolderBuilder);

		ParameterData parameterData = parameterDataBuilder.build();

		_logParameters(parameterData);

		return parameterData;
	}

	@Override
	public Map<String, List<ParameterDefinition>>
		getContributedParameterDefinitions() {

		Map<String, List<ParameterDefinition>> contributedParameterDefinitions =
			new HashMap<>();

		Set<String> keySet = _parameterContributorServiceTrackerMap.keySet();

		keySet.forEach(
			name -> {
				ParameterContributor parameterContributor =
					_parameterContributorServiceTrackerMap.getService(name);

				contributedParameterDefinitions.put(
					parameterContributor.getCategoryNameKey(),
					parameterContributor.getParameterDefinitions());
			});

		return contributedParameterDefinitions;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_keywordsProcessorServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, KeywordsProcessor.class, "name");

		_parameterContributorServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, ParameterContributor.class, "name");
	}

	@Deactivate
	protected void deactivate() {
		_keywordsProcessorServiceTrackerMap.close();

		_parameterContributorServiceTrackerMap.close();
	}

	private void _addCustomParameter(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes, JSONObject jsonObject,
		ProblemsHolderBuilder problemsHolderBuilder) {

		String type = jsonObject.getString("type");

		try {
			ParameterBuilder parameterBuilder =
				_parameterBuilderFactory.getBuilder(type);

			Optional<Parameter> optional = parameterBuilder.build(
				blueprintsAttributes, jsonObject, problemsHolderBuilder);

			if (optional.isPresent()) {
				parameterDataBuilder.addParameter(optional.get());
			}
		}
		catch (IllegalArgumentException illegalArgumentException) {
			_log.error(illegalArgumentException);

			ProblemBuilder problemBuilder =
				_problemBuilders.invalidConfigurationValueError(
					getClass().getName(), illegalArgumentException, jsonObject,
					"type", type);

			problemsHolderBuilder.addProblem(problemBuilder.build());
		}
	}

	private void _addCustomParameters(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes,
		JSONArray configurationJSONArray,
		ProblemsHolderBuilder problemsHolderBuilder) {

		if ((configurationJSONArray == null) ||
			(configurationJSONArray.length() == 0)) {

			return;
		}

		for (int i = 0; i < configurationJSONArray.length(); i++) {
			_addCustomParameter(
				parameterDataBuilder, blueprintsAttributes,
				configurationJSONArray.getJSONObject(i), problemsHolderBuilder);
		}
	}

	private void _addKeywordParameter(
		ParameterDataBuilder parameterDataBuilder, Blueprint blueprint,
		BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		String keywords = GetterUtil.getString(
			blueprintsAttributes.getKeywords());

		parameterDataBuilder.addParameter(
			new StringParameter("keywords.raw", "${keywords.raw}", keywords));

		keywords = _executeKeywordsProcessors(
			keywords, blueprint, blueprintsAttributes, problemsHolderBuilder);

		parameterDataBuilder.addParameter(
			new StringParameter("keywords", "${keywords}", keywords));

		parameterDataBuilder.keywords(keywords);
	}

	private void _addPagingParameter(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes, JSONObject jsonObject) {

		String parameterName = jsonObject.getString("parameter_name");

		Optional<Integer> optional =
			_blueprintsAttributeValuesHelper.getIntegerOptional(
				blueprintsAttributes, parameterName);

		if (optional.isPresent()) {
			parameterDataBuilder.addParameter(
				new IntegerParameter("page", "${page}", optional.get()));
		}
	}

	private void _addSizeParameter(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes, JSONObject jsonObject,
		ProblemsHolderBuilder problemsHolderBuilder) {

		ParameterBuilder parameterBuilder = _parameterBuilderFactory.getBuilder(
			"integer");

		Optional<Parameter> optional = parameterBuilder.build(
			blueprintsAttributes, jsonObject, problemsHolderBuilder);

		if (optional.isPresent()) {
			Parameter parameter = optional.get();

			parameterDataBuilder.addParameter(
				new IntegerParameter(
					"size", "${size}",
					GetterUtil.getInteger(parameter.getValue())));
		}
	}

	private void _addSortParameters(
		ParameterDataBuilder parameterDataBuilder, Blueprint blueprint,
		BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<JSONObject> jsonObjectOptional =
			_blueprintHelper.getSortParameterConfigurationOptional(blueprint);

		if (!jsonObjectOptional.isPresent()) {
			return;
		}

		JSONObject jsonObject = jsonObjectOptional.get();

		Set<String> keySet = jsonObject.keySet();

		keySet.forEach(
			key -> {
				JSONObject sortJSONObject = jsonObject.getJSONObject(key);

				ParameterBuilder parameterBuilder =
					_parameterBuilderFactory.getBuilder("string");

				Optional<Parameter> parameterOptional = parameterBuilder.build(
					blueprintsAttributes, sortJSONObject,
					problemsHolderBuilder);

				if (parameterOptional.isPresent()) {
					parameterDataBuilder.addParameter(parameterOptional.get());
				}
			});
	}

	private String _executeKeywordsProcessors(
		String keywords, Blueprint blueprint,
		BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Set<String> keySet = _keywordsProcessorServiceTrackerMap.keySet();

		for (String name : keySet) {
			KeywordsProcessor keywordsProcessor =
				_keywordsProcessorServiceTrackerMap.getService(name);

			keywords = keywordsProcessor.process(
				keywords, blueprint, blueprintsAttributes,
				problemsHolderBuilder);
		}

		return keywords;
	}

	private void _executeParameterContributors(
		ParameterDataBuilder parameterDataBuilder, Blueprint blueprint,
		BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Set<String> keySet = _parameterContributorServiceTrackerMap.keySet();

		keySet.forEach(
			name -> {
				ParameterContributor parameterContributor =
					_parameterContributorServiceTrackerMap.getService(name);

				parameterContributor.contribute(
					parameterDataBuilder, blueprint, blueprintsAttributes,
					problemsHolderBuilder);
			});
	}

	private void _logParameters(ParameterData parameterData) {
		if (_log.isDebugEnabled()) {
			_log.debug(
				"Available template variables after parameter contributions:");

			List<Parameter> parameters = parameterData.getParameters();

			if (!parameters.isEmpty()) {
				for (Parameter parameter : parameters) {
					_log.debug(parameter.toString());
				}
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ParameterDataCreatorImpl.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintsAttributeValuesHelper _blueprintsAttributeValuesHelper;

	private ServiceTrackerMap<String, KeywordsProcessor>
		_keywordsProcessorServiceTrackerMap;

	@Reference
	private ParameterBuilderFactory _parameterBuilderFactory;

	private ServiceTrackerMap<String, ParameterContributor>
		_parameterContributorServiceTrackerMap;

	@Reference
	private ProblemBuilders _problemBuilders;

}