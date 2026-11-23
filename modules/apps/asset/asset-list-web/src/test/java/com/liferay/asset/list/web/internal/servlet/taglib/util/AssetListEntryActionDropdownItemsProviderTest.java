/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.list.web.internal.servlet.taglib.util;

import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.asset.list.service.AssetListEntryUsageLocalServiceUtil;
import com.liferay.asset.list.web.internal.security.permission.resource.AssetListEntryPermission;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.staging.StagingGroupHelper;
import com.liferay.staging.StagingGroupHelperUtil;
import com.liferay.taglib.security.PermissionsURLTag;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Objects;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Selena Aungst
 */
public class AssetListEntryActionDropdownItemsProviderTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_setUpAssetListEntryUsageLocalServiceUtil();
		_setUpLanguageUtil();
		_setUpLiferayPortletRequest();
		_setUpLiferayPortletResponse();
		_setUpPortalUtil();
		_setUpStagingGroupHelperUtil();
		_setUpThemeDisplay();
	}

	@After
	public void tearDown() {
		_assetListEntryPermissionMockedStatic.close();
		_assetListEntryUsageLocalServiceUtilMockedStatic.close();
		_permissionsURLTagMockedStatic.close();
		_stagingGroupHelperUtilMockedStatic.close();
	}

	@Test
	@TestInfo("LPD-116888")
	public void testGetActionDropdownItems() {
		DropdownItem viewUsagesDropdownItem = _getViewUsagesDropdownItem();

		Assert.assertFalse((Boolean)viewUsagesDropdownItem.get("disabled"));

		Mockito.when(
			_group.isCompany()
		).thenReturn(
			true
		);

		Assert.assertNull(_getViewUsagesDropdownItem());

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

		Assert.assertNull(_getViewUsagesDropdownItem());
	}

	private DropdownItem _getViewUsagesDropdownItem() {
		AssetListEntryActionDropdownItemsProvider
			assetListEntryActionDropdownItemsProvider =
				new AssetListEntryActionDropdownItemsProvider(
					Mockito.mock(AssetListEntry.class), _liferayPortletRequest,
					_liferayPortletResponse);

		for (DropdownItem dropdownGroupItem :
				assetListEntryActionDropdownItemsProvider.
					getActionDropdownItems()) {

			List<DropdownItem> dropdownItems =
				(List<DropdownItem>)dropdownGroupItem.get("items");

			for (DropdownItem dropdownItem : dropdownItems) {
				if (Objects.equals(dropdownItem.get("label"), "view-usages")) {
					return dropdownItem;
				}
			}
		}

		return null;
	}

	private void _setUpAssetListEntryUsageLocalServiceUtil() {
		_assetListEntryUsageLocalServiceUtilMockedStatic.when(
			() ->
				AssetListEntryUsageLocalServiceUtil.
					getAssetListEntryUsagesCount(
						Mockito.anyLong(), Mockito.anyLong(),
						Mockito.anyString())
		).thenReturn(
			1
		);
	}

	private void _setUpLanguageUtil() {
		LanguageUtil languageUtil = new LanguageUtil();

		languageUtil.setLanguage(Mockito.mock(Language.class));

		Mockito.when(
			languageUtil.get(
				Mockito.any(HttpServletRequest.class), Mockito.anyString())
		).thenAnswer(
			invocationOnMock -> invocationOnMock.getArguments()[1]
		);
	}

	private void _setUpLiferayPortletRequest() {
		Mockito.when(
			_liferayPortletRequest.getAttribute(WebKeys.THEME_DISPLAY)
		).thenReturn(
			_themeDisplay
		);
	}

	private void _setUpLiferayPortletResponse() {
		Mockito.when(
			_liferayPortletResponse.createRenderURL()
		).thenReturn(
			Mockito.mock(LiferayPortletURL.class)
		);
	}

	private void _setUpPortalUtil() {
		PortalUtil portalUtil = new PortalUtil();

		portalUtil.setPortal(Mockito.mock(Portal.class));

		Mockito.when(
			portalUtil.getHttpServletRequest(_liferayPortletRequest)
		).thenReturn(
			Mockito.mock(HttpServletRequest.class)
		);
	}

	private void _setUpStagingGroupHelperUtil() {
		_stagingGroupHelperUtilMockedStatic.when(
			StagingGroupHelperUtil::getStagingGroupHelper
		).thenReturn(
			Mockito.mock(StagingGroupHelper.class)
		);
	}

	private void _setUpThemeDisplay() {
		Mockito.when(
			_themeDisplay.getScopeGroup()
		).thenReturn(
			_group
		);
	}

	private final MockedStatic<AssetListEntryPermission>
		_assetListEntryPermissionMockedStatic = Mockito.mockStatic(
			AssetListEntryPermission.class);
	private final MockedStatic<AssetListEntryUsageLocalServiceUtil>
		_assetListEntryUsageLocalServiceUtilMockedStatic = Mockito.mockStatic(
			AssetListEntryUsageLocalServiceUtil.class);
	private final Group _group = Mockito.mock(Group.class);
	private final LiferayPortletRequest _liferayPortletRequest = Mockito.mock(
		LiferayPortletRequest.class);
	private final LiferayPortletResponse _liferayPortletResponse = Mockito.mock(
		LiferayPortletResponse.class);
	private final MockedStatic<PermissionsURLTag>
		_permissionsURLTagMockedStatic = Mockito.mockStatic(
			PermissionsURLTag.class);
	private final MockedStatic<StagingGroupHelperUtil>
		_stagingGroupHelperUtilMockedStatic = Mockito.mockStatic(
			StagingGroupHelperUtil.class);
	private final ThemeDisplay _themeDisplay = Mockito.mock(ThemeDisplay.class);

}