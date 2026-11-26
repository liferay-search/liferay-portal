/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.web.internal.search.results.portlet.action;

import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.search.engine.SearchEngineInformation;
import com.liferay.portal.search.web.internal.search.results.portlet.SearchResultsPortletPreferences;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Joshua Cords
 */
public class SearchResultsConfigurationActionTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		PortalUtil portalUtil = new PortalUtil();

		Portal portal = Mockito.mock(Portal.class);

		Mockito.doAnswer(
			invocationOnMock -> {
				LiferayPortletRequest liferayPortletRequest =
					invocationOnMock.getArgument(0);

				return liferayPortletRequest.getHttpServletRequest();
			}
		).when(
			portal
		).getHttpServletRequest(
			Mockito.any()
		);

		Mockito.doAnswer(
			invocationOnMock -> invocationOnMock.getArgument(0)
		).when(
			portal
		).getLiferayPortletRequest(
			Mockito.any()
		);

		Mockito.doAnswer(
			invocationOnMock -> invocationOnMock.getArgument(0)
		).when(
			portal
		).getOriginalServletRequest(
			Mockito.any()
		);

		portalUtil.setPortal(portal);

		Snapshot<SearchEngineInformation> searchEngineInformationSnapshot =
			Mockito.mock(Snapshot.class);

		SearchEngineInformation searchEngineInformation = Mockito.mock(
			SearchEngineInformation.class);

		Mockito.doReturn(
			500
		).when(
			searchEngineInformation
		).getIndexMaxResultWindow();

		Mockito.doReturn(
			searchEngineInformation
		).when(
			searchEngineInformationSnapshot
		).get();

		ReflectionTestUtil.setFieldValue(
			SearchResultsConfigurationAction.class,
			"_searchEngineInformationSnapshot",
			searchEngineInformationSnapshot);
	}

	@Test
	public void testProcessActionMaximumSearchDepth() throws Exception {
		_testProcessActionMaximumSearchDepth(false, "500");
		_testProcessActionMaximumSearchDepth(false, null);
		_testProcessActionMaximumSearchDepth(true, "501");
	}

	private void _testProcessActionMaximumSearchDepth(
			boolean expectedError, String maximumSearchDepth)
		throws Exception {

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		if (maximumSearchDepth != null) {
			mockLiferayPortletActionRequest.addParameter(
				"preferences--" +
					SearchResultsPortletPreferences.
						PREFERENCE_KEY_MAXIMUM_SEARCH_DEPTH + "--",
				maximumSearchDepth);
		}

		SearchResultsConfigurationAction searchResultsConfigurationAction =
			new SearchResultsConfigurationAction();

		searchResultsConfigurationAction.processAction(
			null, mockLiferayPortletActionRequest,
			new MockLiferayPortletActionResponse());

		Assert.assertEquals(
			expectedError,
			SessionErrors.contains(
				mockLiferayPortletActionRequest,
				SearchResultsPortletPreferences.
					PREFERENCE_KEY_MAXIMUM_SEARCH_DEPTH));

		if (expectedError) {
			Assert.assertEquals(
				500,
				SessionErrors.get(
					mockLiferayPortletActionRequest,
					SearchResultsPortletPreferences.
						PREFERENCE_KEY_MAXIMUM_SEARCH_DEPTH));
		}
	}

}