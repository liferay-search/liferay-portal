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

package com.liferay.search.experiences.blueprints.engine.internal.util;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.highlight.FieldConfig;
import com.liferay.portal.search.highlight.FieldConfigBuilder;
import com.liferay.portal.search.highlight.FieldConfigBuilderFactory;
import com.liferay.portal.search.highlight.Highlight;
import com.liferay.portal.search.highlight.HighlightBuilder;
import com.liferay.portal.search.highlight.HighlightBuilderFactory;
import com.liferay.search.experiences.blueprints.constants.json.keys.highlight.HighlightConfigurationKeys;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.util.util.SetterHelper;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = HighlightHelper.class)
public class HighlightHelper {

	public Optional<Highlight> getHighlight(
		JSONObject jsonObject, ParameterData parameterData, Messages messages) {

		HighlightBuilder highlightBuilder = _highlightBuilderFactory.builder();

		_setterHelper.setIntegerValue(
			jsonObject, HighlightConfigurationKeys.FRAGMENT_SIZE.getJsonKey(),
			highlightBuilder::fragmentSize);

		_setterHelper.setIntegerValue(
			jsonObject,
			HighlightConfigurationKeys.NUMBER_OF_FRAGMENTS.getJsonKey(),
			highlightBuilder::numOfFragments);

		_setterHelper.setStringArrayValue(
			jsonObject, HighlightConfigurationKeys.POST_TAGS.getJsonKey(),
			highlightBuilder::postTags);

		_setterHelper.setStringArrayValue(
			jsonObject, HighlightConfigurationKeys.PRE_TAGS.getJsonKey(),
			highlightBuilder::preTags);

		_setterHelper.setBooleanValue(
			jsonObject,
			HighlightConfigurationKeys.REQUIRE_FIELD_MATCH.getJsonKey(),
			highlightBuilder::requireFieldMatch);

		_setterHelper.setStringValue(
			jsonObject, HighlightConfigurationKeys.TYPE.getJsonKey(),
			highlightBuilder::highlighterType);

		_setFieldConfigs(highlightBuilder, jsonObject);

		return Optional.of(highlightBuilder.build());
	}

	private FieldConfig _getFieldConfig(
		JSONObject fieldsJSONObject, String key) {

		JSONObject fieldJSONObject = fieldsJSONObject.getJSONObject(key);

		FieldConfigBuilder fieldConfigBuilder =
			_fieldConfigBuilderFactory.builder(key);

		_setterHelper.setIntegerValue(
			fieldJSONObject,
			HighlightConfigurationKeys.FRAGMENT_OFFSET.getJsonKey(),
			fieldConfigBuilder::fragmentOffset);

		_setterHelper.setIntegerValue(
			fieldJSONObject,
			HighlightConfigurationKeys.FRAGMENT_SIZE.getJsonKey(),
			fieldConfigBuilder::fragmentSize);

		_setterHelper.setIntegerValue(
			fieldJSONObject,
			HighlightConfigurationKeys.NUMBER_OF_FRAGMENTS.getJsonKey(),
			fieldConfigBuilder::numFragments);

		return fieldConfigBuilder.build();
	}

	private void _setFieldConfigs(
		HighlightBuilder highlightBuilder, JSONObject jsonObject) {

		JSONObject fieldsJSONObject = jsonObject.getJSONObject(
			HighlightConfigurationKeys.FIELDS.getJsonKey());

		if (fieldsJSONObject == null) {
			return;
		}

		Set<String> keySet = fieldsJSONObject.keySet();

		Stream<String> stream = keySet.stream();

		stream.forEach(
			key -> highlightBuilder.addFieldConfig(
				_getFieldConfig(fieldsJSONObject, key)));
	}

	@Reference
	private FieldConfigBuilderFactory _fieldConfigBuilderFactory;

	@Reference
	private HighlightBuilderFactory _highlightBuilderFactory;

	@Reference
	private SetterHelper _setterHelper;

}