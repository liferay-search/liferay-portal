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

package com.liferay.search.experiences.federation.internal.download;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.search.experiences.federation.internal.ingestion.pojo.APIPagePOJO;

import java.io.IOException;

import java.net.URL;

/**
 * @author Gustavo Lima
 */
public class DownloadJsonParsed {

	public static APIPagePOJO parse(String url) throws IOException {
		URL myurl = new URL(url);

		return _objectMapper.readValue(myurl, APIPagePOJO.class);
	}

	private static ObjectMapper _getDefaultObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();

		mapper.configure(
			DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		return mapper;
	}

	private static final ObjectMapper _objectMapper = _getDefaultObjectMapper();

}