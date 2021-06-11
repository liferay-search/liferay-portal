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

package com.liferay.search.experiences.blueprints.engine.internal.condition;

import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.search.experiences.blueprints.engine.spi.clause.ConditionHandler;

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
public class ConditionHandlerFactoryImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_registerConditionHandler();
	}

	@Test
	public void testGetHandler() {
		Assert.assertEquals(
			_conditionHandler, _conditionHandlerFactoryImpl.getHandler(_NAME));
	}

	@Test
	public void testGetHandlerNames() {
		Assert.assertArrayEquals(
			new String[] {_NAME},
			_conditionHandlerFactoryImpl.getHandlerNames());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetHandlerWithIllegalArgumentException() {
		_conditionHandlerFactoryImpl.getHandler("noExistName");
	}

	@Test
	public void testUnregisterConditionHandler() {
		_conditionHandlerFactoryImpl.unregisterConditionHandler(
			_conditionHandler, _properties);

		Assert.assertArrayEquals(
			new String[0], _conditionHandlerFactoryImpl.getHandlerNames());
	}

	private void _registerConditionHandler() {
		_properties = HashMapBuilder.<String, Object>put(
			"name", _NAME
		).build();

		_conditionHandler = Mockito.mock(ConditionHandler.class);

		_conditionHandlerFactoryImpl.registerConditionHandler(
			_conditionHandler, _properties);
	}

	private static final String _NAME = "id.name";

	private ConditionHandler _conditionHandler;
	private final ConditionHandlerFactoryImpl _conditionHandlerFactoryImpl =
		new ConditionHandlerFactoryImpl();
	private Map<String, Object> _properties;

}