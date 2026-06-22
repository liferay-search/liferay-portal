/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.journal.search.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactory;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.service.PortalPreferencesLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.SearchContextTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.legacy.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.model.uid.UIDFactory;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.TermQuery;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.test.rule.SearchTestRule;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * POC (LPD-95367): proves that the search layer can return the correct asset
 * <em>version</em> for a preview context, entirely at query time, without a
 * post-fetch entity swap.
 *
 * <p>
 * This is an integration-test-only proof of the mechanism described in the
 * Preview Framework search POC working doc. It exercises Journal Articles
 * because Journal already indexes every version (drafts included) when
 * {@code indexAllArticleVersionsEnabled} is true, which models the per-version
 * indexing object entries are assumed to gain.
 * </p>
 *
 * <p>
 * The "preview swap" is expressed as the per-entity rewrite of the default
 * {@code head=true} constraint:
 * </p>
 *
 * <pre>
 * (head=true AND ENTRY_CLASS_PK NOT IN previewedResourcePrimKeys)
 *     OR (UID IN previewTargetUIDs)
 * </pre>
 *
 * <p>
 * Key Journal facts this relies on: each version is its own document with a
 * distinct {@code UID} keyed by {@code id_}; all versions share
 * {@code ENTRY_CLASS_PK = resourcePrimKey}; the {@code head} flag is true only on
 * the latest approved version; normal search adds a required {@code head=true}
 * term via {@code JournalArticleModelPreFilterContributor}.
 * </p>
 *
 * @author Prathima Shreenath
 */
