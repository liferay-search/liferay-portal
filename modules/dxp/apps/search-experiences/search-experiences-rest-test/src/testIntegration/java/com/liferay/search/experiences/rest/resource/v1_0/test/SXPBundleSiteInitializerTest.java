/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.security.script.management.test.rule.ScriptManagementConfigurationTestRule;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.search.experiences.rest.dto.v1_0.Configuration;
import com.liferay.search.experiences.rest.dto.v1_0.GeneralConfiguration;
import com.liferay.search.experiences.rest.dto.v1_0.SXPBlueprint;
import com.liferay.search.experiences.rest.resource.v1_0.SXPBlueprintResource;
import com.liferay.site.initializer.SiteInitializer;
import com.liferay.site.initializer.SiteInitializerFactory;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Rodrigo Guedes de Souza
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class SXPBundleSiteInitializerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE,
			ScriptManagementConfigurationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group, TestPropsValues.getUserId());

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.USER, TestPropsValues.getUser());
		mockHttpServletRequest.setParameter(
			"currentURL", "http://www.liferay.com");

		_serviceContext.setRequest(mockHttpServletRequest);

		ServiceContextThreadLocal.pushServiceContext(_serviceContext);
	}

	@After
	public void tearDown() throws Exception {
		ServiceContextThreadLocal.popServiceContext();

		GroupLocalServiceUtil.deleteGroup(_group);
	}

	@Test
	public void testInitializeFromFile() throws Exception {
		File tempDir1 = _getTempDir(
			"/com.liferay.site.initializer.extender.test.bundle.1.jar");
		File tempDir2 = _getTempDir(
			"/com.liferay.site.initializer.extender.test.bundle.2.jar");

		try {
			_test1(
				_siteInitializerFactory.create(
					new File(tempDir1, "site-initializer"), null));
			_test2(
				_siteInitializerFactory.create(
					new File(tempDir2, "site-initializer"), null));
		}
		finally {
			FileUtil.deltree(tempDir1);
			FileUtil.deltree(tempDir2);
		}
	}

	private void _assertSearchableAssetTypes(
		String[] className, Configuration configuration) {

		GeneralConfiguration generalConfiguration =
			configuration.getGeneralConfiguration();

		Assert.assertTrue(
			ArrayUtil.containsAll(
				generalConfiguration.getSearchableAssetTypes(), className));
	}

	private void _assertSXPBlueprint1() throws Exception {
		SXPBlueprintResource.Builder sxpBlueprintResourceBuilder =
			_sxpBlueprintResourceFactory.create();

		SXPBlueprintResource sxpBlueprintResource =
			sxpBlueprintResourceBuilder.user(
				_serviceContext.fetchUser()
			).build();

		SXPBlueprint sxpBlueprint =
			sxpBlueprintResource.getSXPBlueprintByExternalReferenceCode(
				"TESTSXPBLUEPRINT1");

		Assert.assertNotNull(sxpBlueprint);
		_assertSearchableAssetTypes(
			new String[] {"com.liferay.journal.model.JournalArticle"},
			sxpBlueprint.getConfiguration());
		Assert.assertEquals("Test SXBlueprint 1", sxpBlueprint.getTitle());

		sxpBlueprint =
			sxpBlueprintResource.getSXPBlueprintByExternalReferenceCode(
				"TESTSXPBLUEPRINT2");

		Assert.assertNotNull(sxpBlueprint);
		Assert.assertFalse(
			sxpBlueprint.toString(
			).contains(
				"[$TAXONOMY_CATEGORY_ID:/site-initializer/taxonomy-" +
					"vocabularies/company/test-asset-vocabulary-1/test-asset-" +
						"category-1.json$]"
			));
		_assertSearchableAssetTypes(
			new String[] {
				"com.liferay.document.library.kernel.model.DLFileEntry"
			},
			sxpBlueprint.getConfiguration());
		Assert.assertEquals("Test SXBlueprint 2", sxpBlueprint.getTitle());
	}

	private void _assertSXPBlueprint2() throws Exception {
		SXPBlueprintResource.Builder sxpBlueprintResourceBuilder =
			_sxpBlueprintResourceFactory.create();

		SXPBlueprintResource sxpBlueprintResource =
			sxpBlueprintResourceBuilder.user(
				_serviceContext.fetchUser()
			).build();

		SXPBlueprint sxpBlueprint =
			sxpBlueprintResource.getSXPBlueprintByExternalReferenceCode(
				"TESTSXPBLUEPRINT1");

		Assert.assertNotNull(sxpBlueprint);
		_assertSearchableAssetTypes(
			new String[] {"com.liferay.journal.model.JournalArticle"},
			sxpBlueprint.getConfiguration());
		Assert.assertEquals("Test SXBlueprint 1", sxpBlueprint.getTitle());

		sxpBlueprint =
			sxpBlueprintResource.getSXPBlueprintByExternalReferenceCode(
				"TESTSXPBLUEPRINT2");

		Assert.assertNotNull(sxpBlueprint);
		_assertSearchableAssetTypes(
			new String[] {
				"com.liferay.document.library.kernel.model.DLFileEntry",
				"com.liferay.journal.model.JournalArticle"
			},
			sxpBlueprint.getConfiguration());
		Assert.assertEquals(
			"Test SXBlueprint 2 Update", sxpBlueprint.getTitle());

		sxpBlueprint =
			sxpBlueprintResource.getSXPBlueprintByExternalReferenceCode(
				"TESTSXPBLUEPRINT3");

		Assert.assertNotNull(sxpBlueprint);
		_assertSearchableAssetTypes(
			new String[] {"com.liferay.portal.kernel.model.User"},
			sxpBlueprint.getConfiguration());
		Assert.assertEquals("Test SXBlueprint 3", sxpBlueprint.getTitle());
	}

	private File _getTempDir(String location) throws Exception {
		File tempFile = FileUtil.createTempFile();

		FileUtil.write(
			tempFile,
			SXPBundleSiteInitializerTest.class.getResourceAsStream(location));

		File tempDir1 = FileUtil.createTempFolder();

		FileUtil.unzip(tempFile, tempDir1);

		tempFile.delete();

		return tempDir1;
	}

	private void _test1(SiteInitializer siteInitializer) throws Exception {
		siteInitializer.initialize(_group.getGroupId());

		_assertSXPBlueprint1();
	}

	private void _test2(SiteInitializer siteInitializer) throws Exception {
		siteInitializer.initialize(_group.getGroupId());

		_assertSXPBlueprint2();
	}

	private Group _group;
	private ServiceContext _serviceContext;

	@Inject
	private SiteInitializerFactory _siteInitializerFactory;

	@Inject
	private SXPBlueprintResource.Factory _sxpBlueprintResourceFactory;

}