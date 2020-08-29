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

package com.liferay.portal.search.tuning.blueprints.engine.internal.searchrequest.data.contributor;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.aggregation.Aggregation;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.aggregation.AggregationConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.internal.aggregation.AggregationRequestBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.searchrequest.SearchRequestData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.aggregation.AggregationRequestBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.spi.searchrequest.SearchRequestDataContributor;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SearchRequestDataContributor.class)
public class AggregationsSearchRequestDataContributor
	implements SearchRequestDataContributor {

	public void contribute(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData) {

		Optional<JSONArray> aggregationConfigurationJsonArrayOptional =
			_blueprintHelper.getAggregationConfigurationOptional(
				searchRequestContext.getBlueprint());

		if (!aggregationConfigurationJsonArrayOptional.isPresent()) {
			return;
		}

		JSONArray aggregationConfigurationJsonArray =
			aggregationConfigurationJsonArrayOptional.get();

		for (int i = 0; i < aggregationConfigurationJsonArray.length(); i++) {
			JSONObject aggregationJsonObject =
				aggregationConfigurationJsonArray.getJSONObject(i);

			if (!_validate(searchRequestContext, aggregationJsonObject) ||
				aggregationJsonObject.getBoolean(
					AggregationConfigurationKeys.ENABLED.getJsonKey(), false)) {

				continue;
			}

			String type = aggregationJsonObject.getString(
				AggregationConfigurationKeys.TYPE.getJsonKey());

			String name = aggregationJsonObject.getString(
				AggregationConfigurationKeys.NAME.getJsonKey());

			try {
				JSONObject aggregationConfigurationJsonObject =
					aggregationJsonObject.getJSONObject(
						AggregationConfigurationKeys.BODY.getJsonKey());

				AggregationRequestBuilder aggregationBuilder =
					_aggregationBuilderFactory.getBuilder(type);

				Optional<Aggregation> aggregationOptional =
					aggregationBuilder.build(
						searchRequestContext,
						aggregationConfigurationJsonObject, name);

				if (aggregationOptional.isPresent()) {
					List<Aggregation> aggregations =
						searchRequestData.getAggregations();

					aggregations.add(aggregationOptional.get());
				}
			}
			catch (IllegalArgumentException illegalArgumentException) {
				searchRequestContext.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.unknown-aggregation-type",
						illegalArgumentException.getMessage(),
						illegalArgumentException, aggregationJsonObject,
						AggregationConfigurationKeys.TYPE.getJsonKey(), type));

				_log.error(
					illegalArgumentException.getMessage(),
					illegalArgumentException);
			}
			catch (Exception exception) {
				searchRequestContext.addMessage(
					new Message(
						Severity.ERROR, "core",
						"core.error.unknown-aggregation-configuration-error",
						exception.getMessage(), exception,
						aggregationJsonObject, null, null));

				_log.error(exception.getMessage(), exception);
			}
		}
	}

	private boolean _validate(
		SearchRequestContext searchRequestContext,
		JSONObject configurationJsonObject) {

		boolean valid = true;

		if (configurationJsonObject.isNull(
				AggregationConfigurationKeys.NAME.getJsonKey())) {

			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-aggregation-name", null, null,
					configurationJsonObject,
					AggregationConfigurationKeys.NAME.getJsonKey(), null));
			valid = false;

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Aggregation name undefined [ " + configurationJsonObject +
						"]");
			}
		}

		if (configurationJsonObject.isNull(
				AggregationConfigurationKeys.TYPE.getJsonKey())) {

			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-aggregation-type", null, null,
					configurationJsonObject,
					AggregationConfigurationKeys.TYPE.getJsonKey(), null));
			valid = false;

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Aggregation type undefined [ " + configurationJsonObject +
						"]");
			}
		}

		if (configurationJsonObject.isNull(
				AggregationConfigurationKeys.BODY.getJsonKey())) {

			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-aggregation-body", null, null,
					configurationJsonObject,
					AggregationConfigurationKeys.BODY.getJsonKey(), null));
			valid = false;

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Aggregation body undefined [ " + configurationJsonObject +
						"]");
			}
		}

		return valid;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AggregationsSearchRequestDataContributor.class);

	@Reference
	private AggregationRequestBuilderFactory _aggregationBuilderFactory;

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private Queries _queries;

}