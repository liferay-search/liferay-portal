/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.list.internal.util;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Felipe Lorenz
 */
public class AssetListFiltersMigrationUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());
	}

	@Test
	public void testToFiltersTypeSettingsPreservesLegacyProperties() {
		UnicodeProperties unicodeProperties = _toFiltersUnicodeProperties(
			_buildLegacyTypeSettings("assetCategories", "true", "true", "123"));

		Assert.assertEquals(
			"assetCategories", unicodeProperties.getProperty("queryName0"));
		Assert.assertEquals(
			"123", unicodeProperties.getProperty("queryValues0"));
		Assert.assertEquals(
			"true", unicodeProperties.getProperty("queryContains0"));
		Assert.assertEquals(
			"true", unicodeProperties.getProperty("queryAndOperator0"));
	}

	@Test
	public void testToFiltersTypeSettingsWithAssetCategoryFilter()
		throws Exception {

		JSONObject filterJSONObject = _getOnlyFilterJSONObject(
			_buildLegacyTypeSettings(
				"assetCategories", "true", "false", "123,456"));

		Assert.assertEquals(
			"contains", filterJSONObject.getString("operatorName"));
		Assert.assertEquals(
			"assetCategories", filterJSONObject.getString("propertyName"));
		Assert.assertEquals("any", filterJSONObject.getString("quantifier"));

		JSONArray valueJSONArray = filterJSONObject.getJSONArray("value");

		Assert.assertEquals(
			valueJSONArray.toString(), 2, valueJSONArray.length());

		JSONObject valueJSONObject = valueJSONArray.getJSONObject(0);

		Assert.assertEquals("123", valueJSONObject.getString("label"));
		Assert.assertEquals("123", valueJSONObject.getString("value"));
	}

	@Test
	public void testToFiltersTypeSettingsWithAssetTagFilterAll()
		throws Exception {

		JSONObject filterJSONObject = _getOnlyFilterJSONObject(
			_buildLegacyTypeSettings("assetTags", "true", "true", "alpha"));

		Assert.assertEquals(
			"contains", filterJSONObject.getString("operatorName"));
		Assert.assertEquals(
			"assetTags", filterJSONObject.getString("propertyName"));
		Assert.assertEquals("all", filterJSONObject.getString("quantifier"));
	}

	@Test
	public void testToFiltersTypeSettingsWithKeywordsFilter() throws Exception {
		JSONObject filterJSONObject = _getOnlyFilterJSONObject(
			_buildLegacyTypeSettings("keywords", "true", "false", "hello"));

		Assert.assertEquals(
			"contains", filterJSONObject.getString("operatorName"));
		Assert.assertEquals(
			"keywords", filterJSONObject.getString("propertyName"));
		Assert.assertEquals("hello", filterJSONObject.getString("value"));
	}

	@Test
	public void testToFiltersTypeSettingsWithNotContainsOperator()
		throws Exception {

		JSONObject filterJSONObject = _getOnlyFilterJSONObject(
			_buildLegacyTypeSettings("assetTags", "false", "false", "alpha"));

		Assert.assertEquals(
			"not-contains", filterJSONObject.getString("operatorName"));
	}

	@Test
	public void testToFiltersTypeSettingsWithoutAssetFilters() {
		String typeSettings = "anyAssetType=true\ngroupIds=456";

		Assert.assertEquals(
			typeSettings,
			AssetListFiltersMigrationUtil.toFiltersTypeSettings(typeSettings));
	}

	private String _buildLegacyTypeSettings(
		String queryName, String queryContains, String queryAndOperator,
		String queryValues) {

		return UnicodePropertiesBuilder.create(
			true
		).put(
			"queryAndOperator0", queryAndOperator
		).put(
			"queryContains0", queryContains
		).put(
			"queryName0", queryName
		).put(
			"queryValues0", queryValues
		).build(
		).toString();
	}

	private JSONArray _getFiltersJSONArray(String typeSettings)
		throws Exception {

		return JSONFactoryUtil.createJSONArray(
			_toFiltersUnicodeProperties(
				typeSettings
			).getProperty(
				"filters"
			));
	}

	private JSONObject _getOnlyFilterJSONObject(String typeSettings)
		throws Exception {

		JSONArray filtersJSONArray = _getFiltersJSONArray(typeSettings);

		Assert.assertEquals(
			filtersJSONArray.toString(), 1, filtersJSONArray.length());

		return filtersJSONArray.getJSONObject(0);
	}

	private UnicodeProperties _toFiltersUnicodeProperties(String typeSettings) {
		return UnicodePropertiesBuilder.create(
			true
		).fastLoad(
			AssetListFiltersMigrationUtil.toFiltersTypeSettings(typeSettings)
		).build();
	}

}