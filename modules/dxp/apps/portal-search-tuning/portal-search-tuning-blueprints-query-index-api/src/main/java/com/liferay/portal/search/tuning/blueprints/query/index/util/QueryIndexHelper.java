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

package com.liferay.portal.search.tuning.blueprints.query.index.util;

import com.liferay.portal.search.tuning.blueprints.query.index.constants.Reason;

/**
 * @author Petteri Karttunen
 */
public interface QueryIndexHelper {

	public void indexKeywords(
		long companyId, long groupId, String languageId, String keywords);

	public boolean isAnalyzedLanguage(String languageId);

	public void reportKeywords(
		long companyId, long groupId, String keywords, Reason reason);

}