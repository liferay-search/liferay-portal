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

package com.liferay.portal.search.tuning.gsearch.configuration.service;

import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link SearchConfigurationService}.
 *
 * @author Brian Wing Shun Chan
 * @see SearchConfigurationService
 * @generated
 */
public class SearchConfigurationServiceWrapper
	implements SearchConfigurationService,
			   ServiceWrapper<SearchConfigurationService> {

	public SearchConfigurationServiceWrapper(
		SearchConfigurationService searchConfigurationService) {

		_searchConfigurationService = searchConfigurationService;
	}

	@Override
	public com.liferay.portal.search.tuning.gsearch.configuration.model.
		SearchConfiguration addConfiguration(
				java.util.Map<java.util.Locale, String> titleMap,
				java.util.Map<java.util.Locale, String> descriptionMap,
				String configuration, int type,
				com.liferay.portal.kernel.service.ServiceContext serviceContext)
			throws com.liferay.portal.kernel.exception.PortalException {

		return _searchConfigurationService.addConfiguration(
			titleMap, descriptionMap, configuration, type, serviceContext);
	}

	@Override
	public com.liferay.portal.search.tuning.gsearch.configuration.model.
		SearchConfiguration deleteConfiguration(long searchConfigurationId)
			throws com.liferay.portal.kernel.exception.PortalException {

		return _searchConfigurationService.deleteConfiguration(
			searchConfigurationId);
	}

	@Override
	public com.liferay.portal.search.tuning.gsearch.configuration.model.
		SearchConfiguration getConfiguration(long searchConfigurationId)
			throws com.liferay.portal.kernel.exception.PortalException {

		return _searchConfigurationService.getConfiguration(
			searchConfigurationId);
	}

	@Override
	public java.util.List
		<com.liferay.portal.search.tuning.gsearch.configuration.model.
			SearchConfiguration> getGroupConfigurations(
				long groupId, int type, int start, int end) {

		return _searchConfigurationService.getGroupConfigurations(
			groupId, type, start, end);
	}

	@Override
	public java.util.List
		<com.liferay.portal.search.tuning.gsearch.configuration.model.
			SearchConfiguration> getGroupConfigurations(
				long groupId, int status, int type, int start, int end) {

		return _searchConfigurationService.getGroupConfigurations(
			groupId, status, type, start, end);
	}

	@Override
	public java.util.List
		<com.liferay.portal.search.tuning.gsearch.configuration.model.
			SearchConfiguration> getGroupConfigurations(
				long groupId, int status, int type, int start, int end,
				com.liferay.portal.kernel.util.OrderByComparator
					<com.liferay.portal.search.tuning.gsearch.configuration.
						model.SearchConfiguration> orderByComparator) {

		return _searchConfigurationService.getGroupConfigurations(
			groupId, status, type, start, end, orderByComparator);
	}

	@Override
	public java.util.List
		<com.liferay.portal.search.tuning.gsearch.configuration.model.
			SearchConfiguration> getGroupConfigurations(
				long groupId, int type, int start, int end,
				com.liferay.portal.kernel.util.OrderByComparator
					<com.liferay.portal.search.tuning.gsearch.configuration.
						model.SearchConfiguration> orderByComparator) {

		return _searchConfigurationService.getGroupConfigurations(
			groupId, type, start, end, orderByComparator);
	}

	@Override
	public int getGroupConfigurationsCount(long groupId, int type) {
		return _searchConfigurationService.getGroupConfigurationsCount(
			groupId, type);
	}

	@Override
	public int getGroupConfigurationsCount(long groupId, int status, int type) {
		return _searchConfigurationService.getGroupConfigurationsCount(
			groupId, status, type);
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _searchConfigurationService.getOSGiServiceIdentifier();
	}

	@Override
	public com.liferay.portal.search.tuning.gsearch.configuration.model.
		SearchConfiguration updateConfiguration(
				long searchConfigurationId,
				java.util.Map<java.util.Locale, String> titleMap,
				java.util.Map<java.util.Locale, String> descriptionMap,
				String configuration,
				com.liferay.portal.kernel.service.ServiceContext serviceContext)
			throws com.liferay.portal.kernel.exception.PortalException {

		return _searchConfigurationService.updateConfiguration(
			searchConfigurationId, titleMap, descriptionMap, configuration,
			serviceContext);
	}

	@Override
	public SearchConfigurationService getWrappedService() {
		return _searchConfigurationService;
	}

	@Override
	public void setWrappedService(
		SearchConfigurationService searchConfigurationService) {

		_searchConfigurationService = searchConfigurationService;
	}

	private SearchConfigurationService _searchConfigurationService;

}