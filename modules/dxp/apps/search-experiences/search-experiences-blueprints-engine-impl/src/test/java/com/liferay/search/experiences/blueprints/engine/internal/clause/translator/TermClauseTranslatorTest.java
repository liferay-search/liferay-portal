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

package com.liferay.search.experiences.blueprints.engine.internal.clause.translator;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.TermQuery;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.search.experiences.blueprints.constants.json.keys.query.TermQueryConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.internal.parameter.ParameterDataImpl;
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
public class TermClauseTranslatorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		ReflectionTestUtil.setFieldValue(
			_termClauseTranslator, "_queries", _queries);
	}

	@Test
	public void testTranslateWithJsonObject() {
		TermQuery termQuery = Mockito.mock(TermQuery.class);

		Mockito.doReturn(
			termQuery
		).when(
			_queries
		).term(
			Mockito.anyString(), Mockito.anyObject()
		);

		JSONObject jsonObject = JSONUtil.put(
			"field",
			JSONUtil.put(
				TermQueryConfigurationKeys.VALUE.getJsonKey(), "keyword"
			).put(
				TermQueryConfigurationKeys.BOOST.getJsonKey(),
				Float.valueOf(100.0F)
			));

		Assert.assertEquals(
			Optional.of(termQuery),
			_termClauseTranslator.translate(
				jsonObject, new ParameterDataImpl("", Collections.emptyList()),
				new Messages()));
	}

	@Test
	public void testTranslateWithJsonObjectAndBlankKeyword() {
		JSONObject jsonObject = JSONUtil.put(
			"jsonKey",
			JSONUtil.put(
				TermQueryConfigurationKeys.VALUE.getJsonKey(), ""
			).put(
				TermQueryConfigurationKeys.BOOST.getJsonKey(),
				Float.valueOf(1.0F)
			));

		Assert.assertEquals(
			Optional.empty(),
			_termClauseTranslator.translate(
				jsonObject, new ParameterDataImpl("", Collections.emptyList()),
				new Messages()));
	}

	@Test
	public void testTranslateWithNullJsonObject() {
		Assert.assertEquals(
			Optional.empty(),
			_termClauseTranslator.translate(null, null, null));
	}

	@Test
	public void testTranslateWithShortForm() {
		TermQuery termQuery = Mockito.mock(TermQuery.class);

		Mockito.doReturn(
			termQuery
		).when(
			_queries
		).term(
			Mockito.anyString(), Mockito.anyObject()
		);

		JSONObject jsonObject = JSONUtil.put("field", "keyword");

		Assert.assertEquals(
			Optional.of(termQuery),
			_termClauseTranslator.translate(
				jsonObject, new ParameterDataImpl("", Collections.emptyList()),
				new Messages()));
	}

	@Test
	public void testTranslateWithShortFormAndBlankKeyword() {
		JSONObject jsonObject = JSONUtil.put("blankKeyword", "");

		Assert.assertEquals(
			Optional.empty(),
			_termClauseTranslator.translate(
				jsonObject, new ParameterDataImpl("", Collections.emptyList()),
				new Messages()));
	}

	@Mock
	private Queries _queries;

	private final TermClauseTranslator _termClauseTranslator =
		new TermClauseTranslator();

}