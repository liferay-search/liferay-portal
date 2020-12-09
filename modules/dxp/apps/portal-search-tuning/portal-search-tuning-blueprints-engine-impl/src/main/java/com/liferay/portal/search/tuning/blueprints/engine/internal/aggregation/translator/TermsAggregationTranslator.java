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

package com.liferay.portal.search.tuning.blueprints.engine.internal.aggregation.translator;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.Aggregation;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.CollectionMode;
import com.liferay.portal.search.aggregation.bucket.IncludeExcludeClause;
import com.liferay.portal.search.aggregation.bucket.Order;
import com.liferay.portal.search.aggregation.bucket.TermsAggregation;
import com.liferay.portal.search.script.ScriptBuilder;
import com.liferay.portal.search.script.ScriptType;
import com.liferay.portal.search.script.Scripts;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.aggregation.AggregationConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.aggregation.TermsAggregationBodyConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.internal.aggregation.AggregationTranslatorFactory;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.aggregation.AggregationTranslator;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "type=terms",
	service = AggregationTranslator.class
)
public class TermsAggregationTranslator implements AggregationTranslator {

	@Override
	public Optional<Aggregation> translate(
		ParameterData parameterData, Messages messages,
		JSONObject configurationJsonObject, String aggregationName) {

		if (_log.isDebugEnabled()) {
			_log.debug(
				"Translating terms aggregation from [ " +
					configurationJsonObject + " ]");
		}

		if (!_validate(configurationJsonObject, aggregationName, messages)) {
			return Optional.empty();
		}

		String field = configurationJsonObject.getString(
			TermsAggregationBodyConfigurationKeys.FIELD.getJsonKey());

		TermsAggregation aggregation = _aggregations.terms(
			aggregationName, field);

		if (configurationJsonObject.has(
				TermsAggregationBodyConfigurationKeys.AGGREGATIONS.
					getJsonKey())) {

			_addChildAggregations(
				aggregation, parameterData, messages,
				configurationJsonObject.getJSONArray(
					TermsAggregationBodyConfigurationKeys.AGGREGATIONS.
						getJsonKey()));
		}

		if (configurationJsonObject.has(
				TermsAggregationBodyConfigurationKeys.COLLECT_MODE.
					getJsonKey())) {

			_setCollectMode(aggregation, messages, configurationJsonObject);
		}

		if (configurationJsonObject.has(
				TermsAggregationBodyConfigurationKeys.EXECUTION_HINT.
					getJsonKey())) {

			aggregation.setExecutionHint(
				configurationJsonObject.getString(
					TermsAggregationBodyConfigurationKeys.EXECUTION_HINT.
						getJsonKey()));
		}

		_setIncludeExcludeClause(aggregation, configurationJsonObject);

		if (configurationJsonObject.has(
				TermsAggregationBodyConfigurationKeys.MIN_DOC_COUNT.
					getJsonKey())) {

			aggregation.setMinDocCount(
				configurationJsonObject.getInt(
					TermsAggregationBodyConfigurationKeys.MIN_DOC_COUNT.
						getJsonKey()));
		}

		if (configurationJsonObject.has(
				TermsAggregationBodyConfigurationKeys.MISSING.getJsonKey())) {

			aggregation.setMissing(
				configurationJsonObject.getString(
					TermsAggregationBodyConfigurationKeys.MISSING.
						getJsonKey()));
		}

		if (configurationJsonObject.has(
				TermsAggregationBodyConfigurationKeys.ORDER.getJsonKey())) {

			_setOrders(aggregation, messages, configurationJsonObject);
		}

		if (configurationJsonObject.has(
				TermsAggregationBodyConfigurationKeys.SCRIPT.getJsonKey())) {

			_setScript(aggregation, messages, configurationJsonObject);
		}

		if (configurationJsonObject.has(
				TermsAggregationBodyConfigurationKeys.SHARD_MIN_DOC_COUNT.
					getJsonKey())) {

			aggregation.setShardMinDocCount(
				configurationJsonObject.getInt(
					TermsAggregationBodyConfigurationKeys.SHARD_MIN_DOC_COUNT.
						getJsonKey()));
		}

		if (configurationJsonObject.has(
				TermsAggregationBodyConfigurationKeys.SHARD_SIZE.
					getJsonKey())) {

			aggregation.setShardSize(
				configurationJsonObject.getInt(
					TermsAggregationBodyConfigurationKeys.SHARD_SIZE.
						getJsonKey()));
		}

		if (configurationJsonObject.has(
				TermsAggregationBodyConfigurationKeys.SHOW_TERM_DOC_COUNT_ERROR.
					getJsonKey())) {

			aggregation.setShowTermDocCountError(
				configurationJsonObject.getBoolean(
					TermsAggregationBodyConfigurationKeys.
						SHOW_TERM_DOC_COUNT_ERROR.getJsonKey()));
		}

		if (configurationJsonObject.has(
				TermsAggregationBodyConfigurationKeys.SIZE.getJsonKey())) {

			aggregation.setSize(
				configurationJsonObject.getInt(
					TermsAggregationBodyConfigurationKeys.SIZE.getJsonKey()));
		}

		return Optional.of(aggregation);
	}

