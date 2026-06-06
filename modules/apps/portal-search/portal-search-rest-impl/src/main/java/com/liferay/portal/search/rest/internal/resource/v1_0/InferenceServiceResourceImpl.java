/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.rest.internal.resource.v1_0;

import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.rest.dto.v1_0.InferenceService;
import com.liferay.portal.search.rest.resource.v1_0.InferenceServiceResource;
import com.liferay.portal.search.semantic.InferenceServicesResolver;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.util.TransformUtil;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import java.util.Collections;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Petteri Karttunen
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/inference-service.properties",
	scope = ServiceScope.PROTOTYPE, service = InferenceServiceResource.class
)
public class InferenceServiceResourceImpl
	extends BaseInferenceServiceResourceImpl {

	@Override
	public Page<InferenceService> getInferenceServicesPage() throws Exception {
		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-11319")) {

			throw new NotFoundException();
		}

		_checkPermission();

		InferenceServicesResolver inferenceServicesResolver =
			_inferenceServicesResolverSnapshot.get();

		if (inferenceServicesResolver == null) {
			return Page.of(Collections.emptyList());
		}

		return Page.of(
			TransformUtil.transform(
				inferenceServicesResolver.resolveInferenceServices(),
				this::_toInferenceService));
	}

	private void _checkPermission() {
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (!permissionChecker.isCompanyAdmin() &&
			!permissionChecker.isOmniadmin()) {

			throw new NotAuthorizedException(Response.Status.UNAUTHORIZED);
		}
	}

	private InferenceService _toInferenceService(
		com.liferay.portal.search.semantic.InferenceService inferenceService) {

		return new InferenceService() {
			{
				setConfiguration(
					() -> {
						String configurationJSON =
							inferenceService.getConfigurationJSON();

						if (Validator.isBlank(configurationJSON)) {
							return null;
						}

						return _jsonFactory.looseDeserialize(configurationJSON);
					});
				setService(inferenceService::getService);
			}
		};
	}

	private static final Snapshot<InferenceServicesResolver>
		_inferenceServicesResolverSnapshot = new Snapshot<>(
			InferenceServiceResourceImpl.class, InferenceServicesResolver.class,
			null, true);

	@Reference
	private JSONFactory _jsonFactory;

}