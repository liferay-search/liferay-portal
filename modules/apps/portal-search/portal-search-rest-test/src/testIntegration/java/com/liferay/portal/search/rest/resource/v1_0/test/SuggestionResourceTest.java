/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.search.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.search.rest.client.dto.v1_0.Suggestion;
import com.liferay.portal.search.rest.client.dto.v1_0.SuggestionsContributorConfiguration;
import com.liferay.portal.search.rest.client.dto.v1_0.SuggestionsContributorResults;
import com.liferay.portal.search.rest.client.pagination.Page;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Petteri Karttunen
 */
@RunWith(Arquillian.class)
public class SuggestionResourceTest extends BaseSuggestionResourceTestCase {

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_layout = LayoutTestUtil.addTypeContentLayout(_group);

		super.setUp();
	}

	@Test
	public void testPostSuggestionsPage() throws Exception {
		String displayGroupName = "suggestions";

		String title = "Document Title";

		_addJournalArticle(title);

		SuggestionsContributorConfiguration[]
			suggestionsContributorConfigurations = {
				_getSuggestionsContributorConfiguration(
					"basic", displayGroupName)
			};

		Page<SuggestionsContributorResults> suggestionPage =
			_postSuggestionsPage(
				"Document", suggestionsContributorConfigurations);

		Assert.assertEquals(1, suggestionPage.getTotalCount());

		SuggestionsContributorResults suggestionsContributorResults =
			suggestionPage.fetchFirstItem();

		Assert.assertEquals(
			displayGroupName,
			suggestionsContributorResults.getDisplayGroupName());

		Suggestion[] suggestions =
			suggestionsContributorResults.getSuggestions();

		Suggestion suggestion = suggestions[0];

		Assert.assertEquals(title, suggestion.getText());

		JSONObject suggestionAttributesJSONObject =
			_getSuggestionAttributesAsJSONObject(suggestion);

		Assert.assertEquals(
			title, suggestionAttributesJSONObject.get("assetSearchSummary"));
	}

	private JournalArticle _addJournalArticle(String title) throws Exception {
		return JournalTestUtil.addArticle(_group.getGroupId(), title, "");
	}

	private JSONObject _getSuggestionAttributesAsJSONObject(
			Suggestion suggestion)
		throws Exception {

		Object suggestionAttributes = suggestion.getAttributes();

		return JSONFactoryUtil.createJSONObject(
			suggestionAttributes.toString());
	}

	private SuggestionsContributorConfiguration
		_getSuggestionsContributorConfiguration(
			String contributorName, String displayGroupName) {

		SuggestionsContributorConfiguration
			suggestionsContributorConfiguration =
				new SuggestionsContributorConfiguration();

		suggestionsContributorConfiguration.setContributorName(contributorName);

		suggestionsContributorConfiguration.setDisplayGroupName(
			displayGroupName);

		return suggestionsContributorConfiguration;
	}

	private Page<SuggestionsContributorResults> _postSuggestionsPage(
			String search,
			SuggestionsContributorConfiguration[]
				suggestionsContributorConfigurations)
		throws Exception {

		return suggestionResource.postSuggestionsPage(
			"http://localhost:8080/web/guest/home", "%2Fsearch",
			_group.getGroupId(), _layout.getPlid(), "everything", search,
			suggestionsContributorConfigurations);
	}

	private Group _group;
	private Layout _layout;

}