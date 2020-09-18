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

package com.liferay.portal.search.tuning.blueprints.engine.internal.parameter.contributor;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.BooleanParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.DateParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.IntegerParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.LongArrayParameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterDataBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.ParameterDefinition;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.StringParameter;
import com.liferay.portal.search.tuning.blueprints.engine.spi.parameter.ParameterContributor;
import com.liferay.portal.search.tuning.blueprints.message.Message;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.message.Severity;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.segments.context.Context;
import com.liferay.segments.provider.SegmentsEntryProvider;
import com.liferay.segments.simulator.SegmentsEntrySimulator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

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

		User user = _getUser(blueprintsAttributes, messages);

		long companyId = blueprintsAttributes.getCompanyId();

		_addUserInfo(parameterDataBuilder, messages, user);

		_addUserGroupIds(parameterDataBuilder, user);

		_addUserRoleIds(parameterDataBuilder, user);

		_addUserSegmentIds(parameterDataBuilder, companyId, user);
	}

	@Override
	public List<ParameterDefinition> getParameterDefinitions() {
		List<ParameterDefinition> parameterDefinitions = new ArrayList<>();

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName(
					ReservedParameterNames.USER_FULL_NAME.getKey()),
				StringParameter.class.getName(), "parameter.user.full-name"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName(
					ReservedParameterNames.USER_FIRST_NAME.getKey()),
				StringParameter.class.getName(), "parameter.user.first-name"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName(
					ReservedParameterNames.USER_LAST_NAME.getKey()),
				StringParameter.class.getName(), "parameter.user.last-name"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName(
					ReservedParameterNames.USER_LANGUAGE_ID.getKey()),
				StringParameter.class.getName(), "parameter.user.language-id"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName(
					ReservedParameterNames.USER_JOB_TITLE.getKey()),
				StringParameter.class.getName(), "parameter.user.job-title"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName(
					ReservedParameterNames.USER_CREATE_DATE.getKey()),
				DateParameter.class.getName(), "parameter.user.create-date"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName(
					ReservedParameterNames.USER_BIRTHDAY.getKey()),
				DateParameter.class.getName(), "parameter.user.birthday"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName(
					ReservedParameterNames.USER_AGE.getKey()),
				IntegerParameter.class.getName(), "parameter.user.age"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName(
					ReservedParameterNames.USER_IS_MALE.getKey()),
				BooleanParameter.class.getName(), "parameter.user.is-male"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName(
					ReservedParameterNames.USER_IS_FEMALE.getKey()),
				BooleanParameter.class.getName(), "parameter.user.is-female"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName(
					ReservedParameterNames.USER_IS_GENDER_X.getKey()),
				BooleanParameter.class.getName(),
				"parameter.user.is-gender-x"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName(
					ReservedParameterNames.USER_EMAIL_DOMAIN.getKey()),
				StringParameter.class.getName(),
				"parameter.user.email-domain"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName(
					ReservedParameterNames.USER_GROUP_IDS.getKey()),
				LongArrayParameter.class.getName(),
				"parameter.user.group-ids"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName(
					ReservedParameterNames.USER_ROLE_IDS.getKey()),
				LongArrayParameter.class.getName(), "parameter.user.role-ids"));

		parameterDefinitions.add(
			new ParameterDefinition(
				_getTemplateVariableName(
					ReservedParameterNames.USER_SEGMENT_ENTRY_IDS.getKey()),
				LongArrayParameter.class.getName(),
				"parameter.user.segment-entry-ids"));

		return parameterDefinitions;
	}

	private void _addUserGroupIds(
		ParameterDataBuilder parameterDataBuilder, User user) {

		parameterDataBuilder.addParameter(
			new LongArrayParameter(
				ReservedParameterNames.USER_GROUP_IDS.getKey(),
				_getTemplateVariableName(
					ReservedParameterNames.USER_GROUP_IDS.getKey()),
				LongStream.of(
					user.getGroupIds()
				).boxed(
				).toArray(
					Long[]::new
				)));
	}

	private void _addUserInfo(
		ParameterDataBuilder parameterDataBuilder, Messages messages,
		User user) {

		parameterDataBuilder.addParameter(
			new StringParameter(
				ReservedParameterNames.USER_FULL_NAME.getKey(),
				_getTemplateVariableName(
					ReservedParameterNames.USER_FULL_NAME.getKey()),
				user.getFullName()));
		parameterDataBuilder.addParameter(
			new StringParameter(
				ReservedParameterNames.USER_FIRST_NAME.getKey(),
				_getTemplateVariableName(
					ReservedParameterNames.USER_FIRST_NAME.getKey()),
				user.getFirstName()));
		parameterDataBuilder.addParameter(
			new StringParameter(
				ReservedParameterNames.USER_LAST_NAME.getKey(),
				_getTemplateVariableName(
					ReservedParameterNames.USER_LAST_NAME.getKey()),
				user.getLastName()));
		parameterDataBuilder.addParameter(
			new StringParameter(
				ReservedParameterNames.USER_LANGUAGE_ID.getKey(),
				_getTemplateVariableName(
					ReservedParameterNames.USER_LANGUAGE_ID.getKey()),
				user.getLanguageId()));
		parameterDataBuilder.addParameter(
			new StringParameter(
				ReservedParameterNames.USER_JOB_TITLE.getKey(),
				_getTemplateVariableName(
					ReservedParameterNames.USER_JOB_TITLE.getKey()),
				user.getJobTitle()));
		parameterDataBuilder.addParameter(
			new DateParameter(
				ReservedParameterNames.USER_CREATE_DATE.getKey(),
				_getTemplateVariableName(
					ReservedParameterNames.USER_CREATE_DATE.getKey()),
				user.getCreateDate()));

		try {
			parameterDataBuilder.addParameter(
				new DateParameter(
					ReservedParameterNames.USER_BIRTHDAY.getKey(),
					_getTemplateVariableName(
						ReservedParameterNames.USER_BIRTHDAY.getKey()),
					user.getBirthday()));

			parameterDataBuilder.addParameter(
				new IntegerParameter(
					ReservedParameterNames.USER_AGE.getKey(),
					_getTemplateVariableName(
						ReservedParameterNames.USER_AGE.getKey()),
					_getUserAge(user.getBirthday())));
			parameterDataBuilder.addParameter(
				new BooleanParameter(
					ReservedParameterNames.USER_IS_MALE.getKey(),
					_getTemplateVariableName(
						ReservedParameterNames.USER_IS_MALE.getKey()),
					user.isMale()));
			parameterDataBuilder.addParameter(
				new BooleanParameter(
					ReservedParameterNames.USER_IS_FEMALE.getKey(),
					_getTemplateVariableName(
						ReservedParameterNames.USER_IS_FEMALE.getKey()),
					user.isFemale()));
			parameterDataBuilder.addParameter(
				new BooleanParameter(
					ReservedParameterNames.USER_IS_GENDER_X.getKey(),
					_getTemplateVariableName(
						ReservedParameterNames.USER_IS_GENDER_X.getKey()),
					!user.isFemale() && !user.isMale()));
		}
		catch (NumberFormatException numberFormatException) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.unknown-exception",
					numberFormatException.getMessage(), numberFormatException,
					null, null, String.valueOf(user.getUserId())));
			_log.error(
				numberFormatException.getMessage(), numberFormatException);
		}
		catch (PortalException portalException) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.unknown-exception",
					portalException.getMessage(), portalException, null, null,
					String.valueOf(user.getUserId())));
			_log.error(portalException.getMessage(), portalException);
		}

		parameterDataBuilder.addParameter(
			new StringParameter(
				ReservedParameterNames.USER_EMAIL_DOMAIN.getKey(),
				_getTemplateVariableName(
					ReservedParameterNames.USER_EMAIL_DOMAIN.getKey()),
				_getUserEmailDomain(user)));
	}

	private void _addUserRoleIds(
		ParameterDataBuilder parameterDataBuilder, User user) {

		parameterDataBuilder.addParameter(
			new LongArrayParameter(
				ReservedParameterNames.USER_ROLE_IDS.getKey(),
				_getTemplateVariableName(
					ReservedParameterNames.USER_ROLE_IDS.getKey()),
				LongStream.of(
					user.getRoleIds()
				).boxed(
				).toArray(
					Long[]::new
				)));
	}

	private void _addUserSegmentIds(
		ParameterDataBuilder parameterDataBuilder, long companyId, User user) {

		long[] userGroupIds = _getUserAccessibleSiteGroupIds(companyId, user);

		if (userGroupIds.length > 0) {
			Long[] segmentsEntryIds = _getUserSegmentEntryIds(
				user, userGroupIds);

			if (segmentsEntryIds.length > 0) {
				parameterDataBuilder.addParameter(
					new LongArrayParameter(
						ReservedParameterNames.USER_SEGMENT_ENTRY_IDS.getKey(),
						_getTemplateVariableName(
							ReservedParameterNames.USER_SEGMENT_ENTRY_IDS.
								getKey()),
						segmentsEntryIds));
			}
		}
	}

	private String _getTemplateVariableName(String key) {
		StringBundler sb = new StringBundler(3);

		sb.append("${user.");
		sb.append(key);
		sb.append("}");

		return sb.toString();
	}

	private User _getUser(
		BlueprintsAttributes blueprintsAttributes, Messages messages) {

		long userId = blueprintsAttributes.getUserId();

		if (userId == 0) {
			return null;
		}

		try {
			return _userLocalService.getUser(userId);
		}
		catch (PortalException portalException) {
			messages.addMessage(
				new Message(
					Severity.ERROR, "core", "core.error.user-not-found",
					portalException.getMessage(), portalException, null, null,
					String.valueOf(userId)));

			_log.error(portalException.getMessage(), portalException);
		}

		return null;
	}

	private long[] _getUserAccessibleSiteGroupIds(long companyId, User user) {
		List<Long> groupIds = new ArrayList<>();

		try {
			Company company = _companyLocalService.getCompany(companyId);

			long companyGroupId = company.getGroupId();

			groupIds.add(companyGroupId);

			for (Group group :
					_groupLocalService.getGroups(companyId, 0, true)) {

				if (group.isActive() && !group.isStagingGroup() &&
					group.hasPublicLayouts()) {

					groupIds.add(group.getGroupId());
				}
			}

			for (Group group : user.getSiteGroups()) {
				if (!groupIds.contains(group.getGroupId()) &&
					group.isActive() && !group.isStagingGroup()) {

					groupIds.add(group.getGroupId());
				}
			}
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);
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

	private Long[] _getUserSegmentEntryIds(User user, long[] groupIds) {
		if ((_segmentsEntrySimulator != null) &&
			_segmentsEntrySimulator.isSimulationActive(user.getUserId())) {

			long[] simulatedSegmentsEntryIds =
				_segmentsEntrySimulator.getSimulatedSegmentsEntryIds(
					user.getUserId());

			return LongStream.of(
				simulatedSegmentsEntryIds
			).boxed(
			).toArray(
				Long[]::new
			);
		}

		try {
			List<Long> allSegmentEntryIds = new ArrayList<>();

			Context context = new Context();

			for (long groupId : groupIds) {
				long[] segmentsEntryIds =
					_segmentsEntryProvider.getSegmentsEntryIds(
						groupId, User.class.getName(), user.getUserId(),
						context);

				LongStream longStream = Arrays.stream(segmentsEntryIds);

				longStream.forEach(allSegmentEntryIds::add);
			}

			return allSegmentEntryIds.toArray(new Long[0]);
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);
		}

		return new Long[0];
	}

	private static final Log _log = LogFactoryUtil.getLog(
		UserParameterContributor.class);

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private SegmentsEntryProvider _segmentsEntryProvider;

	@Reference(
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(model.class.name=com.liferay.portal.kernel.model.User)"
	)
	private volatile SegmentsEntrySimulator _segmentsEntrySimulator;

	@Reference
	private UserLocalService _userLocalService;

}