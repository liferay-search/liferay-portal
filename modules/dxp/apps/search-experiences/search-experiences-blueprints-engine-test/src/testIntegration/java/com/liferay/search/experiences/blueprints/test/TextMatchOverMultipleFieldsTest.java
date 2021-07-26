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
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DataGuard;
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
		addJournalArticle("drink carbonated pepsi", "carbonated cola cola");
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
			"[drink carbonated coca, drink carbonated pepsi, sprite, fruit " +
				"punch]",
			"coca cola", getSelectedElementString());
	}

	@Test
	public void testSearchMultipleFieldsTypeWithOperator() throws Exception {
		addJournalArticle("drink carbonated pepsi", "carbonated cola cola");
		addJournalArticle("drink carbonated coca", "carbonated cola");
		addJournalArticle("sprite", "carbonated cola cola");
		addJournalArticle("fruit punch", "non-carbonated cola");

		Blueprint blueprint = addCompanyBlueprint(
			Collections.singletonMap(
				LocaleUtil.US, getClass().getName() + "Blueprint"),
			Collections.singletonMap(LocaleUtil.US, ""),
			getConfigurationString((JSONObject[])null), "");

		_testSearchMostFieldsWithOperator(blueprint);
		_testSearchBestFieldsWithOperatorOr(blueprint);
		_testSearchPhrasePrefixWithOperator(blueprint);
		_testSearchBoolPrefixWithOperator(blueprint);
	}

	@Test
	public void testSearchPhraseWithOperatorAnd() throws Exception {
		addJournalArticle("drink carbonated pepsi", "carbonated cola cola");
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
				getMultiMatchQueryElementJSONObject(1, null, "and", "phrase")),
			"[drink carbonated pepsi, sprite]", "cola cola",
			getSelectedElementString());
	}

	@Test
	public void testSearchPhraseWithOperatorOr() throws Exception {
		addJournalArticle("drink carbonated pepsi", "carbonated cola cola");
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
				getMultiMatchQueryElementJSONObject(1, null, "or", "phrase")),
			"[drink carbonated pepsi, sprite]", "cola cola",
			getSelectedElementString());
	}

	@Test
	public void testSearchWithCrossFields() throws Exception {
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

	private void _testSearchBestFieldsWithOperatorOr(Blueprint blueprint)
		throws Exception {

		assertSearch(
			blueprint,
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					1, 2, 20, "AUTO", "or", "best_fields")),
			"[drink carbonated coca, drink carbonated pepsi, sprite, fruit " +
				"punch]",
			"carbonated cola", getSelectedElementString());
	}

	private void _testSearchBoolPrefixWithOperator(Blueprint blueprint)
		throws Exception {

		assertSearch(
			blueprint,
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					1, null, "or", "bool_prefix")),
			"[drink carbonated pepsi, drink carbonated coca]", "drink",
			getSelectedElementString());
		assertSearch(
			blueprint,
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					1, null, "and", "bool_prefix")),
			"[drink carbonated coca]", "drink carbonated co",
			getSelectedElementString());
	}

	private void _testSearchMostFieldsWithOperator(Blueprint blueprint)
		throws Exception {

		assertSearch(
			blueprint,
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					10, 2, 200, "AUTO", null, "or", "most_fields")),
			"[drink carbonated pepsi, sprite, drink carbonated coca, fruit " +
				"punch]",
			"carbonated cola cola", getSelectedElementString());
		assertSearch(
			blueprint,
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					10, 2, 20, "AUTO", null, "and", "most_fields")),
			"[drink carbonated coca, drink carbonated pepsi, sprite, fruit " +
				"punch]",
			"coca carbonated", getSelectedElementString());
	}

	private void _testSearchPhrasePrefixWithOperator(Blueprint blueprint)
		throws Exception {

		assertSearchIgnoreRelevance(
			blueprint,
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					1, null, "or", "phrase_prefix")),
			"[drink carbonated coca, drink carbonated pepsi]",
			"drink carbonated", getSelectedElementString());
		assertSearchIgnoreRelevance(
			blueprint,
			getConfigurationString(
				getMultiMatchQueryElementJSONObject(
					1, null, "and", "phrase_prefix")),
			"[drink carbonated coca, drink carbonated pepsi]", "drink",
			getSelectedElementString());
	}

}