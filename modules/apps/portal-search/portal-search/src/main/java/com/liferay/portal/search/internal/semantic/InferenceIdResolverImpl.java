/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.semantic;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.configuration.SemanticSearchConfiguration;
import com.liferay.portal.search.configuration.SemanticSearchConfigurationProvider;
import com.liferay.portal.search.rest.dto.v1_0.EmbeddingProviderConfiguration;
import com.liferay.portal.search.semantic.InferenceIdResolver;

import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodrigo Guedes de Souza
 */
@Component(service = InferenceIdResolver.class)
public class InferenceIdResolverImpl implements InferenceIdResolver {

	@Override
	public String resolveInferenceId(long companyId) {
		SemanticSearchConfiguration semanticSearchConfiguration =
			_semanticSearchConfigurationProvider.getCompanyConfiguration(
				companyId);

		String[] textEmbeddingProviderConfigurationJSONs =
			semanticSearchConfiguration.
				textEmbeddingProviderConfigurationJSONs();

		if (textEmbeddingProviderConfigurationJSONs == null) {
			return null;
		}

		for (String textEmbeddingProviderConfigurationJSON :
				textEmbeddingProviderConfigurationJSONs) {

			if (Validator.isNull(textEmbeddingProviderConfigurationJSON)) {
				continue;
			}

			EmbeddingProviderConfiguration embeddingProviderConfiguration;

			try {
				embeddingProviderConfiguration =
					EmbeddingProviderConfiguration.unsafeToDTO(
						textEmbeddingProviderConfigurationJSON);
			}
			catch (Exception exception) {
				if (_log.isDebugEnabled()) {
					_log.debug(exception);
				}

				continue;
			}

			if ((embeddingProviderConfiguration == null) ||
				!Objects.equals(
					embeddingProviderConfiguration.getProviderName(),
					_ELASTICSEARCH_PROVIDER_NAME)) {

				continue;
			}

			String service = _getService(
				embeddingProviderConfiguration.getAttributes());

			if (Validator.isBlank(service)) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						StringBundler.concat(
							"Elasticsearch provider configuration has no ",
							"valid \"attributes.service\" value for company ",
							companyId));
				}

				continue;
			}

			return StringBundler.concat(
				"liferay-", companyId, "-inference-", service);
		}

		return null;
	}

	private String _getService(Object attributes) {
		if (!(attributes instanceof Map)) {
			return null;
		}

		Map<?, ?> attributesMap = (Map<?, ?>)attributes;

		Object service = attributesMap.get("service");

		if (service instanceof String) {
			return (String)service;
		}

		return null;
	}

	private static final String _ELASTICSEARCH_PROVIDER_NAME =
		"Elasticsearch Inference Endpoint";

	private static final Log _log = LogFactoryUtil.getLog(
		InferenceIdResolverImpl.class);

	@Reference
	private SemanticSearchConfigurationProvider
		_semanticSearchConfigurationProvider;

}