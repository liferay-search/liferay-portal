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

package com.liferay.portal.search.tuning.blueprints.engine.internal.searchrequest;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.rescore.Rescore;
import com.liferay.portal.search.rescore.RescoreBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.query.ClauseConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.query.ConditionConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.query.QueryConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.ClauseContext;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.Occur;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.Operator;
import com.liferay.portal.search.tuning.blueprints.engine.component.ServiceComponentReference;
import com.liferay.portal.search.tuning.blueprints.engine.internal.clause.ClauseTranslatorFactory;
import com.liferay.portal.search.tuning.blueprints.engine.internal.condition.ConditionHandlerFactory;
import com.liferay.portal.search.tuning.blueprints.engine.internal.util.BlueprintValueUtil;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.clause.ClauseTranslator;
import com.liferay.portal.search.tuning.blueprints.engine.spi.clause.ConditionHandler;
import com.liferay.portal.search.tuning.blueprints.engine.spi.query.QueryContributor;
import com.liferay.portal.search.tuning.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.portal.search.tuning.blueprints.engine.util.BlueprintTemplateVariableParser;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.List;
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
@Component(
	immediate = true, property = "name=query",
	service = SearchRequestBodyContributor.class
)
public class QuerySearchRequestBodyContributor
	implements SearchRequestBodyContributor {

	@Override
	public void contribute(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, Messages messages) {

		_addClauses(searchRequestBuilder, parameterData, blueprint, messages);

		_executeQueryContributors(
			searchRequestBuilder, parameterData, blueprint, messages);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void registerQueryContributor(
		QueryContributor queryContributor, Map<String, Object> properties) {

		String name = (String)properties.get("name");

		if (Validator.isBlank(name)) {
			if (_log.isWarnEnabled()) {
				Class<?> clazz = queryContributor.getClass();

				_log.warn(
					"Unable to register query contributor " + clazz.getName() +
						". Name property empty.");
			}

			return;
		}

		int serviceRanking = GetterUtil.get(
			properties.get("service.ranking"), 0);

		ServiceComponentReference<QueryContributor> serviceComponentReference =
			new ServiceComponentReference<>(queryContributor, serviceRanking);

		if (_queryContributors.containsKey(name)) {
			ServiceComponentReference<QueryContributor> previousReference =
				_queryContributors.get(name);

			if (previousReference.compareTo(serviceComponentReference) < 0) {
				_queryContributors.put(name, serviceComponentReference);
			}
		}
		else {
			_queryContributors.put(name, serviceComponentReference);
		}
	}

	protected void unregisterQueryContributor(
		QueryContributor queryContributor, Map<String, Object> properties) {

		String name = (String)properties.get("name");

		if (Validator.isBlank(name)) {
			return;
		}

		_queryContributors.remove(name);
	}

	private void _addClause(
		SearchRequestBuilder searchRequestBuilder, ClauseContext clauseContext,
		Occur occur, Query subquery, JSONObject queryJsonObject) {

		if (clauseContext.equals(ClauseContext.POST_FILTER)) {
			_addPostFilterClause(searchRequestBuilder, subquery, occur);
		}
		else if (clauseContext.equals(ClauseContext.PRE_FILTER)) {
			_addPreFilterClause(searchRequestBuilder, subquery);
		}
		else if (clauseContext.equals(ClauseContext.QUERY)) {
			_addQueryClause(searchRequestBuilder, occur, subquery);
		}
		else if (clauseContext.equals(ClauseContext.RESCORE)) {
			_addRescoreClause(
				searchRequestBuilder, subquery, _getWindoSize(queryJsonObject));
		}
	}

	private void _addClauses(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, Messages messages) {

		Optional<JSONArray> configurationJsonArrayOptional =
			_blueprintHelper.getQueryConfigurationOptional(blueprint);

		if (!configurationJsonArrayOptional.isPresent()) {
			return;
		}

		JSONArray configurationJsonArray = configurationJsonArrayOptional.get();

		for (int i = 0; i < configurationJsonArray.length(); i++) {
			JSONObject configurationJsonObject =
				configurationJsonArray.getJSONObject(i);

			if (!configurationJsonObject.getBoolean(
					QueryConfigurationKeys.ENABLED.getJsonKey(), true)) {

				continue;
			}

			if (!_isConditionsTrue(
					parameterData, messages, configurationJsonObject)) {

				continue;
			}

			JSONArray clausesJsonArray = configurationJsonObject.getJSONArray(
				QueryConfigurationKeys.CLAUSES.getJsonKey());

			JSONObject clauseJsonObject = null;
			JSONObject queryJsonObject = null;

			for (int j = 0; j < clausesJsonArray.length(); j++) {
				clauseJsonObject = clausesJsonArray.getJSONObject(j);

				String type = clauseJsonObject.getString(
					ClauseConfigurationKeys.TYPE.getJsonKey());

				try {
					ClauseTranslator clauseTranslator =
						_clauseTranslatorFactory.getTranslator(type);

					queryJsonObject =
						_blueprintTemplateVariableParser.parse(
							parameterData, messages,
							clauseJsonObject.getJSONObject(
								ClauseConfigurationKeys.QUERY.getJsonKey()));

					Optional<Query> clauseOptional = clauseTranslator.translate(
						blueprint, parameterData, messages, queryJsonObject);

					if (!clauseOptional.isPresent()) {
						continue;
					}

					ClauseContext clauseContext = _getClauseContext(
						messages, clauseJsonObject);

					if (clauseContext == null) {
						continue;
					}

					Occur occur = _getOccur(messages, clauseJsonObject);

					if (occur == null) {
						continue;
					}

					_addClause(
						searchRequestBuilder, clauseContext, occur,
						clauseOptional.get(), queryJsonObject);
				}
				catch (IllegalArgumentException illegalArgumentException) {
					messages.addMessage(
							new Message.Builder().className(
								getClass().getName()
							).localizationKey(
								"core.error.unknown-query-type"
							).msg(
								illegalArgumentException.getMessage()
							).rootObject(clauseJsonObject
							).rootProperty(
									ClauseConfigurationKeys.TYPE.getJsonKey()
							).rootValue(type
							).severity(
								Severity.ERROR
							).throwable(illegalArgumentException
							).build());

					_log.error(
						illegalArgumentException.getMessage(),
						illegalArgumentException);
				}
				catch (JSONException jsonException) {
					messages.addMessage(
							new Message.Builder().className(
								getClass().getName()
							).localizationKey(
									"core.error.error-in-parsing-configuration-" +
											"parameters"
											).msg(
													jsonException.getMessage()
							).rootObject(queryJsonObject
							).severity(
								Severity.ERROR
							).throwable(jsonException
							).build());

					if (_log.isWarnEnabled()) {
						_log.warn(jsonException.getMessage(), jsonException);
					}
				}
				catch (Exception exception) {
					messages.addMessage(
							new Message.Builder().className(
								getClass().getName()
							).localizationKey(
								"core.error.unknown-clause-building-error"
							).msg(
									exception.getMessage()
							).rootObject(clauseJsonObject
							).severity(
								Severity.ERROR
							).throwable(exception
							).build());

					if (_log.isWarnEnabled()) {
						_log.warn(exception.getMessage(), exception);
					}
				}
			}
		}
	}

	private void _addPostFilterClause(
		SearchRequestBuilder searchRequestBuilder, Query subquery,
		Occur occur) {

		searchRequestBuilder.addPostFilterQueryPart(
			_complexQueryPartBuilderFactory.builder(
			).query(
				subquery
			).occur(
				occur.getjsonValue()
			).build());
	}

	private void _addPreFilterClause(
		SearchRequestBuilder searchRequestBuilder, Query subquery) {

		searchRequestBuilder.addComplexQueryPart(
			_complexQueryPartBuilderFactory.builder(
			).query(
				subquery
			).occur(
				"filter"
			).build());
	}

	private void _addQueryClause(
		SearchRequestBuilder searchRequestBuilder, Occur occur,
		Query subquery) {

		searchRequestBuilder.addComplexQueryPart(
			_complexQueryPartBuilderFactory.builder(
			).query(
				subquery
			).occur(
				occur.getjsonValue()
			).build());
	}

	private void _addRescoreClause(
		SearchRequestBuilder searchRequestBuilder, Query subquery,
		int windowSize) {

		if (_rescoreBuilder == null) {
			return;
		}

		Rescore rescore = _rescoreBuilder.query(
			subquery
		).windowSize(
			windowSize
		).build();

		searchRequestBuilder.addRescore(rescore);
	}

	private void _executeQueryContributors(
		SearchRequestBuilder searchRequestBuilder, ParameterData parameterData,
		Blueprint blueprint, Messages messages) {

		if (_log.isDebugEnabled()) {
			_log.debug("Processing query contributors");
		}

		if (_queryContributors.isEmpty()) {
			return;
		}

		for (Map.Entry<String, ServiceComponentReference<QueryContributor>>
				entry : _queryContributors.entrySet()) {

			try {
				ServiceComponentReference<QueryContributor> value =
					entry.getValue();

				QueryContributor queryContributor = value.getServiceComponent();

				Class<?> clazz = queryContributor.getClass();

				if (_isQueryContributorExcluded(blueprint, clazz.getName())) {
					continue;
				}

				Optional<Query> queryOptional = queryContributor.build(
					blueprint, parameterData, messages);

				if (!queryOptional.isPresent()) {
					continue;
				}

				_addClause(
					searchRequestBuilder, queryContributor.getClauseContext(),
					queryContributor.getOccur(), queryOptional.get(), null);
			}
			catch (Exception exception) {
	 			messages.addMessage(
	 					new Message.Builder().className(
	 						getClass().getName()
	 					).localizationKey(
	 						"core.error.unknown-error-query-contributor"
	 					).msg(
	 							exception.getMessage()
	 					).severity(
	 						Severity.ERROR
	 					).throwable(exception
	 					).build());				

				_log.error(exception.getMessage(), exception);
			}
		}
	}

	private ClauseContext _getClauseContext(
		Messages messages, JSONObject queryJsonObject) {

		String clauseContextString = queryJsonObject.getString(
			ClauseConfigurationKeys.CONTEXT.getJsonKey());

		try {
			clauseContextString = StringUtil.toUpperCase(clauseContextString);

			return ClauseContext.valueOf(clauseContextString);
		}
		catch (IllegalArgumentException illegalArgumentException) {

 			messages.addMessage(
				new Message.Builder().className(
					getClass().getName()
				).localizationKey(
					"core.error.unknown-clause-context"
				).msg(
					illegalArgumentException.getMessage()
				).rootObject(queryJsonObject
				).rootProperty(
						ClauseConfigurationKeys.CONTEXT.getJsonKey()
				).rootValue(clauseContextString
				).severity(
					Severity.ERROR
				).throwable(illegalArgumentException
				).build());

			if (_log.isWarnEnabled()) {
				_log.warn(
					illegalArgumentException.getMessage(),
					illegalArgumentException);
			}
		}

		return null;
	}

	private Occur _getOccur(Messages messages, JSONObject queryJsonObject) {
		String occurString = queryJsonObject.getString(
			ClauseConfigurationKeys.OCCUR.getJsonKey(), "must");

		try {
			occurString = StringUtil.toUpperCase(occurString);

			return Occur.valueOf(occurString);
		}
		catch (IllegalArgumentException illegalArgumentException) {

 			messages.addMessage(
				new Message.Builder().className(
					getClass().getName()
				).localizationKey(
					"core.error.unknown-occur-value"
				).msg(
					illegalArgumentException.getMessage()
				).rootObject(queryJsonObject
				).rootProperty(
						ClauseConfigurationKeys.OCCUR.getJsonKey()
				).rootValue(occurString
				).severity(
					Severity.ERROR
				).throwable(illegalArgumentException
				).build());

		}

		return null;
	}

	private Integer _getWindoSize(JSONObject queryJsonObject) {
		if (queryJsonObject.has(
				ClauseConfigurationKeys.WINDOW_SIZE.getJsonKey())) {

			return queryJsonObject.getInt(
				ClauseConfigurationKeys.WINDOW_SIZE.getJsonKey());
		}

		return null;
	}

	private boolean _isConditionsTrue(
		ParameterData parameterData, Messages messages,
		JSONObject configurationJsonObject) {

		JSONArray conditionsJsonArray = configurationJsonObject.getJSONArray(
			QueryConfigurationKeys.CONDITIONS.getJsonKey());

		if ((conditionsJsonArray == null) ||
			(conditionsJsonArray.length() == 0)) {

			return true;
		}

		boolean valid = false;

		for (int i = 0; i < conditionsJsonArray.length(); i++) {
			JSONObject conditionJsonObject = conditionsJsonArray.getJSONObject(
				i);

			String handler = conditionJsonObject.getString(
				ConditionConfigurationKeys.HANDLER.getJsonKey());

			try {
				ConditionHandler conditionHandler =
					_conditionHandlerFactory.getHandler(handler);

				String operatorString = conditionJsonObject.getString(
					ConditionConfigurationKeys.OPERATOR.getJsonKey(),
					Operator.AND.name());

				Operator operator = BlueprintValueUtil.getOperator(
					operatorString);

				JSONObject handlerParametersJsonObject =
					conditionJsonObject.getJSONObject(
						ConditionConfigurationKeys.CONFIGURATION.getJsonKey());

				boolean conditionTrue = conditionHandler.isTrue(
					parameterData, messages, handlerParametersJsonObject);

				if (operator.equals(Operator.AND) && !conditionTrue) {
					return false;
				}
				else if (operator.equals(Operator.NOT) && conditionTrue) {
					return false;
				}
				else if (conditionTrue) {
					valid = true;
				}
			}
			catch (IllegalArgumentException illegalArgumentException) {

	 			messages.addMessage(
					new Message.Builder().className(
						getClass().getName()
					).localizationKey(
						"core.error.unknown-clause-condition-handler"
					).msg(
						illegalArgumentException.getMessage()
					).rootObject(conditionJsonObject
					).rootProperty(
							ConditionConfigurationKeys.HANDLER.getJsonKey()
					).rootValue(handler
					).severity(
						Severity.ERROR
					).throwable(illegalArgumentException
					).build());

				if (_log.isWarnEnabled()) {
					_log.warn(
						illegalArgumentException.getMessage(),
						illegalArgumentException);
				}
			}
			catch (Exception exception) {

	 			messages.addMessage(
					new Message.Builder().className(
						getClass().getName()
					).localizationKey(
						"core.error.unknown-clause-condition-error"
					).msg(
							exception.getMessage()
					).rootObject(conditionJsonObject
					).severity(
						Severity.ERROR
					).throwable(exception
					).build());


				_log.error(exception.getMessage(), exception);
			}
		}

		return valid;
	}

	private boolean _isQueryContributorExcluded(
		Blueprint blueprint, String className) {

		Optional<List<String>> excludedQueryContributorsOptional =
			_blueprintHelper.getExcludedQueryContributorsOptional(blueprint);

		if (!excludedQueryContributorsOptional.isPresent()) {
			return false;
		}

		List<String> excludedQueryContributors =
			excludedQueryContributorsOptional.get();

		Stream<String> stream = excludedQueryContributors.stream();

		if (stream.anyMatch(s -> s.contentEquals(className) || s.equals("*"))) {
			return true;
		}

		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		QuerySearchRequestBodyContributor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private ClauseTranslatorFactory _clauseTranslatorFactory;

	@Reference
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Reference
	private ConditionHandlerFactory _conditionHandlerFactory;

	@Reference
	private Queries _queries;

	private volatile Map<String, ServiceComponentReference<QueryContributor>>
		_queryContributors = new ConcurrentHashMap<>();

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	private RescoreBuilder _rescoreBuilder;

}