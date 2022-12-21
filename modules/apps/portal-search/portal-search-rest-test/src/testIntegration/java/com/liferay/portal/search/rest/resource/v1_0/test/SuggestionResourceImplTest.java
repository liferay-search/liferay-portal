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
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.search.rest.dto.v1_0.Suggestion;
import com.liferay.portal.search.rest.dto.v1_0.SuggestionsContributorConfiguration;
import com.liferay.portal.search.rest.dto.v1_0.SuggestionsContributorResults;
import com.liferay.portal.search.rest.resource.v1_0.SuggestionResource;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.pagination.Page;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Georgel Pop
 */
@RunWith(Arquillian.class)
public class SuggestionResourceImplTest {

	public static final String SCOPE_EVERYTHING = "everything";

	public static final String SCOPE_THIS_SITE = "this-site";

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		_company = _companyLocalService.fetchCompany(
			TestPropsValues.getCompanyId());
	}

	@Before
	public void setUp() throws Exception {
		_group1 = GroupTestUtil.addGroup();
		_group2 = GroupTestUtil.addGroup();

		long[] groupIds = {};

		_user = UserTestUtil.addUser(groupIds);

		_role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		_userLocalService.addRoleUser(_role.getRoleId(), _user.getUserId());

		_addFiles();

		_setUpSuggestionResource();

		_setUpSuggestionsContributorConfiguration();
	}

	@After
	public void tearDown() throws Exception {
		_removeFiles();
	}

	@Test
	public void testWithScopeEverythingNoPermission() throws Exception {
		Suggestion[] resultsSuggestions = _getResultsSuggestions(
			SCOPE_EVERYTHING);

		Assert.assertEquals(
			_createErrorMessage(resultsSuggestions), 3,
			resultsSuggestions.length);

		_checkFileNames(
			resultsSuggestions,
			LinkedHashMapBuilder.put(
				TestPropsValues.getGroupId(), true
			).put(
				_group1.getGroupId(), true
			).put(
				_group2.getGroupId(), true
			).build());
	}

	@Test
	public void testWithScopeEverythingViewCompanyPermission()
		throws Exception {

		RoleTestUtil.addResourcePermission(
			_role, DLFileEntry.class.getName(), ResourceConstants.SCOPE_COMPANY,
			String.valueOf(_company.getCompanyId()), ActionKeys.VIEW);

		Suggestion[] resultsSuggestions = _getResultsSuggestions(
			SCOPE_EVERYTHING);

		Assert.assertEquals(
			_createErrorMessage(resultsSuggestions), 6,
			resultsSuggestions.length);

		_checkFileNames(
			resultsSuggestions,
			LinkedHashMapBuilder.put(
				TestPropsValues.getGroupId(), false
			).put(
				_group1.getGroupId(), false
			).put(
				_group2.getGroupId(), false
			).build());
	}

	@Test
	public void testWithScopeEverythingViewGroupPermission() throws Exception {
		RoleTestUtil.addResourcePermission(
			_role, DLFileEntry.class.getName(), ResourceConstants.SCOPE_GROUP,
			String.valueOf(TestPropsValues.getGroupId()), ActionKeys.VIEW);

		Suggestion[] resultsSuggestions = _getResultsSuggestions(
			SCOPE_EVERYTHING);

		Assert.assertEquals(
			_createErrorMessage(resultsSuggestions), 4,
			resultsSuggestions.length);

		_checkFileNames(
			resultsSuggestions,
			LinkedHashMapBuilder.put(
				TestPropsValues.getGroupId(), false
			).put(
				_group1.getGroupId(), true
			).put(
				_group2.getGroupId(), true
			).build());

		RoleTestUtil.addResourcePermission(
			_role, DLFileEntry.class.getName(), ResourceConstants.SCOPE_GROUP,
			String.valueOf(_group1.getGroupId()), ActionKeys.VIEW);

		resultsSuggestions = _getResultsSuggestions(SCOPE_EVERYTHING);

		Assert.assertEquals(
			_createErrorMessage(resultsSuggestions), 5,
			resultsSuggestions.length);

		_checkFileNames(
			resultsSuggestions,
			LinkedHashMapBuilder.put(
				TestPropsValues.getGroupId(), false
			).put(
				_group1.getGroupId(), false
			).put(
				_group2.getGroupId(), true
			).build());

		RoleTestUtil.addResourcePermission(
			_role, DLFileEntry.class.getName(), ResourceConstants.SCOPE_GROUP,
			String.valueOf(_group2.getGroupId()), ActionKeys.VIEW);

		resultsSuggestions = _getResultsSuggestions(SCOPE_EVERYTHING);

		Assert.assertEquals(
			_createErrorMessage(resultsSuggestions), 6,
			resultsSuggestions.length);

		_checkFileNames(
			resultsSuggestions,
			LinkedHashMapBuilder.put(
				TestPropsValues.getGroupId(), false
			).put(
				_group1.getGroupId(), false
			).put(
				_group2.getGroupId(), false
			).build());
	}

	@Test
	public void testWithScopeThisSiteNoPermission() throws Exception {
		Suggestion[] resultsSuggestions = _getResultsSuggestions(
			SCOPE_THIS_SITE);

		Assert.assertEquals(
			_createErrorMessage(resultsSuggestions), 1,
			resultsSuggestions.length);

		_checkFileNames(
			resultsSuggestions,
			LinkedHashMapBuilder.put(
				TestPropsValues.getGroupId(), true
			).build());
	}

	@Test
	public void testWithScopeThisSiteViewCompanyPermission() throws Exception {
		RoleTestUtil.addResourcePermission(
			_role, DLFileEntry.class.getName(), ResourceConstants.SCOPE_COMPANY,
			String.valueOf(_company.getCompanyId()), ActionKeys.VIEW);

		Suggestion[] resultsSuggestions = _getResultsSuggestions(
			SCOPE_THIS_SITE);

		Assert.assertEquals(
			_createErrorMessage(resultsSuggestions), 2,
			resultsSuggestions.length);

		_checkFileNames(
			resultsSuggestions,
			LinkedHashMapBuilder.put(
				TestPropsValues.getGroupId(), false
			).build());
	}

	@Test
	public void testWithScopeThisSiteViewGroupPermission() throws Exception {
		RoleTestUtil.addResourcePermission(
			_role, DLFileEntry.class.getName(), ResourceConstants.SCOPE_GROUP,
			String.valueOf(TestPropsValues.getGroupId()), ActionKeys.VIEW);

		Suggestion[] resultsSuggestions = _getResultsSuggestions(
			SCOPE_THIS_SITE);

		Assert.assertEquals(
			_createErrorMessage(resultsSuggestions), 2,
			resultsSuggestions.length);

		_checkFileNames(
			resultsSuggestions,
			LinkedHashMapBuilder.put(
				TestPropsValues.getGroupId(), false
			).build());
	}

	@Rule
	public TestName testName = new TestName();

	private FileEntry _addFileEntry(long groupId, boolean guestPermission)
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(groupId);

		serviceContext.setAddGuestPermissions(guestPermission);

		Folder folder = _dlAppLocalService.addFolder(
			TestPropsValues.getUserId(), groupId,
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			serviceContext);

		String guestOrMember =
			guestPermission ? _KEYWORD_GUEST : _KEYWORD_MEMBER;

		String fileName = _KEYWORD_SEARCH + guestOrMember + groupId;

		return _dlAppLocalService.addFileEntry(
			null, serviceContext.getUserId(), folder.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(), ContentTypes.TEXT_PLAIN, fileName,
			StringPool.BLANK, StringPool.BLANK, StringPool.BLANK,
			"liferay".getBytes(), null, null, serviceContext);
	}

	private void _addFiles() throws Exception {
		_fileEntriesIds = new long[_FILES_COUNT];

		long[] groupIds = {
			TestPropsValues.getGroupId(), _group1.getGroupId(),
			_group2.getGroupId()
		};

		int index = 0;

		for (long groupId : groupIds) {
			FileEntry fileEntryGuest = _addFileEntry(groupId, true);

			_fileEntriesIds[index++] = fileEntryGuest.getFileEntryId();

			FileEntry fileEntryMembers = _addFileEntry(groupId, false);

			_fileEntriesIds[index++] = fileEntryMembers.getFileEntryId();
		}
	}

	private void _checkFileNames(
		Suggestion[] resultsSuggestions,
		LinkedHashMap<Long, Boolean> groupIdsPermissionsMap) {

		Set<Long> longSet = groupIdsPermissionsMap.keySet();

		longSet.forEach(
			groupId -> {
				Boolean guestOnly = groupIdsPermissionsMap.get(groupId);
				String fileNameGuest =
					_KEYWORD_SEARCH + _KEYWORD_GUEST + groupId;

				Stream<Suggestion> suggestionStream = Arrays.stream(
					resultsSuggestions);

				Assert.assertEquals(
					fileNameGuest,
					suggestionStream.anyMatch(
						suggestion -> fileNameGuest.equals(
							suggestion.getText())),
					true);

				if (!guestOnly) {
					suggestionStream = Arrays.stream(resultsSuggestions);
					String fileNameMember =
						_KEYWORD_SEARCH + _KEYWORD_MEMBER + groupId;

					Assert.assertEquals(
						fileNameMember,
						suggestionStream.anyMatch(
							suggestion -> fileNameMember.equals(
								suggestion.getText())),
						true);
				}
			});
	}

	private String _createErrorMessage(Suggestion[] suggestions) {
		StringBundler sb = new StringBundler((suggestions.length * 2) + 2);

		sb.append(suggestions.length);
		sb.append(" files returned: ");

		for (Suggestion suggestion : suggestions) {
			sb.append(suggestion.getText());
			sb.append(", ");
		}

		sb.setIndex(sb.index() - 1);

		return sb.toString();
	}

	private Suggestion[] _getResultsSuggestions(String scope) throws Exception {
		ThemeDisplay themeDisplay = _getThemeDisplay();

		SuggestionsContributorConfiguration[]
			suggestionsContributorConfigurations =
				new SuggestionsContributorConfiguration[1];

		suggestionsContributorConfigurations[0] =
			_suggestionsContributorConfiguration;

		Page<SuggestionsContributorResults> suggestionsContributorResultsPage =
			_suggestionResource.postSuggestionsPage(
				themeDisplay.getURLCurrent() + _KEYWORD_SEARCH,
				themeDisplay.getLayoutFriendlyURL(themeDisplay.getLayout()),
				themeDisplay.getScopeGroupId(), null, themeDisplay.getPlid(),
				scope, _KEYWORD_SEARCH, suggestionsContributorConfigurations);

		Collection<SuggestionsContributorResults> resultsPageItems =
			suggestionsContributorResultsPage.getItems();

		Iterator<SuggestionsContributorResults> resultsIterator =
			resultsPageItems.iterator();

		SuggestionsContributorResults contributorResults =
			resultsIterator.next();

		return contributorResults.getSuggestions();
	}

	private ThemeDisplay _getThemeDisplay() throws Exception {
		ThemeDisplay themeDisplay = new ThemeDisplay();

		Layout layout = _layoutLocalService.fetchLayoutByFriendlyURL(
			TestPropsValues.getGroupId(), false, "/search");

		themeDisplay.setLayout(layout);

		themeDisplay.setPlid(layout.getPlid());

		themeDisplay.setCompany(_company);

		themeDisplay.setScopeGroupId(TestPropsValues.getGroupId());

		themeDisplay.setURLCurrent("http://localhost:8080/search?q=");

		return themeDisplay;
	}

	private void _removeFiles() throws Exception {
		for (long fileEntryId : _fileEntriesIds) {
			_dlAppLocalService.deleteFileEntry(fileEntryId);
		}
	}

	private void _setUpSuggestionResource() {
		_suggestionResource.setContextCompany(_company);
		_suggestionResource.setGroupLocalService(
			GroupLocalServiceUtil.getService());
		_suggestionResource.setContextAcceptLanguage(
			new AcceptLanguage() {

				@Override
				public List<Locale> getLocales() {
					return Arrays.asList(LocaleUtil.getDefault());
				}

				@Override
				public String getPreferredLanguageId() {
					return LocaleUtil.toLanguageId(LocaleUtil.getDefault());
				}

				@Override
				public Locale getPreferredLocale() {
					return LocaleUtil.getDefault();
				}

			});
		_suggestionResource.setContextUser(_user);

		ReflectionTestUtil.setFieldValue(
			_suggestionResource, "contextHttpServletRequest",
			new MockHttpServletRequest());
		ReflectionTestUtil.setFieldValue(
			_suggestionResource, "contextHttpServletResponse",
			new MockHttpServletResponse());
	}

	private void _setUpSuggestionsContributorConfiguration() {
		_suggestionsContributorConfiguration =
			new SuggestionsContributorConfiguration();

		_suggestionsContributorConfiguration.setDisplayGroupName(
			testName.getMethodName());

		_suggestionsContributorConfiguration.setContributorName("basic");

		_suggestionsContributorConfiguration.setSize(_FILES_COUNT);
	}

	private static final int _FILES_COUNT = 6;

	private static final String _KEYWORD_GUEST = " guest ";

	private static final String _KEYWORD_MEMBER = " member ";

	private static final String _KEYWORD_SEARCH = "_FileTestSearchThis_";

	private static Company _company;

	@Inject
	private static CompanyLocalService _companyLocalService;

	@Inject
	private DLAppLocalService _dlAppLocalService;

	private long[] _fileEntriesIds;

	@DeleteAfterTestRun
	private Group _group1;

	@DeleteAfterTestRun
	private Group _group2;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@DeleteAfterTestRun
	private Role _role;

	@Inject
	private SuggestionResource _suggestionResource;

	private SuggestionsContributorConfiguration
		_suggestionsContributorConfiguration;

	@DeleteAfterTestRun
	private User _user;

	@Inject
	private UserLocalService _userLocalService;

}