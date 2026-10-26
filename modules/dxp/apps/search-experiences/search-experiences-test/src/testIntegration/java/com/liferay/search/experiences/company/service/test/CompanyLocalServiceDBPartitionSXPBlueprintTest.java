/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.company.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.list.model.AssetListEntryUsage;
import com.liferay.asset.list.service.AssetListEntryUsageLocalService;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.info.list.provider.item.selector.criterion.InfoListProviderItemSelectorReturnType;
import com.liferay.journal.model.JournalArticle;
import com.liferay.layout.content.model.LayoutContentVersion;
import com.liferay.layout.content.service.LayoutContentVersionLocalService;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructure;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalService;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.layout.util.structure.CollectionStyledLayoutStructureItem;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.test.util.ObjectDefinitionTestUtil;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.db.partition.test.util.BaseDBPartitionTestCase;
import com.liferay.portal.kernel.instance.PortalInstancePool;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.test.context.ContextUserReplace;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.AssumeTestRule;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.test.rule.TransactionalTestRule;
import com.liferay.search.experiences.model.SXPBlueprint;
import com.liferay.search.experiences.service.SXPBlueprintLocalService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Mariano Álvaro Sáiz
 */
@DataGuard(scope = DataGuard.Scope.NONE)
@RunWith(Arquillian.class)
public class CompanyLocalServiceDBPartitionSXPBlueprintTest
	extends BaseDBPartitionTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new AssumeTestRule("assume"), new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE,
			TransactionalTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		BaseDBPartitionTestCase.setUpClass();

		_safeCloseable = CompanyThreadLocal.setCompanyIdWithSafeCloseable(
			PortalInstancePool.getDefaultCompanyId());
	}

	@AfterClass
	public static void tearDownClass() {
		if (_safeCloseable != null) {
			_safeCloseable.close();
		}
	}

	@Test
	public void testCopyDBPartitionCompany() throws Exception {
		Company copiedCompany = null;

		try (SafeCloseable safeCloseable1 =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(
					TestPropsValues.getCompanyId())) {

			_objectDefinition =
				ObjectDefinitionTestUtil.publishObjectDefinition();

			_sxpBlueprint = _sxpBlueprintLocalService.addSXPBlueprint(
				null, TestPropsValues.getUserId(),
				JSONUtil.put(
					"generalConfiguration",
					JSONUtil.put(
						"searchableAssetTypes",
						JSONUtil.put(_objectDefinition.getClassName()))
				).toString(),
				Collections.emptyMap(), StringPool.BLANK, StringPool.BLANK,
				RandomTestUtil.randomLocaleStringMap(),
				ServiceContextTestUtil.getServiceContext());

			copiedCompany = companyLocalService.copyDBPartitionCompany(
				TestPropsValues.getCompanyId(), null,
				RandomTestUtil.randomString(),
				StringUtil.toLowerCase(RandomTestUtil.randomString()),
				RandomTestUtil.randomString());

			try (SafeCloseable safeCloseable2 =
					CompanyThreadLocal.setCompanyIdWithSafeCloseable(
						copiedCompany.getCompanyId())) {

				ObjectDefinition copiedObjectDefinition =
					_objectDefinitionLocalService.fetchObjectDefinition(
						_objectDefinition.getObjectDefinitionId());

				SXPBlueprint copiedSXPBlueprint =
					_sxpBlueprintLocalService.getSXPBlueprint(
						_sxpBlueprint.getSXPBlueprintId());

				Assert.assertEquals(
					StringUtil.replace(
						_sxpBlueprint.getConfigurationJSON(),
						_objectDefinition.getClassName(),
						copiedObjectDefinition.getClassName()),
					copiedSXPBlueprint.getConfigurationJSON());
			}
		}
		finally {
			if (copiedCompany != null) {
				companyLocalService.deleteCompany(copiedCompany.getCompanyId());
			}
		}
	}

	@FeatureFlag("LPD-10622")
	@Test
	public void testCopyDBPartitionCompanyWhenSXPBlueprintIsCollectionProvider()
		throws Exception {

		Company copiedCompany = null;
		Layout layout = null;
		LayoutContentVersion layoutContentVersion = null;
		SXPBlueprint sxpBlueprint1 = null;
		SXPBlueprint sxpBlueprint2 = null;

		try (SafeCloseable safeCloseable1 =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(
					TestPropsValues.getCompanyId())) {

			sxpBlueprint1 = _addSXPBlueprint(
				JSONUtil.put("collectionProvider", true));
			sxpBlueprint2 = _addSXPBlueprint(
				JSONUtil.put(
					"collectionProvider", true
				).put(
					"collectionProviderType", JournalArticle.class.getName()
				).put(
					"searchableAssetTypes",
					JSONUtil.put(JournalArticle.class.getName())
				));

			layout = LayoutTestUtil.addTypeContentLayout(
				_groupLocalService.getGroup(TestPropsValues.getGroupId()));

			LayoutPageTemplateStructure layoutPageTemplateStructure =
				_layoutPageTemplateStructureLocalService.
					fetchLayoutPageTemplateStructure(
						layout.getGroupId(), layout.getPlid());

			LayoutStructure layoutStructure = LayoutStructure.of(
				layoutPageTemplateStructure.getDefaultSegmentsExperienceData());

			String itemId1 = _addCollectionStyledLayoutStructureItem(
				layoutStructure, sxpBlueprint1);
			String itemId2 = _addCollectionStyledLayoutStructureItem(
				layoutStructure, sxpBlueprint2);

			_layoutPageTemplateStructureLocalService.
				updateLayoutPageTemplateStructureData(
					TestPropsValues.getUserId(), layout.getGroupId(),
					layout.getPlid(), layoutStructure.toString());

			Layout draftLayout = layout.fetchDraftLayout();

			layoutContentVersion =
				_layoutContentVersionLocalService.addLayoutContentVersion(
					null, TestPropsValues.getUserId(),
					layoutStructure.toString(),
					RandomTestUtil.randomLocaleStringMap(),
					draftLayout.getPlid(), WorkflowConstants.STATUS_DRAFT);

			copiedCompany = companyLocalService.copyDBPartitionCompany(
				TestPropsValues.getCompanyId(), null,
				RandomTestUtil.randomString(),
				StringUtil.toLowerCase(RandomTestUtil.randomString()),
				RandomTestUtil.randomString());

			long copiedCompanyId = copiedCompany.getCompanyId();

			try (SafeCloseable safeCloseable2 =
					CompanyThreadLocal.setCompanyIdWithSafeCloseable(
						copiedCompanyId)) {

				LayoutPageTemplateStructure copiedLayoutPageTemplateStructure =
					_layoutPageTemplateStructureLocalService.
						fetchLayoutPageTemplateStructure(
							layout.getGroupId(), layout.getPlid());

				LayoutStructure copiedLayoutStructure = LayoutStructure.of(
					copiedLayoutPageTemplateStructure.
						getDefaultSegmentsExperienceData());

				_assertCollectionStyledLayoutStructureItem(
					copiedCompanyId, itemId1, copiedLayoutStructure,
					sxpBlueprint1);
				_assertCollectionStyledLayoutStructureItem(
					copiedCompanyId, itemId2, copiedLayoutStructure,
					sxpBlueprint2);

				List<LayoutContentVersion> layoutContentVersions =
					_layoutContentVersionLocalService.getLayoutContentVersions(
						draftLayout.getPlid());

				Assert.assertEquals(
					layoutContentVersions.toString(), 1,
					layoutContentVersions.size());

				LayoutContentVersion copiedLayoutContentVersion =
					layoutContentVersions.get(0);

				String data = copiedLayoutContentVersion.getData();

				for (SXPBlueprint sxpBlueprint :
						Arrays.asList(sxpBlueprint1, sxpBlueprint2)) {

					Assert.assertTrue(
						data.contains(
							_fetchInfoCollectionProviderKey(
								copiedCompanyId,
								sxpBlueprint.getExternalReferenceCode())));
					Assert.assertFalse(
						data.contains(
							StringBundler.concat(
								StringPool.UNDERLINE,
								TestPropsValues.getCompanyId(),
								StringPool.UNDERLINE,
								sxpBlueprint.getExternalReferenceCode())));
				}

				List<AssetListEntryUsage> assetListEntryUsages =
					_assetListEntryUsageLocalService.
						getAssetEntryListUsagesByPlid(layout.getPlid());

				Assert.assertEquals(
					assetListEntryUsages.toString(),
					SetUtil.fromArray(
						_fetchInfoCollectionProviderKey(
							copiedCompanyId,
							sxpBlueprint1.getExternalReferenceCode()),
						_fetchInfoCollectionProviderKey(
							copiedCompanyId,
							sxpBlueprint2.getExternalReferenceCode())),
					SetUtil.fromList(
						TransformUtil.transform(
							assetListEntryUsages,
							AssetListEntryUsage::getKey)));
			}

			companyLocalService.deleteCompany(copiedCompanyId);

			copiedCompany = null;

			Assert.assertNull(
				_fetchInfoCollectionProviderKey(
					copiedCompanyId, sxpBlueprint1.getExternalReferenceCode()));
		}
		finally {
			if (copiedCompany != null) {
				companyLocalService.deleteCompany(copiedCompany.getCompanyId());
			}

			try (SafeCloseable safeCloseable =
					CompanyThreadLocal.setCompanyIdWithSafeCloseable(
						TestPropsValues.getCompanyId());
				ContextUserReplace contextUserReplace = new ContextUserReplace(
					TestPropsValues.getUser())) {

				if (layoutContentVersion != null) {
					_layoutContentVersionLocalService.
						deleteLayoutContentVersion(
							layoutContentVersion.getLayoutContentVersionId());
				}

				if (layout != null) {
					_layoutLocalService.deleteLayout(layout);
				}

				if (sxpBlueprint1 != null) {
					_sxpBlueprintLocalService.deleteSXPBlueprint(sxpBlueprint1);
				}

				if (sxpBlueprint2 != null) {
					_sxpBlueprintLocalService.deleteSXPBlueprint(sxpBlueprint2);
				}
			}
		}
	}

	private String _addCollectionStyledLayoutStructureItem(
			LayoutStructure layoutStructure, SXPBlueprint sxpBlueprint)
		throws Exception {

		CollectionStyledLayoutStructureItem
			collectionStyledLayoutStructureItem =
				(CollectionStyledLayoutStructureItem)
					layoutStructure.addCollectionStyledLayoutStructureItem(
						layoutStructure.getMainItemId(), 0);

		collectionStyledLayoutStructureItem.setCollectionJSONObject(
			JSONUtil.put(
				"key",
				_fetchInfoCollectionProviderKey(
					TestPropsValues.getCompanyId(),
					sxpBlueprint.getExternalReferenceCode())
			).put(
				"type", InfoListProviderItemSelectorReturnType.class.getName()
			));

		return collectionStyledLayoutStructureItem.getItemId();
	}

	private SXPBlueprint _addSXPBlueprint(
			JSONObject generalConfigurationJSONObject)
		throws Exception {

		return _sxpBlueprintLocalService.addSXPBlueprint(
			null, TestPropsValues.getUserId(),
			JSONUtil.put(
				"generalConfiguration", generalConfigurationJSONObject
			).toString(),
			Collections.emptyMap(), StringPool.BLANK, StringPool.BLANK,
			RandomTestUtil.randomLocaleStringMap(),
			ServiceContextTestUtil.getServiceContext());
	}

	private void _assertCollectionStyledLayoutStructureItem(
		long companyId, String itemId, LayoutStructure layoutStructure,
		SXPBlueprint sxpBlueprint) {

		CollectionStyledLayoutStructureItem
			collectionStyledLayoutStructureItem =
				(CollectionStyledLayoutStructureItem)
					layoutStructure.getLayoutStructureItem(itemId);

		JSONObject collectionJSONObject =
			collectionStyledLayoutStructureItem.getCollectionJSONObject();

		String key = _fetchInfoCollectionProviderKey(
			companyId, sxpBlueprint.getExternalReferenceCode());

		Assert.assertEquals(key, collectionJSONObject.getString("key"));
	}

	private String _fetchInfoCollectionProviderKey(
		long companyId, String externalReferenceCode) {

		String suffix = StringBundler.concat(
			StringPool.UNDERLINE, companyId, StringPool.UNDERLINE,
			externalReferenceCode);

		for (InfoCollectionProvider<?> infoCollectionProvider :
				_infoItemServiceRegistry.getAllInfoItemServices(
					InfoCollectionProvider.class)) {

			String key = infoCollectionProvider.getKey();

			if (key.endsWith(suffix)) {
				return key;
			}
		}

		return null;
	}

	private static SafeCloseable _safeCloseable;

	@Inject
	private AssetListEntryUsageLocalService _assetListEntryUsageLocalService;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private InfoItemServiceRegistry _infoItemServiceRegistry;

	@Inject
	private LayoutContentVersionLocalService _layoutContentVersionLocalService;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject
	private LayoutPageTemplateStructureLocalService
		_layoutPageTemplateStructureLocalService;

	@DeleteAfterTestRun
	private ObjectDefinition _objectDefinition;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@DeleteAfterTestRun
	private SXPBlueprint _sxpBlueprint;

	@Inject
	private SXPBlueprintLocalService _sxpBlueprintLocalService;

}