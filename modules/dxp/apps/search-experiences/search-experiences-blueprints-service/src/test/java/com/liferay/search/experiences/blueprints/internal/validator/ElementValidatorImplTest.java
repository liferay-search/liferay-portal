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
import com.liferay.search.experiences.blueprints.internal.validator.util.BlueprintJSONValidatorUtil;

import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Matchers;
import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Wade Cao
 */
@PrepareForTest(BlueprintJSONValidatorUtil.class)
@RunWith(PowerMockRunner.class)
public class ElementValidatorImplTest extends BaseValidatorTestCase {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		setUpJSONFactoryUtil();
	}

	@Test
	public void testValidateConfiguration() throws Exception {
		setUpBlueprintJSONValidatorUtilDoNothing();

		_elementValidatorImpl.validateConfiguration(
			getElementConfigurationString(), 1);
		_elementValidatorImpl.validateConfiguration(
			getElementConfigurationString(), 5);
		_elementValidatorImpl.validateConfiguration(
			getElementConfigurationString(), 10);
		_elementValidatorImpl.validateConfiguration(
			getElementConfigurationString(), 15);

		PowerMockito.verifyStatic(Mockito.times(4));
		BlueprintJSONValidatorUtil.validate(
			Matchers.anyString(), Matchers.anyObject());
	}

	@Test(expected = ElementValidationException.class)
	public void testValidateConfigurationWithMissingRequiredProperties()
		throws Exception {

		_elementValidatorImpl.validateConfiguration(
			getConfigurationStringWithMissingRequiredProperties(), 1);
		_elementValidatorImpl.validateConfiguration(
			getConfigurationStringWithMissingRequiredProperties(), 15);
	}

	@Test(expected = ElementValidationException.class)
	public void testValidateConfigurationWithNotValidConfigurationValue()
		throws Exception {

		_elementValidatorImpl.validateConfiguration(
			"test not valid configuration value", 1);
	}

	@Test
	public void testValidateConfigurationWithNullConfigurationValue()
		throws Exception {

		_elementValidatorImpl.validateConfiguration(null, -1);

		PowerMockito.verifyZeroInteractions(BlueprintJSONValidatorUtil.class);
	}

	@Test
	public void testValidateElement() throws Exception {
		setUpBlueprintJSONValidatorUtilDoNothing();

		_elementValidatorImpl.validateElement(
			Collections.singletonMap(LocaleUtil.US, "title"),
			getElementConfigurationString(), 1);
		_elementValidatorImpl.validateElement(
			Collections.singletonMap(LocaleUtil.US, "title"),
			getElementConfigurationString(), 5);
		_elementValidatorImpl.validateElement(
			Collections.singletonMap(LocaleUtil.US, "title"),
			getElementConfigurationString(), 10);
		_elementValidatorImpl.validateElement(
			Collections.singletonMap(LocaleUtil.US, "title"),
			getElementConfigurationString(), 15);

		PowerMockito.verifyStatic(Mockito.times(4));
		BlueprintJSONValidatorUtil.validate(
			Matchers.anyString(), Matchers.anyObject());
	}

	@Test(expected = ElementValidationException.class)
	public void testValidateElementWithEmptyTitle() throws Exception {
		_elementValidatorImpl.validateElement(Collections.emptyMap(), null, 1);
	}

	private final ElementValidatorImpl _elementValidatorImpl =
		new ElementValidatorImpl();

}