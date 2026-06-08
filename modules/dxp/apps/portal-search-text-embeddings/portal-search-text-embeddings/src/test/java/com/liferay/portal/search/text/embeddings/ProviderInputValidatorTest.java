/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.text.embeddings;

import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.rest.text.embeddings.configuration.ProviderInputValidator;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Rodrigo Guedes de Souza
 */
public class ProviderInputValidatorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testHuggingFaceMissingRequiredFields() {
		ProviderInputValidator providerInputValidator =
			new HuggingFaceProviderInputValidator();

		Assert.assertEquals(
			"hugging_face", providerInputValidator.getService());

		Map<String, String> fieldErrors = providerInputValidator.validate(
			Collections.emptyMap());

		Assert.assertEquals(fieldErrors.toString(), 2, fieldErrors.size());
		Assert.assertEquals(
			"This field is required.", fieldErrors.get("api_key"));
		Assert.assertEquals("This field is required.", fieldErrors.get("url"));
	}

	@Test
	public void testHuggingFaceUnsupportedSimilarity() {
		ProviderInputValidator providerInputValidator =
			new HuggingFaceProviderInputValidator();

		Map<String, String> fieldErrors = providerInputValidator.validate(
			HashMapBuilder.<String, Object>put(
				"api_key", "secret"
			).put(
				"similarity", "manhattan"
			).put(
				"url", "https://example.com"
			).build());

		Assert.assertEquals(
			"The similarity \"manhattan\" is not supported. Supported values " +
				"are: cosine, dot_product, l2_norm.",
			fieldErrors.get("similarity"));
	}

	@Test
	public void testOpenAIDimensionsExceedMaximum() {
		Map<String, String> fieldErrors = _validateOpenAI(
			HashMapBuilder.<String, Object>put(
				"api_key", "secret"
			).put(
				"dimensions", 4096
			).put(
				"model_id", "text-embedding-3-large"
			).build());

		Assert.assertEquals(
			"The dimensions must not exceed 3072.",
			fieldErrors.get("dimensions"));
	}

	@Test
	public void testOpenAIDimensionsNotPositive() {
		Map<String, String> fieldErrors = _validateOpenAI(
			HashMapBuilder.<String, Object>put(
				"api_key", "secret"
			).put(
				"dimensions", 0
			).put(
				"model_id", "text-embedding-3-large"
			).build());

		Assert.assertEquals(
			"The dimensions must be a positive integer.",
			fieldErrors.get("dimensions"));
	}

	@Test
	public void testOpenAIUnsupportedModelId() {
		Map<String, String> fieldErrors = _validateOpenAI(
			HashMapBuilder.<String, Object>put(
				"api_key", "secret"
			).put(
				"model_id", "text-embedding-9-ultra"
			).build());

		Assert.assertEquals(fieldErrors.toString(), 1, fieldErrors.size());
		Assert.assertEquals(
			"The model \"text-embedding-9-ultra\" is not supported. " +
				"Supported models are: text-embedding-3-large, " +
					"text-embedding-3-small, text-embedding-ada-002.",
			fieldErrors.get("model_id"));
	}

	@Test
	public void testOpenAIValid() {
		Map<String, String> fieldErrors = _validateOpenAI(
			HashMapBuilder.<String, Object>put(
				"api_key", "secret"
			).put(
				"dimensions", 1536
			).put(
				"model_id", "text-embedding-3-large"
			).put(
				"similarity", "cosine"
			).build());

		Assert.assertTrue(fieldErrors.toString(), fieldErrors.isEmpty());
	}

	@Test
	public void testVertexAIMissingRequiredFields() {
		ProviderInputValidator providerInputValidator =
			new VertexAIProviderInputValidator();

		Assert.assertEquals(
			"googlevertexai", providerInputValidator.getService());

		Map<String, String> fieldErrors = providerInputValidator.validate(
			HashMapBuilder.<String, Object>put(
				"model_id", "text-embedding-004"
			).build());

		Assert.assertEquals(fieldErrors.toString(), 3, fieldErrors.size());
		Assert.assertEquals(
			"This field is required.", fieldErrors.get("location"));
		Assert.assertEquals(
			"This field is required.", fieldErrors.get("project_id"));
		Assert.assertEquals(
			"This field is required.", fieldErrors.get("service_account_json"));
	}

	@Test
	public void testVertexAIUnsupportedModelId() {
		ProviderInputValidator providerInputValidator =
			new VertexAIProviderInputValidator();

		Map<String, String> fieldErrors = providerInputValidator.validate(
			HashMapBuilder.<String, Object>put(
				"location", "us-central1"
			).put(
				"model_id", "gemini-pro"
			).put(
				"project_id", "my-project"
			).put(
				"service_account_json", "{}"
			).build());

		Assert.assertEquals(fieldErrors.toString(), 1, fieldErrors.size());
		Assert.assertTrue(
			fieldErrors.toString(), fieldErrors.containsKey("model_id"));
	}

	private Map<String, String> _validateOpenAI(
		Map<String, Object> serviceSettings) {

		ProviderInputValidator providerInputValidator =
			new OpenAIProviderInputValidator();

		Assert.assertEquals("openai", providerInputValidator.getService());

		return providerInputValidator.validate(serviceSettings);
	}

}