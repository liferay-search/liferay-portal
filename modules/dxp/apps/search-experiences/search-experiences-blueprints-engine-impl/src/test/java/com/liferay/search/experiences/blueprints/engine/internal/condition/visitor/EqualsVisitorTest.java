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

package com.liferay.search.experiences.blueprints.engine.internal.condition.visitor;

import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.search.experiences.blueprints.engine.exception.ParameterEvaluationException;
import com.liferay.search.experiences.blueprints.engine.parameter.BooleanParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.DateParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.DoubleParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.FloatParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.IntegerParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.LongParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.StringParameter;

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
public class EqualsVisitorTest extends BaseVisitorTestCase {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		setUpFieldValues(_equalsVisitor);
	}

	@Test
	public void testVisitWithBooleanParameter() throws Exception {
		setUpConditionJsonObjectWithGetBoolean(true);

		Mockito.when(
			_booleanParameter.getValue()
		).thenReturn(
			true
		);

		Assert.assertTrue(_equalsVisitor.visit(_booleanParameter));
	}

	@Test
	public void testVisitWithDateParameter() throws Exception {
		setUpConditionJsonObjectWithGetString("2021-06-09", "yyyy-MM-dd");

		Mockito.when(
			_dateParameter.getValue()
		).thenReturn(
			getDate("2021-06-09", "yyyy-MM-dd")
		);

		Assert.assertTrue(_equalsVisitor.visit(_dateParameter));

		setUpConditionJsonObjectWithGetString("2021-06-10", "yyyy-MM-dd");

		Assert.assertFalse(_equalsVisitor.visit(_dateParameter));
	}

	@Test(expected = ParameterEvaluationException.class)
	public void testVisitWithDateParameterForException() throws Exception {
		setUpConditionJsonObjectWithGetString("abcd-ef-gh", "yyyy-MM-dd");

		Assert.assertTrue(_equalsVisitor.visit(_dateParameter));
	}

	@Test(expected = ParameterEvaluationException.class)
	public void testVisitWithDateParameterForNullDateFormatString()
		throws Exception {

		setUpConditionJsonObjectWithGetString("2021-06-09", null);

		Assert.assertTrue(_equalsVisitor.visit(_dateParameter));
	}

	@Test
	public void testVisitWithDoubleParameter() throws Exception {
		setUpConditionJsonObjectWithGetDouble(Double.valueOf(123D));

		DoubleParameter doubleParameter = new DoubleParameter(
			"testName", "templateVarible", Double.valueOf(123D));

		Assert.assertTrue(_equalsVisitor.visit(doubleParameter));

		setUpConditionJsonObjectWithGetDouble(Double.valueOf(124D));

		Assert.assertFalse(_equalsVisitor.visit(doubleParameter));
	}

	@Test
	public void testVisitWithFloatParameter() throws Exception {
		setUpConditionJsonObjectWithGet(Float.valueOf(123F));

		FloatParameter floatParameter = new FloatParameter(
			"testName", "templateVarible", Float.valueOf(123F));

		Assert.assertTrue(_equalsVisitor.visit(floatParameter));

		setUpConditionJsonObjectWithGet(Float.valueOf(124F));

		Assert.assertFalse(_equalsVisitor.visit(floatParameter));
	}

	@Test
	public void testVisitWithIntegerParameter() throws Exception {
		setUpConditionJsonObjectWithGetInt(Integer.valueOf(123));

		IntegerParameter integerParameter = new IntegerParameter(
			"testName", "templateVarible", Integer.valueOf(123));

		Assert.assertTrue(_equalsVisitor.visit(integerParameter));

		setUpConditionJsonObjectWithGetInt(Integer.valueOf(124));

		Assert.assertFalse(_equalsVisitor.visit(integerParameter));
	}

	@Test
	public void testVisitWithLongParameter() throws Exception {
		setUpConditionJsonObjectWithGetLong(Long.valueOf(123L));

		LongParameter longParameter = new LongParameter(
			"testName", "templateVarible", Long.valueOf(123L));

		Assert.assertTrue(_equalsVisitor.visit(longParameter));

		setUpConditionJsonObjectWithGetLong(Long.valueOf(124L));

		Assert.assertFalse(_equalsVisitor.visit(longParameter));
	}

	@Test
	public void testVisitWithStringParameter() throws Exception {
		setUpConditionJsonObjectWithGetString(String.valueOf("abc"));

		StringParameter stringParameter = new StringParameter(
			"testName", "templateVarible", String.valueOf("abc"));

		Assert.assertTrue(_equalsVisitor.visit(stringParameter));

		setUpConditionJsonObjectWithGetString(String.valueOf("cbs"));

		Assert.assertFalse(_equalsVisitor.visit(stringParameter));
	}

	@Mock
	private BooleanParameter _booleanParameter;

	@Mock
	private DateParameter _dateParameter;

	private final EqualsVisitor _equalsVisitor = new EqualsVisitor(
		conditionJSONObject);

}