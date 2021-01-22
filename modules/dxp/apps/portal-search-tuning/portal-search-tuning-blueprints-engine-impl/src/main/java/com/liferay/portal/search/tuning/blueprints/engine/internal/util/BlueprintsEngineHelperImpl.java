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

package com.liferay.portal.search.tuning.blueprints.engine.internal.util;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.component.ServiceComponentReference;
import com.liferay.portal.search.tuning.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.portal.search.tuning.blueprints.engine.exception.BlueprintsEngineException;
import com.liferay.portal.search.tuning.blueprints.engine.internal.executor.SearchExecutor;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterDataCreator;
import com.liferay.portal.search.tuning.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.portal.search.tuning.blueprints.engine.util.BlueprintsEngineHelper;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintsEngineHelper.class)
public class BlueprintsEngineHelperImpl implements BlueprintsEngineHelper {

	@Override
	public void combine(
		SearchRequestBuilder searchRequestBuilder,
		BlueprintsAttributes blueprintsAttributes, Messages messages,
		long blueprintId) {

		Blueprint blueprint = _getBlueprint(blueprintId);

		ParameterData parameterData = _parameterDataCreator.create(
			blueprint, blueprintsAttributes, messages);

		_executeSearchRequestBodyContributors(
			searchRequestBuilder, parameterData, blueprint, messages);
	}

	@Override
	public SearchRequestBuilder getSearchRequestBuilder(
		BlueprintsAttributes blueprintsAttributes, Messages messages,
		long blueprintId) {

		Blueprint blueprint = _getBlueprint(blueprintId);

		ParameterData parameterData = _parameterDataCreator.create(
			blueprint, blueprintsAttributes, messages);

		return _getSearchRequestBuilder(
			parameterData, blueprint, messages,
			blueprintsAttributes.getCompanyId(),
			blueprintsAttributes.getLocale());
	}

	@Override
	public SearchResponse search(
			BlueprintsAttributes blueprintsAttributes, Blueprint blueprint,
			Messages messages)
		throws BlueprintsEngineException, JSONException, PortalException {

		ParameterData parameterData = _parameterDataCreator.create(
			blueprint, blueprintsAttributes, messages);

		SearchRequestBuilder searchRequestBuilder = _getSearchRequestBuilder(
			parameterData, blueprint, messages,
			blueprintsAttributes.getCompanyId(),
			blueprintsAttributes.getLocale());

		return _searchExecutor.execute(
			searchRequestBuilder, parameterData, blueprint, messages);
	}

