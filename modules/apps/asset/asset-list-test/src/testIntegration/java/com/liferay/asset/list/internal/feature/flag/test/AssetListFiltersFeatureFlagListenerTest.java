/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.list.internal.feature.flag.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.asset.list.service.AssetListEntryLocalService;
import com.liferay.asset.list.test.util.AssetListTestUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.segments.constants.SegmentsEntryConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Felipe Lorenz
 */
@RunWith(Arquillian.class)
public class AssetListFiltersFeatureFlagListenerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testOnValueDoesNotMigrateLegacyUICollection() throws Exception {
		long classNameId = _portal.getClassNameId(JournalArticle.class);

		AssetListEntry assetListEntry = _addAssetListEntry(
			UnicodePropertiesBuilder.create(
				true
			).put(
				"anyAssetType", String.valueOf(classNameId)
			).put(
				"classNameIds", String.valueOf(classNameId)
			).put(
				"queryAndOperator0", "false"
			).put(
				"queryContains0", "true"
			).put(
				"queryName0", "assetTags"
			).put(
				"queryValues0", "alpha"
			).build(
			).toString());

		FeatureFlagTestUtil.invokeFeatureFlagListeners(
			_group.getCompanyId(), true, "LPD-74731");

		UnicodeProperties unicodeProperties = _getTypeSettingsUnicodeProperties(
			assetListEntry);

		Assert.assertNull(unicodeProperties.getProperty("filters"));
	}

	@Test
	public void testOnValueIsIdempotent() throws Exception {
		AssetListEntry assetListEntry = _addAssetListEntry(
			_buildMultiSelectionTypeSettings());

		FeatureFlagTestUtil.invokeFeatureFlagListeners(
			_group.getCompanyId(), true, "LPD-74731");
		FeatureFlagTestUtil.invokeFeatureFlagListeners(
			_group.getCompanyId(), true, "LPD-74731");

		JSONArray filtersJSONArray = _getFiltersJSONArray(assetListEntry);

		Assert.assertEquals(
			filtersJSONArray.toString(), 1, filtersJSONArray.length());
	}

	@Test
	public void testOnValueMigratesMultiSelectionCollection() throws Exception {
		AssetListEntry assetListEntry = _addAssetListEntry(
			_buildMultiSelectionTypeSettings());

		FeatureFlagTestUtil.invokeFeatureFlagListeners(
			_group.getCompanyId(), true, "LPD-74731");

		UnicodeProperties unicodeProperties = _getTypeSettingsUnicodeProperties(
			assetListEntry);

		JSONArray filtersJSONArray = JSONFactoryUtil.createJSONArray(
			unicodeProperties.getProperty("filters"));

		Assert.assertEquals(
			filtersJSONArray.toString(), 1, filtersJSONArray.length());

		JSONObject filterJSONObject = filtersJSONArray.getJSONObject(0);

		Assert.assertEquals(
			"contains", filterJSONObject.getString("operatorName"));
		Assert.assertEquals(
			"assetTags", filterJSONObject.getString("propertyName"));
		Assert.assertEquals("any", filterJSONObject.getString("quantifier"));

		JSONObject valueJSONObject = filterJSONObject.getJSONArray(
			"value"
		).getJSONObject(
			0
		);

		Assert.assertEquals("alpha", valueJSONObject.getString("value"));

		Assert.assertEquals(
			"assetTags", unicodeProperties.getProperty("queryName0"));
	}

	private AssetListEntry _addAssetListEntry(String typeSettings)
		throws Exception {

		AssetListEntry assetListEntry = AssetListTestUtil.addAssetListEntry(
			_group.getGroupId(), 0);

		_assetListEntryLocalService.updateAssetListEntryTypeSettings(
			assetListEntry.getAssetListEntryId(),
			SegmentsEntryConstants.ID_DEFAULT, typeSettings);

		return assetListEntry;
	}

	private String _buildMultiSelectionTypeSettings() {
		return UnicodePropertiesBuilder.create(
			true
		).put(
			"anyAssetType", "true"
		).put(
			"queryAndOperator0", "false"
		).put(
			"queryContains0", "true"
		).put(
			"queryName0", "assetTags"
		).put(
			"queryValues0", "alpha"
		).build(
		).toString();
	}

	private JSONArray _getFiltersJSONArray(AssetListEntry assetListEntry)
		throws Exception {

		return JSONFactoryUtil.createJSONArray(
			_getTypeSettingsUnicodeProperties(
				assetListEntry
			).getProperty(
				"filters"
			));
	}

	private UnicodeProperties _getTypeSettingsUnicodeProperties(
			AssetListEntry assetListEntry)
		throws Exception {

		return UnicodePropertiesBuilder.create(
			true
		).fastLoad(
			_assetListEntryLocalService.getAssetListEntry(
				assetListEntry.getAssetListEntryId()
			).getTypeSettings(
				SegmentsEntryConstants.ID_DEFAULT
			)
		).build();
	}

	@Inject
	private AssetListEntryLocalService _assetListEntryLocalService;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private Portal _portal;

}