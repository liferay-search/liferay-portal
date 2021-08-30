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

package com.liferay.search.experiences.blueprints.internal.security.permission.resource;

import com.liferay.exportimport.kernel.staging.permission.StagingPermission;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermissionFactory;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.security.permission.resource.StagedModelPermissionLogic;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.search.experiences.blueprints.constants.BlueprintsConstants;
import com.liferay.search.experiences.blueprints.constants.BlueprintsPortletKeys;
import com.liferay.search.experiences.blueprints.model.Element;
import com.liferay.search.experiences.blueprints.service.ElementLocalService;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = {})
public class ElementModelResourcePermissionRegistrar {

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceRegistration = bundleContext.registerService(
			(Class<ModelResourcePermission<Element>>)
				(Class<?>)ModelResourcePermission.class,
			ModelResourcePermissionFactory.create(
				Element.class, Element::getElementId,
				_elementLocalService::getElement, _portletResourcePermission,
				(modelResourcePermission, consumer) -> consumer.accept(
					new StagedModelPermissionLogic<>(
						_stagingPermission,
						BlueprintsPortletKeys.BLUEPRINTS_ADMIN,
						Element::getElementId))),
			HashMapDictionaryBuilder.<String, Object>put(
				"model.class.name", Element.class.getName()
			).build());
	}

	@Deactivate
	protected void deactivate() {
		_serviceRegistration.unregister();
	}

	@Reference
	private ElementLocalService _elementLocalService;

	@Reference(
		target = "(resource.name=" + BlueprintsConstants.RESOURCE_NAME + ")"
	)
	private PortletResourcePermission _portletResourcePermission;

	private ServiceRegistration<ModelResourcePermission<Element>>
		_serviceRegistration;

	@Reference
	private StagingPermission _stagingPermission;

}