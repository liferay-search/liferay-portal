/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.publisher.web.internal.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.portlet.PortletPreferences;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
	public void testGetTypeSettings() {
		_testGetTypeSettings(
			null,
			_CLASS_NAME_ID + StringPool.COMMA + _NONEXISTENT_CLASS_NAME_ID,
			"true", String.valueOf(_CLASS_NAME_ID));
		_testGetTypeSettings(
			"false",
			_CLASS_NAME_ID + StringPool.COMMA + _NONEXISTENT_CLASS_NAME_ID,
			"false", String.valueOf(_CLASS_NAME_ID));
		_testGetTypeSettings(
			"false", String.valueOf(_NONEXISTENT_CLASS_NAME_ID), "true", null);
		_testGetTypeSettings(
			"true",
			_CLASS_NAME_ID + StringPool.COMMA + _NONEXISTENT_CLASS_NAME_ID,
			"true", String.valueOf(_CLASS_NAME_ID));
		_testGetTypeSettings(
			"true", String.valueOf(_NONEXISTENT_CLASS_NAME_ID), "true", null);
		_testGetTypeSettings(
			String.valueOf(_CLASS_NAME_ID),
			String.valueOf(_NONEXISTENT_CLASS_NAME_ID),
			String.valueOf(_CLASS_NAME_ID), null);
		_testGetTypeSettings(
			String.valueOf(_NONEXISTENT_CLASS_NAME_ID), null, "true", null);
	}

	private void _testGetTypeSettings(
		String anyAssetType, String classNameIds, String expectedAnyAssetType,
		String expectedClassNameIds) {

		Map<String, String> preferences = new HashMap<>();

		if (anyAssetType != null) {
			preferences.put("anyAssetType", anyAssetType);
		}

		if (classNameIds != null) {
			preferences.put("classNameIds", classNameIds);
		}

		PortletPreferences portletPreferences = Mockito.mock(
			PortletPreferences.class);

		Mockito.when(
			portletPreferences.getNames()
		).thenReturn(
			Collections.enumeration(preferences.keySet())
		);

		Mockito.when(
			portletPreferences.getValues(Mockito.anyString(), Mockito.any())
		).thenAnswer(
			invocation -> new String[] {
				preferences.get(invocation.getArgument(0))
			}
		);

		UnicodeProperties unicodeProperties = UnicodePropertiesBuilder.load(
			AssetListTypeSettingsUtil.getTypeSettings(
				RandomTestUtil.randomLong(), portletPreferences)
		).build();

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