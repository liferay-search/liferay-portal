/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.web.internal.upgrade.v2_1_3.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.change.tracking.configuration.CTSettingsConfiguration;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.service.CTCollectionLocalService;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.cache.MultiVMPool;
import com.liferay.portal.kernel.change.tracking.CTCollectionThreadLocal;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.dao.orm.FinderCache;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutPrototype;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.LayoutPrototypeLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Javier Moral
 */
@RunWith(Arquillian.class)
public class
	SearchLayoutPageTemplateEntryExternalReferenceCodeUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_company = CompanyTestUtil.addCompany(true);

		Group companyGroup = _company.getGroup();

		_searchLayoutPageTemplateEntry =
			_layoutPageTemplateEntryLocalService.
				fetchLayoutPageTemplateEntryByExternalReferenceCode(
					_EXTERNAL_REFERENCE_CODE_SEARCH, companyGroup.getGroupId());

		_searchLayoutPageTemplateEntry = _setExternalReferenceCode(
			_searchLayoutPageTemplateEntry.getUuid(),
			_searchLayoutPageTemplateEntry);

		_otherLayoutPageTemplateEntry = _addLayoutPageTemplateEntry(
			RandomTestUtil.randomString());
		_translatedLayoutPageTemplateEntry = _addLayoutPageTemplateEntry(
			LanguageUtil.get(LocaleUtil.SPAIN, "search"));
	}

	@Test
	@TestInfo("LPD-108560")
	public void testUpgrade() throws Exception {
		_testUpgradeWithMultipleCandidates();
		_testUpgradeWithExternalReferenceCodeInUse();
		_testUpgradeWithCustomExternalReferenceCode();
		_testUpgradeWithPublication();
	}

	private LayoutPageTemplateEntry _addLayoutPageTemplateEntry(String name)
		throws Exception {

		LayoutPrototype layoutPrototype =
			_layoutPrototypeLocalService.addLayoutPrototype(
				_userLocalService.getGuestUserId(_company.getCompanyId()),
				_company.getCompanyId(),
				HashMapBuilder.put(
					LocaleUtil.getSiteDefault(), name
				).build(),
				Collections.emptyMap(), true, new ServiceContext());

		return _layoutPageTemplateEntryLocalService.
			getFirstLayoutPageTemplateEntry(
				layoutPrototype.getLayoutPrototypeId());
	}

	private void _assertExternalReferenceCode(
			String externalReferenceCode,
			LayoutPageTemplateEntry layoutPageTemplateEntry)
		throws Exception {

		layoutPageTemplateEntry =
			_layoutPageTemplateEntryLocalService.getLayoutPageTemplateEntry(
				layoutPageTemplateEntry.getLayoutPageTemplateEntryId());

		Assert.assertEquals(
			externalReferenceCode,
			layoutPageTemplateEntry.getExternalReferenceCode());
	}

	private void _assertPortletLayoutPageTemplateEntryERC(
			Layout layout, String portletLayoutPageTemplateEntryERC)
		throws Exception {

		layout = _layoutLocalService.getLayout(layout.getPlid());

		Assert.assertEquals(
			portletLayoutPageTemplateEntryERC,
			layout.getPortletLayoutPageTemplateEntryERC());
	}

	private void _runUpgrade() throws Exception {
		UpgradeProcess upgradeProcess = UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator,
			"com.liferay.portal.search.web.internal.upgrade.v2_1_3." +
				"SearchLayoutPageTemplateEntry" +
					"ExternalReferenceCodeUpgradeProcess");

		upgradeProcess.upgrade();

		_entityCache.clearCache();
		_finderCache.clearCache();
		_multiVMPool.clear();
	}

	private LayoutPageTemplateEntry _setExternalReferenceCode(
			String externalReferenceCode,
			LayoutPageTemplateEntry layoutPageTemplateEntry)
		throws Exception {

		layoutPageTemplateEntry.setExternalReferenceCode(externalReferenceCode);

		return _layoutPageTemplateEntryLocalService.
			updateLayoutPageTemplateEntry(layoutPageTemplateEntry);
	}

	private void _testUpgradeWithCustomExternalReferenceCode()
		throws Exception {

		String externalReferenceCode = RandomTestUtil.randomString();

		_searchLayoutPageTemplateEntry = _setExternalReferenceCode(
			externalReferenceCode, _searchLayoutPageTemplateEntry);

		_translatedLayoutPageTemplateEntry = _setExternalReferenceCode(
			_translatedLayoutPageTemplateEntry.getUuid(),
			_translatedLayoutPageTemplateEntry);

		_runUpgrade();

		_assertExternalReferenceCode(
			_EXTERNAL_REFERENCE_CODE_SEARCH,
			_translatedLayoutPageTemplateEntry);
		_assertExternalReferenceCode(
			externalReferenceCode, _searchLayoutPageTemplateEntry);
	}

	private void _testUpgradeWithExternalReferenceCodeInUse() throws Exception {
		_searchLayoutPageTemplateEntry = _setExternalReferenceCode(
			_searchLayoutPageTemplateEntry.getUuid(),
			_searchLayoutPageTemplateEntry);
		_translatedLayoutPageTemplateEntry = _setExternalReferenceCode(
			_EXTERNAL_REFERENCE_CODE_SEARCH,
			_translatedLayoutPageTemplateEntry);

		_runUpgrade();

		_assertExternalReferenceCode(
			_EXTERNAL_REFERENCE_CODE_SEARCH,
			_translatedLayoutPageTemplateEntry);
		_assertExternalReferenceCode(
			_searchLayoutPageTemplateEntry.getUuid(),
			_searchLayoutPageTemplateEntry);
	}

	private void _testUpgradeWithMultipleCandidates() throws Exception {
		User user = UserTestUtil.getAdminUser(_company.getCompanyId());

		Group group = GroupTestUtil.addGroup(
			_company.getCompanyId(), user.getUserId(),
			GroupConstants.DEFAULT_PARENT_GROUP_ID);

		Group companyGroup = _company.getGroup();

		Layout groupLayout = LayoutTestUtil.addTypePortletLayout(
			group, false, companyGroup.getGroupId(),
			_layoutPrototypeLocalService.getLayoutPrototype(
				_searchLayoutPageTemplateEntry.getLayoutPrototypeId()),
			false);

		Layout otherLayout = LayoutTestUtil.addTypePortletLayout(group);

		otherLayout.setPortletLayoutPageTemplateEntryERC(
			_searchLayoutPageTemplateEntry.getExternalReferenceCode());

		otherLayout = _layoutLocalService.updateLayout(otherLayout);

		_runUpgrade();

		_assertExternalReferenceCode(
			_EXTERNAL_REFERENCE_CODE_SEARCH, _searchLayoutPageTemplateEntry);
		_assertExternalReferenceCode(
			_otherLayoutPageTemplateEntry.getUuid(),
			_otherLayoutPageTemplateEntry);
		_assertExternalReferenceCode(
			_translatedLayoutPageTemplateEntry.getUuid(),
			_translatedLayoutPageTemplateEntry);

		_assertPortletLayoutPageTemplateEntryERC(
			groupLayout, _EXTERNAL_REFERENCE_CODE_SEARCH);
		_assertPortletLayoutPageTemplateEntryERC(
			otherLayout, _searchLayoutPageTemplateEntry.getUuid());
	}

	private void _testUpgradeWithPublication() throws Exception {
		_searchLayoutPageTemplateEntry = _setExternalReferenceCode(
			_searchLayoutPageTemplateEntry.getUuid(),
			_searchLayoutPageTemplateEntry);
		_translatedLayoutPageTemplateEntry = _setExternalReferenceCode(
			RandomTestUtil.randomString(), _translatedLayoutPageTemplateEntry);

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						_company.getCompanyId(),
						CTSettingsConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"enabled", true
						).build())) {

			_ctCollection = _ctCollectionLocalService.addCTCollection(
				null, _company.getCompanyId(),
				_userLocalService.getGuestUserId(_company.getCompanyId()), 0,
				RandomTestUtil.randomString(), null);

			try (SafeCloseable safeCloseable =
					CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
						_ctCollection.getCtCollectionId())) {

				_searchLayoutPageTemplateEntry =
					_layoutPageTemplateEntryLocalService.
						updateLayoutPageTemplateEntry(
							_searchLayoutPageTemplateEntry);
			}

			_runUpgrade();

			_assertExternalReferenceCode(
				_EXTERNAL_REFERENCE_CODE_SEARCH,
				_searchLayoutPageTemplateEntry);

			try (SafeCloseable safeCloseable =
					CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
						_ctCollection.getCtCollectionId())) {

				_assertExternalReferenceCode(
					_EXTERNAL_REFERENCE_CODE_SEARCH,
					_searchLayoutPageTemplateEntry);
			}
		}
	}

	private static final String _EXTERNAL_REFERENCE_CODE_SEARCH = "L_SEARCH";

	@DeleteAfterTestRun
	private Company _company;

	@DeleteAfterTestRun
	private CTCollection _ctCollection;

	@Inject
	private CTCollectionLocalService _ctCollectionLocalService;

	@Inject
	private EntityCache _entityCache;

	@Inject
	private FinderCache _finderCache;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject
	private LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService;

	@Inject
	private LayoutPrototypeLocalService _layoutPrototypeLocalService;

	@Inject
	private MultiVMPool _multiVMPool;

	private LayoutPageTemplateEntry _otherLayoutPageTemplateEntry;
	private LayoutPageTemplateEntry _searchLayoutPageTemplateEntry;
	private LayoutPageTemplateEntry _translatedLayoutPageTemplateEntry;

	@Inject(
		filter = "component.name=com.liferay.portal.search.web.internal.upgrade.registry.SearchWebUpgradeStepRegistrator"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

	@Inject
	private UserLocalService _userLocalService;

}