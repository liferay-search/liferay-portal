/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.text.embeddings;

import com.liferay.portal.search.rest.text.embeddings.configuration.ProviderInputValidator;

import java.util.Set;

import org.osgi.service.component.annotations.Component;

/**
 * @author Rodrigo Guedes de Souza
 */
@Component(service = ProviderInputValidator.class)
public class VertexAIProviderInputValidator extends BaseProviderInputValidator {

	@Override
	public String getService() {
		return "googlevertexai";
	}

	@Override
	protected Set<String> getModelIds() {
		return _modelIds;
	}

	@Override
	protected Set<String> getRequiredFieldNames() {
		return _requiredFieldNames;
	}

	private static final Set<String> _modelIds = Set.of(
		"text-embedding-004", "text-embedding-005",
		"text-multilingual-embedding-002");
	private static final Set<String> _requiredFieldNames = Set.of(
		"location", "model_id", "project_id", "service_account_json");

}