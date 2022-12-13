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

package com.liferay.portal.search.web.internal.search.bar.portlet.shared.search.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.search.BooleanClause;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.web.constants.SearchBarPortletKeys;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchContributor;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchSettings;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portletmvc4spring.test.mock.web.portlet.MockPortletPreferences;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.RenderRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Georgel Pop
 */
@RunWith(Arquillian.class)
public class SearchBarPortletSharedSearchContributorTest {

	public static final String SCOPE_EVERYTHING = "everything";

	public static final String SCOPE_THIS_SITE = "this-site";

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group1 = GroupTestUtil.addGroup();
		_group2 = GroupTestUtil.addGroup();
	}

	@Test
	public void testContributeWithScopeEverything() throws Exception {
		PortletSharedSearchSettings portletSharedSearchSettings =
			_getPortletSharedSearchSettings(
				_group2.getGroupId(), SCOPE_EVERYTHING);

		SearchRequestBuilder searchRequestBuilder =
			portletSharedSearchSettings.getFederatedSearchRequestBuilder(
				Optional.empty());

		portletSharedSearchSettings.getPortletPreferencesOptional();

		_searchBarPortletSharedSearchContributor.contribute(
			portletSharedSearchSettings);

		long[] groupIds = searchRequestBuilder.withSearchContextGet(
			searchContext -> searchContext.getGroupIds());

		Assert.assertEquals(Arrays.toString(groupIds), 4, groupIds.length);

		ThemeDisplay themeDisplay =
			portletSharedSearchSettings.getThemeDisplay();

		List<Group> groups = GroupLocalServiceUtil.getGroups(
			themeDisplay.getCompanyId(), GroupConstants.ANY_PARENT_GROUP_ID,
			true);

		int index = 0;

		for (Group group : groups) {
			Assert.assertEquals(group.getGroupId(), groupIds[index++]);
		}
	}

	@Test
	public void testContributeWithScopeThisSite() throws Exception {
		PortletSharedSearchSettings portletSharedSearchSettings =
			_getPortletSharedSearchSettings(
				_group1.getGroupId(), SCOPE_THIS_SITE);

		SearchRequestBuilder searchRequestBuilder =
			portletSharedSearchSettings.getFederatedSearchRequestBuilder(
				Optional.empty());

		portletSharedSearchSettings.getPortletPreferencesOptional();

		_searchBarPortletSharedSearchContributor.contribute(
			portletSharedSearchSettings);

		long[] groupIds = searchRequestBuilder.withSearchContextGet(
			searchContext -> searchContext.getGroupIds());

		Assert.assertEquals(Arrays.toString(groupIds), 1, groupIds.length);

		Assert.assertEquals(_group1.getGroupId(), groupIds[0]);
	}

	private PortletSharedSearchSettings _getPortletSharedSearchSettings(
			long scopeGroupId, String scope)
		throws Exception {

		SearchContext searchContext = new SearchContext();

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setLayout(
			_layoutLocalService.addLayout(
				TestPropsValues.getUserId(), _group1.getGroupId(), false, 0,
				"name", "title", "description", LayoutConstants.TYPE_PORTLET,
				false, StringPool.BLANK,
				ServiceContextTestUtil.getServiceContext(
					_group1.getGroupId())));

		themeDisplay.setCompany(
			_companyLocalService.fetchCompany(TestPropsValues.getCompanyId()));

		themeDisplay.setScopeGroupId(scopeGroupId);

		return new PortletSharedSearchSettings() {

			@Override
			public void addCondition(BooleanClause<Query> booleanClause) {
			}

			@Override
			public void addFacet(Facet facet) {
			}

			@Override
			public SearchRequestBuilder getFederatedSearchRequestBuilder(
				Optional<String> federatedSearchKeyOptional) {

				if (_searchRequestBuilder == null) {
					_searchRequestBuilder =
						_searchRequestBuilderFactory.builder();
				}

				return _searchRequestBuilder;
			}

			@Override
			public Optional<String> getKeywordsParameterName() {
				return Optional.empty();
			}

			@Override
			public Optional<Integer> getPaginationDelta() {
				return Optional.empty();
			}

			@Override
			public Optional<String> getPaginationDeltaParameterName() {
				return Optional.empty();
			}

			@Override
			public Optional<Integer> getPaginationStart() {
				return Optional.empty();
			}

			@Override
			public Optional<String> getPaginationStartParameterName() {
				return Optional.empty();
			}

			@Override
			public Optional<String> getParameter71(String name) {
				return Optional.empty();
			}

			@Override
			public Optional<String> getParameterOptional(String name) {
				return Optional.empty();
			}

			@Override
			public String[] getParameterValues(String name) {
				return new String[0];
			}

			@Override
			public Optional<String[]> getParameterValues71(String name) {
				return Optional.empty();
			}

			@Override
			public String getPortletId() {
				return null;
			}

			@Override
			public Optional<PortletPreferences> getPortletPreferences71() {
				return Optional.empty();
			}

			@Override
			public Optional<PortletPreferences>
				getPortletPreferencesOptional() {

				PortletPreferences portletPreferences =
					new MockPortletPreferences();

				try {
					portletPreferences.setValue("searchScope", scope);
				}
				catch (ReadOnlyException readOnlyException) {
					throw new RuntimeException(readOnlyException);
				}

				return Optional.of(portletPreferences);
			}

			@Override
			public QueryConfig getQueryConfig() {
				return null;
			}

			@Override
			public RenderRequest getRenderRequest() {
				return null;
			}

			@Override
			public Optional<String> getScope() {
				return Optional.empty();
			}

			@Override
			public Optional<String> getScopeParameterName() {
				return Optional.empty();
			}

			@Override
			public SearchContext getSearchContext() {
				return searchContext;
			}

			@Override
			public SearchRequestBuilder getSearchRequestBuilder() {
				return _searchRequestBuilderFactory.builder();
			}

			@Override
			public ThemeDisplay getThemeDisplay() {
				return themeDisplay;
			}

			@Override
			public void setKeywords(String keywords) {
			}

			@Override
			public void setKeywordsParameterName(String keywordsParameterName) {
			}

			@Override
			public void setPaginationDelta(int paginationDelta) {
			}

			@Override
			public void setPaginationDeltaParameterName(
				String paginationDeltaParameterName) {
			}

			@Override
			public void setPaginationStart(int paginationStart) {
			}

			@Override
			public void setPaginationStartParameterName(
				String paginationStartParameterName) {
			}

			@Override
			public void setScope(String scope) {
			}

			@Override
			public void setScopeParameterName(String scopeParameterName) {
			}

			private SearchRequestBuilder _searchRequestBuilder;

		};
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@DeleteAfterTestRun
	private Group _group1;

	@DeleteAfterTestRun
	private Group _group2;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject(filter = "javax.portlet.name=" + SearchBarPortletKeys.SEARCH_BAR)
	private PortletSharedSearchContributor
		_searchBarPortletSharedSearchContributor;

	@Inject
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

}