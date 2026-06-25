/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.spi.reindexer;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Felipe Lorenz
 */
public class IndexReindexerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testCreateExecutionModeDefaultsToFull() {
		Assert.assertEquals(
			IndexReindexer.ExecutionMode.FULL,
			IndexReindexer.ExecutionMode.create(null));
		Assert.assertEquals(
			IndexReindexer.ExecutionMode.FULL,
			IndexReindexer.ExecutionMode.create(""));
		Assert.assertEquals(
			IndexReindexer.ExecutionMode.FULL,
			IndexReindexer.ExecutionMode.create("   "));
		Assert.assertEquals(
			IndexReindexer.ExecutionMode.FULL,
			IndexReindexer.ExecutionMode.create("unknown"));
	}

	@Test
	public void testCreateExecutionModeFromValidValues() {
		Assert.assertEquals(
			IndexReindexer.ExecutionMode.CONCURRENT,
			IndexReindexer.ExecutionMode.create("concurrent"));
		Assert.assertEquals(
			IndexReindexer.ExecutionMode.CONCURRENT,
			IndexReindexer.ExecutionMode.create("CONCURRENT"));
		Assert.assertEquals(
			IndexReindexer.ExecutionMode.FULL,
			IndexReindexer.ExecutionMode.create("full"));
		Assert.assertEquals(
			IndexReindexer.ExecutionMode.FULL,
			IndexReindexer.ExecutionMode.create("FULL"));
		Assert.assertEquals(
			IndexReindexer.ExecutionMode.SYNC,
			IndexReindexer.ExecutionMode.create("sync"));
		Assert.assertEquals(
			IndexReindexer.ExecutionMode.SYNC,
			IndexReindexer.ExecutionMode.create("SYNC"));
		Assert.assertEquals(
			IndexReindexer.ExecutionMode.SYNC,
			IndexReindexer.ExecutionMode.create("SyNc"));
	}

}