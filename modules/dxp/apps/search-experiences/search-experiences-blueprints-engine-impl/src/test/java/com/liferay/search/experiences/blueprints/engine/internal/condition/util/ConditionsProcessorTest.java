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
import com.liferay.search.experiences.blueprints.engine.internal.condition.ConditionHandlerFactory;
import com.liferay.search.experiences.blueprints.engine.internal.util.ProblemBuilders;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.clause.ConditionHandler;
import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Wade Cao
 */
public class ConditionsProcessorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		_conditionsProcessor = createConditionsProcessor();
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
				_jsonObject, _parameterData, null, _problemsHolderBuilder));
	}

	@Test
	public void testProcessConditionsWithAllOf() {
		setUpJsonObjectLength();

		Mockito.when(
			_jsonObject.keySet()
		).thenReturn(
			new HashSet<>(Arrays.asList("all_of"))
		);

		Assert.assertTrue(
			_conditionsProcessor.processConditions(
				_jsonObject, _parameterData, null, _problemsHolderBuilder));
	}

	@Test
	public void testProcessConditionsWithAnyOf() {
		setUpJsonObjectLength();
		setUpJsonObjectKeySet(new HashSet<>(Arrays.asList("any_of")));

		Assert.assertTrue(
			_conditionsProcessor.processConditions(
				_jsonObject, _parameterData, null, _problemsHolderBuilder));
	}

	@Test
	public void testProcessConditionsWithChildrenUnValid() {
		setUpJsonObjectLength();
		setUpJsonObjectKeySet(new HashSet<>(Arrays.asList("testKey")));

		Assert.assertFalse(
			_conditionsProcessor.processConditions(
				_jsonObject, _parameterData, null, _problemsHolderBuilder));
	}

	@Test
	public void testProcessConditionsWithNullArguments() {
		Assert.assertTrue(
			_conditionsProcessor.processConditions(null, null, null, null));
	}

	protected ConditionsProcessor createConditionsProcessor() {
		ConditionsProcessor conditionsProcessor = new ConditionsProcessor();

		ProblemBuilders problemBuilders = Mockito.mock(ProblemBuilders.class);

		Mockito.doReturn(
			Mockito.mock(ProblemBuilder.class)
		).when(
			problemBuilders
		).unknownError(
			Mockito.any(), Mockito.any(), Mockito.any()
		);

		conditionsProcessor.setProblemBuilders(problemBuilders);

		return conditionsProcessor;
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

	private ConditionsProcessor _conditionsProcessor;

	@Mock
	private JSONObject _jsonObject;

	@Mock
	private ParameterData _parameterData;

	@Mock
	private ProblemsHolderBuilder _problemsHolderBuilder;

}