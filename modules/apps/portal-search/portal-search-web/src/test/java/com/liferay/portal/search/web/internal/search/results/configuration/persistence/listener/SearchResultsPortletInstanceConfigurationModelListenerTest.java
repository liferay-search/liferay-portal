/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.web.internal.search.results.configuration.persistence.listener;

import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.search.engine.SearchEngineInformation;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Joshua Cords
 */
public class SearchResultsPortletInstanceConfigurationModelListenerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
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
			SearchResultsPortletInstanceConfigurationModelListener.class,
			"_searchEngineInformationSnapshot",
			searchEngineInformationSnapshot);

		Language language = Mockito.mock(Language.class);

		Mockito.doAnswer(
			invocationOnMock ->
				invocationOnMock.getArgument(1) + " " +
					invocationOnMock.getArgument(2)
		).when(
			language
		).format(
			Mockito.nullable(Locale.class), Mockito.anyString(),
			Mockito.any(Object.class)
		);

		ReflectionTestUtil.setFieldValue(
			_searchResultsPortletInstanceConfigurationModelListener,
			"_language", language);
	}

	@FeatureFlag("LPD-64988")
	@Test
	public void testOnBeforeSave() throws Exception {
		_testOnBeforeSave(
			"the-maximum-search-depth-must-be-less-than-or-equal-to-the-max-" +
				"result-window-x 500",
			501);
		_testOnBeforeSave(null, 500);
		_testOnBeforeSave(null, null);
	}

	@FeatureFlag(enable = false, value = "LPD-64988")
	@Test
	public void testOnBeforeSaveWhenFeatureFlagIsDisabled() throws Exception {
		_testOnBeforeSave(null, 501);
	}

	private void _testOnBeforeSave(
			String expectedMessage, Integer maximumSearchDepth)
		throws Exception {

		Dictionary<String, Object> properties = new Hashtable<>();

		if (maximumSearchDepth != null) {
			properties.put("maximumSearchDepth", maximumSearchDepth);
		}

		try {
			_searchResultsPortletInstanceConfigurationModelListener.
				onBeforeSave(RandomTestUtil.randomString(), properties);

			Assert.assertNull(expectedMessage);
		}
		catch (ConfigurationModelListenerException
					configurationModelListenerException) {

			Assert.assertNotNull(expectedMessage);

			String message = configurationModelListenerException.getMessage();

			Assert.assertTrue(message.contains(expectedMessage));
		}
	}

	private final SearchResultsPortletInstanceConfigurationModelListener
		_searchResultsPortletInstanceConfigurationModelListener =
			new SearchResultsPortletInstanceConfigurationModelListener();

}