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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.parameter.BooleanParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.DateParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.IntegerParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.LongArrayParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.LongParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataBuilder;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDefinition;
import com.liferay.search.experiences.blueprints.engine.parameter.StringParameter;
import com.liferay.search.experiences.blueprints.engine.spi.parameter.ParameterContributor;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.util.util.MessagesUtil;
import com.liferay.segments.provider.SegmentsEntryProvider;
import com.liferay.segments.simulator.SegmentsEntrySimulator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=user",
	service = ParameterContributor.class
)
public class UserParameterContributor implements ParameterContributor {

	@Override
	public void contribute(
		ParameterDataBuilder parameterDataBuilder, Blueprint blueprint,
		BlueprintsAttributes blueprintsAttributes, Messages messages) {

		long userId = _getUserId(blueprintsAttributes);

		if (userId == 0) {
			MessagesUtil.warning(
				messages, getClass().getName(),
				"User ID not set in request attributes", null, null, null,
				"core.error.user-id-not-set-in-request-attributes");

			return;
		}

		_contribute(
			parameterDataBuilder, blueprintsAttributes, userId, messages);
	}

	@Override
	public String getCategoryNameKey() {
		return "user";
	}

	@Override
	public List<ParameterDefinition> getParameterDefinitions() {
		List<ParameterDefinition> parameterDefinitions = new ArrayList<>();

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_id"),
				LongParameter.class.getName(), "core.parameter.user.id"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_is_signed_in"),
				BooleanParameter.class.getName(),
				"core.parameter.user.is-signed-in"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_full_name"),
				StringParameter.class.getName(),
				"core.parameter.user.full-name"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_first_name"),
				StringParameter.class.getName(),
				"core.parameter.user.first-name"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_last_name"),
				StringParameter.class.getName(),
				"core.parameter.user.last-name"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_language_id"),
				StringParameter.class.getName(),
				"core.parameter.user.language-id"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_job_title"),
				StringParameter.class.getName(),
				"core.parameter.user.job-title"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_create_date"),
				DateParameter.class.getName(),
				"core.parameter.user.create-date"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_birthday"),
				DateParameter.class.getName(), "core.parameter.user.birthday"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_age"),
				IntegerParameter.class.getName(), "core.parameter.user.age"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_is_male"),
				BooleanParameter.class.getName(),
				"core.parameter.user.is-male"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_is_female"),
				BooleanParameter.class.getName(),
				"core.parameter.user.is-female"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_is_gender_x"),
				BooleanParameter.class.getName(),
				"core.parameter.user.is-gender-x"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_email_domain"),
				StringParameter.class.getName(),
				"core.parameter.user.email-domain"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_group_ids"),
				LongArrayParameter.class.getName(),
				"core.parameter.user.group-ids"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_role_ids"),
				LongArrayParameter.class.getName(),
				"core.parameter.user.role-ids"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName("user_segment_entry_ids"),
				LongArrayParameter.class.getName(),
				"core.parameter.user.segment-entry-ids"));

		return parameterDefinitions;
	}

	private void _addUserGroupIds(
		ParameterDataBuilder parameterDataBuilder, User user) {

		parameterDataBuilder.addParameter(
			new LongArrayParameter(
				"user_group_ids", _getTemplateVariableName("user_group_ids"),
				LongStream.of(
					user.getGroupIds()
				).boxed(
				).toArray(
					Long[]::new
				)));
	}

	private void _addUserInfo(
			ParameterDataBuilder parameterDataBuilder, User user)
		throws NumberFormatException, PortalException {

		parameterDataBuilder.addParameter(
			new LongParameter(
				"user_id", _getTemplateVariableName("user_id"),
				user.getUserId()));
		parameterDataBuilder.addParameter(
			new BooleanParameter(
				"user_is_signed_in",
				_getTemplateVariableName("user_is_signed_in"),
				_isSignedIn(user)));
		parameterDataBuilder.addParameter(
			new StringParameter(
				"user_full_name", _getTemplateVariableName("user_full_name"),
				user.getFullName()));
		parameterDataBuilder.addParameter(
			new StringParameter(
				"user_first_name", _getTemplateVariableName("user_first_name"),
				user.getFirstName()));
		parameterDataBuilder.addParameter(
			new StringParameter(
				"user_last_name", _getTemplateVariableName("user_last_name"),
				user.getLastName()));
		parameterDataBuilder.addParameter(
			new StringParameter(
				"user_language_id",
				_getTemplateVariableName("user_language_id"),
				user.getLanguageId()));
		parameterDataBuilder.addParameter(
			new StringParameter(
				"user_job_title", _getTemplateVariableName("user_job_title"),
				user.getJobTitle()));
		parameterDataBuilder.addParameter(
			new DateParameter(
				"user_create_date",
				_getTemplateVariableName("user_create_date"),
				user.getCreateDate()));

		parameterDataBuilder.addParameter(
			new DateParameter(
				"user_birthday", _getTemplateVariableName("user_birthday"),
				user.getBirthday()));

		parameterDataBuilder.addParameter(
			new IntegerParameter(
				"user_age", _getTemplateVariableName("user_age"),
				_getUserAge(user.getBirthday())));
		parameterDataBuilder.addParameter(
			new BooleanParameter(
				"user_is_male", _getTemplateVariableName("user_is_male"),
				user.isMale()));
		parameterDataBuilder.addParameter(
			new BooleanParameter(
				"user_is_female", _getTemplateVariableName("user_is_female"),
				user.isFemale()));
		parameterDataBuilder.addParameter(
			new BooleanParameter(
				"user_is_gender_x",
				_getTemplateVariableName("user_is_gender_x"),
				!user.isFemale() && !user.isMale()));

		parameterDataBuilder.addParameter(
			new StringParameter(
				"user_email_domain",
				_getTemplateVariableName("user_email_domain"),
				_getUserEmailDomain(user)));
	}

	private void _addUserRoleIds(
		ParameterDataBuilder parameterDataBuilder, User user) {

		parameterDataBuilder.addParameter(
			new LongArrayParameter(
				"user_role_ids", _getTemplateVariableName("user_role_ids"),
				LongStream.of(
					user.getRoleIds()
				).boxed(
				).toArray(
					Long[]::new
				)));
	}

	private void _addUserSegments(
			ParameterDataBuilder parameterDataBuilder,
			BlueprintsAttributes blueprintsAttributes, User user)
		throws PortalException {

		long userId = user.getUserId();

		List<Long> segmentEntryIds = new ArrayList<>();

		long[] simulatedSegmentEntryIds = _getSimulatedSegmentEntryIds(userId);

		if (simulatedSegmentEntryIds.length > 0) {
			LongStream longStream = LongStream.of(simulatedSegmentEntryIds);

			segmentEntryIds.addAll(
				longStream.boxed(
				).collect(
					Collectors.toList()
				));
		}
		else {
			long[] groupIds = _getUserAccessibleSiteGroupIds(
				blueprintsAttributes.getCompanyId(), user);

			if (groupIds.length == 0) {
				return;
			}

			for (long groupId : groupIds) {
				long[] ids = _segmentsEntryProvider.getSegmentsEntryIds(
					groupId, User.class.getName(), user.getPrimaryKey());

				if ((ids != null) && (ids.length > 0)) {
					LongStream longStream = LongStream.of(ids);

					segmentEntryIds.addAll(
						longStream.boxed(
						).collect(
							Collectors.toList()
						));
				}
			}
		}

		if (segmentEntryIds.isEmpty()) {
			return;
		}

		parameterDataBuilder.addParameter(
			new LongArrayParameter(
				"user_segment_entry_ids",
				_getTemplateVariableName("user_segment_entry_ids"),
				segmentEntryIds.toArray(new Long[0])));
	}

	private void _contribute(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes, long userId,
		Messages messages) {

		try {
			User user = _userLocalService.getUser(userId);

			_addUserInfo(parameterDataBuilder, user);

			_addUserGroupIds(parameterDataBuilder, user);

			_addUserRoleIds(parameterDataBuilder, user);

			_addUserSegments(parameterDataBuilder, blueprintsAttributes, user);
		}
		catch (Exception exception) {
			MessagesUtil.error(
				messages, getClass().getName(), exception, null, null, null,
				"core.error.unknown-error");
		}
	}

	private long[] _getSimulatedSegmentEntryIds(long userId) {
		if (_segmentsEntrySimulator != null) {
			return _segmentsEntrySimulator.getSimulatedSegmentsEntryIds(userId);
		}

		return new long[0];
	}

	private String _getTemplateVariableName(String key) {
		StringBundler sb = new StringBundler(3);

		sb.append("${user.");
		sb.append(key);
		sb.append("}");

		return sb.toString();
	}

	private long[] _getUserAccessibleSiteGroupIds(long companyId, User user)
		throws PortalException {

		List<Long> groupIds = new ArrayList<>();

		Company company = _companyLocalService.getCompany(companyId);

		long companyGroupId = company.getGroupId();

		groupIds.add(companyGroupId);

		for (Group group : _groupLocalService.getGroups(companyId, 0, true)) {
			if (group.isActive() && !group.isStagingGroup() &&
				group.hasPublicLayouts()) {

				groupIds.add(group.getGroupId());
			}
		}

		for (Group group : user.getSiteGroups()) {
			if (!groupIds.contains(group.getGroupId()) && group.isActive() &&
				!group.isStagingGroup()) {

				groupIds.add(group.getGroupId());
			}
		}

		groupIds.toArray(new Long[0]);

		Stream<Long> stream = groupIds.stream();

		return stream.mapToLong(
			l -> l
		).toArray();
	}

	private int _getUserAge(Date birthday) {
		Date now = new Date();

		DateFormat formatter = new SimpleDateFormat("yyyyMMdd");

		int d1 = GetterUtil.getInteger(formatter.format(birthday));

		int d2 = GetterUtil.getInteger(formatter.format(now));

		return (d2 - d1) / 10000;
	}

	private String _getUserEmailDomain(User user) {
		String email = user.getEmailAddress();

		return email.substring(email.indexOf("@") + 1);
	}

	private Long _getUserId(BlueprintsAttributes blueprintsAttributes) {
		return GetterUtil.getLong(blueprintsAttributes.getUserId());
	}

	private Boolean _isSignedIn(User user) {
		return !user.isDefaultUser();
	}

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private SegmentsEntryProvider _segmentsEntryProvider;

	@Reference
	private SegmentsEntrySimulator _segmentsEntrySimulator;

	@Reference
	private UserLocalService _userLocalService;

}