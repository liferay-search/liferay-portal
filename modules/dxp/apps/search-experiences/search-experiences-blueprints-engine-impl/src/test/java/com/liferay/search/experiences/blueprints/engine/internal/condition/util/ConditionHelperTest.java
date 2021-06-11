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
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.search.experiences.blueprints.engine.exception.ParameterEvaluationException;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.parameter.visitor.EvaluationVisitor;
import com.liferay.search.experiences.blueprints.engine.template.variable.BlueprintTemplateVariableParser;
import com.liferay.search.experiences.blueprints.message.Messages;

import java.util.Optional;
import java.util.function.Function;

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
public class ConditionHelperTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		setUpFieldValues();
	}

	@Test
	public void testEvaluate() throws Exception {
		setUpParameterDataGetByTemplateVariableNameOptional(
			Optional.of(_parameter));

		setUpBlueprintTemplateVariableParserParse(Optional.of(_jsonObject));

		setUpFunctionApply();

		Mockito.when(
			_parameter.accept(Mockito.anyObject())
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_conditionHelper.evaluate(
				_function, false, _jsonObject, _parameterData, _messages));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEvaluateParameterEvaluationException() throws Exception {
		setUpParameterDataGetByTemplateVariableNameOptional(
			Optional.of(_parameter));

		setUpBlueprintTemplateVariableParserParse(Optional.of(_jsonObject));

		setUpFunctionApply();

		Mockito.when(
			_parameter.accept(Mockito.anyObject())
		).thenThrow(
			ParameterEvaluationException.class
		);

		Mockito.doNothing(
		).when(
			_log
		).error(
			Mockito.anyObject(), Mockito.anyObject()
		);

		ReflectionTestUtil.setFieldValue(_conditionHelper, "_log", _log);

		Assert.assertFalse(
			_conditionHelper.evaluate(
				_function, false, _jsonObject, _parameterData, _messages));
	}

	@Test
	public void testEvaluateWithNoParameterOptional() {
		setUpParameterDataGetByTemplateVariableNameOptional(Optional.empty());

		Assert.assertFalse(
			_conditionHelper.evaluate(
				_function, false, _jsonObject, _parameterData, _messages));
	}

	@Test
	public void testEvaluateWithNoParsedValueOptional() {
		setUpParameterDataGetByTemplateVariableNameOptional(
			Optional.of(_parameter));

		setUpBlueprintTemplateVariableParserParse(Optional.empty());

		Assert.assertFalse(
			_conditionHelper.evaluate(
				_function, false, _jsonObject, _parameterData, _messages));
	}

	@Test
	public void testGetParameterOptional() {
		Parameter parameter = Mockito.mock(Parameter.class);

		Mockito.doReturn(
			Optional.of(parameter)
		).when(
			_parameterData
		).getByTemplateVariableNameOptional(
			Mockito.anyString()
		);

		Assert.assertEquals(
			Optional.of(parameter),
			_conditionHelper.getParameterOptional(_parameterData, _jsonObject));
	}

	protected void setUpBlueprintTemplateVariableParserParse(
		Optional<Object> objectOptional) {

		Mockito.when(
			_blueprintTemplateVariableParser.parse(
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())
		).thenReturn(
			objectOptional
		);
	}

	protected void setUpFieldValues() {
		ReflectionTestUtil.setFieldValue(
			_conditionHelper, "_blueprintTemplateVariableParser",
			_blueprintTemplateVariableParser);
	}

	protected void setUpFunctionApply() {
		EvaluationVisitor visitor = Mockito.mock(EvaluationVisitor.class);

		Mockito.when(
			_function.apply(Mockito.anyObject())
		).thenReturn(
			visitor
		);
	}

	protected void setUpParameterDataGetByTemplateVariableNameOptional(
		Optional<Parameter> parameterOptional) {

		Mockito.when(
			_parameterData.getByTemplateVariableNameOptional(
				Mockito.anyString())
		).thenReturn(
			parameterOptional
		);
	}

	@Mock
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	private final ConditionHelper _conditionHelper = new ConditionHelper();

	@Mock
	private Function<JSONObject, EvaluationVisitor> _function;

	@Mock
	private JSONObject _jsonObject;

	@Mock
	private Log _log;

	@Mock
	private Messages _messages;

	@Mock
	private Parameter _parameter;

	@Mock
	private ParameterData _parameterData;

}