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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.response;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.response.ResponseAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.spi.response.ResponseContributor;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ResponseBuilder.class)
public class ResponseBuilderImpl implements ResponseBuilder {

	@Override
	public JSONObject build(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse,
		ResponseAttributes responseAttributes) {

		long startTime = System.currentTimeMillis();

		JSONObject responseJsonObject = JSONFactoryUtil.createJSONObject();

		for (ResponseContributor responseContributor : _responseContributors) {
			responseContributor.contribute(
				searchRequestContext, searchResponse, responseAttributes,
				responseJsonObject);
		}

		if (_log.isDebugEnabled()) {
			_log.debug(
				"Building response took took: " +
					(System.currentTimeMillis() - startTime));
		}

		return responseJsonObject;
	}

	protected void addResponseContributor(
		ResponseContributor responseContributor) {

		_responseContributors.add(responseContributor);
	}

	protected void removeResponseContributor(
		ResponseContributor responseContributor) {

		_responseContributors.remove(responseContributor);
	}

	private static final Logger _log = LoggerFactory.getLogger(
		ResponseBuilderImpl.class);

	@Reference(
		bind = "addResponseContributor",
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC, service = ResponseContributor.class,
		unbind = "removeResponseContributor"
	)
	private volatile List<ResponseContributor> _responseContributors =
		new ArrayList<>();

}