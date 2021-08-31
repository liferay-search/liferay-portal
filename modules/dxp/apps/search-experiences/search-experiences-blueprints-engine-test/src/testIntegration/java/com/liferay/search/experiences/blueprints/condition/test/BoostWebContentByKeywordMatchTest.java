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

package com.liferay.search.experiences.blueprints.condition.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.test.BaseBlueprintsTestCase;

import java.util.Collections;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Wade Cao
 */
@RunWith(Arquillian.class)
public class BoostWebContentByKeywordMatchTest extends BaseBlueprintsTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testContainsCondition() throws Exception {
		addJournalArticle("Coca Cola", "cola cola");

		JournalArticle journalArticle = addJournalArticle("Pepsi Cola", "");

		String articleId = journalArticle.getArticleId();

		Blueprint blueprint = addCompanyBlueprint(
			Collections.singletonMap(
				LocaleUtil.US, getClass().getName() + "Blueprint"),
			Collections.singletonMap(LocaleUtil.US, ""),
			getConfigurationString((JSONObject[])null), "");

		assertSearch(blueprint, null, "[coca cola, pepsi cola]", "cola", null);
		assertSearch(
			blueprint,
			getConfigurationString(
				_getQueryElementJSONObject(articleId, 100, "contains", "cola")),
			"[pepsi cola, coca cola]", "cola", getSelectedElementString());
		assertSearch(
			blueprint,
			getConfigurationString(
				_getQueryElementJSONObject(
					articleId, 100, "not_contains", "cola")),
			"[coca cola, pepsi cola]", "cola", getSelectedElementString());
	}

	private JSONObject _getQueryElementJSONObject(
		String articleId, int boost, String evaluationType, String keywords) {

		return JSONUtil.put(
			"category", "conditional"
		).put(
			"clauses",
			createJSONArray().put(
				JSONUtil.put(
					"context", "query"
				).put(
					"occur", "should"
				).put(
					"query",
					JSONUtil.put(
						"wrapper",
						JSONUtil.put(
							"query",
							JSONUtil.put(
								"terms",
								JSONUtil.put(
									"articleId_String_sortable",
									createJSONArray().put(articleId)
								).put(
									"boost", boost
								))))
				))
		).put(
			"conditions",
			JSONUtil.put(
				evaluationType,
				JSONUtil.put(
					"parameter_name", "${keywords}"
				).put(
					"value", createJSONArray().put(keywords)
				))
		).put(
			"description",
			JSONUtil.put(
				"en_US", "Show selected Web Contents higher in the results")
		).put(
			"enabled", true
		).put(
			"icon", "thumbs-up"
		).put(
			"title",
			JSONUtil.put("en_US", "Boost Web Contents by Keyword Match")
		);
	}

}