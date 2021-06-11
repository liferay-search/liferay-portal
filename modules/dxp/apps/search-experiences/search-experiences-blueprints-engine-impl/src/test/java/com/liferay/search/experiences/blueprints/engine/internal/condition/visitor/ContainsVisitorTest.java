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
import com.liferay.search.experiences.blueprints.engine.parameter.IntegerArrayParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.LongArrayParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.StringArrayParameter;
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
public class ContainsVisitorTest extends BaseVisitorTestCase {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		setUpFieldValues(_containsVisitor);
	}

	@Test
	public void testVisitWithIntegerArrayParameterForJSONArray()
		throws Exception {

		setUpConditionJsonObjectByJsonArray("123");

		Mockito.when(
			_integerArrayParameter.getValue()
		).thenReturn(
			new Integer[] {123}
		);

		Assert.assertTrue(_containsVisitor.visit(_integerArrayParameter));

		setUpConditionJsonObjectByJsonArray("456");

		Assert.assertFalse(_containsVisitor.visit(_integerArrayParameter));
	}

	@Test
	public void testVisitWithIntegerArrayParameterForNotJSONArray()
		throws Exception {

		setUpConditionJsonObjectWithGet("123");

		Mockito.when(
			_integerArrayParameter.getValue()
		).thenReturn(
			new Integer[] {123}
		);

		Assert.assertTrue(_containsVisitor.visit(_integerArrayParameter));

		setUpConditionJsonObjectWithGet("456");

		Assert.assertFalse(_containsVisitor.visit(_integerArrayParameter));
	}

	@Test(expected = ParameterEvaluationException.class)
	public void testVisitWithIntegerArrayParameterForNumberFormatException()
		throws Exception {

		setUpConditionJsonObjectWithGet("abc");

		Mockito.when(
			_integerArrayParameter.getValue()
		).thenReturn(
			new Integer[] {123}
		);

		Assert.assertTrue(_containsVisitor.visit(_integerArrayParameter));
	}

	@Test
	public void testVisitWithLongArrayParameterForJSONArray() throws Exception {
		setUpConditionJsonObjectByJsonArray(Long.valueOf(123L));

		Mockito.when(
			_longArrayParameter.getValue()
		).thenReturn(
			new Long[] {123L}
		);

		Assert.assertTrue(_containsVisitor.visit(_longArrayParameter));

		setUpConditionJsonObjectByJsonArray(Long.valueOf(234L));

		Assert.assertFalse(_containsVisitor.visit(_longArrayParameter));
	}

	@Test
	public void testVisitWithLongArrayParameterForNotJSONArray()
		throws Exception {

		setUpConditionJsonObjectWithGet(Long.valueOf(123L));

		Mockito.when(
			_longArrayParameter.getValue()
		).thenReturn(
			new Long[] {123L}
		);

		Assert.assertTrue(_containsVisitor.visit(_longArrayParameter));

		setUpConditionJsonObjectWithGet(Long.valueOf(456L));

		Assert.assertFalse(_containsVisitor.visit(_longArrayParameter));
	}

	@Test
	public void testVisitWithLongArrayParameterForNumberFormatException()
		throws Exception {

		setUpConditionJsonObjectByJsonArray("abc");

		Mockito.when(
			_longArrayParameter.getValue()
		).thenReturn(
			new Long[] {123L}
		);

		Assert.assertFalse(_containsVisitor.visit(_longArrayParameter));
	}

	@Test
	public void testVisitWithStringArrayParameterForJSONArray()
		throws Exception {

		setUpConditionJsonObjectByJsonArray("abc");

		Mockito.when(
			_stringArrayParameter.getValue()
		).thenReturn(
			new String[] {"abc"}
		);

		Assert.assertTrue(_containsVisitor.visit(_stringArrayParameter));

		setUpConditionJsonObjectByJsonArray("cbs");

		Assert.assertFalse(_containsVisitor.visit(_stringArrayParameter));
	}

	@Test
	public void testVisitWithStringArrayParameterForNotJSONArray()
		throws Exception {

		setUpConditionJsonObjectWithGet("abc");

		Mockito.when(
			_stringArrayParameter.getValue()
		).thenReturn(
			new String[] {"abc"}
		);

		Assert.assertTrue(_containsVisitor.visit(_stringArrayParameter));

		setUpConditionJsonObjectWithGet("cbs");

		Assert.assertFalse(_containsVisitor.visit(_stringArrayParameter));
	}

	@Test
	public void testVisitWithStringArrayParameterForParameterEvaluationException()
		throws Exception {

		setUpConditionJsonObjectWithGet(null);

		Mockito.when(
			_stringArrayParameter.getValue()
		).thenReturn(
			new String[] {""}
		);

		Assert.assertFalse(_containsVisitor.visit(_stringArrayParameter));
	}

	@Test
	public void testVisitWithStringParameterForJSONArray() throws Exception {
		setUpConditionJsonObjectByJsonArray("efg");

		Mockito.when(
			_stringParameter.getValue()
		).thenReturn(
			"abcEFG"
		);

		Assert.assertTrue(_containsVisitor.visit(_stringParameter));

		setUpConditionJsonObjectByJsonArray("hij");

		Assert.assertFalse(_containsVisitor.visit(_stringParameter));
	}

	@Test
	public void testVisitWithStringParameterForNotJSONArray() throws Exception {
		setUpConditionJsonObjectWithGet("EFG");

		Mockito.when(
			_stringParameter.getValue()
		).thenReturn(
			"abcefg"
		);

		Assert.assertTrue(_containsVisitor.visit(_stringParameter));

		setUpConditionJsonObjectWithGet("hij");

		Assert.assertFalse(_containsVisitor.visit(_stringParameter));
	}

	@Test(expected = ParameterEvaluationException.class)
	public void testVisitWithStringParameterForNumberFormatException()
		throws Exception {

		setUpConditionJsonObjectWithGet(null);

		Assert.assertTrue(_containsVisitor.visit(_stringParameter));
	}

	private final ContainsVisitor _containsVisitor = new ContainsVisitor(
		conditionJSONObject);

	@Mock
	private IntegerArrayParameter _integerArrayParameter;

	@Mock
	private LongArrayParameter _longArrayParameter;

	@Mock
	private StringArrayParameter _stringArrayParameter;

	@Mock
	private StringParameter _stringParameter;

}