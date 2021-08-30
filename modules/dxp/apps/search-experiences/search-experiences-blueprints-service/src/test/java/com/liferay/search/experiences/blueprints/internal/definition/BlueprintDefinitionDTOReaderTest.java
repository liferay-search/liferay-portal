/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.blueprints.internal.definition;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * @author André de Oliveira
 */
public class BlueprintDefinitionDTOReaderTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testRead() throws Exception {
		BlueprintDefinitionDTO blueprintDefinitionDTO = read();

		Assert.assertTrue(
			blueprintDefinitionDTO.frameworkDefinitionDTO.applyIndexerClauses);

		String actual = String.valueOf(
			Arrays.asList(
				blueprintDefinitionDTO.frameworkDefinitionDTO.
					searchableAssetTypes));

		String expected = "com.liferay.journal.model.JournalArticle";

		if (!actual.contains(expected)) {
			Assert.assertEquals(expected, actual);
		}
	}

	@Rule
	public TestName testName = new TestName();

	protected BlueprintDefinitionDTO read() throws Exception {
		return _blueprintDefinitionDTOReader.read(readJSON());
	}

	protected String readJSON() {
		Class<?> clazz = getClass();

		return StringUtil.read(
			clazz,
			StringBundler.concat(
				clazz.getSimpleName(), StringPool.PERIOD,
				testName.getMethodName(), ".json"));
	}

	private final BlueprintDefinitionDTOReader _blueprintDefinitionDTOReader =
		new BlueprintDefinitionDTOReader();

}