@RunWith(Arquillian.class)
public class JournalArticlePreviewSearchPOCTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		// Force per-version indexing so draft versions are indexed as their
		// own searchable documents (the precondition for the query-time swap).

		PortalPreferences portalPreferences =
			_portletPreferencesFactory.getPortalPreferences(
				TestPropsValues.getUserId(), true);

		_originalPortalPreferencesXML = _portletPreferencesFactory.toXML(
			portalPreferences);

		portalPreferences.setValue(
			"", "indexAllArticleVersionsEnabled", "true");

		_portalPreferencesLocalService.updatePreferences(
			TestPropsValues.getCompanyId(),
			PortletKeys.PREFS_OWNER_TYPE_COMPANY,
			PortletPreferencesFactoryUtil.toXML(portalPreferences));
	}

	@After
	public void tearDown() throws Exception {
		_portalPreferencesLocalService.updatePreferences(
			TestPropsValues.getCompanyId(),
			PortletKeys.PREFS_OWNER_TYPE_COMPANY,
			_originalPortalPreferencesXML);
	}

	@Test
	public void testScenario1BaselineNoPreviewContext() throws Exception {

		// No preview context: a match-all search returns only the live
		// (approved/head) versions. Drafts must not appear.

		JournalArticle approvedArticle = _addApprovedArticle(
			"alpha shared approvedword");

		JournalArticle draftArticle = _addDraftVersion(
			approvedArticle, "alpha shared draftword");

		JournalArticle otherApprovedArticle = _addApprovedArticle(
			"beta shared");

		SearchResponse searchResponse = _search(_baselineSearchContext(), null);

		_assertUIDs(searchResponse, approvedArticle, otherApprovedArticle);

		Assert.assertFalse(
			"Draft version must not appear without a preview context",
			_uids(
				searchResponse
			).contains(
				_uidFactory.getUID(draftArticle)
			));
	}

	@Test
	public void testScenario2AdHocSingleSwap() throws Exception {

		// Preview maps one entry to its draft version. A match-all search swaps
		// that one entry to its draft and leaves everything else live.

		JournalArticle approvedArticle = _addApprovedArticle(
			"alpha shared approvedword");

		JournalArticle draftArticle = _addDraftVersion(
			approvedArticle, "alpha shared draftword");

		JournalArticle otherApprovedArticle = _addApprovedArticle(
			"beta shared");

		SearchResponse searchResponse = _search(
			_previewSearchContext(), _previewFilter(draftArticle));

		_assertUIDs(searchResponse, draftArticle, otherApprovedArticle);

		Assert.assertFalse(
			"Live version of the previewed entry must be swapped out",
			_uids(
				searchResponse
			).contains(
				_uidFactory.getUID(approvedArticle)
			));
	}

	@Test
	public void testScenario2MatchesDraftOnlyContent() throws Exception {

		// The crux: a keyword that exists only in the draft must match under a
		// preview context, and must not match without one. This is what a
		// post-fetch swap can never achieve.

		JournalArticle approvedArticle = _addApprovedArticle(
			"alpha shared approvedword");

		JournalArticle draftArticle = _addDraftVersion(
			approvedArticle, "alpha shared draftword");

		SearchContext baselineSearchContext = _baselineSearchContext();

		baselineSearchContext.setKeywords("draftword");

		Assert.assertEquals(
			"Draft-only content must not match without a preview context", 0,
			_search(
				baselineSearchContext, null
			).getCount());

		SearchContext previewSearchContext = _previewSearchContext();

		previewSearchContext.setKeywords("draftword");

		SearchResponse searchResponse = _search(
			previewSearchContext, _previewFilter(draftArticle));

		_assertUIDs(searchResponse, draftArticle);
	}

	@Test
	public void testScenario3LaunchMultiAssetSwap() throws Exception {

		// A launch swaps multiple entries at once via a single terms filter.

		JournalArticle approvedArticle1 = _addApprovedArticle(
			"alpha shared approvedword");

		JournalArticle draftArticle1 = _addDraftVersion(
			approvedArticle1, "alpha shared draftword");

		JournalArticle approvedArticle2 = _addApprovedArticle(
			"beta shared approvedword");

		JournalArticle draftArticle2 = _addDraftVersion(
			approvedArticle2, "beta shared draftword");

		JournalArticle unmappedApprovedArticle = _addApprovedArticle(
			"gamma shared");

		SearchResponse searchResponse = _search(
			_previewSearchContext(),
			_previewFilter(draftArticle1, draftArticle2));

		_assertUIDs(
			searchResponse, draftArticle1, draftArticle2,
			unmappedApprovedArticle);
	}

	@Rule
	public SearchTestRule searchTestRule = new SearchTestRule();

	private JournalArticle _addApprovedArticle(String title) throws Exception {
		return JournalTestUtil.addArticle(_group.getGroupId(), title, title);
	}

	private JournalArticle _addDraftVersion(
			JournalArticle approvedArticle, String title)
		throws Exception {

		// Save an UNAPPROVED draft version: workflowEnabled=true +
		// approved=false routes to ACTION_SAVE_DRAFT (otherwise the helper
		// publishes). Reuse the approved version's valid article XML for
		// content; vary only the title, where the draft keyword lives.

		return JournalTestUtil.updateArticle(
			approvedArticle, title, approvedArticle.getContent(), true, false,
			ServiceContextTestUtil.getServiceContext());
	}

	private void _assertUIDs(
		SearchResponse searchResponse, JournalArticle... expectedArticles) {

		List<String> expectedUIDs = new ArrayList<>();

		for (JournalArticle expectedArticle : expectedArticles) {
			expectedUIDs.add(_uidFactory.getUID(expectedArticle));
		}

		Collections.sort(expectedUIDs);

		List<String> actualUIDs = _uids(searchResponse);

		Collections.sort(actualUIDs);

		Assert.assertEquals(
			searchResponse.getRequestString(), expectedUIDs, actualUIDs);
	}

	private SearchContext _baselineSearchContext() throws Exception {
		SearchContext searchContext = SearchContextTestUtil.getSearchContext(
			_group.getGroupId());

		searchContext.setGroupIds(new long[] {_group.getGroupId()});

		return searchContext;
	}

	private BooleanQuery _previewFilter(JournalArticle... draftArticles) {
		TermsQuery previewedResourcePrimKeysTermsQuery = new TermsQuery(
			Field.ENTRY_CLASS_PK);
		TermsQuery previewTargetUIDsTermsQuery = new TermsQuery(Field.UID);

		for (JournalArticle draftArticle : draftArticles) {
			previewedResourcePrimKeysTermsQuery.addValue(
				String.valueOf(draftArticle.getResourcePrimKey()));
			previewTargetUIDsTermsQuery.addValue(
				_uidFactory.getUID(draftArticle));
		}

		BooleanQuery liveForUnmappedBooleanQuery = new BooleanQuery();

		liveForUnmappedBooleanQuery.addMustQueryClauses(
			new TermQuery("head", Boolean.TRUE));
		liveForUnmappedBooleanQuery.addMustNotQueryClauses(
			previewedResourcePrimKeysTermsQuery);

		BooleanQuery previewBooleanQuery = new BooleanQuery();

		previewBooleanQuery.addShouldQueryClauses(
			liveForUnmappedBooleanQuery, previewTargetUIDsTermsQuery);
		previewBooleanQuery.setMinimumShouldMatch(1);

		return previewBooleanQuery;
	}

	private SearchContext _previewSearchContext() throws Exception {
		SearchContext searchContext = _baselineSearchContext();

		// Drop the blanket head=true / approved-only restriction so the preview
		// filter can select specific draft versions.

		searchContext.setAttribute(Field.STATUS, WorkflowConstants.STATUS_ANY);
		searchContext.setAttribute("head", Boolean.FALSE);

		return searchContext;
	}

	private SearchResponse _search(
		SearchContext searchContext, BooleanQuery postFilterBooleanQuery) {

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder(
				searchContext
			).emptySearchEnabled(
				true
			).modelIndexerClasses(
				JournalArticle.class
			);

		if (postFilterBooleanQuery != null) {
			searchRequestBuilder = searchRequestBuilder.postFilterQuery(
				postFilterBooleanQuery);
		}

		return _searcher.search(searchRequestBuilder.build());
	}

	private List<String> _uids(SearchResponse searchResponse) {
		List<String> uids = new ArrayList<>();

		for (Document document : searchResponse.getDocuments()) {
			uids.add(document.getString(Field.UID));
		}

		return uids;
	}

	@DeleteAfterTestRun
	private Group _group;

	private String _originalPortalPreferencesXML;

	@Inject
	private PortalPreferencesLocalService _portalPreferencesLocalService;

	@Inject
	private PortletPreferencesFactory _portletPreferencesFactory;

	@Inject
	private Searcher _searcher;

	@Inject
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Inject
	private UIDFactory _uidFactory;

}