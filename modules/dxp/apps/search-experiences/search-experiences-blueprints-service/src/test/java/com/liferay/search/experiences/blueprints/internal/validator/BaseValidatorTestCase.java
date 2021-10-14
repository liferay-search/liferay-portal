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
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;

import java.io.InputStream;

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
				"advanced_configuration", JSONFactoryUtil.createJSONObject()
			).put(
				"aggregation_configuration", JSONFactoryUtil.createJSONObject()
			).put(
				"facet_configuration", JSONFactoryUtil.createJSONObject()
			).put(
				"highlight_configuration", JSONFactoryUtil.createJSONObject()
			).put(
				"parameter_configuration", JSONFactoryUtil.createJSONObject()
			).put(
				"query_configuration", JSONFactoryUtil.createJSONObject()
			).put(
				"sort_configuration", JSONFactoryUtil.createJSONObject()
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

}