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

package com.liferay.search.experiences.blueprints.internal.validator;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.search.experiences.blueprints.internal.validator.util.BlueprintJSONValidatorUtil;

import java.io.InputStream;

import java.lang.reflect.Constructor;

import org.mockito.Matchers;

import org.powermock.api.mockito.PowerMockito;

/**
 * @author Wade Cao
 */
public abstract class BaseValidatorTestCase {

	protected JSONArray createJSONArray() {
		return JSONFactoryUtil.createJSONArray();
	}

	protected String getBlueprintConfigurationString() {
		return JSONUtil.put(
			"description", "Blueprint configuration schema test"
		).put(
			"properties",
			JSONUtil.put(
				"advanced_configuration", JSONUtil.put(null, null)
			).put(
				"aggregation_configuration", JSONUtil.put(null, null)
			).put(
				"facet_configuration", JSONUtil.put(null, null)
			).put(
				"highlight_configuration", JSONUtil.put(null, null)
			).put(
				"parameter_configuration", JSONUtil.put(null, null)
			).put(
				"query_configuration", JSONUtil.put(null, null)
			).put(
				"sort_configuration", JSONUtil.put(null, null)
			)
		).put(
			"title", "test Blueprint"
		).toString();
	}

	protected InputStream getConfigurationJSONSchemaInputStream(
		String resource) {

		return BlueprintValidatorImpl.class.getResourceAsStream(resource);
	}

	protected String getConfigurationStringWithMissingRequiredProperties() {
		return JSONUtil.put(
			"description", "Blueprint element configuration schema test"
		).put(
			"title", "Element"
		).put(
			"type", "object"
		).put(
			"uiConfigurationJSON", getTestUIConfigurationJSONObject()
		).toString();
	}

	protected String getElementConfigurationString() {
		return JSONUtil.put(
			"description", "Blueprint element configuration schema test"
		).put(
			"elementTemplateJSON", getTestElementTemplateJSONObject()
		).put(
			"title", "Element"
		).put(
			"type", "object"
		).put(
			"uiConfigurationJSON", getTestUIConfigurationJSONObject()
		).toString();
	}

	protected JSONObject getTestElementTemplateJSONObject() {
		return JSONUtil.put(
			"enabled", true
		).put(
			"field", "testField"
		);
	}

	protected JSONObject getTestUIConfigurationJSONObject() {
		return JSONUtil.put(
			"fieldSets",
			createJSONArray().put(
				JSONUtil.put(
					"fields",
					createJSONArray().put(
						JSONUtil.put(
							"defaultValue", 40
						).put(
							"label", "Boost"
						).put(
							"name", "boost"
						).put(
							"type", "slider"
						)))));
	}

	protected void setUpBlueprintJSONValidatorUtilDoNothing() throws Exception {
		PowerMockito.mockStatic(BlueprintJSONValidatorUtil.class);

		PowerMockito.doNothing(
		).when(
			BlueprintJSONValidatorUtil.class, "validate", Matchers.anyString(),
			Matchers.anyObject()
		);
	}

	protected void setUpJSONFactoryUtil() throws ReflectiveOperationException {
		Thread thread = Thread.currentThread();

		ClassLoader classLoader = thread.getContextClassLoader();

		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		Class<?> clazz = classLoader.loadClass(
			"com.liferay.portal.json.JSONFactoryImpl");

		Constructor<?> constructor = clazz.getDeclaredConstructor();

		jsonFactoryUtil.setJSONFactory((JSONFactory)constructor.newInstance());
	}

}