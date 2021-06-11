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
import com.liferay.search.experiences.blueprints.engine.parameter.DateParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.DoubleParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.FloatParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.IntegerParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.LongParameter;

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
public class GreaterThanVisitorTest extends BaseVisitorTestCase {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		setUpFieldValues(_greaterThanVisitorTrueClosedRange);
		setUpFieldValues(_greaterThanVisitorFalseClosedRange);
	}

	@Test
	public void testVisitWithDateParameter() throws Exception {
		setUpConditionJsonObjectWithGetString("2021-06-09", "yyyy-MM-dd");

		Mockito.when(
			_dateParameter.getValue()
		).thenReturn(
			getDate("2021-06-08", "yyyy-MM-dd")
		);

		Assert.assertTrue(
			_greaterThanVisitorTrueClosedRange.visit(_dateParameter));
		Assert.assertTrue(
			_greaterThanVisitorFalseClosedRange.visit(_dateParameter));

		setUpConditionJsonObjectWithGetString("2021-06-07", "yyyy-MM-dd");

		Assert.assertFalse(
			_greaterThanVisitorTrueClosedRange.visit(_dateParameter));
		Assert.assertFalse(
			_greaterThanVisitorFalseClosedRange.visit(_dateParameter));
	}

	@Test
	public void testVisitWithDoubleParameter() throws Exception {
		setUpConditionJsonObjectWithGetDouble(Double.valueOf(123D));

		DoubleParameter doubleParameter = new DoubleParameter(
			"testName", "templateVarible", Double.valueOf(123D));

		Assert.assertTrue(
			_greaterThanVisitorTrueClosedRange.visit(doubleParameter));
		Assert.assertFalse(
			_greaterThanVisitorFalseClosedRange.visit(doubleParameter));

		doubleParameter = new DoubleParameter(
			"testName", "templateVarible", Double.valueOf(122D));

		Assert.assertFalse(
			_greaterThanVisitorTrueClosedRange.visit(doubleParameter));
		Assert.assertFalse(
			_greaterThanVisitorFalseClosedRange.visit(doubleParameter));
	}

	@Test
	public void testVisitWithFloatParameter() throws Exception {
		setUpConditionJsonObjectWithGetFloat(Float.valueOf(123.1F));

		FloatParameter floatParameter = new FloatParameter(
			"testName", "templateVarible", Float.valueOf(123.1F));

		Assert.assertTrue(
			_greaterThanVisitorTrueClosedRange.visit(floatParameter));
		Assert.assertFalse(
			_greaterThanVisitorFalseClosedRange.visit(floatParameter));

		floatParameter = new FloatParameter(
			"testName", "templateVarible", Float.valueOf(123F));

		Assert.assertFalse(
			_greaterThanVisitorTrueClosedRange.visit(floatParameter));
		Assert.assertFalse(
			_greaterThanVisitorFalseClosedRange.visit(floatParameter));
	}

	@Test
	public void testVisitWithIntegerParameter() throws Exception {
		setUpConditionJsonObjectWithGetInt(Integer.valueOf(123));

		IntegerParameter integerParameter = new IntegerParameter(
			"testName", "templateVarible", Integer.valueOf(123));

		Assert.assertTrue(
			_greaterThanVisitorTrueClosedRange.visit(integerParameter));
		Assert.assertFalse(
			_greaterThanVisitorFalseClosedRange.visit(integerParameter));

		integerParameter = new IntegerParameter(
			"testName", "templateVarible", Integer.valueOf(122));

		Assert.assertFalse(
			_greaterThanVisitorTrueClosedRange.visit(integerParameter));
		Assert.assertFalse(
			_greaterThanVisitorFalseClosedRange.visit(integerParameter));
	}

	@Test
	public void testVisitWithLongParameter() throws Exception {
		setUpConditionJsonObjectWithGetLong(Long.valueOf(123L));

		LongParameter longParameter = new LongParameter(
			"testName", "templateVarible", Long.valueOf(123L));

		Assert.assertTrue(
			_greaterThanVisitorTrueClosedRange.visit(longParameter));
		Assert.assertFalse(
			_greaterThanVisitorFalseClosedRange.visit(longParameter));

		longParameter = new LongParameter(
			"testName", "templateVarible", Long.valueOf(122L));

		Assert.assertFalse(
			_greaterThanVisitorTrueClosedRange.visit(longParameter));
		Assert.assertFalse(
			_greaterThanVisitorFalseClosedRange.visit(longParameter));
	}

	@Mock
	private DateParameter _dateParameter;

	private final GreaterThanVisitor _greaterThanVisitorFalseClosedRange =
		new GreaterThanVisitor(conditionJSONObject, false);
	private final GreaterThanVisitor _greaterThanVisitorTrueClosedRange =
		new GreaterThanVisitor(conditionJSONObject, true);

}