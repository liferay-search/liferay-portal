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

package com.liferay.portal.search.tuning.blueprints.searchrequest.contributor.searcher.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.CharPool;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.test.util.DocumentsAssert;
import com.liferay.portal.search.test.util.SearchTestRule;
import com.liferay.portal.search.tuning.blueprints.engine.constants.SearchContextAttributeKeys;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.users.admin.test.util.search.GroupBlueprint;
import com.liferay.users.admin.test.util.search.GroupSearchFixture;
import com.liferay.users.admin.test.util.search.UserSearchFixture;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

/**
 * @author Wade Cao
 */
@RunWith(Arquillian.class)
public class BlueprintsSearchRequestContributorTest {

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
	}

	@Test
	public void testIPStack() throws Exception {
		_addCompanyBlueprint(readConfiguration());

		_addUsers("Brea", "Helsinki");

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				getIPStackConfigurationTemporarySwapper("91.233.116.229")) {

			_assertSearch("firstName", "[Helsinki]");
		}

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				getIPStackConfigurationTemporarySwapper("104.172.41.95")) {

			_assertSearch("firstName", "[Brea]");
		}
	}

	@Test
	public void testQuery() throws Exception {
		_addCompanyBlueprint(readConfiguration());

		_addUsers("user1", "user2", "user3");

		_assertSearch("screenName", "[user3]");
	}

	@Test
	public void testSort() throws Exception {
		_addCompanyBlueprint(readConfiguration());

		_addUsers("user1", "user2", "user3");

		_assertSearch("screenName", "[user3, user2, user1]");
	}

	@Rule
	public SearchTestRule searchTestRule = new SearchTestRule();

	@Rule
	public TestName testName = new TestName();

	protected ConfigurationTemporarySwapper
			getIPStackConfigurationTemporarySwapper(String ip)
		throws Exception {

		return new ConfigurationTemporarySwapper(
			"com.liferay.portal.search.tuning.blueprints.ipstack.internal." +
				"configuration.IPStackConfiguration",
			_toDictionary(
				HashMapBuilder.put(
					"isEnabled", "true"
				).put(
					"testIpAddress", ip
				).build()));
	}

	protected String readConfiguration() {
		Class<?> clazz = getClass();

		StringBundler sb = new StringBundler(5);

		sb.append(clazz.getSimpleName());
		sb.append(CharPool.PERIOD);
		sb.append(testName.getMethodName());
		sb.append(CharPool.PERIOD);
		sb.append("json");

		return StringUtil.read(clazz, sb.toString());
	}

	private Blueprint _addCompanyBlueprint(String configurationString)
		throws Exception {

		Blueprint blueprint = _blueprintService.addCompanyBlueprint(
			Collections.singletonMap(LocaleUtil.US, "testTitle"),
			Collections.singletonMap(LocaleUtil.US, "testDescription"),
			configurationString, "", 1, _getServiceContext());

		_blueprint = blueprint;

		return blueprint;
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
			_userSearchFixture.addUser(
				userName, userName, userName, LocaleUtil.getDefault(), _group,
				assetTagNames);
		}
	}

	private void _assertSearch(String fieldName, String expected) {
		SearchResponse searchResponse = _searcher.search(
			_searchRequestBuilderFactory.builder(
			).addComplexQueryPart(
				_complexQueryPartBuilderFactory.builder(
				).field(
					"screenName"
				).occur(
					"must_not"
				).type(
					"term"
				).value(
					"test"
				).build()
			).companyId(
				_group.getCompanyId()
			).emptySearchEnabled(
				true
			).groupIds(
				_group.getGroupId()
			).withSearchContext(
				searchContext -> searchContext.setAttribute(
					SearchContextAttributeKeys.BLUEPRINT_ID,
					_blueprint.getBlueprintId())
			).build());

		DocumentsAssert.assertValues(
			searchResponse.getRequestString(),
			searchResponse.getDocumentsStream(), fieldName, expected);
	}

	private ServiceContext _getServiceContext() throws Exception {
		return ServiceContextTestUtil.getServiceContext(
			_group.getGroupId(), _user.getUserId());
	}

	private Dictionary<String, Object> _toDictionary(Map<String, String> map) {
		return new HashMapDictionary<>(new HashMap<String, Object>(map));
	}

	@DeleteAfterTestRun
	private Blueprint _blueprint;

	@Inject
	private BlueprintService _blueprintService;

	@Inject
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	private Group _group;

	@DeleteAfterTestRun
	private List<Group> _groups;

	@Inject
	private PermissionCheckerFactory _permissionCheckerFactory;

	@Inject
	private Searcher _searcher;

	@Inject
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	private User _user;

	@DeleteAfterTestRun
	private List<User> _users;

	private UserSearchFixture _userSearchFixture;

}