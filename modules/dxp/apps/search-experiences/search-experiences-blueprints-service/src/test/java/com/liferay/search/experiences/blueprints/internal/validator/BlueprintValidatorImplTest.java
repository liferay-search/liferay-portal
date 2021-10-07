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

package com.liferay.search.experiences.blueprints.internal.validator;

import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.search.experiences.blueprints.exception.BlueprintValidationException;
import com.liferay.search.experiences.blueprints.validator.BlueprintValidator;
import com.liferay.search.experiences.problems.internal.ProblemBuilderFactoryImpl;

import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Wade Cao
 */
public class BlueprintValidatorImplTest extends BaseValidatorTestCase {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_blueprintValidator = createBlueprintValidator();
	}

	@Test
	public void testValidateBlueprintWithConfigurationValue() throws Exception {
		_blueprintValidator.validateBlueprint(
			Collections.singletonMap(LocaleUtil.US, "title"),
			getBlueprintConfigurationString());
	}

	@Test(expected = BlueprintValidationException.class)
	public void testValidateBlueprintWithEmptyTitle() throws Exception {
		_blueprintValidator.validateBlueprint(Collections.emptyMap(), null);
	}

	@Test(expected = RuntimeException.class)
	public void testValidateBlueprintWithNotValidConfigurationValue()
		throws Exception {

		_blueprintValidator.validateBlueprint(
			Collections.singletonMap(LocaleUtil.US, "title"),
			"test not valid configuration value");
	}

	@Test
	public void testValidateBlueprintWithNullConfigurationValue()
		throws Exception {

		_blueprintValidator.validateBlueprint(
			Collections.singletonMap(LocaleUtil.US, "title"), null);
	}

	@Test
	public void testValidateConfiguration() throws Exception {
		_blueprintValidator.validateConfiguration(
			getBlueprintConfigurationString());
	}

	@Test(expected = RuntimeException.class)
	public void testValidateConfigurationWithValidationException()
		throws Exception {

		_blueprintValidator.validateConfiguration("test me");
	}

	protected static BlueprintValidatorImpl createBlueprintValidator() {
		BlueprintValidatorImpl blueprintValidatorImpl =
			new BlueprintValidatorImpl();

		blueprintValidatorImpl.setProblemBuilderFactory(
			new ProblemBuilderFactoryImpl());

		return blueprintValidatorImpl;
	}

	private BlueprintValidator _blueprintValidator;

}