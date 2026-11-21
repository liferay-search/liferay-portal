/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.list.web.internal.display.context;

import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.asset.list.service.AssetListEntryAudienceEntryRelLocalServiceUtil;
import com.liferay.asset.list.service.AssetListEntrySegmentsEntryRelLocalServiceUtil;
import com.liferay.asset.list.service.AssetListEntryUsageLocalServiceUtil;
import com.liferay.asset.util.AssetRendererFactoryClassProvider;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactory;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.portlet.PortletRequest;
import jakarta.portlet.RenderRequest;
import jakarta.portlet.RenderResponse;

import jakarta.servlet.http.HttpServletRequest;

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
public class AssetListDisplayContextTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_setUpPortalGetHttpServletRequest();
		_setUpPortalUtil();
		_setUpPortletPreferencesFactoryUtil();
		_setUpThemeDisplay();
	}

	@Test
	@TestInfo("LPD-116888")
	public void testGetAssetListEntryUsageCount() {
		AssetListEntry assetListEntry = Mockito.mock(AssetListEntry.class);

		Mockito.when(
			assetListEntry.getAssetListEntryId()
		).thenReturn(
			_ASSET_LIST_ENTRY_ID
		);

		Mockito.when(
			_portal.getClassNameId(AssetListEntry.class)
		).thenReturn(
			_CLASS_NAME_ID
		);

		try (MockedStatic<AssetListEntryUsageLocalServiceUtil>
				assetListEntryUsageLocalServiceUtilMockedStatic =
					Mockito.mockStatic(
						AssetListEntryUsageLocalServiceUtil.class)) {

			assetListEntryUsageLocalServiceUtilMockedStatic.when(
				() ->
					AssetListEntryUsageLocalServiceUtil.
						getAssetListEntryUsagesCount(
							_GROUP_ID, _CLASS_NAME_ID,
							String.valueOf(_ASSET_LIST_ENTRY_ID))
			).thenReturn(
				1
			);

			assetListEntryUsageLocalServiceUtilMockedStatic.when(
				() ->
					AssetListEntryUsageLocalServiceUtil.
						getCompanyAssetListEntryUsagesCount(
							_COMPANY_ID, _CLASS_NAME_ID,
							String.valueOf(_ASSET_LIST_ENTRY_ID))
			).thenReturn(
				3
			);

			AssetListDisplayContext assetListDisplayContext =
				new AssetListDisplayContext(
					_assetRendererFactoryClassProvider,
					Mockito.mock(RenderRequest.class),
					Mockito.mock(RenderResponse.class));

			Assert.assertEquals(
				1,
				assetListDisplayContext.getAssetListEntryUsageCount(
					assetListEntry));

			Mockito.when(
				_group.isCompany()
			).thenReturn(
				true
			);

			Assert.assertEquals(
				3,
				assetListDisplayContext.getAssetListEntryUsageCount(
					assetListEntry));

			Mockito.when(
				_group.isCompany()
			).thenReturn(
				false
			);

			Mockito.when(
				_group.isDepot()
			).thenReturn(
				true
			);

			Assert.assertEquals(
				3,
				assetListDisplayContext.getAssetListEntryUsageCount(
					assetListEntry));
		}
	}

	@Test
	public void testGetAssetListEntryVariationsCount() {
		AssetListEntry assetListEntry = Mockito.mock(AssetListEntry.class);

		Mockito.when(
			assetListEntry.getAssetListEntryId()
		).thenReturn(
			_ASSET_LIST_ENTRY_ID
		);

		Mockito.when(
			assetListEntry.getCompanyId()
		).thenReturn(
			_COMPANY_ID
		);

		AssetListDisplayContext assetListDisplayContext =
			new AssetListDisplayContext(
				_assetRendererFactoryClassProvider,
				Mockito.mock(RenderRequest.class),
				Mockito.mock(RenderResponse.class));

		try (MockedStatic<AssetListEntryAudienceEntryRelLocalServiceUtil>
				assetListEntryAudienceEntryRelLocalServiceUtilMockedStatic =
					Mockito.mockStatic(
						AssetListEntryAudienceEntryRelLocalServiceUtil.class);
			MockedStatic<AssetListEntrySegmentsEntryRelLocalServiceUtil>
				assetListEntrySegmentsEntryRelLocalServiceUtilMockedStatic =
					Mockito.mockStatic(
						AssetListEntrySegmentsEntryRelLocalServiceUtil.class);
			MockedStatic<FeatureFlagManagerUtil>
				featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
					FeatureFlagManagerUtil.class)) {

			featureFlagManagerUtilMockedStatic.when(
				() -> FeatureFlagManagerUtil.isEnabled(
					_COMPANY_ID, "LPD-109242")
			).thenReturn(
				false
			);

			assetListEntrySegmentsEntryRelLocalServiceUtilMockedStatic.when(
				() ->
					AssetListEntrySegmentsEntryRelLocalServiceUtil.
						getAssetListEntrySegmentsEntryRelsCount(
							_ASSET_LIST_ENTRY_ID)
			).thenReturn(
				1
			);

			Assert.assertEquals(
				0,
				assetListDisplayContext.getAssetListEntryVariationsCount(
					assetListEntry));

			assetListEntrySegmentsEntryRelLocalServiceUtilMockedStatic.when(
				() ->
					AssetListEntrySegmentsEntryRelLocalServiceUtil.
						getAssetListEntrySegmentsEntryRelsCount(
							_ASSET_LIST_ENTRY_ID)
			).thenReturn(
				3
			);

			Assert.assertEquals(
				3,
				assetListDisplayContext.getAssetListEntryVariationsCount(
					assetListEntry));

			featureFlagManagerUtilMockedStatic.when(
				() -> FeatureFlagManagerUtil.isEnabled(
					_COMPANY_ID, "LPD-109242")
			).thenReturn(
				true
			);

			assetListEntryAudienceEntryRelLocalServiceUtilMockedStatic.when(
				() ->
					AssetListEntryAudienceEntryRelLocalServiceUtil.
						getAssetListEntryAudienceEntryRelsCount(
							_ASSET_LIST_ENTRY_ID)
			).thenReturn(
				0
			);

			Assert.assertEquals(
				0,
				assetListDisplayContext.getAssetListEntryVariationsCount(
					assetListEntry));

			assetListEntryAudienceEntryRelLocalServiceUtilMockedStatic.when(
				() ->
					AssetListEntryAudienceEntryRelLocalServiceUtil.
						getAssetListEntryAudienceEntryRelsCount(
							_ASSET_LIST_ENTRY_ID)
			).thenReturn(
				2
			);

			Assert.assertEquals(
				3,
				assetListDisplayContext.getAssetListEntryVariationsCount(
					assetListEntry));

			featureFlagManagerUtilMockedStatic.when(
				() -> FeatureFlagManagerUtil.isEnabled(
					CompanyConstants.SYSTEM, "LPD-78863")
			).thenReturn(
				true
			);

			assetListEntrySegmentsEntryRelLocalServiceUtilMockedStatic.when(
				() ->
					AssetListEntrySegmentsEntryRelLocalServiceUtil.
						getAssetListEntrySegmentsEntryRelsCount(
							_ASSET_LIST_ENTRY_ID)
			).thenReturn(
				5
			);

			Assert.assertEquals(
				5,
				assetListDisplayContext.getAssetListEntryVariationsCount(
					assetListEntry));
		}
	}

	@Test
	public void testGetClassName() throws Exception {
		_setUpAssetRendererFactoryClassProvider();

		AssetListDisplayContext assetListDisplayContext =
			new AssetListDisplayContext(
				_assetRendererFactoryClassProvider,
				Mockito.mock(RenderRequest.class),
				Mockito.mock(RenderResponse.class));

		Class<? extends AssetRendererFactory> clazz =
			_assetRendererFactory.getClass();

		Assert.assertEquals(
			clazz.getSimpleName(),
			assetListDisplayContext.getClassName(_assetRendererFactory));
	}

	private void _setUpAssetRendererFactoryClassProvider() {
		Mockito.doReturn(
			_assetRendererFactory.getClass()
		).when(
			_assetRendererFactoryClassProvider
		).getClass(
			_assetRendererFactory
		);
	}

	private void _setUpPortalGetHttpServletRequest() {
		Mockito.doReturn(
			_httpServletRequest
		).when(
			_portal
		).getHttpServletRequest(
			Mockito.any(PortletRequest.class)
		);
	}

	private void _setUpPortalUtil() {
		PortalUtil portalUtil = new PortalUtil();

		portalUtil.setPortal(_portal);
	}

	private void _setUpPortletPreferencesFactoryUtil() {
		PortletPreferencesFactoryUtil portletPreferencesFactoryUtil =
			new PortletPreferencesFactoryUtil();

		portletPreferencesFactoryUtil.setPortletPreferencesFactory(
			Mockito.mock(PortletPreferencesFactory.class));
	}

	private void _setUpThemeDisplay() {
		Mockito.when(
			_themeDisplay.getCompanyId()
		).thenReturn(
			_COMPANY_ID
		);

		Mockito.when(
			_themeDisplay.getScopeGroup()
		).thenReturn(
			_group
		);

		Mockito.when(
			_themeDisplay.getScopeGroupId()
		).thenReturn(
			_GROUP_ID
		);

		Mockito.when(
			_httpServletRequest.getAttribute(WebKeys.THEME_DISPLAY)
		).thenReturn(
			_themeDisplay
		);
	}

	private static final long _ASSET_LIST_ENTRY_ID =
		RandomTestUtil.randomLong();

	private static final long _CLASS_NAME_ID = RandomTestUtil.randomLong();

	private static final long _COMPANY_ID = RandomTestUtil.randomLong();

	private static final long _GROUP_ID = RandomTestUtil.randomLong();

	private final AssetRendererFactory<?> _assetRendererFactory = Mockito.mock(
		AssetRendererFactory.class);
	private final AssetRendererFactoryClassProvider
		_assetRendererFactoryClassProvider = Mockito.mock(
			AssetRendererFactoryClassProvider.class);
	private final Group _group = Mockito.mock(Group.class);
	private final HttpServletRequest _httpServletRequest = Mockito.mock(
		HttpServletRequest.class);
	private final Portal _portal = Mockito.mock(Portal.class);
	private final ThemeDisplay _themeDisplay = Mockito.mock(ThemeDisplay.class);

}