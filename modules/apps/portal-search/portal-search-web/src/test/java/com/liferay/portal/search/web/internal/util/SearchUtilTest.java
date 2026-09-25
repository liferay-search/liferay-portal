/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.web.internal.util;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.kernel.service.AssetEntryLocalServiceUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.portlet.PortletURL;
import jakarta.portlet.RenderRequest;
import jakarta.portlet.RenderResponse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Jürgen Kappler
 */
public class SearchUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_assetEntryLocalServiceUtilMockedStatic = Mockito.mockStatic(
			AssetEntryLocalServiceUtil.class);
		_assetRendererFactoryRegistryUtilMockedStatic = Mockito.mockStatic(
			AssetRendererFactoryRegistryUtil.class);

		_setUpAssetRendererFactory();
		_setUpPortalUtil();
		_setUpRenderResponse();
	}

	@After
	public void tearDown() {
		_assetEntryLocalServiceUtilMockedStatic.close();
		_assetRendererFactoryRegistryUtilMockedStatic.close();
	}

	@Test
	public void testGetSearchResultViewURLWithViewInContext() {
		Assert.assertEquals(
			_VIEW_IN_CONTEXT_URL,
			SearchUtil.getSearchResultViewURL(
				_renderRequest, _renderResponse, _CLASS_NAME, _CLASS_PK, true));
	}

	@Test
	public void testGetSearchResultViewURLWithoutViewInContext()
		throws Exception {

		Assert.assertEquals(
			_VIEW_CONTENT_URL,
			SearchUtil.getSearchResultViewURL(
				_renderRequest, _renderResponse, _CLASS_NAME, _CLASS_PK,
				false));

		Mockito.verify(
			_assetRenderer, Mockito.never()
		).getURLViewInContext(
			Mockito.any(LiferayPortletRequest.class),
			Mockito.any(LiferayPortletResponse.class), Mockito.anyString()
		);
	}

	private void _setUpAssetRendererFactory() throws Exception {
		AssetEntry assetEntry = Mockito.mock(AssetEntry.class);

		_assetEntryLocalServiceUtilMockedStatic.when(
			() -> AssetEntryLocalServiceUtil.getEntry(_CLASS_NAME, _CLASS_PK)
		).thenReturn(
			assetEntry
		);

		AssetRendererFactory<?> assetRendererFactory = Mockito.mock(
			AssetRendererFactory.class);

		_assetRendererFactoryRegistryUtilMockedStatic.when(
			() ->
				AssetRendererFactoryRegistryUtil.
					getAssetRendererFactoryByClassName(_CLASS_NAME)
		).thenReturn(
			assetRendererFactory
		);

		Mockito.doReturn(
			_assetRenderer
		).when(
			assetRendererFactory
		).getAssetRenderer(
			_CLASS_PK
		);

		Mockito.when(
			_assetRenderer.getURLViewInContext(
				Mockito.any(LiferayPortletRequest.class),
				Mockito.any(LiferayPortletResponse.class),
				Mockito.eq(_VIEW_CONTENT_URL))
		).thenReturn(
			_VIEW_IN_CONTEXT_URL
		);
	}

	private void _setUpPortalUtil() {
		PortalUtil portalUtil = new PortalUtil();

		Portal portal = Mockito.mock(Portal.class);

		Mockito.when(
			portal.getLiferayPortletRequest(_renderRequest)
		).thenReturn(
			Mockito.mock(LiferayPortletRequest.class)
		);

		Mockito.when(
			portal.getLiferayPortletResponse(_renderResponse)
		).thenReturn(
			Mockito.mock(LiferayPortletResponse.class)
		);

		portalUtil.setPortal(portal);
	}

	private void _setUpRenderResponse() {
		PortletURL portletURL = Mockito.mock(PortletURL.class);

		Mockito.when(
			portletURL.toString()
		).thenReturn(
			_VIEW_CONTENT_URL
		);

		Mockito.when(
			_renderResponse.createRenderURL()
		).thenReturn(
			portletURL
		);
	}

	private static final String _CLASS_NAME = RandomTestUtil.randomString();

	private static final long _CLASS_PK = RandomTestUtil.randomLong();

	private static final String _VIEW_CONTENT_URL =
		RandomTestUtil.randomString();

	private static final String _VIEW_IN_CONTEXT_URL =
		RandomTestUtil.randomString();

	private MockedStatic<AssetEntryLocalServiceUtil>
		_assetEntryLocalServiceUtilMockedStatic;
	private final AssetRenderer<?> _assetRenderer = Mockito.mock(
		AssetRenderer.class);
	private MockedStatic<AssetRendererFactoryRegistryUtil>
		_assetRendererFactoryRegistryUtilMockedStatic;
	private final RenderRequest _renderRequest = Mockito.mock(
		RenderRequest.class);
	private final RenderResponse _renderResponse = Mockito.mock(
		RenderResponse.class);

}