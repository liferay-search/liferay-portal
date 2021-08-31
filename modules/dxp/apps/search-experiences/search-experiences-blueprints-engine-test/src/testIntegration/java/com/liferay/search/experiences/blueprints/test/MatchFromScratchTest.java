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

package com.liferay.search.experiences.blueprints.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.search.experiences.blueprints.model.Blueprint;

import java.util.Collections;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Wade Cao
 */
@RunWith(Arquillian.class)
public class MatchFromScratchTest extends BaseQueryElementsTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testSearchWithPasteESQueryMust() throws Exception {
		addJournalArticle("Cafe Rio", "Los Angeles");
		addJournalArticle("Starbucks Cafe", "Los Angeles");
		addJournalArticle("Cloud Cafe", "Orange County");
		addJournalArticle("Denny's", "Los Angeles");

		Blueprint blueprint = addCompanyBlueprint(
			Collections.singletonMap(
				LocaleUtil.US, getClass().getName() + "Blueprint"),
			Collections.singletonMap(LocaleUtil.US, ""),
			getConfigurationString(
				getMatchQueryElementJSONObject(200, "must", "orange county"),
				getMultiMatchQueryElementJSONObject(1, "AUTO", "or")),
			getSelectedElementString());

		assertSearchIgnoreRelevance(
			blueprint, null, "[cloud cafe]", "cafe", null);
		assertSearchIgnoreRelevance(
			blueprint,
			getConfigurationString(
				getMatchQueryElementJSONObject(200, "must", "los angeles"),
				getMultiMatchQueryElementJSONObject(1, "AUTO", "or")),
			"[cafe rio, starbucks cafe]", "cafe", getSelectedElementString());
	}

}