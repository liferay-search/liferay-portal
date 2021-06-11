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

package com.liferay.search.experiences.blueprints.engine.internal.condition.util;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.search.experiences.blueprints.constants.json.values.ConditionGroup;
import com.liferay.search.experiences.blueprints.engine.internal.condition.ConditionHandlerFactory;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.clause.ConditionHandler;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Wade Cao
 */
@PrepareForTest(MessagesUtil.class)
@RunWith(PowerMockRunner.class)
public class ConditionsProcessorTest extends PowerMockito {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		setUpMessageUtil();
	}

	@Test
	public void testProcessConditions() {
		setUpJsonObjectLength();
		setUpJsonObjectKeySet(new HashSet<>(Arrays.asList("testKey")));

		Mockito.when(
			_jsonObject.getJSONObject(Mockito.anyString())
		).thenReturn(
			Mockito.mock(JSONObject.class)
		);

		ConditionHandler conditionHandler = Mockito.mock(
			ConditionHandler.class);

		Mockito.when(
			conditionHandler.isTrue(
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())
		).thenReturn(
			false
		);

		ConditionHandlerFactory conditionHandlerFactory = Mockito.mock(
			ConditionHandlerFactory.class);

		Mockito.when(
			conditionHandlerFactory.getHandler(Mockito.anyString())
		).thenReturn(
			conditionHandler
		);

		ReflectionTestUtil.setFieldValue(
			_conditionsProcessor, "_conditionHandlerFactory",
			conditionHandlerFactory);

		Assert.assertFalse(
			_conditionsProcessor.processConditions(
				_jsonObject, _parameterData, _messages));
	}

	@Test
	public void testProcessConditionsWithAllOf() {
		setUpJsonObjectLength();
		Mockito.when(
			_jsonObject.keySet()
		).thenReturn(
			new HashSet<>(Arrays.asList(ConditionGroup.ALL_OF.getJsonValue()))
		);

		Assert.assertTrue(
			_conditionsProcessor.processConditions(
				_jsonObject, _parameterData, _messages));
	}

	@Test
	public void testProcessConditionsWithAnyOf() {
		setUpJsonObjectLength();
		setUpJsonObjectKeySet(
			new HashSet<>(Arrays.asList(ConditionGroup.ANY_OF.getJsonValue())));

		Assert.assertTrue(
			_conditionsProcessor.processConditions(
				_jsonObject, _parameterData, _messages));
	}

	@Test
	public void testProcessConditionsWithChildrenUnValid() {
		setUpJsonObjectLength();
		setUpJsonObjectKeySet(new HashSet<>(Arrays.asList("testKey")));

		Assert.assertFalse(
			_conditionsProcessor.processConditions(
				_jsonObject, _parameterData, _messages));
	}

	@Test
	public void testProcessConditionsWithNullArguments() {
		Assert.assertTrue(
			_conditionsProcessor.processConditions(null, null, null));
	}

	protected void setUpJsonObjectKeySet(Set<String> stringSet) {
		Mockito.when(
			_jsonObject.keySet()
		).thenReturn(
			stringSet
		);
	}

	protected void setUpJsonObjectLength() {
		Mockito.when(
			_jsonObject.length()
		).thenReturn(
			1
		);
	}

	protected void setUpMessageUtil() throws Exception {
		mockStatic(MessagesUtil.class);

		doNothing().when(
			MessagesUtil.class, "unknownError", Matchers.any(Messages.class),
			Matchers.anyString(), Matchers.any(Throwable.class),
			Matchers.any(Object.class), Matchers.anyString(),
			Matchers.anyString());
	}

	private final ConditionsProcessor _conditionsProcessor =
		new ConditionsProcessor();

	@Mock
	private JSONObject _jsonObject;

	@Mock
	private Messages _messages;

	@Mock
	private ParameterData _parameterData;

}