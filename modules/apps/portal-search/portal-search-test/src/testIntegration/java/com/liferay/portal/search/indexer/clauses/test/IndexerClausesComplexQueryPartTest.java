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

package com.liferay.portal.search.indexer.clauses.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.blogs.model.BlogsEntry;
import com.liferay.blogs.service.BlogsEntryLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.test.util.search.JournalArticleBlueprintBuilder;
import com.liferay.journal.test.util.search.JournalArticleContent;
import com.liferay.journal.test.util.search.JournalArticleSearchFixture;
import com.liferay.journal.test.util.search.JournalArticleTitle;
import com.liferay.message.boards.constants.MBCategoryConstants;
import com.liferay.message.boards.constants.MBMessageConstants;
import com.liferay.message.boards.model.MBMessage;
import com.liferay.message.boards.service.MBMessageLocalService;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.BaseIndexer;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.search.filter.ComplexQueryPart;
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.query.MatchQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.portal.search.test.blogs.util.BlogsEntrySearchFixture;
import com.liferay.portal.search.test.util.DocumentsAssert;
import com.liferay.portal.search.test.util.SearchTestRule;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.users.admin.test.util.search.GroupBlueprint;
import com.liferay.users.admin.test.util.search.GroupSearchFixture;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Adam Brandizzi
 * @author André de Oliveira
 */
