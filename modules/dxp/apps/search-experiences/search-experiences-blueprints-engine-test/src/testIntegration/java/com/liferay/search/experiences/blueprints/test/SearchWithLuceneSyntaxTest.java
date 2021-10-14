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
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.search.experiences.blueprints.Blueprint;

import java.util.Collections;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Wade Cao
 */
@RunWith(Arquillian.class)
public class SearchWithLuceneSyntaxTest extends BaseBlueprintsTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testSearchWithLuceneSyntax() throws Exception {
		addJournalArticle("Coca Cola", "");
		addJournalArticle("Pepsi Cola", "");

		Blueprint blueprint = addCompanyBlueprint(
			Collections.singletonMap(
				LocaleUtil.US, getClass().getName() + "Blueprint"),
			Collections.singletonMap(LocaleUtil.US, ""),
			getConfigurationString((JSONObject[])null), "");

		assertSearch(
			blueprint, null, "[coca cola, pepsi cola]", "cola +coca", null);
		assertSearch(
			blueprint,
			getConfigurationString(_getQueryElementJSONObject(1, 2, "and", 2)),
			"[coca cola]", "cola +coca", getSelectedElementString());
		assertSearch(
			blueprint,
			getConfigurationString(_getQueryElementJSONObject(1, 2, "and", 2)),
			"[pepsi cola]", "cola -coca", getSelectedElementString());
	}

	private JSONObject _getQueryElementJSONObject(
		int boost, int contentBoost, String operator, int titleBoost) {

		JSONArray fieldsJSONArray = createJSONArray();

		return JSONUtil.put(
			"category", "match"
		).put(
			"clauses",
			createJSONArray().put(
				JSONUtil.put(
					"context", "query"
				).put(
					"occur", "must"
				).put(
					"query",
					JSONUtil.put(
						"wrapper",
						JSONUtil.put(
							"query",
							JSONUtil.put(
								"simple_query_string",
								JSONUtil.put(
									"boost", boost
								).put(
									"default_operator", operator
								).put(
									"fields",
									fieldsJSONArray.put(
										"localized_title${context." +
											"language_id}^" + titleBoost
									).put(
										"content${context.language_id}^" +
											contentBoost
									)
								).put(
									"query", "${keywords}"
								))))
				))
		).put(
			"conditions", JSONFactoryUtil.createJSONObject()
		).put(
			"description",
			JSONUtil.put("en_US", "Enable searching using the Lucene syntax")
		).put(
			"enabled", true
		).put(
			"icon", "picture"
		).put(
			"title", JSONUtil.put("en_US", "Search with the Lucene Syntax")
		);
	}

}