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

package com.liferay.search.experiences.blueprints.engine.internal.condition.handler;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.search.experiences.blueprints.engine.internal.condition.util.ConditionHelper;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.spi.clause.ConditionHandler;

import java.util.Optional;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Wade Cao
 */
public abstract class BaseConditionHandlerTestCase {

	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	protected void setUpConditionHelperEvaluate(boolean valueBoolean) {
		Mockito.when(
			conditionHelper.evaluate(
				Mockito.anyObject(), Mockito.anyBoolean(), Mockito.anyObject(),
				Mockito.anyObject(), Mockito.anyObject())
		).thenReturn(
			valueBoolean
		);
	}

	protected void setUpConditionHelperGetParameterOptional(
		Optional<Parameter> parameterOptional) {

		Mockito.when(
			conditionHelper.getParameterOptional(
				Mockito.anyObject(), Mockito.anyObject())
		).thenReturn(
			parameterOptional
		);
	}

	protected void setUpFieldValues(ConditionHandler conditionHandler) {
		ReflectionTestUtil.setFieldValue(
			conditionHandler, "_conditionHelper", conditionHelper);
	}

	@Mock
	protected ConditionHelper conditionHelper;

}