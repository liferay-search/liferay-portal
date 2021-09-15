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

package com.liferay.search.experiences.blueprints.commerce.internal.parameter;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.parameter.LongArrayParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.LongParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataBuilder;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDefinition;
import com.liferay.search.experiences.blueprints.engine.spi.parameter.ParameterContributor;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=commerce",
	service = ParameterContributor.class
)
public class CommerceParameterContributor implements ParameterContributor {

	@Override
	public void contribute(
		ParameterDataBuilder parameterDataBuilder, Blueprint blueprint,
		BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		_addAccountGroupIds(parameterDataBuilder, blueprintsAttributes);

		_addChannelGroupId(parameterDataBuilder, blueprintsAttributes);
	}

	@Override
	public String getCategoryNameKey() {
		return "commerce";
	}

	@Override
	public List<ParameterDefinition> getParameterDefinitions() {
		return ListUtil.fromArray(
			new ParameterDefinition(
				_getTemplateVariableName("account_group_ids"),
				LongParameter.class.getName(),
				"commerce.parameter.account-group-ids"),
			new ParameterDefinition(
				_getTemplateVariableName("channel_group_id"),
				LongParameter.class.getName(),
				"commerce.parameter.channel-group-id"));
	}

	private void _addAccountGroupIds(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes) {

		Optional<Object> optional = blueprintsAttributes.getAttributeOptional(
			"account_group_ids");

		if (!optional.isPresent()) {
			return;
		}

		long[] accountGroupIds = GetterUtil.getLongValues(optional.get());

		parameterDataBuilder.addParameter(
			new LongArrayParameter(
				"account_group_ids",
				_getTemplateVariableName("account_group_ids"),
				_toBoxedArray(accountGroupIds)));
	}

	private void _addChannelGroupId(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes) {

		Optional<Object> optional = blueprintsAttributes.getAttributeOptional(
			"channel_group_id");

		if (!optional.isPresent()) {
			return;
		}

		parameterDataBuilder.addParameter(
			new LongParameter(
				"channel_group_id",
				_getTemplateVariableName("channel_group_id"),
				GetterUtil.getLong(optional.get())));
	}

	private String _getTemplateVariableName(String key) {
		StringBundler sb = new StringBundler(3);

		sb.append("${commerce.");
		sb.append(key);
		sb.append("}");

		return sb.toString();
	}

	private Long[] _toBoxedArray(long[] array) {
		return LongStream.of(
			array
		).boxed(
		).toArray(
			Long[]::new
		);
	}

}