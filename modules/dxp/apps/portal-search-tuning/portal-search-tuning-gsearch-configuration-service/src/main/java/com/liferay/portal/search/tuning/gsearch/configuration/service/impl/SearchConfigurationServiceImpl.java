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

package com.liferay.portal.search.tuning.gsearch.configuration.service.impl;

import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SearchConfigurationActionKeys;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SearchConfigurationConstants;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.SearchConfigurationTypes;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SearchConfiguration;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationLocalService;
import com.liferay.portal.search.tuning.gsearch.configuration.service.base.SearchConfigurationServiceBaseImpl;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * The implementation of the search configuration remote service.
 *
 * <p>
 * All custom service methods should be put in this class. Whenever methods are added, rerun ServiceBuilder to copy their definitions into the <code>com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationService</code> interface.
 *
 * <p>
 * This is a remote service. Methods of this service are expected to have security checks based on the propagated JAAS credentials because this service can be accessed remotely.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see SearchConfigurationServiceBaseImpl
 */
@Component(
	property = {
		"json.web.service.context.name=search",
		"json.web.service.context.path=SearchConfiguration"
	},
	service = AopService.class
)
public class SearchConfigurationServiceImpl
	extends SearchConfigurationServiceBaseImpl {

	public SearchConfiguration addConfiguration(
			Map<Locale, String> titleMap,
			Map<Locale, String> descriptionMap, String configuration, int type,
			ServiceContext serviceContext)
		throws PortalException {

		String actionKey = SearchConfigurationActionKeys.
				getTypedActionKey(type, ActionKeys.ADD_ENTRY);
		
		_portletResourcePermission.check(
			getPermissionChecker(), serviceContext.getScopeGroupId(),
			actionKey);

		return searchConfigurationLocalService.addConfiguration(
			getUserId(), titleMap, descriptionMap, configuration, type,
			serviceContext);
	}

	public SearchConfiguration deleteConfiguration(long searchConfigurationId)
		throws PortalException {

		SearchConfiguration searchConfiguration =
				_searchConfigurationLocalService.getSearchConfiguration(
					searchConfigurationId);
		
		int type = searchConfiguration.getType();
		
		String actionKey = SearchConfigurationActionKeys.
				getTypedActionKey(type, ActionKeys.DELETE);

		_searchConfigurationModelResourcePermission.check(
			getPermissionChecker(), searchConfigurationId, actionKey);

		return searchConfigurationLocalService.deleteConfiguration(
			searchConfigurationId);
	}

	public SearchConfiguration getConfiguration(long searchConfigurationId)
		throws PortalException {

		SearchConfiguration searchConfiguration =
			_searchConfigurationLocalService.getSearchConfiguration(
				searchConfigurationId);
		
		int type = searchConfiguration.getType();

		String actionKey = SearchConfigurationActionKeys.
				getTypedActionKey(type, ActionKeys.VIEW);

		_searchConfigurationModelResourcePermission.check(
				getPermissionChecker(), searchConfiguration, 
				actionKey);
		
		return searchConfiguration;
	}

	public List<SearchConfiguration> getGroupConfigurations(
		long groupId, int type, int start, int end) {

		return getGroupConfigurations(
			groupId, WorkflowConstants.STATUS_APPROVED, type, start, end);
	}

	public List<SearchConfiguration> getGroupConfigurations(
		long groupId, int status, int type, int start, int end) {

		if (status == WorkflowConstants.STATUS_ANY) {
			return searchConfigurationPersistence.filterFindByG_T(
				groupId, type, start, end);
		}

		return searchConfigurationPersistence.filterFindByG_S_T(
			groupId, status, type, start, end);
	}

	public List<SearchConfiguration> getGroupConfigurations(
		long groupId, int status, int type, int start, int end,
		OrderByComparator<SearchConfiguration> orderByComparator) {

		if (status == WorkflowConstants.STATUS_ANY) {
			return searchConfigurationPersistence.findByG_T(
				groupId, type, start, end, orderByComparator);
		}

		return searchConfigurationPersistence.filterFindByG_S_T(
			groupId, status, type, start, end, orderByComparator);
	}

	public List<SearchConfiguration> getGroupConfigurations(
		long groupId, int type, int start, int end,
		OrderByComparator<SearchConfiguration> orderByComparator) {

		return getGroupConfigurations(
			groupId, WorkflowConstants.STATUS_APPROVED, type, start, end,
			orderByComparator);
	}

	public int getGroupConfigurationsCount(long groupId, int type) {
		return getGroupConfigurationsCount(
			groupId, WorkflowConstants.STATUS_APPROVED, type);
	}

	public int getGroupConfigurationsCount(long groupId, int status, int type) {
		if (status == WorkflowConstants.STATUS_ANY) {
			return searchConfigurationPersistence.countByG_T(groupId, type);
		}

		return searchConfigurationPersistence.countByG_S_T(
			groupId, status, type);
	}

	public SearchConfiguration updateConfiguration(
			long searchConfigurationId, Map<Locale, String> titleMap,
			Map<Locale, String> descriptionMap, String configuration,
			ServiceContext serviceContext)
		throws PortalException {

		SearchConfiguration searchConfiguration =
				_searchConfigurationLocalService.getSearchConfiguration(
					searchConfigurationId);
			
		int type = searchConfiguration.getType();
	
		String actionKey = SearchConfigurationActionKeys.
				getTypedActionKey(type, ActionKeys.UPDATE);
		
		_searchConfigurationModelResourcePermission.check(
			getPermissionChecker(), searchConfigurationId, actionKey);

		return _searchConfigurationLocalService.updateConfiguration(
			getUserId(), searchConfigurationId, titleMap, descriptionMap,
			configuration, serviceContext);
	}

	@Reference(
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(resource.name=" + SearchConfigurationConstants.RESOURCE_NAME + ")"
	)
	private volatile PortletResourcePermission _portletResourcePermission;

	@Reference
	private SearchConfigurationLocalService _searchConfigurationLocalService;

	@Reference(
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(model.class.name=com.liferay.portal.search.tuning.gsearch.configuration.model.SearchConfiguration)"
	)
	private volatile ModelResourcePermission<SearchConfiguration>
		_searchConfigurationModelResourcePermission;

	@Reference
	private UserLocalService _userLocalService;

}