	private void _addChildAggregations(
		TermsAggregation aggregation, ParameterData parameterData,
		Messages messages, JSONArray childAggregationJsonArray) {

		for (int i = 0; i < childAggregationJsonArray.length(); i++) {
			JSONObject childAggregationJsonObject =
				childAggregationJsonArray.getJSONObject(i);

			String type = childAggregationJsonObject.getString(
				AggregationConfigurationKeys.TYPE.getJsonKey());

			String name = childAggregationJsonObject.getString(
				AggregationConfigurationKeys.NAME.getJsonKey());

			if (!_validate(childAggregationJsonObject, name, messages)) {
				continue;
			}

			try {
				AggregationTranslator aggregationBuilder =
					_aggregationRequestBuilderFactory.getTranslator(type);

				Optional<Aggregation> aggregationOptional =
					aggregationBuilder.translate(
						parameterData, messages, childAggregationJsonObject,
						name);

				if (aggregationOptional.isPresent()) {
					aggregation.addChildAggregation(aggregationOptional.get());
				}
			}
			catch (IllegalArgumentException illegalArgumentException) {

	 			messages.addMessage(
					new Message.Builder().className(
						getClass().getName()
					).localizationKey(
						"core.error.invalid-aggregation-builder-type"
					).msg(
						illegalArgumentException.getMessage()
					).rootObject(childAggregationJsonObject
					).rootProperty(
							TermsAggregationBodyConfigurationKeys.AGGREGATIONS.getJsonKey()
					).rootValue(type
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
		}
	}

	private Order _getOrder(JSONObject orderJsonObject, Messages messages) {
		String orderMetric;

		try {
			orderMetric = orderJsonObject.keys(
			).next();
		}
		catch (NoSuchElementException noSuchElementException) {

 			messages.addMessage(
				new Message.Builder().className(
					getClass().getName()
				).localizationKey(
					"core.error.invalid-aggregation-order-syntax"
				).msg(
						noSuchElementException.getMessage()
				).rootObject(orderJsonObject
				).rootProperty(
						TermsAggregationBodyConfigurationKeys.ORDER.getJsonKey()
				).severity(
					Severity.ERROR
				).throwable(noSuchElementException
				).build());

			if (_log.isWarnEnabled()) {
				_log.warn(
					noSuchElementException.getMessage(),
					noSuchElementException);
			}

			return null;
		}

		String orderDirection = orderJsonObject.getString(orderMetric);

		Order order;

		if (Order.COUNT_METRIC_NAME.equals(orderMetric) ||
			Order.KEY_METRIC_NAME.equals(orderMetric)) {

			order = new Order(null);

			order.setMetricName(orderMetric);
		}
		else {
			String[] arr = orderMetric.split(".");

			if (arr.length != 2) {
				messages.addMessage(
						new Message.Builder().className(
							getClass().getName()
						).localizationKey(
							"core.error.invalid-aggregation-order-syntax"
						).msg(
							"Invalid aggregation order syntax"
						).rootObject(orderJsonObject
						).rootProperty(
								TermsAggregationBodyConfigurationKeys.ORDER.getJsonKey()
						).rootValue(orderMetric
						).severity(
							Severity.ERROR
						).build());
				
				return null;
			}

			String path = arr[0];
			String metric = arr[1];

			order = new Order(path);

			order.setMetricName(metric);
		}

		order.setAscending(StringUtil.equalsIgnoreCase(orderDirection, "asc"));

		return order;
	}

	private void _setCollectMode(
		TermsAggregation aggregation, Messages messages,
		JSONObject configurationJsonObject) {

		String collectModeString = configurationJsonObject.getString(
			TermsAggregationBodyConfigurationKeys.COLLECT_MODE.getJsonKey());

		try {
			CollectionMode collectMode = CollectionMode.valueOf(
				StringUtil.toUpperCase(collectModeString));

			aggregation.setCollectionMode(collectMode);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			messages.addMessage(
					new Message.Builder().className(
						getClass().getName()
					).localizationKey(
						"core.error.invalid-aggregation-collect-mode"
					).msg(
						illegalArgumentException.getMessage()
					).rootObject(configurationJsonObject
					).rootProperty(
							TermsAggregationBodyConfigurationKeys.COLLECT_MODE.getJsonKey()
					).rootValue(collectModeString
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
	}

	private void _setIncludeExcludeClause(
		TermsAggregation aggregation, JSONObject configurationJsonObject) {

		Object excludeObject = configurationJsonObject.get(
			TermsAggregationBodyConfigurationKeys.EXCLUDE.getJsonKey());
		Object includeObject = configurationJsonObject.get(
			TermsAggregationBodyConfigurationKeys.INCLUDE.getJsonKey());

		if (Validator.isNotNull(excludeObject) ||
			Validator.isNotNull(includeObject)) {

			IncludeExcludeClause includeExcludeClause = null;

			if ((excludeObject instanceof JSONArray) ||
				(includeObject instanceof JSONArray)) {

				String[] excludeArray = JSONUtil.toStringArray(
					(JSONArray)excludeObject);
				String[] includeArray = JSONUtil.toStringArray(
					(JSONArray)includeObject);

				includeExcludeClause = new IncludeExcludeClause() {

					@Override
					public String[] getExcludedValues() {
						return excludeArray;
					}

					@Override
					public String getExcludeRegex() {
						return null;
					}

					@Override
					public String[] getIncludedValues() {
						return includeArray;
					}

					@Override
					public String getIncludeRegex() {
						return null;
					}

				};
			}
			else if ((excludeObject instanceof String) ||
					 (includeObject instanceof String)) {

				includeExcludeClause = new IncludeExcludeClause() {

					@Override
					public String[] getExcludedValues() {
						return null;
					}

					@Override
					public String getExcludeRegex() {
						return (String)excludeObject;
					}

					@Override
					public String[] getIncludedValues() {
						return null;
					}

					@Override
					public String getIncludeRegex() {
						return (String)includeObject;
					}

				};
			}

			aggregation.setIncludeExcludeClause(includeExcludeClause);
		}
	}

	private void _setOrders(
		TermsAggregation aggregation, Messages messages,
		JSONObject configurationJsonObject) {

		Object orderObject = configurationJsonObject.get(
			TermsAggregationBodyConfigurationKeys.ORDER.getJsonKey());

		List<Order> orders = new ArrayList<>();

		if (orderObject instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray)orderObject;

			jsonArray.forEach(
				object -> {
					JSONObject orderJsonObject = (JSONObject)object;

					Order order = _getOrder(orderJsonObject, messages);

					orders.add(order);
				});
		}
		else if (orderObject instanceof JSONObject) {
			JSONObject orderJsonObject = (JSONObject)orderObject;

			Order order = _getOrder(orderJsonObject, messages);

			orders.add(order);
		}

		Stream<Order> stream = orders.stream();

		aggregation.addOrders(stream.toArray(Order[]::new));
	}

	private void _setScript(
		TermsAggregation aggregation, Messages messages,
		JSONObject configurationJsonObject) {

		JSONObject scriptJsonObject = configurationJsonObject.getJSONObject(
			TermsAggregationBodyConfigurationKeys.SCRIPT.getJsonKey());

		ScriptBuilder scriptBuilder = _scripts.builder();

		if (scriptJsonObject.has(
				TermsAggregationBodyConfigurationKeys.ID.getJsonKey())) {

			String id = scriptJsonObject.getString(
				TermsAggregationBodyConfigurationKeys.ID.getJsonKey());

			scriptBuilder.idOrCode(
				id
			).scriptType(
				ScriptType.STORED
			);
		}
		else if (scriptJsonObject.has(
					TermsAggregationBodyConfigurationKeys.SOURCE.
						getJsonKey())) {

			String source = scriptJsonObject.getString(
				TermsAggregationBodyConfigurationKeys.SOURCE.getJsonKey());

			scriptBuilder.idOrCode(
				source
			).scriptType(
				ScriptType.INLINE
			);
		}
		else {
			messages.addMessage(
					new Message.Builder().className(
						getClass().getName()
					).localizationKey(
						"core.error.aggregation-script-id-or-source-missing"
					).msg(
						"Aggregation script id or source has to be set"
							).rootObject(scriptJsonObject
									).rootProperty(
											TermsAggregationBodyConfigurationKeys.SCRIPT.getJsonKey()
							
					).severity(
						Severity.ERROR
					).build());
			
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Aggregation script id or source has to be set [ " +
						scriptJsonObject + "]");
			}

			return;
		}

		if (scriptJsonObject.has(
				TermsAggregationBodyConfigurationKeys.LANG.getJsonKey())) {

			scriptBuilder.language(
				scriptJsonObject.getString(
					TermsAggregationBodyConfigurationKeys.LANG.getJsonKey()));
		}

		if (scriptJsonObject.has(
				TermsAggregationBodyConfigurationKeys.PARAMS.getJsonKey())) {

			JSONObject paramsJsonObject = scriptJsonObject.getJSONObject(
				TermsAggregationBodyConfigurationKeys.PARAMS.getJsonKey());

			paramsJsonObject.keySet(
			).stream(
			).forEach(
				key -> scriptBuilder.putParameter(
					key, paramsJsonObject.get(key))
			);
		}

		if (scriptJsonObject.has(
				TermsAggregationBodyConfigurationKeys.OPTIONS.getJsonKey())) {

			JSONObject optionsJsonObject = scriptJsonObject.getJSONObject(
				TermsAggregationBodyConfigurationKeys.OPTIONS.getJsonKey());

			optionsJsonObject.keySet(
			).stream(
			).forEach(
				key -> scriptBuilder.putParameter(
					key, optionsJsonObject.get(key))
			);
		}

		aggregation.setScript(scriptBuilder.build());
	}

	private boolean _validate(
		JSONObject configurationJsonObject, String name, Messages messages) {

		boolean valid = true;

		if (Validator.isBlank(name)) {

 			messages.addMessage(
				new Message.Builder().className(
					getClass().getName()
				).localizationKey(
					"core.error.undefined-aggregation-name"
				).msg(
						"Aggregation name is not defined"
				).rootObject(configurationJsonObject
				).rootProperty(
						AggregationConfigurationKeys.NAME.getJsonKey()
				).severity(
					Severity.ERROR
				).build());

 			valid = false;

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Aggregation name is not defined [ " +
						configurationJsonObject + "]");
			}
		}

		if (!configurationJsonObject.has(
				TermsAggregationBodyConfigurationKeys.FIELD.getJsonKey())) {

 			messages.addMessage(
				new Message.Builder().className(
					getClass().getName()
				).localizationKey(
					"core.error.undefined-aggregation-field"
				).msg(
					"Aggregation field is not defined"
				).rootObject(configurationJsonObject
				).rootProperty(
						TermsAggregationBodyConfigurationKeys.FIELD.getJsonKey()
				).severity(
					Severity.ERROR
				).build());

			valid = false;

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Aggregation field is not defined [ " +
						configurationJsonObject + "]");
			}
		}

		return valid;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		TermsAggregationTranslator.class);

	@Reference
	private AggregationTranslatorFactory _aggregationRequestBuilderFactory;

	@Reference
	private Aggregations _aggregations;

	@Reference
	private Scripts _scripts;

}