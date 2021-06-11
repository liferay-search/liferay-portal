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
public class InRangeVisitorTest extends BaseVisitorTestCase {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		setUpFieldValues(_inRangeVisitor);
	}

	@Test
	public void testVisitWithDateParameter() throws Exception {
		setUpConditionJsonObjectWithGet(
			createJSONArray("2021-06-08", "2021-06-10"));
		setUpConditionJsonObjectWithGetString("2021-06-09", "yyyy-MM-dd");

		Mockito.when(
			_dateParameter.getValue()
		).thenReturn(
			getDate("2021-06-09", "yyyy-MM-dd")
		);

		Assert.assertTrue(_inRangeVisitor.visit(_dateParameter));

		Mockito.when(
			_dateParameter.getValue()
		).thenReturn(
			getDate("2021-06-07", "yyyy-MM-dd")
		);

		Assert.assertFalse(_inRangeVisitor.visit(_dateParameter));
	}

	@Test(expected = ParameterEvaluationException.class)
	public void testVisitWithDateParameterConditionValueJSONArrayException()
		throws Exception {

		setUpConditionJsonObjectWithGet(String.valueOf("not JSONArray"));

		Assert.assertTrue(_inRangeVisitor.visit(_dateParameter));
	}

	@Test(expected = ParameterEvaluationException.class)
	public void testVisitWithDateParameterDateFormatStringException()
		throws Exception {

		setUpConditionJsonObjectWithGet(
			createJSONArray("2021-06-08", "2021-06-10"));
		setUpConditionJsonObjectWithGetString("2021-06-09", null);

		Assert.assertTrue(_inRangeVisitor.visit(_dateParameter));
	}

	@Test(expected = ParameterEvaluationException.class)
	public void testVisitWithDateParameterException() throws Exception {
		setUpConditionJsonObjectWithGet(
			createJSONArray("2021-06-08", "2021-06-10"));
		setUpConditionJsonObjectWithGetString(
			"2021-06-09", "not a date format");

		Assert.assertTrue(_inRangeVisitor.visit(_dateParameter));
	}

	@Test(expected = ParameterEvaluationException.class)
	public void testVisitWithDateParameterRangeValueException()
		throws Exception {

		setUpConditionJsonObjectWithGet(createJSONArray("2021-06-08", null));

		Assert.assertTrue(_inRangeVisitor.visit(_dateParameter));
	}

	@Test
	public void testVisitWithDoubleParameter() throws Exception {
		setUpConditionJsonObjectWithGet(
			createJSONArray(Double.valueOf(123D), Double.valueOf(125D)));

		Mockito.when(
			_doubleParameter.getValue()
		).thenReturn(
			Double.valueOf(124D)
		);

		Assert.assertTrue(_inRangeVisitor.visit(_doubleParameter));

		Mockito.when(
			_doubleParameter.getValue()
		).thenReturn(
			Double.valueOf(128D)
		);

		Assert.assertFalse(_inRangeVisitor.visit(_doubleParameter));
	}

	@Test
	public void testVisitWithFloatParameter() throws Exception {
		setUpConditionJsonObjectWithGet(
			createJSONArray(Float.valueOf(123F), Float.valueOf(125F)));

		Mockito.when(
			_floatParameter.getValue()
		).thenReturn(
			Float.valueOf(124F)
		);

		Assert.assertTrue(_inRangeVisitor.visit(_floatParameter));

		Mockito.when(
			_floatParameter.getValue()
		).thenReturn(
			Float.valueOf(128F)
		);

		Assert.assertFalse(_inRangeVisitor.visit(_floatParameter));
	}

	@Test
	public void testVisitWithIntegerParameter() throws Exception {
		setUpConditionJsonObjectWithGet(
			createJSONArray(Integer.valueOf(123), Integer.valueOf(125)));

		Mockito.when(
			_integerParameter.getValue()
		).thenReturn(
			Integer.valueOf(124)
		);

		Assert.assertTrue(_inRangeVisitor.visit(_integerParameter));

		Mockito.when(
			_integerParameter.getValue()
		).thenReturn(
			Integer.valueOf(128)
		);

		Assert.assertFalse(_inRangeVisitor.visit(_integerParameter));
	}

	@Test
	public void testVisitWithLongParameter() throws Exception {
		setUpConditionJsonObjectWithGet(
			createJSONArray(Long.valueOf(123L), Long.valueOf(125L)));

		Mockito.when(
			_longParameter.getValue()
		).thenReturn(
			Long.valueOf(124L)
		);

		Assert.assertTrue(_inRangeVisitor.visit(_longParameter));

		Mockito.when(
			_longParameter.getValue()
		).thenReturn(
			Long.valueOf(128L)
		);

		Assert.assertFalse(_inRangeVisitor.visit(_longParameter));
	}

	@Mock
	private DateParameter _dateParameter;

	@Mock
	private DoubleParameter _doubleParameter;

	@Mock
	private FloatParameter _floatParameter;

	private final InRangeVisitor _inRangeVisitor = new InRangeVisitor(
		conditionJSONObject);

	@Mock
	private IntegerParameter _integerParameter;

	@Mock
	private LongParameter _longParameter;

}