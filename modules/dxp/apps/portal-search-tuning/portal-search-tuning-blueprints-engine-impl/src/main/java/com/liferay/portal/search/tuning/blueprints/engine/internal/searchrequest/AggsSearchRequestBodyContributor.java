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
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.aggregation.Aggregation;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.aggregation.AggregationConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.internal.aggregation.AggregationTranslatorFactory;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.aggregation.AggregationTranslator;
import com.liferay.portal.search.tuning.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.portal.search.tuning.blueprints.engine.util.BlueprintTemplateVariableParser;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=aggs",
	service = SearchRequestBodyContributor.class
)
public class AggsSearchRequestBodyContributor
	implements SearchRequestBodyContributor {

	@Override
	public void contribute(
		SearchRequestBuilder searchRequestBuilder, Blueprint blueprint,
		ParameterData parameterData, Messages messages) {

		Optional<JSONArray> configurationJSONArrayOptional =
			_blueprintHelper.getAggsConfigurationOptional(blueprint);

		if (!configurationJSONArrayOptional.isPresent()) {
			return;
		}

		JSONArray configurationJSONArray = configurationJSONArrayOptional.get();

		for (int i = 0; i < configurationJSONArray.length(); i++) {
			JSONObject configurationJSONObject =
				configurationJSONArray.getJSONObject(i);

			if (!_validateConfiguration(configurationJSONObject, messages) ||
				!configurationJSONObject.getBoolean(
					AggregationConfigurationKeys.ENABLED.getJsonKey(), true)) {

				continue;
			}

			String type = configurationJSONObject.getString(
				AggregationConfigurationKeys.TYPE.getJsonKey());

			String name = configurationJSONObject.getString(
				AggregationConfigurationKeys.NAME.getJsonKey());

			try {
				AggregationTranslator aggregationTranslator =
					_aggregationBuilderFactory.getTranslator(type);

				JSONObject bodyJSONObject =
					_blueprintTemplateVariableParser.parse(
						configurationJSONObject.getJSONObject(
							AggregationConfigurationKeys.BODY.getJsonKey()),
						parameterData, messages);

				Optional<Aggregation> aggregationOptional =
					aggregationTranslator.translate(
						name, bodyJSONObject, parameterData, messages);

				if (aggregationOptional.isPresent()) {
					searchRequestBuilder.addAggregation(
						aggregationOptional.get());
				}
			}
			catch (IllegalArgumentException illegalArgumentException) {
				messages.addMessage(
					new Message.Builder().className(
						getClass().getName()
					).localizationKey(
						"core.error.unknown-aggregation-type"
					).msg(
						illegalArgumentException.getMessage()
					).rootObject(
						configurationJSONObject
					).rootProperty(
						AggregationConfigurationKeys.TYPE.getJsonKey()
					).rootValue(
						type
					).severity(
						Severity.ERROR
					).throwable(
						illegalArgumentException
					).build());

				_log.error(
					illegalArgumentException.getMessage(),
					illegalArgumentException);
			}
			catch (Exception exception) {
				messages.addMessage(
					new Message.Builder().className(
						getClass().getName()
					).localizationKey(
						"core.error.unknown-aggregation-configuration-error"
					).msg(
						exception.getMessage()
					).rootObject(
						configurationJSONObject
					).severity(
						Severity.ERROR
					).throwable(
						exception
					).build());

				_log.error(exception.getMessage(), exception);
			}
		}
	}

	private boolean _validateConfiguration(
		JSONObject configurationJSONObject, Messages messages) {

		boolean valid = true;

		if (configurationJSONObject.isNull(
				AggregationConfigurationKeys.NAME.getJsonKey())) {

			messages.addMessage(
				new Message.Builder().className(
					getClass().getName()
				).localizationKey(
					"core.error.undefined-aggregation-name"
				).msg(
					"Aggregation name is not defined"
				).rootObject(
					configurationJSONObject
				).rootProperty(
					AggregationConfigurationKeys.NAME.getJsonKey()
				).severity(
					Severity.ERROR
				).build());

			valid = false;

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Aggregation name is not defined [ " +
						configurationJSONObject + "]");
			}
		}

		if (configurationJSONObject.isNull(
				AggregationConfigurationKeys.TYPE.getJsonKey())) {

			messages.addMessage(
				new Message.Builder().className(
					getClass().getName()
				).localizationKey(
					"core.error.undefined-aggregation-type"
				).msg(
					"Aggregation type is not defined"
				).rootObject(
					configurationJSONObject
				).rootProperty(
					AggregationConfigurationKeys.TYPE.getJsonKey()
				).severity(
					Severity.ERROR
				).build());

			valid = false;

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Aggregation type is not defined [ " +
						configurationJSONObject + "]");
			}
		}

		if (configurationJSONObject.isNull(
				AggregationConfigurationKeys.BODY.getJsonKey())) {

			messages.addMessage(
				new Message.Builder().className(
					getClass().getName()
				).localizationKey(
					"core.error.undefined-aggregation-body"
				).msg(
					"Aggregation body is not defined"
				).rootObject(
					configurationJSONObject
				).rootProperty(
					AggregationConfigurationKeys.BODY.getJsonKey()
				).severity(
					Severity.ERROR
				).build());

			valid = false;

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Aggregation body is not defined [ " +
						configurationJSONObject + "]");
			}
		}

		return valid;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AggsSearchRequestBodyContributor.class);

	@Reference
	private AggregationTranslatorFactory _aggregationBuilderFactory;

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	@Reference
	private Queries _queries;

}