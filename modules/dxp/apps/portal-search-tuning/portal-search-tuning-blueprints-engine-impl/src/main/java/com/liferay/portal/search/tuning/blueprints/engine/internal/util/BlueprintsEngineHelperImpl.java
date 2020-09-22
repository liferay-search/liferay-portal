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

import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
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
import com.liferay.portal.search.tuning.blueprints.poc.util.POCMockUtil;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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

		for (Map.Entry
				<String,
				 ServiceComponentReference<SearchRequestBodyContributor>>
					entry : _searchRequestBodyContributors.entrySet()) {

			try {
				ServiceComponentReference<SearchRequestBodyContributor> value =
					entry.getValue();

				SearchRequestBodyContributor searchRequestBodyContributor =
					value.getServiceComponent();

				searchRequestBodyContributor.contribute(
					searchRequestBuilder, parameterData, blueprint, messages);
			}
			catch (IllegalStateException illegalStateException) {
				messages.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.error-in-executing-search-request-body-" +
							"contributors",
						illegalStateException.getMessage(),
						illegalStateException, null, null, null));

				_log.error(
					illegalStateException.getMessage(), illegalStateException);
			}
		}
	}

	// TODO: REMOVE MOCKUPS WHEN READY

	private Blueprint _getBlueprint(long blueprintId) {
		try {
			Blueprint blueprint = _blueprintService.getBlueprint(blueprintId);

			_pocMockUtil.mockConfigurations(blueprint);

			return blueprint;
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
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

	private String[] _getIndexNames(
		SearchRequestBuilder searchRequestBuilder, Blueprint blueprint) {

		Optional<JSONObject> jsonObjectOptional =
			_blueprintHelper.getIndexesConfigurationOptional(blueprint);

		if (!jsonObjectOptional.isPresent()) {
			return null;
		}

		// TODO: waiting for suggestions module implementation.
		// boolean suggestionsIndex =

		//		jsonObject.getBoolean("use_query_suggestions_index", false);

		// Make SF happy for now

		searchRequestBuilder.getClass();

		return null;
	}

	// TODO: is this right and how to add/configure other classes?

	private Class<?>[] _getModelIndexerClasses(Blueprint blueprint) {
		Optional<JSONArray> optional =
			_blueprintHelper.getModelIndexerClassesOptional(blueprint);

		if (!optional.isPresent()) {
			return new Class<?>[0];
		}

		JSONArray jsonArray = optional.get();

		List<Class<?>> list = new ArrayList<>();

		for (int i = 0; i < jsonArray.length(); i++) {
			String s = jsonArray.getString(i);

			if (StringUtil.equals(s, JournalArticle.class.getName())) {
				list.add(JournalArticle.class);
				//			} else if (StringUtil.equals(s,
				//DLFileEntry.class.getName())) {
				//				list.add(DLFileEntry.class);
			}
		}

		return list.toArray(new Class<?>[0]);
	}

	private SearchRequestBuilder _getSearchRequestBuilder(
		ParameterData parameterData, Blueprint blueprint, Messages messages,
		long companyId, Locale locale) {

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder(
			).companyId(
				companyId
			).excludeContributors(
				"com.liferay.portal.search.tuning.blueprints"
			).explain(
				_isExplain(parameterData)
			).includeResponseString(
				_isIncludeResponseString(parameterData)
			).locale(
				locale
			).modelIndexerClasses(
				_getModelIndexerClasses(blueprint)
			).size(
				_blueprintHelper.getSize(blueprint)
			).from(
				_getFrom(parameterData, blueprint)
			);

		String[] indexNames = _getIndexNames(searchRequestBuilder, blueprint);

		if (indexNames != null) {
			searchRequestBuilder.indexes(indexNames);
		}

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

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintsEngineHelperImpl.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintService _blueprintService;

	@Reference
	private ParameterDataCreator _parameterDataCreator;

	@Reference
	private POCMockUtil _pocMockUtil;

	@Reference
	private SearchExecutor _searchExecutor;

	private volatile Map
		<String, ServiceComponentReference<SearchRequestBodyContributor>>
			_searchRequestBodyContributors = new ConcurrentHashMap<>();

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

}