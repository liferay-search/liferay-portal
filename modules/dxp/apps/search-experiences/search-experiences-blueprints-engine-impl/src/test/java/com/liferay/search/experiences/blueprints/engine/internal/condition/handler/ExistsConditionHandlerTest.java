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

package com.liferay.search.experiences.blueprints.engine.internal.condition.handler;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.message.Messages;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * @author Wade Cao
 */
public class ExistsConditionHandlerTest extends BaseConditionHandlerTestCase {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		setUpFieldValues(_existsConditionHandler);
	}

	@Test
	public void testIsTrue() {
		setUpConditionHelperGetParameterOptional(
			Optional.of(Mockito.mock(Parameter.class)));

		Assert.assertTrue(
			_existsConditionHandler.isTrue(
				_jsonObject, _parameterData, _messages));

		setUpConditionHelperGetParameterOptional(Optional.empty());

		Assert.assertFalse(
			_existsConditionHandler.isTrue(
				_jsonObject, _parameterData, _messages));
	}

	private final ExistsConditionHandler _existsConditionHandler =
		new ExistsConditionHandler();

	@Mock
	private JSONObject _jsonObject;

	@Mock
	private Messages _messages;

	@Mock
	private ParameterData _parameterData;

}