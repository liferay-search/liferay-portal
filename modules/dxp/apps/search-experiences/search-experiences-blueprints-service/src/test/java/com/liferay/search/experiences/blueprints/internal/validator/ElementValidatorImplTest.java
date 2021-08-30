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
import com.liferay.search.experiences.blueprints.exception.ElementValidationException;
import com.liferay.search.experiences.blueprints.validator.ElementValidator;
import com.liferay.search.experiences.problems.internal.ProblemBuilderFactoryImpl;

import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Wade Cao
 */
public class ElementValidatorImplTest extends BaseValidatorTestCase {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_elementValidator = createElementValidator();
	}

	@Test
	public void testValidateConfiguration() throws Exception {
		_elementValidator.validateConfiguration(
			getElementConfigurationString(), 1);
		_elementValidator.validateConfiguration(
			getElementConfigurationString(), 5);
		_elementValidator.validateConfiguration(
			getElementConfigurationString(), 10);
		_elementValidator.validateConfiguration(
			getElementConfigurationString(), 15);
	}

	@Test(expected = ElementValidationException.class)
	public void testValidateConfigurationWithMissingRequiredProperties()
		throws Exception {

		_elementValidator.validateConfiguration(
			getConfigurationStringWithMissingRequiredProperties(), 1);
		_elementValidator.validateConfiguration(
			getConfigurationStringWithMissingRequiredProperties(), 15);
	}

	@Test(expected = ElementValidationException.class)
	public void testValidateConfigurationWithNotValidConfigurationValue()
		throws Exception {

		_elementValidator.validateConfiguration(
			"test not valid configuration value", 1);
	}

	@Test
	public void testValidateConfigurationWithNullConfigurationValue()
		throws Exception {

		_elementValidator.validateConfiguration(null, -1);
	}

	@Test
	public void testValidateElement() throws Exception {
		_elementValidator.validateElement(
			Collections.singletonMap(LocaleUtil.US, "title"),
			getElementConfigurationString(), 1);
		_elementValidator.validateElement(
			Collections.singletonMap(LocaleUtil.US, "title"),
			getElementConfigurationString(), 5);
		_elementValidator.validateElement(
			Collections.singletonMap(LocaleUtil.US, "title"),
			getElementConfigurationString(), 10);
		_elementValidator.validateElement(
			Collections.singletonMap(LocaleUtil.US, "title"),
			getElementConfigurationString(), 15);
	}

	@Test(expected = ElementValidationException.class)
	public void testValidateElementWithEmptyTitle() throws Exception {
		_elementValidator.validateElement(Collections.emptyMap(), null, 1);
	}

	protected static ElementValidator createElementValidator() {
		ElementValidatorImpl elementValidatorImpl = new ElementValidatorImpl();

		elementValidatorImpl.setProblemBuilderFactory(
			new ProblemBuilderFactoryImpl());

		return elementValidatorImpl;
	}

	private ElementValidator _elementValidator;

}