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

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.search.experiences.blueprints.constants.json.keys.query.ConditionConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.parameter.visitor.EvaluationVisitor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Wade Cao
 */
public abstract class BaseVisitorTestCase {

	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	protected JSONArray createJSONArray() {
		return JSONFactoryUtil.createJSONArray();
	}

	protected JSONArray createJSONArray(Object object1, Object object2) {
		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		return jsonArray.put(
			object1
		).put(
			object2
		);
	}

	protected Date getDate(String dateString, String dateFormatString)
		throws Exception {

		DateFormat dateFormat = new SimpleDateFormat(dateFormatString);

		return dateFormat.parse(dateString);
	}

	protected void setUpConditionJsonObjectByJsonArray(Object object) {
		JSONArray jsonArray = createJSONArray();

		jsonArray.put(object);

		Mockito.when(
			conditionJSONObject.get(Mockito.anyString())
		).thenReturn(
			jsonArray
		);
	}

	protected void setUpConditionJsonObjectWithGet(Object object) {
		Mockito.when(
			conditionJSONObject.get(Mockito.anyString())
		).thenReturn(
			object
		);
	}

	protected void setUpConditionJsonObjectWithGetBoolean(
		Boolean valueBoolean) {

		Mockito.when(
			conditionJSONObject.getBoolean(Mockito.anyString())
		).thenReturn(
			valueBoolean
		);
	}

	protected void setUpConditionJsonObjectWithGetDouble(Double valueDouble) {
		Mockito.when(
			conditionJSONObject.getDouble(Mockito.anyString())
		).thenReturn(
			valueDouble
		);
	}

	protected void setUpConditionJsonObjectWithGetFloat(Float valueFloat) {
		Mockito.when(
			conditionJSONObject.get(Mockito.anyString())
		).thenReturn(
			valueFloat
		);
	}

	protected void setUpConditionJsonObjectWithGetInt(Integer valueInteger) {
		Mockito.when(
			conditionJSONObject.getInt(Mockito.anyString())
		).thenReturn(
			valueInteger
		);
	}

	protected void setUpConditionJsonObjectWithGetLong(Long valueLong) {
		Mockito.when(
			conditionJSONObject.getLong(Mockito.anyString())
		).thenReturn(
			valueLong
		);
	}

	protected void setUpConditionJsonObjectWithGetString(String valueString) {
		Mockito.when(
			conditionJSONObject.getString(Mockito.anyString())
		).thenReturn(
			valueString
		);
	}

	protected void setUpConditionJsonObjectWithGetString(
		String value, String dateFormat) {

		Mockito.when(
			conditionJSONObject.getString(Mockito.anyString())
		).thenAnswer(
			invocationOnMock -> {
				String argument = (String)invocationOnMock.getArguments()[0];

				if (argument.equals(
						ConditionConfigurationKeys.VALUE.getJsonKey())) {

					return value;
				}
				else if (argument.equals(
							ConditionConfigurationKeys.DATE_FORMAT.
								getJsonKey())) {

					return dateFormat;
				}
				else {
					return null;
				}
			}
		);
	}

	protected void setUpFieldValues(EvaluationVisitor evaluationVisitor) {
		ReflectionTestUtil.setFieldValue(
			evaluationVisitor, "conditionJSONObject", conditionJSONObject);
	}

	@Mock
	protected JSONObject conditionJSONObject;

}