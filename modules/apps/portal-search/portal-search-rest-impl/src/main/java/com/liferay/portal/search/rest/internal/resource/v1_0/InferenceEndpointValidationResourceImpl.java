/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.rest.internal.resource.v1_0;

import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.rest.dto.v1_0.InferenceEndpoint;
import com.liferay.portal.search.rest.dto.v1_0.InferenceEndpointValidation;
import com.liferay.portal.search.rest.internal.text.embeddings.configuration.ProviderInputValidatorRegistry;
import com.liferay.portal.search.rest.resource.v1_0.InferenceEndpointValidationResource;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Petteri Karttunen
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/inference-endpoint-validation.properties",
	scope = ServiceScope.PROTOTYPE,
	service = InferenceEndpointValidationResource.class
)
public class InferenceEndpointValidationResourceImpl
	extends BaseInferenceEndpointValidationResourceImpl {

	@Override
	public InferenceEndpointValidation postInferenceEndpointValidate(
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

		Map<String, String> validationFieldErrors =
			_providerInputValidatorRegistry.validate(
				service, inferenceEndpoint.getServiceSettings());

		return new InferenceEndpointValidation() {
			{

				// The local variable name must not match the inherited
				// "fieldErrors" field, or the lazy suppliers would capture
				// the inherited null field instead of the enclosing local

				setFieldErrors(() -> validationFieldErrors);
				setValid(validationFieldErrors::isEmpty);
			}
		};
	}

	private void _checkPermission() {
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (!permissionChecker.isCompanyAdmin() &&
			!permissionChecker.isOmniadmin()) {

			throw new NotAuthorizedException(Response.Status.UNAUTHORIZED);
		}
	}

	@Reference
	private ProviderInputValidatorRegistry _providerInputValidatorRegistry;

}