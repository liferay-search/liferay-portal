/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.rest.internal.resource.v1_0;

import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.ml.embedding.EmbeddingProviderStatus;
import com.liferay.portal.search.ml.embedding.text.TextEmbeddingRetriever;
import com.liferay.portal.search.rest.dto.v1_0.EmbeddingProviderConfiguration;
import com.liferay.portal.search.rest.dto.v1_0.EmbeddingProviderValidationResult;
import com.liferay.portal.search.rest.resource.v1_0.EmbeddingProviderValidationResultResource;
import com.liferay.portal.search.semantic.InferenceEndpointTester;
import com.liferay.portal.search.semantic.InferenceIdResolver;
import com.liferay.portal.search.semantic.TextEmbeddingProviderNames;

import jakarta.ws.rs.NotFoundException;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Petteri Karttunen
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/embedding-provider-validation-result.properties",
	scope = ServiceScope.PROTOTYPE,
	service = EmbeddingProviderValidationResultResource.class
)
public class EmbeddingProviderValidationResultResourceImpl
	extends BaseEmbeddingProviderValidationResultResourceImpl {

	@Override
	public EmbeddingProviderValidationResult
		postEmbeddingValidateProviderConfiguration(
			EmbeddingProviderConfiguration embeddingProviderConfiguration) {

		if (Objects.equals(
				embeddingProviderConfiguration.getProviderName(),
				TextEmbeddingProviderNames.ELASTICSEARCH_INFERENCE_ENDPOINT)) {

			return _validateInferenceEndpoint();
		}

		try {
			EmbeddingProviderStatus embeddingProviderStatus =
				_textEmbeddingRetriever.getEmbeddingProviderStatus(
					embeddingProviderConfiguration.toString());

			return new EmbeddingProviderValidationResult() {
				{
					setErrorMessage(
						() -> {
							if (Validator.isBlank(
									embeddingProviderStatus.
										getErrorMessage())) {

								return null;
							}

							return embeddingProviderStatus.getErrorMessage();
						});
					setExpectedDimensions(
						() -> {
							if (!Validator.isBlank(
									embeddingProviderStatus.
										getErrorMessage())) {

								return null;
							}

							return embeddingProviderStatus.
								getEmbeddingVectorDimensions();
						});
				}
			};
		}
		catch (Exception exception) {
			return _toErrorResult(exception.getMessage());
		}
	}

	private EmbeddingProviderValidationResult _toErrorResult(String message) {
		return new EmbeddingProviderValidationResult() {
			{
				setErrorMessage(() -> message);
			}
		};
	}

	private EmbeddingProviderValidationResult _validateInferenceEndpoint() {
		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-11319")) {

			throw new NotFoundException();
		}

		InferenceEndpointTester inferenceEndpointTester =
			_inferenceEndpointTesterSnapshot.get();

		if (inferenceEndpointTester == null) {
			return _toErrorResult(
				_language.get(
					contextAcceptLanguage.getPreferredLocale(),
					"inference-endpoints-are-supported-only-on-elasticsearch"));
		}

		try {
			String inferenceId = _inferenceIdResolver.resolveInferenceId(
				contextCompany.getCompanyId());

			if (Validator.isBlank(inferenceId)) {
				return _toErrorResult(
					_language.get(
						contextAcceptLanguage.getPreferredLocale(),
						"there-is-no-active-inference-endpoint-configured"));
			}

			int embeddingVectorDimensions = inferenceEndpointTester.test(
				inferenceId, _INPUT);

			return new EmbeddingProviderValidationResult() {
				{
					setExpectedDimensions(() -> embeddingVectorDimensions);
				}
			};
		}
		catch (Exception exception) {
			return _toErrorResult(exception.getMessage());
		}
	}

	private static final String _INPUT = "Liferay Semantic Search test";

	private static final Snapshot<InferenceEndpointTester>
		_inferenceEndpointTesterSnapshot = new Snapshot<>(
			EmbeddingProviderValidationResultResourceImpl.class,
			InferenceEndpointTester.class, null, true);

	@Reference
	private InferenceIdResolver _inferenceIdResolver;

	@Reference
	private Language _language;

	@Reference
	private TextEmbeddingRetriever _textEmbeddingRetriever;

}