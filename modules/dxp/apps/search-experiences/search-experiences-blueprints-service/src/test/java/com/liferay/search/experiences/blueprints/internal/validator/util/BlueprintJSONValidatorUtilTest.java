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

package com.liferay.search.experiences.blueprints.internal.validator.util;

import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.search.experiences.blueprints.internal.validator.BaseValidatorTestCase;

import org.everit.json.schema.loader.SchemaLoader;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.internal.verification.VerificationModeFactory;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Wade Cao
 */
@PrepareForTest(SchemaLoader.class)
@RunWith(PowerMockRunner.class)
public class BlueprintJSONValidatorUtilTest extends BaseValidatorTestCase {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		setUpJSONFactoryUtil();
	}

	@Test
	public void testValidate() {
		BlueprintJSONValidatorUtil.validate(
			getElementConfigurationString(),
			getConfigurationJSONSchemaInputStream(
				"dependencies/element.schema.json"));

		BlueprintJSONValidatorUtil.validate(
			getBlueprintConfigurationString(),
			getConfigurationJSONSchemaInputStream(
				"dependencies/blueprint.schema.json"));

		PowerMockito.verifyStatic(VerificationModeFactory.times(2));
		SchemaLoader.builder();
	}

	@Test(expected = RuntimeException.class)
	public void testValidateBlueprintWithNotValidConfigurationValue()
		throws Exception {

		BlueprintJSONValidatorUtil.validate(
			"test not valid json configuration value", null);
	}

	@Test
	public void testValidateWithNullJsonString() {
		BlueprintJSONValidatorUtil.validate(null, null);

		PowerMockito.verifyZeroInteractions(SchemaLoader.class);
	}

}