	@Override
	public SearchResponse search(
			BlueprintsAttributes blueprintsAttributes, Messages messages,
			long blueprintId)
		throws BlueprintsEngineException, JSONException, PortalException {

		Blueprint blueprint = _getBlueprint(blueprintId);

		ParameterData parameterData = _parameterDataCreator.create(
			blueprint, blueprintsAttributes, messages);

		SearchRequestBuilder searchRequestBuilder = _getSearchRequestBuilder(
			parameterData, blueprint, messages,
			blueprintsAttributes.getCompanyId(),
			blueprintsAttributes.getLocale());

		return _searchExecutor.execute(
			searchRequestBuilder, parameterData, blueprint, messages);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void registerSearchRequestBodyContributor(
		SearchRequestBodyContributor searchRequestBodyContributor,
		Map<String, Object> properties) {

		String name = (String)properties.get("name");

		if (Validator.isBlank(name)) {
			if (_log.isWarnEnabled()) {
				Class<?> clazz = searchRequestBodyContributor.getClass();

				_log.warn(
					"Unable to register search request contributor " +
						clazz.getName() + ". Name property empty.");
			}

			return;
		}

		int serviceRanking = GetterUtil.get(
			properties.get("service.ranking"), 0);

		ServiceComponentReference<SearchRequestBodyContributor>
			serviceComponentReference = new ServiceComponentReference<>(
				searchRequestBodyContributor, serviceRanking);

		if (_searchRequestBodyContributors.containsKey(name)) {
			ServiceComponentReference<SearchRequestBodyContributor>
				previousReference = _searchRequestBodyContributors.get(name);

			if (previousReference.compareTo(serviceComponentReference) < 0) {
				_searchRequestBodyContributors.put(
					name, serviceComponentReference);
			}
		}
		else {
			_searchRequestBodyContributors.put(name, serviceComponentReference);
		}
	}

	protected void unregisterSearchRequestBodyContributor(
		SearchRequestBodyContributor searchRequestBodyContributor,
		Map<String, Object> properties) {

		String name = (String)properties.get("name");

		if (Validator.isBlank(name)) {
			return;
		}

		_searchRequestBodyContributors.remove(name);
	}

	private void _executeSearchRequestBodyContributors(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, Messages messages) {

		if (_log.isDebugEnabled()) {
			_log.debug("Executing search request body contributors");
		}

		List<String> excludedSearchRequestBodyContributors =
			_getExcludedSearchRequestBodyContributors(parameterData);

		for (Map.Entry
				<String,
				 ServiceComponentReference<SearchRequestBodyContributor>>
					entry : _searchRequestBodyContributors.entrySet()) {

			try {
				if (excludedSearchRequestBodyContributors.contains(
						entry.getKey())) {

					continue;
				}

				ServiceComponentReference<SearchRequestBodyContributor> value =
					entry.getValue();

				SearchRequestBodyContributor searchRequestBodyContributor =
					value.getServiceComponent();

				searchRequestBodyContributor.contribute(
					searchRequestBuilder, blueprint, parameterData, messages);
			}
			catch (IllegalStateException illegalStateException) {
				messages.addMessage(
					new Message.Builder().className(
						getClass().getName()
					).localizationKey(
						"core.error.error-in-executing-search-request-body-" +
							"contributors"
					).msg(
						illegalStateException.getMessage()
					).severity(
						Severity.ERROR
					).throwable(
						illegalStateException
					).build());

				_log.error(
					illegalStateException.getMessage(), illegalStateException);
			}
		}

		if (!_shouldApplyIndexerClauses(blueprint)) {
			searchRequestBuilder.withSearchContext(
				searchContext -> searchContext.setAttribute(
					"search.full.query.suppress.indexer.provided.clauses",
					Boolean.TRUE));
		}
	}

	private Blueprint _getBlueprint(long blueprintId) {
		try {
			return _blueprintService.getBlueprint(blueprintId);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
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

	private int _getFrom(ParameterData parameterData, Blueprint blueprint) {
		Optional<String> optional1 =
			_blueprintHelper.getPageParameterNameOptional(blueprint);

		if (!optional1.isPresent()) {
			return 1;
		}

		Optional<Parameter> optional2 = parameterData.getByNameOptional(
			optional1.get());

		if (!optional2.isPresent()) {
			return 1;
		}

		Parameter parameter = optional2.get();

		int page = GetterUtil.getInteger(parameter.getValue());

		return _getFromValue(_blueprintHelper.getSize(blueprint), page);
	}

	private int _getFromValue(int size, int page) {
		if (page <= 1) {
			return 0;
		}

		return (page - 1) * size;
	}

	private String[] _getModelIndexerClassNames(
		Blueprint blueprint, long companyId) {

		Optional<JSONArray> optional =
			_blueprintHelper.getSearchableAssetTypesOptional(blueprint);

		if (optional.isPresent()) {
			return JSONUtil.toStringArray(optional.get());
		}

		// TODO: remove after asset type selection is available on the UI

		return _getSearchableAssetTypes(companyId);
	}

	private String[] _getSearchableAssetTypes(long companyId) {
		List<AssetRendererFactory<?>> assetRendererFactories =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactories(
				companyId, true);

		Stream<AssetRendererFactory<?>> stream =
			assetRendererFactories.stream();

		return stream.filter(
			item -> item.isSearchable()
		).map(
			AssetRendererFactory::getClassName
		).toArray(
			String[]::new
		);
	}

	private SearchRequestBuilder _getSearchRequestBuilder(
		ParameterData parameterData, Blueprint blueprint, Messages messages,
		long companyId, Locale locale) {

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder(
			).companyId(
				companyId
			).emptySearchEnabled(
				true
			).excludeContributors(
				"com.liferay.portal.search.tuning.blueprints"
			).explain(
				_isExplain(parameterData)
			).includeResponseString(
				_isIncludeResponseString(parameterData)
			).locale(
				locale
			).modelIndexerClassNames(
				_getModelIndexerClassNames(blueprint, companyId)
			).size(
				_blueprintHelper.getSize(blueprint)
			).from(
				_getFrom(parameterData, blueprint)
			);

		_executeSearchRequestBodyContributors(
			searchRequestBuilder, parameterData, blueprint, messages);

		return searchRequestBuilder;
	}

	private boolean _isExplain(ParameterData parameterData) {
		return GetterUtil.getBoolean(
			parameterData.getByNameOptional(
				ReservedParameterNames.EXPLAIN.getKey()));
	}

	private boolean _isIncludeResponseString(ParameterData parameterData) {
		return GetterUtil.getBoolean(
			parameterData.getByNameOptional(
				ReservedParameterNames.INCLUDE_RESPONSE_STRING.getKey()));
	}

	private boolean _shouldApplyIndexerClauses(Blueprint blueprint) {
		return _blueprintHelper.applyIndexerClauses(blueprint);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintsEngineHelperImpl.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintService _blueprintService;

	@Reference
	private ParameterDataCreator _parameterDataCreator;

	@Reference
	private SearchExecutor _searchExecutor;

	private volatile Map
		<String, ServiceComponentReference<SearchRequestBodyContributor>>
			_searchRequestBodyContributors = new ConcurrentHashMap<>();

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

}