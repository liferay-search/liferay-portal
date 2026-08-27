/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.list.internal.exportimport.content.processor;

import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Gustavo Lima
 */
public class AssetListEntryExportImportContentProcessorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testAddGroupMappingsElementWithNonexistentGroup() {
		PortletDataContext portletDataContext = Mockito.mock(
			PortletDataContext.class);

		Element rootElement = Mockito.spy(Element.class);

		Mockito.doReturn(
			rootElement
		).when(
			portletDataContext
		).getExportDataRootElement();

		Element groupIdMappingsElement = Mockito.spy(Element.class);

		Mockito.doReturn(
			groupIdMappingsElement
		).when(
			rootElement
		).addElement(
			Mockito.anyString()
		);

		GroupLocalService groupLocalService = Mockito.mock(
			GroupLocalService.class);

		Mockito.doReturn(
			null
		).when(
			groupLocalService
		).fetchGroup(
			Mockito.anyLong()
		);

		ReflectionTestUtil.setFieldValue(
			_assetListEntryExportImportContentProcessor, "_groupLocalService",
			groupLocalService);

		_assetListEntryExportImportContentProcessor.addGroupMappingsElement(
			portletDataContext, new long[] {RandomTestUtil.randomLong()});

		Mockito.verify(
			groupIdMappingsElement, Mockito.never()
		).addElement(
			Mockito.anyString()
		);

		Mockito.verify(
			rootElement, Mockito.atMostOnce()
		).addElement(
			Mockito.anyString()
		);
	}

	@Test
	public void testReplaceExportContentReferencesWithUnresolvableClassNameIds()
		throws Exception {

		Portal portal = Mockito.mock(Portal.class);

		String className = RandomTestUtil.randomString();
		long classNameId = RandomTestUtil.randomLong();

		Mockito.doReturn(
			className
		).when(
			portal
		).fetchClassName(
			classNameId
		);

		long unresolvableClassNameId = RandomTestUtil.randomLong();

		Mockito.doReturn(
			""
		).when(
			portal
		).fetchClassName(
			unresolvableClassNameId
		);

		ReflectionTestUtil.setFieldValue(
			_assetListEntryExportImportContentProcessor, "_portal", portal);

		PortletDataContext portletDataContext = Mockito.mock(
			PortletDataContext.class);

		Element rootElement = Mockito.spy(Element.class);

		Mockito.doReturn(
			rootElement
		).when(
			portletDataContext
		).getExportDataRootElement();

		Mockito.doReturn(
			Mockito.spy(Element.class)
		).when(
			rootElement
		).addElement(
			Mockito.anyString()
		);

		UnicodeProperties unicodeProperties = UnicodePropertiesBuilder.create(
			true
		).put(
			"anyAssetType", String.valueOf(unresolvableClassNameId)
		).put(
			"classNameIds", classNameId + "," + unresolvableClassNameId
		).build();

		String content =
			_assetListEntryExportImportContentProcessor.
				replaceExportContentReferences(
					portletDataContext, null, unicodeProperties.toString(),
					false, false);

		UnicodeProperties contentUnicodeProperties =
			UnicodePropertiesBuilder.load(
				content
			).build();

		Assert.assertEquals(
			className,
			contentUnicodeProperties.getProperty("classNames", null));
		Assert.assertNull(
			contentUnicodeProperties.getProperty(
				"anyAssetTypeClassName", null));
		Assert.assertNull(
			contentUnicodeProperties.getProperty("anyAssetType", null));
	}

	private final AssetListEntryExportImportContentProcessor
		_assetListEntryExportImportContentProcessor =
			new AssetListEntryExportImportContentProcessor();

}