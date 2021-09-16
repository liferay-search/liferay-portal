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

package com.liferay.search.experiences.blueprints.engine.internal.parameter.contributor;

import com.liferay.portal.kernel.change.tracking.CTCollectionThreadLocal;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributeValuesHelper;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.parameter.BooleanParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.LongParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataBuilder;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDefinition;
import com.liferay.search.experiences.blueprints.engine.parameter.StringParameter;
import com.liferay.search.experiences.blueprints.engine.spi.parameter.ParameterContributor;
import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemBuilders;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=context",
	service = ParameterContributor.class
)
public class ContextParameterContributor implements ParameterContributor {

	@Override
	public void contribute(
		ParameterDataBuilder parameterDataBuilder, Blueprint blueprint,
		BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		_addCompanyId(parameterDataBuilder, blueprintsAttributes);

		_addCTCollectionParameter(parameterDataBuilder);

		_addGroupParameters(
			parameterDataBuilder, blueprintsAttributes, problemsHolderBuilder);

		_addLanguage(parameterDataBuilder, blueprintsAttributes);

		_addLayoutNameCurrentValue(
			parameterDataBuilder, blueprintsAttributes, problemsHolderBuilder);

		_addPlid(parameterDataBuilder, blueprintsAttributes);
	}

	@Override
	public String getCategoryNameKey() {
		return "context";
	}

	@Override
	public List<ParameterDefinition> getParameterDefinitions() {
		return ListUtil.fromArray(
			new ParameterDefinition(
				_getTemplateVariableName("company_id"),
				LongParameter.class.getName(),
				"core.parameter.context.company-id"),
			new ParameterDefinition(
				_getTemplateVariableName("ct_collection_id"),
				LongParameter.class.getName(),
				"core.parameter.context.ct-collection-id"),
			new ParameterDefinition(
				_getTemplateVariableName("scope_group_id"),
				LongParameter.class.getName(),
				"core.parameter.context.scope-group-id"),
			new ParameterDefinition(
				_getTemplateVariableName("layout_locale_name"),
				StringParameter.class.getName(),
				"core.parameter.context.layout-locale-name"),
			new ParameterDefinition(
				_getTemplateVariableName("plid"), LongParameter.class.getName(),
				"core.parameter.context.plid"),
			new ParameterDefinition(
				_getTemplateVariableName("language"),
				StringParameter.class.getName(),
				"core.parameter.context.language"),
			new ParameterDefinition(
				_getTemplateVariableName("language_id"),
				StringParameter.class.getName(),
				"core.parameter.context.language-id"));
	}

	private void _addCompanyId(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes) {

		parameterDataBuilder.addParameter(
			new LongParameter(
				"company_id", _getTemplateVariableName("company_id"),
				blueprintsAttributes.getCompanyId()));
	}

	private void _addCTCollectionParameter(
		ParameterDataBuilder parameterDataBuilder) {

		parameterDataBuilder.addParameter(
			new LongParameter(
				"ct_collection_id",
				_getTemplateVariableName("ct_collection_id"),
				CTCollectionThreadLocal.getCTCollectionId()));
	}

	private void _addGroupParameters(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<Long> optional =
			_blueprintsAttributeValuesHelper.getLongOptional(
				blueprintsAttributes, "scope_group_id");

		if (!optional.isPresent()) {
			return;
		}

		long scopeGroupId = optional.get();

		parameterDataBuilder.addParameter(
			new LongParameter(
				"scope_group_id", _getTemplateVariableName("scope_group_id"),
				scopeGroupId));

		Group group = _getGroup(scopeGroupId, problemsHolderBuilder);

		if (group == null) {
			return;
		}

		parameterDataBuilder.addParameter(
			new BooleanParameter(
				"is_staging_group",
				_getTemplateVariableName("is_staging_group"),
				group.isStagingGroup()));
	}

	private void _addLanguage(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes) {

		Locale locale = blueprintsAttributes.getLocale();

		parameterDataBuilder.addParameter(
			new StringParameter(
				"language_id", _getTemplateVariableName("language_id"),
				"_" + _language.getLanguageId(locale)));

		parameterDataBuilder.addParameter(
			new StringParameter(
				"language", _getTemplateVariableName("language"),
				locale.getLanguage()));
	}

	private void _addLayoutNameCurrentValue(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		Optional<Long> optional =
			_blueprintsAttributeValuesHelper.getLongOptional(
				blueprintsAttributes, "plid");

		if (!optional.isPresent()) {
			return;
		}

		long plid = optional.get();

		try {
			Layout layout = _layoutLocalService.getLayout(plid);

			parameterDataBuilder.addParameter(
				new StringParameter(
					"layout_locale_name",
					_getTemplateVariableName("layout_locale_name"),
					layout.getName(blueprintsAttributes.getLocale(), true)));
		}
		catch (PortalException portalException) {
			ProblemBuilder problemBuilder = _problemBuilders.error(
				getClass().getName(), "core.error.layout-not-found", null, null,
				String.valueOf(plid), portalException);

			problemsHolderBuilder.addProblem(problemBuilder.build());
		}
	}

	private void _addPlid(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes) {

		Optional<Long> optional =
			_blueprintsAttributeValuesHelper.getLongOptional(
				blueprintsAttributes, "plid");

		if (!optional.isPresent()) {
			return;
		}

		parameterDataBuilder.addParameter(
			new LongParameter(
				"plid", _getTemplateVariableName("plid"), optional.get()));
	}

	private Group _getGroup(
		long groupId, ProblemsHolderBuilder problemsHolderBuilder) {

		try {
			return _groupLocalService.getGroup(groupId);
		}
		catch (PortalException portalException) {
			ProblemBuilder problemBuilder = _problemBuilders.error(
				getClass().getName(), "core.error.group-not-found", null, null,
				GetterUtil.getString(groupId), portalException);

			problemsHolderBuilder.addProblem(problemBuilder.build());
		}

		return null;
	}

	private String _getTemplateVariableName(String key) {
		StringBundler sb = new StringBundler(3);

		sb.append("${context.");
		sb.append(key);
		sb.append("}");

		return sb.toString();
	}

	@Reference
	private BlueprintsAttributeValuesHelper _blueprintsAttributeValuesHelper;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Language _language;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private ProblemBuilders _problemBuilders;

}