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

import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.BlueprintLookup;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.exception.BlueprintsEngineException;
import com.liferay.search.experiences.blueprints.engine.internal.executor.SearchExecutor;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataCreator;
import com.liferay.search.experiences.blueprints.engine.searcher.BlueprintToSearchRequestTranslator;
import com.liferay.search.experiences.blueprints.engine.searcher.BlueprintsSearchRequestHelper;
import com.liferay.search.experiences.blueprints.engine.util.BlueprintsEngineHelper;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.util.Locale;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintsEngineHelper.class)
public class BlueprintsEngineHelperImpl implements BlueprintsEngineHelper {

	@Override
	public SearchRequestBuilder getSearchRequestBuilder(
		long blueprintId, BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<Blueprint> optional = _blueprintLookup.getBlueprintOptional(
			blueprintId);

		return optional.map(
			blueprint -> _getSearchRequestBuilder(
				blueprint, blueprintsAttributes, problemsHolderBuilder)
		).orElseThrow(
			BlueprintsEngineException::new
		);
	}

	@Override
	public SearchResponse search(
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		return _search(blueprint, blueprintsAttributes, problemsHolderBuilder);
	}

	@Override
	public SearchResponse search(
		long blueprintId, BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<Blueprint> optional = _blueprintLookup.getBlueprintOptional(
			blueprintId);

		return optional.map(
			blueprint -> _search(
				blueprint, blueprintsAttributes, problemsHolderBuilder)
		).orElseThrow(
			BlueprintsEngineException::new
		);
	}

	private int _getFrom(
		ParameterData parameterData, Blueprint blueprint, int size) {

		Optional<Parameter> optional = parameterData.getByNameOptional(
			_blueprintHelper.getPageParameterName(blueprint));

		if (!optional.isPresent()) {
			return 0;
		}

		Parameter parameter = optional.get();

		int page = GetterUtil.getInteger(parameter.getValue(), 1);

		return _getFromValue(size, page);
	}

	private int _getFromValue(int size, int page) {
		if (page <= 1) {
			return 0;
		}

		return (page - 1) * size;
	}

	private SearchRequestBuilder _getSearchRequestBuilder(
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		ParameterData parameterData = _parameterDataCreator.create(
			blueprint, blueprintsAttributes, problemsHolderBuilder);

		SearchRequestBuilder searchRequestBuilder = _getSearchRequestBuilder(
			parameterData, blueprint, problemsHolderBuilder,
			blueprintsAttributes.getCompanyId(),
			blueprintsAttributes.getLocale());

		_blueprintsSearchRequestHelper.checkEngineErrors(
			blueprint.getBlueprintId(), problemsHolderBuilder.build());

		return searchRequestBuilder;
	}

	private SearchRequestBuilder _getSearchRequestBuilder(
		ParameterData parameterData, Blueprint blueprint,
		ProblemsHolderBuilder problemsHolderBuilder, long companyId,
		Locale locale) {

		int size = _getSize(parameterData, blueprint);

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder(
			).companyId(
				companyId
			).emptySearchEnabled(
				true
			).excludeContributors(
				"com.liferay.search.experiences.blueprints"
			).explain(
				_isExplain(parameterData)
			).includeResponseString(
				_isIncludeResponseString(parameterData)
			).locale(
				locale
			).size(
				size
			).from(
				_getFrom(parameterData, blueprint, size)
			);

		_blueprintToSearchRequestTranslator.translate(
			blueprint, searchRequestBuilder, parameterData, null);

		_blueprintsSearchRequestHelper.setFieldRetrieval(
			searchRequestBuilder, parameterData, blueprint,
			problemsHolderBuilder);

		_blueprintsSearchRequestHelper.setPreview(
			searchRequestBuilder, parameterData, blueprint);

		_blueprintsSearchRequestHelper.executeSearchRequestBodyContributors(
			searchRequestBuilder, parameterData, blueprint,
			problemsHolderBuilder);

		return searchRequestBuilder;
	}

	private int _getSize(ParameterData parameterData, Blueprint blueprint) {
		String parameterName = _blueprintHelper.getSizeParameterName(blueprint);

		Optional<Parameter> optional = parameterData.getByNameOptional(
			parameterName);

		if (!optional.isPresent()) {
			_blueprintHelper.getDefaultSize(blueprint);
		}

		Parameter parameter = optional.get();

		return GetterUtil.getInteger(parameter.getValue());
	}

	private boolean _isExplain(ParameterData parameterData) {
		Optional<Parameter> parameterOptional = parameterData.getByNameOptional(
			"explain");

		if (!parameterOptional.isPresent()) {
			return false;
		}

		Parameter parameter = parameterOptional.get();

		return GetterUtil.getBoolean(parameter.getValue());
	}

	private boolean _isIncludeResponseString(ParameterData parameterData) {
		Optional<Parameter> parameterOptional = parameterData.getByNameOptional(
			"include_response_string");

		if (!parameterOptional.isPresent()) {
			return false;
		}

		Parameter parameter = parameterOptional.get();

		return GetterUtil.getBoolean(parameter.getValue());
	}

	private SearchResponse _search(
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		ParameterData parameterData = _parameterDataCreator.create(
			blueprint, blueprintsAttributes, problemsHolderBuilder);

		SearchRequestBuilder searchRequestBuilder = _getSearchRequestBuilder(
			parameterData, blueprint, problemsHolderBuilder,
			blueprintsAttributes.getCompanyId(),
			blueprintsAttributes.getLocale());

		SearchResponse searchResponse = _searchExecutor.execute(
			searchRequestBuilder, parameterData, blueprint,
			problemsHolderBuilder);

		_blueprintsSearchRequestHelper.checkEngineErrors(
			blueprint.getBlueprintId(), problemsHolderBuilder.build());

		return searchResponse;
	}

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintLookup _blueprintLookup;

	@Reference
	private BlueprintsSearchRequestHelper _blueprintsSearchRequestHelper;

	@Reference
	private BlueprintToSearchRequestTranslator
		_blueprintToSearchRequestTranslator;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private ParameterDataCreator _parameterDataCreator;

	@Reference
	private SearchExecutor _searchExecutor;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

}