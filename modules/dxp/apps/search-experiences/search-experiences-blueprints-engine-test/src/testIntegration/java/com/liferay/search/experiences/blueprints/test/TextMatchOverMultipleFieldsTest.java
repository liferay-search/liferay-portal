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
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.wiki.model.WikiNode;

import java.util.Collections;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Wade Cao
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class TextMatchOverMultipleFieldsTest extends BaseQueryElementsTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testSearchBestFieldsWithOperatorAnd() throws Exception {
		addJournalArticle(
			"drink carbonated pepsi cola", "carbonated cola cola");
		addJournalArticle("drink carbonated coca", "carbonated cola");
		addJournalArticle("sprite", "carbonated cola cola");
		addJournalArticle("fruit punch", "non-carbonated cola");

		assertSearch(
			addCompanyBlueprint(
				Collections.singletonMap(
					LocaleUtil.US, getClass().getName() + "Blueprint"),
				Collections.singletonMap(LocaleUtil.US, ""),
				getConfigurationString((JSONObject[])null), ""),
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					1, 2, 1, "AUTO", "and", "best_fields")),
			"[drink carbonated coca, drink carbonated pepsi cola, sprite, " +
				"fruit punch]",
			"coca cola", getSelectedElementString());
	}

	@Test
	public void testSearchBestFieldsWithOperatorOr() throws Exception {
		WikiNode wikiNode = addWikiNode();

		addWikiPage(wikiNode, "lorem ipsum sit", "ipsum sit sit");
		addWikiPage(wikiNode, "lorem ipsum dolor", "ipsum sit");
		addWikiPage(wikiNode, "nunquis", "non-lorem ipsum sit");

		assertSearch(
			addCompanyBlueprint(
				Collections.singletonMap(
					LocaleUtil.US, getClass().getName() + "Blueprint"),
				Collections.singletonMap(LocaleUtil.US, ""),
				getConfigurationString((JSONObject[])null), ""),
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					1, 1, 1, "0", "or", "best_fields")),
			"[lorem ipsum sit, lorem ipsum dolor, nunquis]", "ipsum sit sit",
			getSelectedElementString());
	}

	@Test
	public void testSearchBoolPrefixWithOperator() throws Exception {
		addBlogsEntry("lorem ipsum sit", "ipsum sit sit");
		addBlogsEntry("lorem ipsum dolor", "ipsum sit");
		addBlogsEntry("amet", "ipsum sit sit");
		addBlogsEntry("nunquis", "non-lorem ipsum sit");

		Blueprint blueprint = addCompanyBlueprint(
			Collections.singletonMap(
				LocaleUtil.US, getClass().getName() + "Blueprint"),
			Collections.singletonMap(LocaleUtil.US, ""),
			getConfigurationString((JSONObject[])null), "");

		assertSearchIgnoreRelevance(
			blueprint,
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					1, null, "or", "bool_prefix")),
			"[lorem ipsum dolor, lorem ipsum sit, nunquis]", "lorem dol",
			getSelectedElementString());
		assertSearchIgnoreRelevance(
			blueprint,
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					1, null, "and", "bool_prefix")),
			"[lorem ipsum dolor]", "lorem dol", getSelectedElementString());
	}

	@Test
	public void testSearchCrossFieldsWithOperator() throws Exception {
		addJournalArticle("alpha beta", "foxtrot, golf");
		addJournalArticle("alpha edison", "hotel golf");
		addJournalArticle("beta charlie", "alpha");
		addJournalArticle("edison india", "beta");

		Blueprint blueprint = addCompanyBlueprint(
			Collections.singletonMap(
				LocaleUtil.US, getClass().getName() + "Blueprint"),
			Collections.singletonMap(LocaleUtil.US, ""),
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					1, null, "or", "cross_fields")),
			getSelectedElementString());

		assertSearchIgnoreRelevance(
			blueprint, null, "[alpha beta, alpha edison, beta charlie]",
			"alpha golf", "");
		assertSearchIgnoreRelevance(
			blueprint,
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					1, null, "and", "cross_fields")),
			"[alpha beta, alpha edison]", "alpha golf",
			getSelectedElementString());
	}

	@Test
	public void testSearchMostFieldsWithOperator() throws Exception {
		addBlogsEntry("lorem ipsum sit", "ipsum sit sit");
		addBlogsEntry("lorem ipsum dolor", "ipsum sit");
		addBlogsEntry("amet", "ipsum sit sit");
		addBlogsEntry("nunquis", "non-lorem ipsum sit");

		Blueprint blueprint = addCompanyBlueprint(
			Collections.singletonMap(
				LocaleUtil.US, getClass().getName() + "Blueprint"),
			Collections.singletonMap(LocaleUtil.US, ""),
			getConfigurationString((JSONObject[])null), "");

		assertSearch(
			blueprint,
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					1, 1, 1, "0", null, "or", "most_fields")),
			"[lorem ipsum sit, amet, lorem ipsum dolor, nunquis]",
			"ipsum sit sit", getSelectedElementString());
		assertSearch(
			blueprint,
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					1, 1, 1, "0", null, "and", "most_fields")),
			"[lorem ipsum sit, nunquis]", "sit lorem",
			getSelectedElementString());
	}

	@Test
	public void testSearchPhrase() throws Exception {
		addJournalArticle("listen to birds", "listen listen to birds");
		addJournalArticle("listen to planes", "listen to birds");
		addJournalArticle("silence", "listen listen to birds");
		addJournalArticle("listen something", "do not listen to birds");

		assertSearch(
			addCompanyBlueprint(
				Collections.singletonMap(
					LocaleUtil.US, getClass().getName() + "Blueprint"),
				Collections.singletonMap(LocaleUtil.US, ""),
				getConfigurationString((JSONObject[])null), ""),
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(1, null, null, "phrase")),
			"[listen to birds, silence]", "listen listen",
			getSelectedElementString());
	}

	@Test
	public void testSearchPhrasePrefix() throws Exception {
		addJournalArticle(
			"watch birds on the sky", "simple things are beautiful");
		addJournalArticle(
			"watch planes on the sky", "simple things are not good");
		addJournalArticle("clouds", "simple things are beautiful sometimes");
		addJournalArticle("watch trains", "simple things are bad");

		assertSearch(
			addCompanyBlueprint(
				Collections.singletonMap(
					LocaleUtil.US, getClass().getName() + "Blueprint"),
				Collections.singletonMap(LocaleUtil.US, ""),
				getConfigurationString((JSONObject[])null), ""),
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					1, null, null, "phrase_prefix")),
			"[watch birds on the sky, clouds]", "simple things are beau",
			getSelectedElementString());
	}

	@Override
	protected JSONObject getFrameworkConfiguration() {
		JSONObject jsonObject = super.getFrameworkConfiguration();

		JSONArray fieldsJSONArray = (JSONArray)jsonObject.get(
			"searchable_asset_types");

		fieldsJSONArray.put("com.liferay.blogs.model.BlogsEntry");

		return jsonObject;
	}

}