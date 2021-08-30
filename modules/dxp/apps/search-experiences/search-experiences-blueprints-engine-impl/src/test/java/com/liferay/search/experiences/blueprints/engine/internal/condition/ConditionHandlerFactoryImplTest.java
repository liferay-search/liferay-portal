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

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.search.experiences.blueprints.engine.spi.clause.ConditionHandler;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import org.powermock.core.classloader.annotations.PrepareForTest;

/**
 * @author Wade Cao
 */
@PrepareForTest
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
	public void testDeactivate() {
		Mockito.when(
			_conditionHandlerServiceTrackerMap.keySet()
		).thenReturn(
			new HashSet<>(Arrays.asList())
		);

		Assert.assertArrayEquals(
			new String[0], _conditionHandlerFactoryImpl.getHandlerNames());
	}

	@Test
	public void testGetHandler() {
		Mockito.when(
			_conditionHandlerServiceTrackerMap.getService(Mockito.anyObject())
		).thenReturn(
			_conditionHandler
		);

		Assert.assertEquals(
			_conditionHandler, _conditionHandlerFactoryImpl.getHandler(_NAME));
	}

	@Test
	public void testGetHandlerNames() {
		Mockito.when(
			_conditionHandlerServiceTrackerMap.keySet()
		).thenReturn(
			new HashSet<>(Arrays.asList(_NAME))
		);

		Assert.assertArrayEquals(
			new String[] {_NAME},
			_conditionHandlerFactoryImpl.getHandlerNames());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetHandlerWithIllegalArgumentException() {
		_conditionHandlerFactoryImpl.getHandler("noExistName");
	}

	@SuppressWarnings("unchecked")
	private void _registerConditionHandler() {
		_conditionHandlerServiceTrackerMap = Mockito.mock(
			ServiceTrackerMap.class);

		_conditionHandler = Mockito.mock(ConditionHandler.class);

		ReflectionTestUtil.setFieldValue(
			_conditionHandlerFactoryImpl, "_conditionHandlerServiceTrackerMap",
			_conditionHandlerServiceTrackerMap);
	}

	private static final String _NAME = "id.name";

	private ConditionHandler _conditionHandler;
	private final ConditionHandlerFactoryImpl _conditionHandlerFactoryImpl =
		new ConditionHandlerFactoryImpl();
	private ServiceTrackerMap<String, ConditionHandler>
		_conditionHandlerServiceTrackerMap;

}