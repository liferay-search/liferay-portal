/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.semantic;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.configuration.SemanticSearchConfiguration;
import com.liferay.portal.search.configuration.SemanticSearchConfigurationProvider;
import com.liferay.portal.search.semantic.InferenceIdResolver;
import com.liferay.portal.search.semantic.TextEmbeddingProviderNames;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodrigo Guedes de Souza
 */
@Component(service = InferenceIdResolver.class)
public class InferenceIdResolverImpl implements InferenceIdResolver {

	@Override
	public String composeInferenceId(long companyId, String service) {
		return composeInferenceIdPrefix(companyId) + service;
	}

	@Override
	public String composeInferenceIdPrefix(long companyId) {
		return StringBundler.concat("liferay-", companyId, "-inference-");
	}

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

			JSONObject jsonObject;

			try {
				jsonObject = _jsonFactory.createJSONObject(
					textEmbeddingProviderConfigurationJSON);
			}
			catch (Exception exception) {
				if (_log.isDebugEnabled()) {
					_log.debug(exception);
				}

				continue;
			}

			if (!Objects.equals(
					jsonObject.getString("providerName"),
					TextEmbeddingProviderNames.
						ELASTICSEARCH_INFERENCE_ENDPOINT)) {

				continue;
			}

			JSONObject attributesJSONObject = jsonObject.getJSONObject(
				"attributes");

			String service = null;

			if (attributesJSONObject != null) {
				Object serviceObject = attributesJSONObject.get("service");

				if (serviceObject instanceof String) {
					service = (String)serviceObject;
				}
			}

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

			return composeInferenceId(companyId, service);
		}

		return null;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		InferenceIdResolverImpl.class);

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private SemanticSearchConfigurationProvider
		_semanticSearchConfigurationProvider;

}