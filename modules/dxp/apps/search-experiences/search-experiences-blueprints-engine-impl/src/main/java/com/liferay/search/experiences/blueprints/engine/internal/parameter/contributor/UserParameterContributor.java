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
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.model.UserGroupGroupRole;
import com.liferay.portal.kernel.model.UserGroupRole;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserGroupGroupRoleLocalService;
import com.liferay.portal.kernel.service.UserGroupLocalService;
import com.liferay.portal.kernel.service.UserGroupRoleLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributeValuesHelper;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.internal.util.ProblemBuilders;
import com.liferay.search.experiences.blueprints.engine.parameter.BooleanParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.DateParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.IntegerParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.LongArrayParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.LongParameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataBuilder;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDefinition;
import com.liferay.search.experiences.blueprints.engine.parameter.StringParameter;
import com.liferay.search.experiences.blueprints.engine.spi.parameter.ParameterContributor;
import com.liferay.search.experiences.problems.Problem;
import com.liferay.search.experiences.problems.ProblemBuilder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;
import com.liferay.segments.SegmentsEntryRetriever;
import com.liferay.segments.context.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
		BlueprintsAttributes blueprintsAttributes,
		ProblemsHolderBuilder problemsHolderBuilder) {

		long userId = _getUserId(blueprintsAttributes);

		if (userId == 0) {
			ProblemBuilder problemBuilder = _problemBuilders1.warning(
				getClass().getName(),
				"core.error.user-id-not-set-in-request-attributes",
				"User ID not set in request attributes", null, null, null);

			Problem problem = problemBuilder.build();

			problemsHolderBuilder.addProblem(problem);

			return;
		}

		_contribute(
			parameterDataBuilder, blueprintsAttributes, userId,
			problemsHolderBuilder);
	}

	@Override
	public String getCategoryNameKey() {
		return "user";
	}

	@Override
	public List<ParameterDefinition> getParameterDefinitions() {
		return ListUtil.fromArray(
			new ParameterDefinition(
				_getTemplateVariableName("id"), LongParameter.class.getName(),
				"core.parameter.user.id"),
			new ParameterDefinition(
				_getTemplateVariableName("is_signed_in"),
				BooleanParameter.class.getName(),
				"core.parameter.user.is-signed-in"),
			new ParameterDefinition(
				_getTemplateVariableName("full_name"),
				StringParameter.class.getName(),
				"core.parameter.user.full-name"),
			new ParameterDefinition(
				_getTemplateVariableName("first_name"),
				StringParameter.class.getName(),
				"core.parameter.user.first-name"),
			new ParameterDefinition(
				_getTemplateVariableName("last_name"),
				StringParameter.class.getName(),
				"core.parameter.user.last-name"),
			new ParameterDefinition(
				_getTemplateVariableName("language_id"),
				StringParameter.class.getName(),
				"core.parameter.user.language-id"),
			new ParameterDefinition(
				_getTemplateVariableName("job_title"),
				StringParameter.class.getName(),
				"core.parameter.user.job-title"),
			new ParameterDefinition(
				_getTemplateVariableName("create_date"),
				DateParameter.class.getName(),
				"core.parameter.user.create-date"),
			new ParameterDefinition(
				_getTemplateVariableName("birthday"),
				DateParameter.class.getName(), "core.parameter.user.birthday"),
			new ParameterDefinition(
				_getTemplateVariableName("age"),
				IntegerParameter.class.getName(), "core.parameter.user.age"),
			new ParameterDefinition(
				_getTemplateVariableName("is_male"),
				BooleanParameter.class.getName(),
				"core.parameter.user.is-male"),
			new ParameterDefinition(
				_getTemplateVariableName("is_female"),
				BooleanParameter.class.getName(),
				"core.parameter.user.is-female"),
			new ParameterDefinition(
				_getTemplateVariableName("is_gender_x"),
				BooleanParameter.class.getName(),
				"core.parameter.user.is-gender-x"),
			new ParameterDefinition(
				_getTemplateVariableName("email_domain"),
				StringParameter.class.getName(),
				"core.parameter.user.email-domain"),
			new ParameterDefinition(
				_getTemplateVariableName("group_ids"),
				LongArrayParameter.class.getName(),
				"core.parameter.user.group-ids"),
			new ParameterDefinition(
				_getTemplateVariableName("usergroup_ids"),
				LongArrayParameter.class.getName(),
				"core.parameter.user.usergroup-ids"),
			new ParameterDefinition(
				_getTemplateVariableName("regular_role_ids"),
				LongArrayParameter.class.getName(),
				"core.parameter.user.regular-role-ids"),
			new ParameterDefinition(
				_getTemplateVariableName("current_site_role_ids"),
				LongArrayParameter.class.getName(),
				"core.parameter.user.current-site-role-ids"),
			new ParameterDefinition(
				_getTemplateVariableName("active_segment_entry_ids"),
				LongArrayParameter.class.getName(),
				"core.parameter.user.active-segment-entry-ids"));
	}

	private void _addCurrentSiteRoleIds(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes, User user) {

		long[] roleIds = _getCurrentSiteRoleIds(
			blueprintsAttributes, user.getUserId());

		if ((roleIds == null) || (roleIds.length == 0)) {
			return;
		}

		parameterDataBuilder.addParameter(
			new LongArrayParameter(
				"current_site_role_ids",
				_getTemplateVariableName("current_site_role_ids"),
				_toBoxedLongArray(roleIds)));
	}

	private void _addGroupIds(
		ParameterDataBuilder parameterDataBuilder, User user) {

		parameterDataBuilder.addParameter(
			new LongArrayParameter(
				"group_ids", _getTemplateVariableName("group_ids"),
				_toBoxedLongArray(user.getGroupIds())));
	}

	private void _addRegularUserRoleIds(
		ParameterDataBuilder parameterDataBuilder, User user,
		ProblemsHolderBuilder problemsHolderBuilder) {

		long[] roleIds = _getRegularRoleIds(user, problemsHolderBuilder);

		parameterDataBuilder.addParameter(
			new LongArrayParameter(
				"regular_role_ids",
				_getTemplateVariableName("regular_role_ids"),
				_toBoxedLongArray(roleIds)));
	}

	private void _addUserGroupGroupIds(
		ParameterDataBuilder parameterDataBuilder, User user) {

		long[] userGroupIds = _getUserGroupIds(user.getUserId());

		if (userGroupIds.length == 0) {
			return;
		}

		parameterDataBuilder.addParameter(
			new LongArrayParameter(
				"usergroup_ids", _getTemplateVariableName("usergroup_ids"),
				_toBoxedLongArray(_getUserGroupIds(user.getUserId()))));
	}

	private void _addUserInfo(
			ParameterDataBuilder parameterDataBuilder, User user)
		throws NumberFormatException, PortalException {

		parameterDataBuilder.addParameter(
			new LongParameter(
				"id", _getTemplateVariableName("id"), user.getUserId()));
		parameterDataBuilder.addParameter(
			new BooleanParameter(
				"is_signed_in", _getTemplateVariableName("is_signed_in"),
				_isSignedIn(user)));
		parameterDataBuilder.addParameter(
			new StringParameter(
				"full_name", _getTemplateVariableName("full_name"),
				user.getFullName()));
		parameterDataBuilder.addParameter(
			new StringParameter(
				"first_name", _getTemplateVariableName("first_name"),
				user.getFirstName()));
		parameterDataBuilder.addParameter(
			new StringParameter(
				"last_name", _getTemplateVariableName("last_name"),
				user.getLastName()));
		parameterDataBuilder.addParameter(
			new StringParameter(
				"language_id", _getTemplateVariableName("language_id"),
				user.getLanguageId()));
		parameterDataBuilder.addParameter(
			new StringParameter(
				"job_title", _getTemplateVariableName("job_title"),
				user.getJobTitle()));
		parameterDataBuilder.addParameter(
			new DateParameter(
				"create_date", _getTemplateVariableName("create_date"),
				user.getCreateDate()));

		parameterDataBuilder.addParameter(
			new DateParameter(
				"birthday", _getTemplateVariableName("birthday"),
				user.getBirthday()));

		parameterDataBuilder.addParameter(
			new IntegerParameter(
				"age", _getTemplateVariableName("age"),
				_getUserAge(user.getBirthday())));
		parameterDataBuilder.addParameter(
			new BooleanParameter(
				"is_male", _getTemplateVariableName("is_male"), user.isMale()));
		parameterDataBuilder.addParameter(
			new BooleanParameter(
				"is_female", _getTemplateVariableName("is_female"),
				user.isFemale()));
		parameterDataBuilder.addParameter(
			new BooleanParameter(
				"is_gender_x", _getTemplateVariableName("is_gender_x"),
				!user.isFemale() && !user.isMale()));

		parameterDataBuilder.addParameter(
			new StringParameter(
				"email_domain", _getTemplateVariableName("email_domain"),
				_getUserEmailDomain(user)));
	}

	private void _addUserSegments(
			ParameterDataBuilder parameterDataBuilder,
			BlueprintsAttributes blueprintsAttributes, User user)
		throws PortalException {

		Optional<Long> optional = _getScopeGroupId(blueprintsAttributes);

		if (!optional.isPresent()) {
			return;
		}

		Locale locale = blueprintsAttributes.getLocale();

		Context context = new Context();

		context.put(Context.SIGNED_IN, !user.isDefaultUser());
		context.put(Context.LANGUAGE_ID, locale.toString());

		long[] segmentsEntryIds = _segmentsEntryRetriever.getSegmentsEntryIds(
			optional.get(), user.getUserId(), context);

		long[] filteredArray = LongStream.of(
			segmentsEntryIds
		).filter(
			value -> value > 0
		).toArray();

		if (filteredArray.length == 0) {
			return;
		}

		parameterDataBuilder.addParameter(
			new LongArrayParameter(
				"active_segment_entry_ids",
				_getTemplateVariableName("active_segment_entry_ids"),
				_toBoxedLongArray(segmentsEntryIds)));
	}

	private void _contribute(
		ParameterDataBuilder parameterDataBuilder,
		BlueprintsAttributes blueprintsAttributes, long userId,
		ProblemsHolderBuilder problemsHolderBuilder) {

		try {
			User user = _userLocalService.getUser(userId);

			_addUserInfo(parameterDataBuilder, user);

			_addGroupIds(parameterDataBuilder, user);

			_addUserGroupGroupIds(parameterDataBuilder, user);

			_addCurrentSiteRoleIds(
				parameterDataBuilder, blueprintsAttributes, user);

			_addRegularUserRoleIds(
				parameterDataBuilder, user, problemsHolderBuilder);

			_addUserSegments(parameterDataBuilder, blueprintsAttributes, user);
		}
		catch (Exception exception) {
			ProblemBuilder problemBuilder = _problemBuilders2.error(
				getClass().getName(), "core.error.unknown-error", null, null,
				null, exception);

			problemsHolderBuilder.addProblem(problemBuilder.build());
		}
	}

	private long[] _getCurrentSiteRoleIds(
		BlueprintsAttributes blueprintsAttributes, long userId) {

		long[] userGroupRoleIds = _getUserGroupRoleIds(userId);

		Optional<Long> optional = _getScopeGroupId(blueprintsAttributes);

		if (!optional.isPresent()) {
			return userGroupRoleIds;
		}

		return LongStream.concat(
			Arrays.stream(userGroupRoleIds),
			Arrays.stream(_getUserGroupGroupRoleIds(userId, optional.get()))
		).toArray();
	}

	private long[] _getRegularRoleIds(
		User user, ProblemsHolderBuilder problemsHolderBuilder) {

		long[] regularRoleIds = user.getRoleIds();

		long[] userGroupRoleIds = _getUserGroupInheritedRoleIds(
			user.getUserId(), problemsHolderBuilder);

		if ((userGroupRoleIds == null) || (userGroupRoleIds.length == 0)) {
			return regularRoleIds;
		}

		return LongStream.concat(
			Arrays.stream(regularRoleIds),
			Arrays.stream(
				_getUserGroupInheritedRoleIds(
					user.getUserId(), problemsHolderBuilder))
		).toArray();
	}

	private Optional<Long> _getScopeGroupId(
		BlueprintsAttributes blueprintsAttributes) {

		return _blueprintsAttributeValuesHelper.getLongOptional(
			blueprintsAttributes, "scope_group_id");
	}

	private String _getTemplateVariableName(String key) {
		StringBundler sb = new StringBundler(3);

		sb.append("${user.");
		sb.append(key);
		sb.append("}");

		return sb.toString();
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

	private long[] _getUserGroupGroupRoleIds(long userId, long groupId) {
		List<UserGroupGroupRole> userGroupGroupRoles =
			_userGroupGroupRoleLocalService.getUserGroupGroupRolesByUser(
				userId, groupId);

		Stream<UserGroupGroupRole> stream = userGroupGroupRoles.stream();

		return stream.mapToLong(
			UserGroupGroupRole::getRoleId
		).toArray();
	}

	private long[] _getUserGroupIds(long userId) {
		List<UserGroup> userGroups = _userGroupLocalService.getUserUserGroups(
			userId);

		Stream<UserGroup> stream = userGroups.stream();

		return stream.mapToLong(
			UserGroup::getUserGroupId
		).toArray();
	}

	private long[] _getUserGroupInheritedRoleIds(
		long userId, ProblemsHolderBuilder problemsHolderBuilder) {

		List<UserGroup> userGroups = _userGroupLocalService.getUserUserGroups(
			userId);

		if (userGroups.isEmpty()) {
			return null;
		}

		List<Role> roles = new ArrayList<>();

		userGroups.forEach(
			userGroup -> {
				try {
					roles.addAll(
						_roleLocalService.getGroupRoles(
							userGroup.getGroupId()));
				}
				catch (PortalException portalException) {
					ProblemBuilder problemBuilder = _problemBuilders2.error(
						getClass().getName(), "core.error.unknown-error", null,
						null, null, portalException);

					problemsHolderBuilder.addProblem(problemBuilder.build());
				}
			});

		if (roles.isEmpty()) {
			return null;
		}

		Stream<Role> stream = roles.stream();

		return stream.mapToLong(
			Role::getRoleId
		).toArray();
	}

	private long[] _getUserGroupRoleIds(long userId) {
		List<UserGroupRole> roles =
			_userGroupRoleLocalService.getUserGroupRoles(userId);

		Stream<UserGroupRole> stream = roles.stream();

		return stream.mapToLong(
			UserGroupRole::getRoleId
		).toArray();
	}

	private Long _getUserId(BlueprintsAttributes blueprintsAttributes) {
		return GetterUtil.getLong(blueprintsAttributes.getUserId());
	}

	private Boolean _isSignedIn(User user) {
		return !user.isDefaultUser();
	}

	private Long[] _toBoxedLongArray(long[] arr) {
		return LongStream.of(
			arr
		).boxed(
		).toArray(
			Long[]::new
		);
	}

	@Reference
	private BlueprintsAttributeValuesHelper _blueprintsAttributeValuesHelper;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private ProblemBuilders _problemBuilders1;

	@Reference
	private com.liferay.search.experiences.problems.ProblemBuilders
		_problemBuilders2;

	@Reference
	private RoleLocalService _roleLocalService;

	@Reference
	private SegmentsEntryRetriever _segmentsEntryRetriever;

	@Reference
	private UserGroupGroupRoleLocalService _userGroupGroupRoleLocalService;

	@Reference
	private UserGroupLocalService _userGroupLocalService;

	@Reference
	private UserGroupRoleLocalService _userGroupRoleLocalService;

	@Reference
	private UserLocalService _userLocalService;

}