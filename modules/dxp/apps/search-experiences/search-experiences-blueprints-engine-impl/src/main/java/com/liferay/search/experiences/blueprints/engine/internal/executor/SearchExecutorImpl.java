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

package com.liferay.search.experiences.blueprints.engine.internal.executor;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.rescore.Rescore;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.sort.Sort;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.query.QueryPostProcessor;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.util.BlueprintHelper;
import com.liferay.search.experiences.blueprints.util.component.ServiceComponentReference;
import com.liferay.search.experiences.blueprints.util.component.ServiceComponentReferenceUtil;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SearchExecutor.class)
public class SearchExecutorImpl implements SearchExecutor {

	@Override
	public SearchResponse execute(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, Messages messages) {

		SearchRequestBuilder searchRequestBuilder2 = _rescoreOrSort(
			searchRequestBuilder);

		return _execute(
			searchRequestBuilder2, parameterData, blueprint, messages);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void registerQueryPostProcessor(
		QueryPostProcessor queryPostProcessor, Map<String, Object> properties) {

		ServiceComponentReferenceUtil.addToMapByName(
			_queryPostProcessors, queryPostProcessor, properties);
	}

	protected void unregisterQueryPostProcessor(
		QueryPostProcessor queryPostProcessor, Map<String, Object> properties) {

		ServiceComponentReferenceUtil.removeFromMapByName(
			_queryPostProcessors, queryPostProcessor, properties);
	}

	private void _checkErrors(
		SearchResponse searchResponse, Messages messages) {

		String errorMessage = searchResponse.withSearchContextGet(
			searchContext -> GetterUtil.getString(
				searchContext.getAttribute("search.exception.message")));

		if (!Validator.isBlank(errorMessage)) {
			MessagesUtil.error(
				messages, getClass().getName(), new Throwable(errorMessage),
				null, null, null, "core.error.unknown-error");
		}
	}

	private SearchResponse _execute(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, Messages messages) {

		try {
			SearchResponse searchResponse = _searcher.search(
				searchRequestBuilder.build());

			if (_log.isDebugEnabled() && (searchResponse != null)) {
				_log.debug(
					"Request string: " + searchResponse.getRequestString());
				_log.debug("Hits: " + searchResponse.getCount());
			}

			_checkErrors(searchResponse, messages);

			_executeQueryPostProcessors(
				searchResponse, parameterData, blueprint, messages);

			return searchResponse;
		}
		catch (RuntimeException runtimeException) {
			MessagesUtil.error(
				messages, getClass().getName(), runtimeException, null, null,
				null, "core.error.unknown-error");
		}

		return null;
	}

	private void _executeQueryPostProcessors(
		SearchResponse searchResponse, ParameterData parameterData,
		Blueprint blueprint, Messages messages) {

		if (_log.isDebugEnabled()) {
			_log.debug("Executing query post processors");
		}

		if (_queryPostProcessors == null) {
			return;
		}

		for (Map.Entry<String, ServiceComponentReference<QueryPostProcessor>>
				entry : _queryPostProcessors.entrySet()) {

			ServiceComponentReference<QueryPostProcessor> value =
				entry.getValue();

			QueryPostProcessor queryPostProcessor = value.getServiceComponent();

			queryPostProcessor.process(
				searchResponse, blueprint, parameterData, messages);
		}
	}

	private SearchRequestBuilder _rescoreOrSort(
		SearchRequestBuilder searchRequestBuilder) {

		// Sorts cannot be used with rescorer (See Elasticsearch documentation)

		SearchRequest searchRequest = searchRequestBuilder.build();

		SearchRequestBuilder searchRequestBuilder2 =
			_searchRequestBuilderFactory.builder(searchRequest);

		List<Rescore> rescores = searchRequest.getRescores();

		if ((rescores != null) && !rescores.isEmpty()) {
			searchRequestBuilder2.sorts(new Sort[0]);
		}

		return searchRequestBuilder;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SearchExecutorImpl.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	private volatile Map<String, ServiceComponentReference<QueryPostProcessor>>
		_queryPostProcessors = new ConcurrentHashMap<>();

	@Reference
	private Searcher _searcher;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

}