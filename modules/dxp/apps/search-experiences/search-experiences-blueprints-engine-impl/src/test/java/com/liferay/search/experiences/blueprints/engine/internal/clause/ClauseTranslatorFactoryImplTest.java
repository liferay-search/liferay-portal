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

package com.liferay.search.experiences.blueprints.engine.internal.clause;

import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.search.experiences.blueprints.engine.spi.clause.ClauseTranslator;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Wade Cao
 */
public class ClauseTranslatorFactoryImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_registerClauseTranslator();
	}

	@Test
	public void testGetTranslator() {
		Assert.assertEquals(
			_clauseTranslator,
			_clauseTranslatorFactoryImpl.getTranslator(_NAME));
	}

	@Test
	public void testGetTranslatorNames() {
		Assert.assertArrayEquals(
			new String[] {_NAME},
			_clauseTranslatorFactoryImpl.getTranslatorNames());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTranslatorWithIllegalArgumentException() {
		_clauseTranslatorFactoryImpl.getTranslator("noExistName");
	}

	@Test
	public void testUnregisterClauseTranslator() {
		_clauseTranslatorFactoryImpl.unregisterClauseTranslator(
			_clauseTranslator, _properties);

		Assert.assertArrayEquals(
			new String[0], _clauseTranslatorFactoryImpl.getTranslatorNames());
	}

	private void _registerClauseTranslator() {
		_properties = HashMapBuilder.<String, Object>put(
			"name", _NAME
		).build();

		_clauseTranslator = Mockito.mock(ClauseTranslator.class);

		_clauseTranslatorFactoryImpl.registerClauseTranslator(
			_clauseTranslator, _properties);
	}

	private static final String _NAME = "id.name";

	private ClauseTranslator _clauseTranslator;
	private final ClauseTranslatorFactoryImpl _clauseTranslatorFactoryImpl =
		new ClauseTranslatorFactoryImpl();
	private Map<String, Object> _properties;

}