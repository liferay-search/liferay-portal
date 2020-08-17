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

package com.liferay.portal.search.tuning.gsearch.searchrequest.contributor.searcher.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.query.MatchAllQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.test.util.SearchTestRule;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SearchConfiguration;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationService;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.users.admin.test.util.search.GroupBlueprint;
import com.liferay.users.admin.test.util.search.GroupSearchFixture;
import com.liferay.users.admin.test.util.search.UserSearchFixture;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Wade Cao
 */
@RunWith(Arquillian.class)
public class GSearchSearchRequestContributorTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_userSearchFixture = new UserSearchFixture();

		_userSearchFixture.setUp();

		_users = _userSearchFixture.getUsers();

		_addGroupAndUser();

		SearchConfiguration searchConfiguration =
			_addCompanySearchConfiguration();

		_searchConfigurationId = searchConfiguration.getSearchConfigurationId();
	}

	@After
	public void tearDown() throws Exception {
		_searchConfigurationService.deleteSearchConfiguration(
			_searchConfigurationId);
	}

	@Test
	public void testUserKeywordSearch() throws Exception {
		_addUsers("user1", "user2", "user3");

		List<String> expectedEntryClassNames = Arrays.asList(
			User.class.getCanonicalName(), User.class.getCanonicalName(),
			User.class.getCanonicalName());

		_assertSearch(expectedEntryClassNames, "user");
	}

	@Rule
	public SearchTestRule searchTestRule = new SearchTestRule();

	private SearchConfiguration _addCompanySearchConfiguration()
		throws Exception {

		Map<Locale, String> titleMap = HashMapBuilder.put(
			LocaleUtil.US, "testTitle"
		).build();

		Map<Locale, String> descriptionMap = HashMapBuilder.put(
			LocaleUtil.US, "testDescription"
		).build();

		MatchAllQuery matchAll = _queries.matchAll();

		return _searchConfigurationService.addCompanySearchConfiguration(
			titleMap, descriptionMap, matchAll.toString(), 1,
			_getServiceContext());
	}

	private void _addGroupAndUser() throws Exception {
		GroupSearchFixture groupSearchFixture = new GroupSearchFixture();

		_group = groupSearchFixture.addGroup(new GroupBlueprint());

		_groups = groupSearchFixture.getGroups();

		_user = TestPropsValues.getUser();

		PermissionThreadLocal.setPermissionChecker(
			_permissionCheckerFactory.create(_user));
	}

	private void _addUsers(String... userNames) throws Exception {
		String[] assetTagNames = {};

		for (String userName : userNames) {
			_userSearchFixture.addUser(userName, _group, assetTagNames);
		}
	}

	private void _assertSearch(
			List<?> expectedEntryClassNames, String searchTerm)
		throws Exception {

		SearchResponse searchResponse = _searcher.search(
			_searchRequestBuilderFactory.builder(
			).companyId(
				_group.getCompanyId()
			).groupIds(
				_group.getGroupId()
			).queryString(
				searchTerm
			).build());

		Stream<Document> documentsStream = searchResponse.getDocumentsStream();

		List<String> acturalEntryClassNames = documentsStream.map(
			document -> document.getString(Field.ENTRY_CLASS_NAME)
		).collect(
			Collectors.toList()
		);

		Assert.assertEquals(
			searchResponse.getRequestString(), expectedEntryClassNames,
			acturalEntryClassNames);
	}

	private ServiceContext _getServiceContext() throws Exception {
		return ServiceContextTestUtil.getServiceContext(
			_group.getGroupId(), _user.getUserId());
	}

	private Group _group;

	@DeleteAfterTestRun
	private List<Group> _groups;

	@Inject
	private PermissionCheckerFactory _permissionCheckerFactory;

	@Inject
	private Queries _queries;

	private long _searchConfigurationId;

	@Inject
	private SearchConfigurationService _searchConfigurationService;

	@Inject
	private Searcher _searcher;

	@Inject
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	private User _user;

	@DeleteAfterTestRun
	private List<User> _users;

	private UserSearchFixture _userSearchFixture;

}