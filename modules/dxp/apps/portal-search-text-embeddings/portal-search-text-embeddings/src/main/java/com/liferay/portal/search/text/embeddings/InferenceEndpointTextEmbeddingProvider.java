/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.text.embeddings;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.rest.dto.v1_0.EmbeddingProviderConfiguration;
import com.liferay.portal.search.rest.text.embeddings.configuration.TextEmbeddingProvider;
import com.liferay.portal.search.semantic.InferenceEndpointMetadata;
import com.liferay.portal.search.semantic.InferenceEndpointMetadataResolver;
import com.liferay.portal.search.semantic.InferenceIdResolver;
import com.liferay.portal.search.semantic.TextEmbeddingProviderNames;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Delegates text embedding to Elasticsearch via the configured inference
 * endpoint. Unlike the Liferay-integrated providers in this module, it never
 * calls an external LLM service from Liferay's side — Elasticsearch computes
 * the embeddings server-side through {@code semantic_text} fields.
 *
 * @author Rodrigo Guedes de Souza
 */
@Component(enabled = false, service = TextEmbeddingProvider.class)
public class InferenceEndpointTextEmbeddingProvider
	implements TextEmbeddingProvider {

	@Override
	public Double[] getEmbedding(
		EmbeddingProviderConfiguration embeddingProviderConfiguration,
		String text) {

		throw new UnsupportedOperationException(
			"Embeddings are computed server-side by Elasticsearch via " +
				"semantic_text fields");
	}

	public InferenceEndpointMetadata getEndpointMetadata() {
		long companyId = CompanyThreadLocal.getCompanyId();

		String inferenceId = _inferenceIdResolver.resolveInferenceId(companyId);

		if (Validator.isBlank(inferenceId)) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No inference endpoint name was resolved for company " +
						companyId);
			}

			return null;
		}

		return _inferenceEndpointMetadataResolver.
			resolveInferenceEndpointMetadata(inferenceId);
	}

	@Override
	public String getProviderName() {
		return TextEmbeddingProviderNames.ELASTICSEARCH_INFERENCE_ENDPOINT;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		InferenceEndpointTextEmbeddingProvider.class);

	@Reference
	private InferenceEndpointMetadataResolver
		_inferenceEndpointMetadataResolver;

	@Reference
	private InferenceIdResolver _inferenceIdResolver;

}