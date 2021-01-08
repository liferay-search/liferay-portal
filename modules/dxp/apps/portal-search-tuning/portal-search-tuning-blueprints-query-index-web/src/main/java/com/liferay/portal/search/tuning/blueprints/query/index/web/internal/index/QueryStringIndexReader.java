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

package com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index;

import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexName;

import java.util.Optional;

/**
 * @author Petteri Karttunen
 */
public interface QueryStringIndexReader {

	public Optional<String> fetchIdOptional(
		QueryStringIndexName queryStringIndexName, long companyId, long groupId,
		String keywords);

	public Optional<QueryString> fetchQueryStringOptional(
		QueryStringIndexName queryStringIndexName, String id);

	public boolean isIndexExists(QueryStringIndexName queryStringIndexName);

}