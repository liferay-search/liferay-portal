/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.list.constants.AssetListEntryTypeConstants;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.asset.list.service.AssetListEntryLocalService;
import com.liferay.headless.delivery.client.dto.v1_0.CollectionEntry;
import com.liferay.headless.delivery.client.pagination.Page;
import com.liferay.headless.delivery.client.pagination.Pagination;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.Inject;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class CollectionEntryResourceTest
	extends BaseCollectionEntryResourceTestCase {

	@Test
	public void testGetSiteCollectionEntriesPageWithItemSubtypeFilter()
		throws Exception {

		Long siteId = testGroup.getGroupId();

		String itemSubtype1 = RandomTestUtil.randomString();
		String itemSubtype2 = RandomTestUtil.randomString();
		String itemType = RandomTestUtil.randomString();

		CollectionEntry collectionEntry1 =
			_addCollectionEntryWithTypeAndSubtype(
				itemSubtype1, itemType, siteId, RandomTestUtil.randomString());
		CollectionEntry collectionEntry2 =
			_addCollectionEntryWithTypeAndSubtype(
				itemSubtype2, itemType, siteId, RandomTestUtil.randomString());

		String filter = StringBundler.concat(
			"itemSubtype eq '", itemSubtype1, "'");

		Page<CollectionEntry> page =
			collectionEntryResource.getSiteCollectionEntriesPage(
				siteId, null, filter, Pagination.of(1, 50), null);

		List<CollectionEntry> collectionEntries =
			(List<CollectionEntry>)page.getItems();

		Assert.assertTrue(
			_containsCollectionEntryId(
				collectionEntries, collectionEntry1.getCollectionEntryId()));
		Assert.assertFalse(
			_containsCollectionEntryId(
				collectionEntries, collectionEntry2.getCollectionEntryId()));
	}

	@Test
	public void testGetSiteCollectionEntriesPageWithItemTypeFilter()
		throws Exception {

		Long siteId = testGroup.getGroupId();

		String itemType1 = RandomTestUtil.randomString();
		String itemType2 = RandomTestUtil.randomString();

		CollectionEntry collectionEntry1 =
			_addCollectionEntryWithTypeAndSubtype(
				null, itemType1, siteId, RandomTestUtil.randomString());
		CollectionEntry collectionEntry2 =
			_addCollectionEntryWithTypeAndSubtype(
				null, itemType2, siteId, RandomTestUtil.randomString());

		String filter = StringBundler.concat("itemType eq '", itemType1, "'");

		Page<CollectionEntry> page =
			collectionEntryResource.getSiteCollectionEntriesPage(
				siteId, null, filter, Pagination.of(1, 50), null);

		List<CollectionEntry> collectionEntries =
			(List<CollectionEntry>)page.getItems();

		Assert.assertTrue(
			_containsCollectionEntryId(
				collectionEntries, collectionEntry1.getCollectionEntryId()));
		Assert.assertFalse(
			_containsCollectionEntryId(
				collectionEntries, collectionEntry2.getCollectionEntryId()));
	}

	@Test
	public void testGetSiteCollectionEntriesPageWithItemTypeInFilter()
		throws Exception {

		Long siteId = testGroup.getGroupId();

		String itemType1 = RandomTestUtil.randomString();
		String itemType2 = RandomTestUtil.randomString();
		String itemType3 = RandomTestUtil.randomString();

		CollectionEntry collectionEntry1 =
			_addCollectionEntryWithTypeAndSubtype(
				null, itemType1, siteId, RandomTestUtil.randomString());
		CollectionEntry collectionEntry2 =
			_addCollectionEntryWithTypeAndSubtype(
				null, itemType2, siteId, RandomTestUtil.randomString());
		CollectionEntry collectionEntry3 =
			_addCollectionEntryWithTypeAndSubtype(
				null, itemType3, siteId, RandomTestUtil.randomString());

		String filter = StringBundler.concat(
			"itemType in ('", itemType1, "', '", itemType2, "')");

		Page<CollectionEntry> page =
			collectionEntryResource.getSiteCollectionEntriesPage(
				siteId, null, filter, Pagination.of(1, 50), null);

		List<CollectionEntry> collectionEntries =
			(List<CollectionEntry>)page.getItems();

		Assert.assertTrue(
			_containsCollectionEntryId(
				collectionEntries, collectionEntry1.getCollectionEntryId()));
		Assert.assertTrue(
			_containsCollectionEntryId(
				collectionEntries, collectionEntry2.getCollectionEntryId()));
		Assert.assertFalse(
			_containsCollectionEntryId(
				collectionEntries, collectionEntry3.getCollectionEntryId()));
	}

	@Test
	public void testGetSiteCollectionEntriesPageWithSearch() throws Exception {
		Long siteId = testGroup.getGroupId();

		String keyword = RandomTestUtil.randomString();

		CollectionEntry collectionEntry1 = _addCollectionEntry(
			siteId, "Title " + keyword);

		CollectionEntry collectionEntry2 = _addCollectionEntry(siteId, "Title");

		Page<CollectionEntry> page =
			collectionEntryResource.getSiteCollectionEntriesPage(
				siteId, keyword, null, Pagination.of(1, 50), null);

		List<CollectionEntry> collectionEntries =
			(List<CollectionEntry>)page.getItems();

		Assert.assertTrue(
			_containsCollectionEntryId(
				collectionEntries, collectionEntry1.getCollectionEntryId()));
		Assert.assertFalse(
			_containsCollectionEntryId(
				collectionEntries, collectionEntry2.getCollectionEntryId()));
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"classNameId", "classPK", "externalReferenceCode", "itemSubtype",
			"itemType", "title"
		};
	}

	@Override
	protected CollectionEntry
			testGetAssetLibraryCollectionEntriesPage_addCollectionEntry(
				Long assetLibraryId, CollectionEntry collectionEntry)
		throws Exception {

		return _addCollectionEntryFromDTO(collectionEntry, assetLibraryId);
	}

	@Override
	protected Long testGetAssetLibraryCollectionEntriesPage_getAssetLibraryId()
		throws Exception {

		return testDepotEntryGroup.getGroupId();
	}

	@Override
	protected Long
			testGetAssetLibraryCollectionEntriesPage_getIrrelevantAssetLibraryId()
		throws Exception {

		return irrelevantDepotEntryGroup.getGroupId();
	}

	@Override
	protected CollectionEntry
			testGetSiteCollectionEntriesPage_addCollectionEntry(
				Long siteId, CollectionEntry collectionEntry)
		throws Exception {

		return _addCollectionEntryFromDTO(collectionEntry, siteId);
	}

	private CollectionEntry _addCollectionEntry(Long groupId, String title)
		throws Exception {

		AssetListEntry assetListEntry =
			_assetListEntryLocalService.addAssetListEntry(
				RandomTestUtil.randomString(), TestPropsValues.getUserId(),
				groupId, title, AssetListEntryTypeConstants.TYPE_MANUAL,
				ServiceContextTestUtil.getServiceContext(
					groupId, TestPropsValues.getUserId()));

		return _toClientDTO(assetListEntry);
	}

	private CollectionEntry _addCollectionEntryFromDTO(
			CollectionEntry collectionEntry, Long groupId)
		throws Exception {

		AssetListEntry persistedAssetListEntry =
			_assetListEntryLocalService.addAssetListEntry(
				collectionEntry.getExternalReferenceCode(),
				TestPropsValues.getUserId(), groupId,
				collectionEntry.getTitle(),
				AssetListEntryTypeConstants.TYPE_MANUAL,
				ServiceContextTestUtil.getServiceContext(
					groupId, TestPropsValues.getUserId()));

		if ((collectionEntry.getItemType() != null) ||
			(collectionEntry.getItemSubtype() != null)) {

			persistedAssetListEntry.setAssetEntrySubtype(
				collectionEntry.getItemSubtype());
			persistedAssetListEntry.setAssetEntryType(
				collectionEntry.getItemType());

			persistedAssetListEntry =
				_assetListEntryLocalService.updateAssetListEntry(
					persistedAssetListEntry);
		}

		return _toClientDTO(persistedAssetListEntry);
	}

	private CollectionEntry _addCollectionEntryWithTypeAndSubtype(
			String assetEntrySubtype, String assetEntryType, Long groupId,
			String title)
		throws Exception {

		AssetListEntry assetListEntry =
			_assetListEntryLocalService.addAssetListEntry(
				RandomTestUtil.randomString(), TestPropsValues.getUserId(),
				groupId, title, AssetListEntryTypeConstants.TYPE_MANUAL,
				ServiceContextTestUtil.getServiceContext(
					groupId, TestPropsValues.getUserId()));

		assetListEntry.setAssetEntrySubtype(assetEntrySubtype);
		assetListEntry.setAssetEntryType(assetEntryType);

		assetListEntry = _assetListEntryLocalService.updateAssetListEntry(
			assetListEntry);

		return _toClientDTO(assetListEntry);
	}

	private boolean _containsCollectionEntryId(
		List<CollectionEntry> collectionEntries, long collectionEntryId) {

		for (CollectionEntry collectionEntry : collectionEntries) {
			Long actualCollectionEntryId =
				collectionEntry.getCollectionEntryId();

			if ((actualCollectionEntryId != null) &&
				(actualCollectionEntryId == collectionEntryId)) {

				return true;
			}
		}

		return false;
	}

	private CollectionEntry _toClientDTO(AssetListEntry assetListEntry) {
		CollectionEntry collectionEntry = new CollectionEntry();

		collectionEntry.setClassNameId(
			PortalUtil.getClassNameId(AssetListEntry.class));
		collectionEntry.setClassPK(assetListEntry.getAssetListEntryId());
		collectionEntry.setCollectionEntryId(
			assetListEntry.getAssetListEntryId());
		collectionEntry.setDateCreated(assetListEntry.getCreateDate());
		collectionEntry.setDateModified(assetListEntry.getModifiedDate());
		collectionEntry.setExternalReferenceCode(
			assetListEntry.getExternalReferenceCode());
		collectionEntry.setItemSubtype(assetListEntry.getAssetEntrySubtype());
		collectionEntry.setItemType(assetListEntry.getAssetEntryType());
		collectionEntry.setTitle(assetListEntry.getTitle());

		return collectionEntry;
	}

	@Inject
	private AssetListEntryLocalService _assetListEntryLocalService;

}