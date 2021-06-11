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

/**
 * @author Wade Cao
 */
public class InVisitorTest extends BaseVisitorTestCase {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		setUpFieldValues(_inVisitor);
	}

	@Test
	public void testVisitWithDoubleParameter() throws Exception {
		setUpConditionJsonObjectWithGet(
			createJSONArray(Double.valueOf(123D), Double.valueOf(125D)));

		DoubleParameter doubleParameter = new DoubleParameter(
			"name", "templateVariable", Double.valueOf(125D));

		Assert.assertTrue(_inVisitor.visit(doubleParameter));

		doubleParameter = new DoubleParameter(
			"name", "templateVariable", Double.valueOf(128D));

		Assert.assertFalse(_inVisitor.visit(doubleParameter));
	}

	@Test
	public void testVisitWithFloatParameter() throws Exception {
		setUpConditionJsonObjectWithGet(
			createJSONArray(Float.valueOf(123F), Float.valueOf(125F)));

		FloatParameter floatParameter = new FloatParameter(
			"name", "templateVariable", Float.valueOf(125F));

		Assert.assertTrue(_inVisitor.visit(floatParameter));

		floatParameter = new FloatParameter(
			"name", "templateVariable", Float.valueOf(128F));

		Assert.assertFalse(_inVisitor.visit(floatParameter));
	}

	@Test
	public void testVisitWithIntegerParameter() throws Exception {
		setUpConditionJsonObjectWithGet(
			createJSONArray(Integer.valueOf(123), Integer.valueOf(125)));

		IntegerParameter integerParameter = new IntegerParameter(
			"name", "templateVariable", Integer.valueOf(125));

		Assert.assertTrue(_inVisitor.visit(integerParameter));

		integerParameter = new IntegerParameter(
			"name", "templateVariable", Integer.valueOf(128));

		Assert.assertFalse(_inVisitor.visit(integerParameter));
	}

	@Test
	public void testVisitWithLongParameter() throws Exception {
		setUpConditionJsonObjectWithGet(
			createJSONArray(Long.valueOf(123L), Long.valueOf(125L)));

		LongParameter longParameter = new LongParameter(
			"name", "templateVariable", Long.valueOf(125L));

		Assert.assertTrue(_inVisitor.visit(longParameter));

		longParameter = new LongParameter(
			"name", "templateVariable", Long.valueOf(128L));

		Assert.assertFalse(_inVisitor.visit(longParameter));
	}

	@Test
	public void testVisitWithStringParameter() throws Exception {
		setUpConditionJsonObjectWithGet(createJSONArray("abc", "def"));

		StringParameter stringParameter = new StringParameter(
			"name", "templateVariable", String.valueOf("def"));

		Assert.assertTrue(_inVisitor.visit(stringParameter));

		stringParameter = new StringParameter(
			"name", "templateVariable", String.valueOf("ghi"));

		Assert.assertFalse(_inVisitor.visit(stringParameter));
	}

	private final InVisitor _inVisitor = new InVisitor(conditionJSONObject);

}