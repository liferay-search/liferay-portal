/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.index;

import co.elastic.clients.elasticsearch.inference.InferenceEndpointInfo;
import co.elastic.clients.elasticsearch.inference.TaskType;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.search.elasticsearch8.internal.semantic.InferenceEndpointInfoFetcher;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Validates that an Elasticsearch inference endpoint exists with {@code
 * task_type=text_embedding} before any {@code semantic_text} mapping
 * operation, aborting with an actionable {@code RuntimeException} otherwise.
 *
 * <p>
 * The validation fails fast at the single mapping operation, where the
 * configuration cause is close by — without it, a missing or mistyped
 * endpoint would only surface later, once per document, at indexing time.
 * </p>
 *
 * @author Rodrigo Guedes de Souza
 */
@Component(service = InferenceEndpointValidator.class)
public class InferenceEndpointValidator {

	public void validate(String inferenceId) {
		List<InferenceEndpointInfo> inferenceEndpointInfos =
			_inferenceEndpointInfoFetcher.fetchInferenceEndpointInfos(
				inferenceId);

		for (InferenceEndpointInfo inferenceEndpointInfo :
				inferenceEndpointInfos) {

			TaskType taskType = inferenceEndpointInfo.taskType();

			if (taskType != TaskType.TextEmbedding) {
				throw new RuntimeException(
					StringBundler.concat(
						"Inference endpoint \"", inferenceId,
						"\" has task_type \"", taskType.jsonValue(),
						"\", expected \"", TaskType.TextEmbedding.jsonValue(),
						"\". Recreate it in the Semantic Search admin UI."));
			}
		}
	}

	@Reference
	private InferenceEndpointInfoFetcher _inferenceEndpointInfoFetcher;

}