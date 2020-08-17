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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.response.contributor;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.tuning.blueprints.engine.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.util.ResponseUtil;
import com.liferay.portal.search.tuning.blueprints.engine.response.ResponseAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.spi.response.ResponseContributor;

import java.util.IllegalFormatException;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ResponseContributor.class)
public class MetaResponseContributor implements ResponseContributor {

	@Override
	public void contribute(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse,
		ResponseAttributes responseAttributes, JSONObject responseJsonObject) {

		responseJsonObject.put(
			JSONResponseKeys.META,
			_getMeta(searchRequestContext, searchResponse));
	}

	private JSONObject _getMeta(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse) {

		JSONObject metaJsonObject = JSONFactoryUtil.createJSONObject();

		String initialKeywords = searchRequestContext.getInitialKeywords(
		).orElse(
			null
		);

		if (Validator.isBlank(initialKeywords)) {
			metaJsonObject.put(
				JSONResponseKeys.INITIAL_KEYWORDS, initialKeywords);
		}

		metaJsonObject.put(
			JSONResponseKeys.KEYWORDS, searchRequestContext.getRawKeywords());

		Hits hits = searchResponse.getHits();

		try {
			metaJsonObject.put(
				JSONResponseKeys.EXECUTION_TIME,
				String.format("%.3f", hits.getSearchTime()));
		}
		catch (IllegalFormatException illegalFormatException) {
			_log.error(
				illegalFormatException.getMessage(), illegalFormatException);
		}

		try {
			metaJsonObject.put(
				JSONResponseKeys.START,
				ResponseUtil.getStart(
					searchRequestContext, searchResponse.getSearchHits()));
		}
		catch (ArithmeticException arithmeticException) {
			_log.error(arithmeticException.getMessage(), arithmeticException);
		}

		metaJsonObject.put(JSONResponseKeys.TOTAL_HITS, hits.getLength());

		return metaJsonObject;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MetaResponseContributor.class);

}