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

package com.liferay.portal.search.tuning.blueprints.engine.internal.query.postprocessor;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.QueryIndexingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.portal.search.tuning.blueprints.engine.internal.util.ParameterDataUtil;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.query.QueryPostProcessor;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.query.index.util.QueryIndexHelper;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=query_indexer",
	service = QueryPostProcessor.class
)
public class QueryIndexerPostProcessor implements QueryPostProcessor {

	@Override
	public boolean process(
		SearchResponse searchResponse, Blueprint blueprint,
		ParameterData parameterData, Messages messages) {

		SearchHits searchHits = searchResponse.getSearchHits();

		long totalHits = searchHits.getTotalHits();

		if (totalHits == 0) {
			return true;
		}

		Optional<JSONObject> configurationJsonObjectOptional =
			_blueprintHelper.getQueryIndexingConfigurationOptional(blueprint);

		if (!configurationJsonObjectOptional.isPresent()) {
			return true;
		}

		JSONObject configurationJsonObject =
			configurationJsonObjectOptional.get();

		if (!configurationJsonObject.getBoolean(
				QueryIndexingConfigurationKeys.ENABLED.getJsonKey())) {

			return true;
		}

		int threshold = configurationJsonObject.getInt(
			QueryIndexingConfigurationKeys.HITS_THRESHOLD.getJsonKey(), 5);

		if (totalHits < threshold) {
			return true;
		}

		_process(parameterData, messages, configurationJsonObject);

		return true;
	}

	private String _filterKeyKeywords(
			String keywords, JSONObject configurationJsonObject)
		throws Exception {

		JSONArray blackListJsonArray = configurationJsonObject.getJSONArray(
			QueryIndexingConfigurationKeys.BLACKLIST.getJsonKey());

		if ((blackListJsonArray == null) ||
			(blackListJsonArray.length() == 0)) {

			return keywords;
		}

		String regex = "[\\ ,.;\\\\-]+";

		String[] keywordArray = keywords.split(regex);

		for (String keyword : keywordArray) {
			for (int i = 0; i < blackListJsonArray.length(); i++) {
				String exclude = blackListJsonArray.getString(i);

				if (exclude.endsWith("*")) {
					exclude = exclude.substring(0, exclude.length() - 1);

					if (keyword.startsWith(exclude)) {
						if (_log.isDebugEnabled()) {
							_log.debug("Excluding keyword stem " + keyword);
						}

						keywords = StringUtil.removeSubstring(
							keywords, keyword);
					}
				}
				else if (keyword.equals(exclude)) {
					if (_log.isDebugEnabled()) {
						_log.debug("Excluding keyword " + keyword);
					}

					keywords = StringUtil.removeSubstring(keywords, keyword);
				}
			}
		}

		return keywords;
	}

	private void _indexQuery(
		ParameterData parameterData, String keywords,
		JSONObject configurationJsonObject) {

		String queryIndexConfigurationId = configurationJsonObject.getString(
			QueryIndexingConfigurationKeys.QUERY_INDEX_CONFIGURATION_ID.
				getJsonKey());

		Long companyId = ParameterDataUtil.getLongValueByName(
			parameterData, ReservedParameterNames.COMPANY_ID.getKey());

		String languageId = ParameterDataUtil.getStringValueByName(
			parameterData, ReservedParameterNames.LANGUAGE_ID.getKey());

		_queryIndexHelper.indexQuery(
			queryIndexConfigurationId, companyId, languageId, keywords);
	}

	private void _process(
		ParameterData parameterData, Messages messages,
		JSONObject configurationJsonObject) {

		String keywords = parameterData.getKeywords();

		if (Validator.isBlank(keywords)) {
			return;
		}

		try {
			String filteredKeyWords = _filterKeyKeywords(
				keywords, configurationJsonObject);

			if (filteredKeyWords.length() == 0) {
				return;
			}

			_indexQuery(
				parameterData, filteredKeyWords, configurationJsonObject);
		}
		catch (Exception exception) {
 			messages.addMessage(
				new Message.Builder().className(
					getClass().getName()
				).localizationKey(
					"core.error.error-in-indexing-search-query"
				).msg(
						exception.getMessage()
				).severity(
					Severity.ERROR
				).throwable(exception
				).build());

 			_log.error(exception.getMessage(), exception);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		QueryIndexerPostProcessor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private QueryIndexHelper _queryIndexHelper;

}