@RunWith(Arquillian.class)
public class IndexerClausesComplexQueryPartTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		BlogsEntrySearchFixture blogsEntrySearchFixture =
			new BlogsEntrySearchFixture(blogsEntryLocalService);

		GroupSearchFixture groupSearchFixture = new GroupSearchFixture();

		JournalArticleSearchFixture journalArticleSearchFixture =
			new JournalArticleSearchFixture(journalArticleLocalService);

		_blogsEntries = blogsEntrySearchFixture.getBlogsEntries();
		_blogsEntrySearchFixture = blogsEntrySearchFixture;
		_group = groupSearchFixture.addGroup(new GroupBlueprint());
		_groups = groupSearchFixture.getGroups();
		_journalArticles = journalArticleSearchFixture.getJournalArticles();
		_journalArticleSearchFixture = journalArticleSearchFixture;
		_user = TestPropsValues.getUser();
	}

	@Test
	public void testBaseIndexer() throws Exception {
		Assert.assertTrue(journalArticleIndexer instanceof BaseIndexer);

		addJournalArticle("Gamma Article");
		addJournalArticle("Omega Article");

		assertSearch(JournalArticle.class, "Gamma Article", "Omega Article");
	}

	@Test
	public void testDefaultIndexer() throws Exception {
		Assert.assertEquals(
			"class com.liferay.portal.search.internal.indexer.DefaultIndexer",
			String.valueOf(blogsEntryIndexer.getClass()));

		addBlogsEntry("Gamma Blog");
		addBlogsEntry("Omega Blog");

		assertSearch(BlogsEntry.class, "Gamma Blog", "Omega Blog");
	}

	@Test
	public void testFacetedSearcher() throws Exception {
		addBlogsEntry("Gamma Blog");
		addBlogsEntry("Omega Blog");
		addJournalArticle("Gamma Article");
		addJournalArticle("Omega Article");
		addMessage("Gamma Message");
		addMessage("Omega Message");

		assertSearch(
			Arrays.asList(BlogsEntry.class, JournalArticle.class),
			Arrays.asList("Gamma Article", "Gamma Blog"),
			Arrays.asList("Omega Article", "Omega Blog"),
			Arrays.asList("Omega Message"));
	}

	@Rule
	public SearchTestRule searchTestRule = new SearchTestRule();

	protected BlogsEntry addBlogsEntry(String title) throws Exception {
		return _blogsEntrySearchFixture.addBlogsEntry(_group, _user, title);
	}

	protected JournalArticle addJournalArticle(String title) {
		return _journalArticleSearchFixture.addArticle(
			JournalArticleBlueprintBuilder.builder(
			).groupId(
				_group.getGroupId()
			).journalArticleContent(
				new JournalArticleContent() {
					{
						put(LocaleUtil.US, RandomTestUtil.randomString());

						setDefaultLocale(LocaleUtil.US);
						setName("content");
					}
				}
			).journalArticleTitle(
				new JournalArticleTitle() {
					{
						put(LocaleUtil.US, title);
					}
				}
			).userId(
				_user.getUserId()
			).build());
	}

	protected MBMessage addMessage(String title) throws Exception {
		return mbMessageLocalService.addMessage(
			null, _user.getUserId(), RandomTestUtil.randomString(),
			_group.getGroupId(), MBCategoryConstants.DEFAULT_PARENT_CATEGORY_ID,
			0L, MBMessageConstants.DEFAULT_PARENT_MESSAGE_ID, title,
			RandomTestUtil.randomString(), MBMessageConstants.DEFAULT_FORMAT,
			null, false, 0.0, false, _createServiceContext());
	}

	protected void assertSearch(
		Class<?> clazz, String indexerValue, String partValue) {

		assertSearch(
			Arrays.asList(clazz), Arrays.asList(indexerValue),
			Arrays.asList(partValue), Arrays.asList());
	}

	protected void assertSearch(
		List<Class<?>> classes, List<String> indexerValues,
		List<String> partValues, List<String> partAdditiveValues) {

		Consumer<SearchRequestBuilder> consumer =
			searchRequestBuilder -> searchRequestBuilder.modelIndexerClasses(
				classes.toArray(new Class<?>[0])
			).queryString(
				getFirstWord(indexerValues.get(0))
			);

		MatchQuery query = _queries.match(
			_TITLE_EN_US, getFirstWord(partValues.get(0)));

		List<String> bothValues = ListUtil.concat(indexerValues, partValues);

		assertSearch(indexerValues, consumer);
		assertSearch(bothValues, withoutIndexerClauses(), consumer);

		assertSearch(indexerValues, withPart("should", query), consumer);
		assertSearch(
			bothValues, withPart("should", query), consumer,
			withoutIndexerClauses());

		assertSearch(Arrays.asList(), withPart("must", query), consumer);
		assertSearch(
			partValues, withPart("must", query), consumer,
			withoutIndexerClauses());

		List<String> allValues = ListUtil.concat(
			bothValues, partAdditiveValues);

		assertSearch(allValues, withPartAdditive("should", query), consumer);
		assertSearch(
			allValues, withPartAdditive("should", query), consumer,
			withoutIndexerClauses());

		List<String> allPartValues = ListUtil.concat(
			partValues, partAdditiveValues);

		assertSearch(allPartValues, withPartAdditive("must", query), consumer);
		assertSearch(
			allPartValues, withPartAdditive("must", query), consumer,
			withoutIndexerClauses());
	}

	protected void assertSearch(
		List<String> expectedValues,
		Consumer<SearchRequestBuilder>... consumers) {

		SearchResponse searchResponse = searcher.search(
			getSearchRequestBuilder(
			).withSearchRequestBuilder(
				consumers
			).build());

		DocumentsAssert.assertValuesIgnoreRelevance(
			searchResponse.getRequestString(),
			searchResponse.getDocumentsStream(), _TITLE_EN_US,
			expectedValues.stream());
	}

	protected ComplexQueryPart getComplexQueryPart(Query query) {
		return _complexQueryPartBuilderFactory.builder(
		).occur(
			"must"
		).query(
			query
		).build();
	}

	protected SearchRequestBuilder getSearchRequestBuilder() {
		return searchRequestBuilderFactory.builder(
		).companyId(
			_group.getCompanyId()
		).fields(
			StringPool.STAR
		).groupIds(
			_group.getGroupId()
		);
	}

	protected Consumer<SearchRequestBuilder> withoutIndexerClauses() {
		return searchRequestBuilder -> searchRequestBuilder.withSearchContext(
			searchContext -> searchContext.setAttribute(
				"search.full.query.suppress.indexer.provided.clauses", true));
	}

	protected Consumer<SearchRequestBuilder> withPart(
		String occur, Query query) {

		return searchRequestBuilder -> searchRequestBuilder.addComplexQueryPart(
			_complexQueryPartBuilderFactory.builder(
			).occur(
				occur
			).query(
				query
			).build());
	}

	protected Consumer<SearchRequestBuilder> withPartAdditive(
		String occur, Query query) {

		return searchRequestBuilder -> searchRequestBuilder.addComplexQueryPart(
			_complexQueryPartBuilderFactory.builder(
			).additive(
				true
			).occur(
				occur
			).query(
				query
			).build());
	}

	@Inject(filter = "indexer.class.name=com.liferay.blogs.model.BlogsEntry")
	protected Indexer<BlogsEntry> blogsEntryIndexer;

	@Inject
	protected BlogsEntryLocalService blogsEntryLocalService;

	@Inject(filter = "component.name=*.JournalArticleIndexer")
	protected Indexer<JournalArticle> journalArticleIndexer;

	@Inject
	protected JournalArticleLocalService journalArticleLocalService;

	@Inject
	protected MBMessageLocalService mbMessageLocalService;

	@Inject
	protected Searcher searcher;

	@Inject
	protected SearchRequestBuilderFactory searchRequestBuilderFactory;

	private ServiceContext _createServiceContext() throws Exception {
		return ServiceContextTestUtil.getServiceContext(
			_group.getGroupId(), _user.getUserId());
	}

	private String getFirstWord(String indexerTarget) {
		List<String> words = StringUtil.split(indexerTarget, CharPool.SPACE);

		return words.get(0);
	}

	private static final String _TITLE_EN_US = StringBundler.concat(
		Field.TITLE, StringPool.UNDERLINE, LocaleUtil.US);

	@DeleteAfterTestRun
	private List<BlogsEntry> _blogsEntries;

	private BlogsEntrySearchFixture _blogsEntrySearchFixture;

	@Inject
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Inject
	private FacetedSearcherManager _facetedSearcherManager;

	private Group _group;

	@DeleteAfterTestRun
	private List<Group> _groups;

	@DeleteAfterTestRun
	private List<JournalArticle> _journalArticles;

	private JournalArticleSearchFixture _journalArticleSearchFixture;

	@Inject
	private Queries _queries;

	@Inject
	private Sorts _sorts;

	private User _user;

	@DeleteAfterTestRun
	private List<User> _users;

}