/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.tuning.rankings.web.internal.index.creation.activator;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.engine.SearchEngineInformation;
import com.liferay.portal.search.tuning.rankings.web.internal.index.importer.SingleIndexToMultipleIndexImporter;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Wade Cao
 */
public class RankingIndexCreationBundleActivatorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_rankingIndexCreationBundleActivator =
			new RankingIndexCreationBundleActivator();

		ReflectionTestUtil.setFieldValue(
			_rankingIndexCreationBundleActivator, "_searchEngineInformation",
			_searchEngineInformation);
		ReflectionTestUtil.setFieldValue(
			_rankingIndexCreationBundleActivator,
			"_singleIndexToMultipleIndexImporter",
			_singleIndexToMultipleIndexImporter);
	}

	@Test
	public void testActivatorSingleIndexToMultipleIndexImporterFalse()
		throws Exception {

		_setUpSingleIndexToMultipleIndexImporter(false);

		_rankingIndexCreationBundleActivator.activate();

		Mockito.verify(
			_singleIndexToMultipleIndexImporter, Mockito.times(1)
		).needImport();
		Mockito.verify(
			_singleIndexToMultipleIndexImporter, Mockito.times(0)
		).importRankings();
	}

	@Test
	public void testActivatorSingleIndexToMultipleIndexImporterTrue()
		throws Exception {

		_setUpSingleIndexToMultipleIndexImporter(true);

		_rankingIndexCreationBundleActivator.activate();

		Mockito.verify(
			_singleIndexToMultipleIndexImporter, Mockito.times(1)
		).needImport();
		Mockito.verify(
			_singleIndexToMultipleIndexImporter, Mockito.times(1)
		).importRankings();
	}

	private void _setUpSingleIndexToMultipleIndexImporter(boolean exist) {
		Mockito.doReturn(
			exist
		).when(
			_singleIndexToMultipleIndexImporter
		).needImport();
	}

	private RankingIndexCreationBundleActivator
		_rankingIndexCreationBundleActivator;
	private final SearchEngineInformation _searchEngineInformation =
		Mockito.mock(SearchEngineInformation.class);
	private final SingleIndexToMultipleIndexImporter
		_singleIndexToMultipleIndexImporter = Mockito.mock(
			SingleIndexToMultipleIndexImporter.class);

}