/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.publisher.web.internal.util;

import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Mariano Álvaro Sáiz
 */
public class AssetListTypeSettingsUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@AfterClass
	public static void tearDownClass() {
		_classNameLocalServiceUtilMockedStatic.close();
	}

	@Before
	public void setUp() {
		_classNameLocalServiceUtilMockedStatic.reset();

		_classNameLocalServiceUtilMockedStatic.when(
			() -> ClassNameLocalServiceUtil.fetchClassName(_CLASS_NAME_ID)
		).thenReturn(
			Mockito.mock(ClassName.class)
		);
	}

	@Test
	public void testSanitizeClassNameIds() {
		_testSanitizeClassNameIds(
			"true", _CLASS_NAME_ID + "," + _NONEXISTENT_CLASS_NAME_ID, "true",
			String.valueOf(_CLASS_NAME_ID));
		_testSanitizeClassNameIds(
			"true", String.valueOf(_NONEXISTENT_CLASS_NAME_ID), "true", null);
		_testSanitizeClassNameIds(
			"false", String.valueOf(_NONEXISTENT_CLASS_NAME_ID), "false",
			String.valueOf(_NONEXISTENT_CLASS_NAME_ID));
		_testSanitizeClassNameIds(
			String.valueOf(_NONEXISTENT_CLASS_NAME_ID), null, null, null);
	}

	private void _testSanitizeClassNameIds(
		String anyAssetType, String classNameIds, String expectedAnyAssetType,
		String expectedClassNameIds) {

		UnicodeProperties unicodeProperties = new UnicodeProperties(true);

		if (anyAssetType != null) {
			unicodeProperties.put("anyAssetType", anyAssetType);
		}

		if (classNameIds != null) {
			unicodeProperties.put("classNameIds", classNameIds);
		}

		AssetListTypeSettingsUtil.sanitizeClassNameIds(unicodeProperties);

		Assert.assertEquals(
			expectedAnyAssetType,
			unicodeProperties.getProperty("anyAssetType", null));
		Assert.assertEquals(
			expectedClassNameIds,
			unicodeProperties.getProperty("classNameIds", null));
	}

	private static final long _CLASS_NAME_ID = RandomTestUtil.randomLong();

	private static final long _NONEXISTENT_CLASS_NAME_ID =
		RandomTestUtil.randomLong();

	private static final MockedStatic<ClassNameLocalServiceUtil>
		_classNameLocalServiceUtilMockedStatic = Mockito.mockStatic(
			ClassNameLocalServiceUtil.class);

}