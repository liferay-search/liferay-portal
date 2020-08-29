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
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.IndexWriterHelper;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.suggester.KeywordIndexingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.spi.query.QueryPostProcessor;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * TODO:https://issues.liferay.com/browse/LPS-118888
 *
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = QueryPostProcessor.class)
public class KeywordIndexerPostProcessor implements QueryPostProcessor {

	@Override
	public boolean process(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse) {

		Optional<JSONObject> keywordIndexingConfigurationJsonObjectOptional =
			_blueprintHelper.getKeywordIndexingConfigurationOptional(
				searchRequestContext.getBlueprint());

		if (!keywordIndexingConfigurationJsonObjectOptional.isPresent()) {
			return true;
		}

		JSONObject keywordIndexingconfigurationJsonObject =
			keywordIndexingConfigurationJsonObjectOptional.get();

		boolean enabled = keywordIndexingconfigurationJsonObject.getBoolean(
			KeywordIndexingConfigurationKeys.ENABLED.getJsonKey());

		if (!enabled) {
			return true;
		}

		SearchHits searchHits = searchResponse.getSearchHits();

		String keywords = searchRequestContext.getKeywords();

		int hitsThreshold = keywordIndexingconfigurationJsonObject.getInt(
			KeywordIndexingConfigurationKeys.HITS_THRESHOLD.getJsonKey(), 2);

		if (!Validator.isBlank(keywords) &&
			(searchHits.getTotalHits() >= hitsThreshold)) {

			keywords = _filterKeywords(
				keywordIndexingconfigurationJsonObject, keywords);

			if (keywords.length() == 0) {
				return true;
			}

			_addDocument(searchRequestContext, keywords);
		}

		return true;
	}

	private void _addDocument(
		SearchRequestContext searchRequestContext, String keywords) {

		// TODO: https://issues.liferay.com/browse/LPS-118888

	}

	private String _filterKeywords(
		JSONObject keywordIndexingconfigurationJsonObject, String keywords) {

		JSONObject blackListConfigurationJsonObject =
			keywordIndexingconfigurationJsonObject.getJSONObject(
				KeywordIndexingConfigurationKeys.BLACKLIST_CONFIGURATION.
					getJsonKey());

		if (blackListConfigurationJsonObject == null) {
			return keywords;
		}

		JSONArray excludedWordsJsonArray =
			blackListConfigurationJsonObject.getJSONArray("blacklist");

		if ((excludedWordsJsonArray == null) ||
			(excludedWordsJsonArray.length() == 0)) {

			return keywords;
		}

		String[] excludedWords = JSONUtil.toStringArray(excludedWordsJsonArray);

		String splitter = blackListConfigurationJsonObject.getString(
			"keyword_splitter", " ");

		try {
			String[] keywordArray = keywords.split(splitter);

			for (String keyword : keywordArray) {
				for (String exclude : excludedWords) {
					if (exclude.endsWith("*")) {
						exclude = exclude.substring(0, exclude.length() - 1);

						if (keyword.startsWith(exclude)) {
							if (_log.isDebugEnabled()) {
								_log.debug(
									"Excluding keyword by stem: " + keyword);
							}

							keywords = StringUtil.removeSubstring(
								keywords, keyword);
						}
					}
					else if (keyword.equals(exclude)) {
						if (_log.isDebugEnabled()) {
							_log.debug("Excluding keyword: " + keyword);
						}

						keywords = StringUtil.removeSubstring(
							keywords, keyword);
					}
				}
			}
		}
		catch (Exception exception) {
			_log.error(exception.getMessage(), exception);
		}

		return keywords;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		KeywordIndexerPostProcessor.class);

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private IndexWriterHelper _indexWriterHelper;

}