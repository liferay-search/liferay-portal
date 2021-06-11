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

import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.WrapperQuery;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
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
public class WrapperClauseTranslatorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testTranslate() {
		WrapperQuery wrapperQuery = Mockito.mock(WrapperQuery.class);

		Mockito.doReturn(
			wrapperQuery
		).when(
			_queries
		).wrapper(
			Mockito.anyString()
		);

		ReflectionTestUtil.setFieldValue(
			_wrapperClauseTranslator, "_queries", _queries);

		Assert.assertEquals(
			Optional.of(wrapperQuery),
			_wrapperClauseTranslator.translate(
				JSONUtil.put("anyKey", "anyType"),
				new ParameterDataImpl("", Collections.emptyList()),
				new Messages()));
	}

	@Mock
	private Queries _queries;

	private final WrapperClauseTranslator _wrapperClauseTranslator =
		new WrapperClauseTranslator();

}