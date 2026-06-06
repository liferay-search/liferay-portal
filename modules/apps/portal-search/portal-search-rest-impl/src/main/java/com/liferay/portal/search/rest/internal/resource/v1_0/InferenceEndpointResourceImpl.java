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
import com.liferay.portal.search.rest.dto.v1_0.InferenceEndpoint;
import com.liferay.portal.search.rest.resource.v1_0.InferenceEndpointResource;
import com.liferay.portal.search.semantic.InferenceEndpointCreator;
import com.liferay.portal.search.semantic.InferenceIdResolver;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Petteri Karttunen
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/inference-endpoint.properties",
	scope = ServiceScope.PROTOTYPE, service = InferenceEndpointResource.class
)
public class InferenceEndpointResourceImpl
	extends BaseInferenceEndpointResourceImpl {

	@Override
	public InferenceEndpoint postInferenceEndpoint(
			InferenceEndpoint inferenceEndpoint)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-11319")) {

			throw new NotFoundException();
		}

		_checkPermission();

		String service = inferenceEndpoint.getService();

		if (Validator.isBlank(service)) {
			throw new BadRequestException("Service is null or empty");
		}

		InferenceEndpointCreator inferenceEndpointCreator =
			_inferenceEndpointCreatorSnapshot.get();

		if (inferenceEndpointCreator == null) {
			return _toInferenceEndpoint(
				"Inference endpoints are only supported when the search " +
					"engine is Elasticsearch.",
				null, service);
		}

		String inferenceId = _inferenceIdResolver.composeInferenceId(
			contextCompany.getCompanyId(), service);

		String serviceSettingsJSON = null;

		Object serviceSettings = inferenceEndpoint.getServiceSettings();

		if (serviceSettings != null) {
			serviceSettingsJSON = _jsonFactory.looseSerialize(serviceSettings);
		}

		try {
			inferenceEndpointCreator.createInferenceEndpoint(
				inferenceId, service, serviceSettingsJSON);
		}
		catch (Exception exception) {
			return _toInferenceEndpoint(
				exception.getMessage(), inferenceId, service);
		}

		return _toInferenceEndpoint(null, inferenceId, service);
	}

	private void _checkPermission() {
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (!permissionChecker.isCompanyAdmin() &&
			!permissionChecker.isOmniadmin()) {

			throw new NotAuthorizedException(Response.Status.UNAUTHORIZED);
		}
	}

	private InferenceEndpoint _toInferenceEndpoint(
		String createErrorMessage, String createInferenceId,
		String createService) {

		// The response never echoes the service settings: they may carry
		// secrets (e.g., the provider API key)

		return new InferenceEndpoint() {
			{
				setErrorMessage(() -> createErrorMessage);
				setInferenceId(() -> createInferenceId);
				setService(() -> createService);
			}
		};
	}

	private static final Snapshot<InferenceEndpointCreator>
		_inferenceEndpointCreatorSnapshot = new Snapshot<>(
			InferenceEndpointResourceImpl.class, InferenceEndpointCreator.class,
			null, true);

	@Reference
	private InferenceIdResolver _inferenceIdResolver;

	@Reference
	private JSONFactory _jsonFactory;

}