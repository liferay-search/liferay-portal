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

package com.liferay.search.experiences.blueprints.engine.internal.clause.util;

import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.search.experiences.blueprints.engine.internal.clause.ClauseTranslatorFactory;
import com.liferay.search.experiences.blueprints.engine.internal.parameter.ParameterDataImpl;
import com.liferay.search.experiences.blueprints.engine.spi.clause.ClauseTranslator;
import com.liferay.search.experiences.blueprints.engine.template.variable.BlueprintTemplateVariableParser;
import com.liferay.search.experiences.blueprints.message.Messages;

import java.util.Collections;
import java.util.Optional;

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
public class ClauseHelperTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		setupFieldValues();
	}

	@Test
	public void testGetClauseWithClauseTranslator() {
		Query query = Mockito.mock(Query.class);

		Mockito.doReturn(
			Optional.of(query)
		).when(
			_clauseTranslator
		).translate(
			Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject()
		);

		setUpClauseTranslatorFactory();

		setUpBlueprintTemplateVariableParser();

		Assert.assertEquals(
			Optional.of(query),
			_clauseHelper.getClause(
				JSONUtil.put("anyKey", "anyType"),
				new ParameterDataImpl("", Collections.emptyList()),
				new Messages()));
	}

	@Test
	public void testGetClauseWithEmptyParsedClause() {
		Mockito.doReturn(
			Optional.empty()
		).when(
			_blueprintTemplateVariableParser
		).parseObject(
			Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject()
		);

		Assert.assertEquals(
			Optional.empty(),
			_clauseHelper.getClause(
				JSONUtil.put("anyKey", "anyType"),
				new ParameterDataImpl("", Collections.emptyList()),
				new Messages()));
	}

	@Test
	public void testGetClauseWithGetTranslatorException() {
		Mockito.doThrow(
			new IllegalArgumentException("throw exception")
		).when(
			_clauseTranslatorFactory
		).getTranslator(
			Mockito.anyString()
		);

		setUpBlueprintTemplateVariableParser();

		Assert.assertEquals(
			Optional.empty(),
			_clauseHelper.getClause(
				JSONUtil.put("anyKey", "anyType"),
				new ParameterDataImpl("", Collections.emptyList()),
				new Messages()));
	}

	@Test
	public void testGetClauseWithNullJsonObject() {
		Assert.assertEquals(
			Optional.empty(), _clauseHelper.getClause(null, null, null));
	}

	protected void setUpBlueprintTemplateVariableParser() {
		Mockito.doReturn(
			Optional.of(JSONUtil.put("anyParameterName", "anyParameterValue"))
		).when(
			_blueprintTemplateVariableParser
		).parseObject(
			Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject()
		);
	}

	protected void setUpClauseTranslatorFactory() {
		Mockito.doReturn(
			_clauseTranslator
		).when(
			_clauseTranslatorFactory
		).getTranslator(
			Mockito.anyString()
		);
	}

	protected void setupFieldValues() {
		ReflectionTestUtil.setFieldValue(
			_clauseHelper, "_clauseTranslatorFactory",
			_clauseTranslatorFactory);

		ReflectionTestUtil.setFieldValue(
			_clauseHelper, "_blueprintTemplateVariableParser",
			_blueprintTemplateVariableParser);
	}

	@Mock
	private BlueprintTemplateVariableParser _blueprintTemplateVariableParser;

	private final ClauseHelper _clauseHelper = new ClauseHelper();

	@Mock
	private ClauseTranslator _clauseTranslator;

	@Mock
	private ClauseTranslatorFactory _clauseTranslatorFactory;

}