/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.text.embeddings;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.rest.text.embeddings.configuration.ProviderInputValidator;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Rodrigo Guedes de Souza
 */
public abstract class BaseProviderInputValidator
	implements ProviderInputValidator {

	@Override
	public Map<String, String> validate(Map<String, Object> serviceSettings) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();

		if (serviceSettings == null) {
			serviceSettings = Collections.emptyMap();
		}

		for (String requiredFieldName : getRequiredFieldNames()) {
			if (Validator.isBlank(
					GetterUtil.getString(
						serviceSettings.get(requiredFieldName)))) {

				fieldErrors.put(requiredFieldName, "This field is required.");
			}
		}

		_validateModelId(serviceSettings, fieldErrors);

		_validateDimensions(serviceSettings, fieldErrors);

		_validateSimilarity(serviceSettings, fieldErrors);

		return fieldErrors;
	}

	protected int getMaxDimensions() {
		return 0;
	}

	protected Set<String> getModelIds() {
		return Collections.emptySet();
	}

	protected Set<String> getRequiredFieldNames() {
		return Collections.emptySet();
	}

	protected Set<String> getSimilarities() {
		return Collections.emptySet();
	}

	private String _merge(Set<String> values) {
		return StringUtil.merge(new TreeSet<>(values), ", ");
	}

	private void _validateDimensions(
		Map<String, Object> serviceSettings, Map<String, String> fieldErrors) {

		Object dimensions = serviceSettings.get("dimensions");

		if (dimensions == null) {
			return;
		}

		int value = GetterUtil.getInteger(dimensions, -1);

		if (value <= 0) {
			fieldErrors.put(
				"dimensions", "The dimensions must be a positive integer.");

			return;
		}

		int maxDimensions = getMaxDimensions();

		if ((maxDimensions > 0) && (value > maxDimensions)) {
			fieldErrors.put(
				"dimensions",
				"The dimensions must not exceed " + maxDimensions + ".");
		}
	}

	private void _validateModelId(
		Map<String, Object> serviceSettings, Map<String, String> fieldErrors) {

		Set<String> modelIds = getModelIds();

		if (modelIds.isEmpty()) {
			return;
		}

		String modelId = GetterUtil.getString(serviceSettings.get("model_id"));

		if (Validator.isBlank(modelId) || modelIds.contains(modelId)) {
			return;
		}

		fieldErrors.put(
			"model_id",
			StringBundler.concat(
				"The model \"", modelId, "\" is not supported. Supported ",
				"models are: ", _merge(modelIds), "."));
	}

	private void _validateSimilarity(
		Map<String, Object> serviceSettings, Map<String, String> fieldErrors) {

		Set<String> similarities = getSimilarities();

		if (similarities.isEmpty()) {
			return;
		}

		String similarity = GetterUtil.getString(
			serviceSettings.get("similarity"));

		if (Validator.isBlank(similarity) ||
			similarities.contains(similarity)) {

			return;
		}

		fieldErrors.put(
			"similarity",
			StringBundler.concat(
				"The similarity \"", similarity, "\" is not supported. ",
				"Supported values are: ", _merge(similarities), "."));
	}

}