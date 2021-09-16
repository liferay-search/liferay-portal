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

package com.liferay.search.experiences.internal.blueprints.definition;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author André de Oliveira
 */
public class FrameworkDefinitionImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testDefaultsAreNullSafe() {
		FrameworkDefinition frameworkDefinition = new FrameworkDefinitionImpl(
			new FrameworkDefinitionDTO());

		assertIs(
			frameworkDefinition.getApplyIndexerClausesOptional(),
			emptyOptional());
		assertIs(
			frameworkDefinition.getClauseContributorsDefinitionOptional(),
			emptyOptional());
		assertIs(frameworkDefinition.getSearchableAssetTypes(), emptyArray());
	}

	protected static <T> void assertIs(T actual, Consumer<T> consumer) {
		consumer.accept(actual);
	}

	protected static Consumer<Object[]> emptyArray() {
		return array -> Assert.assertEquals(
			"[]", String.valueOf(Arrays.asList(array)));
	}

	protected static Consumer<Optional<?>> emptyOptional() {
		return optional -> Assert.assertFalse(optional.isPresent());
	}

}