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

package com.liferay.search.experiences.predict.misspellings.web.internal.util;

import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.search.experiences.predict.misspellings.web.internal.index.MisspellingSetFields;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = MisspellingsQueryHelper.class)
public class MisspellingsQueryHelper {

	public void addGroupFilterClause(
		BooleanQuery booleanQuery, long... groupIds) {

		BooleanQuery groupQuery = _queries.booleanQuery();

		TermsQuery termsQuery = _queries.terms(MisspellingSetFields.GROUP_ID);

		termsQuery.addValues(ArrayUtil.toStringArray(groupIds));

		groupQuery.addShouldQueryClauses(termsQuery);

		groupQuery.addShouldQueryClauses(
			_queries.term(MisspellingSetFields.GROUP_ID, "*"));

		booleanQuery.addFilterQueryClauses(groupQuery);
	}

	public void addLanguageFilterClause(
		BooleanQuery booleanQuery, Object... languageIds) {

		BooleanQuery languageQuery = _queries.booleanQuery();

		TermsQuery termsQuery = _queries.terms(MisspellingSetFields.GROUP_ID);

		termsQuery.addValues(languageIds);

		languageQuery.addShouldQueryClauses(termsQuery);

		languageQuery.addShouldQueryClauses(
			_queries.term(MisspellingSetFields.LANGUAGE_ID, "*"));

		booleanQuery.addFilterQueryClauses(languageQuery);
	}

	@Reference
	private Queries _queries;

}