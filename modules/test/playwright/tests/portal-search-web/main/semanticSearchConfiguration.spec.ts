/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../fixtures/loginTest';
import {semanticSearchConfigurationPageTest} from '../../../fixtures/semanticSearchConfigurationPageTest';
import {clickAndExpectToBeVisible} from '../../../utils/clickAndExpectToBeVisible';

const testWithBYOLLMDisabled = mergeTests(
	loginTest(),
	featureFlagsTest({'LPD-11319': {enabled: false}}),
	semanticSearchConfigurationPageTest
);

const testWithBYOLLMEnabled = mergeTests(
	loginTest(),
	featureFlagsTest({'LPD-11319': {enabled: true}}),
	semanticSearchConfigurationPageTest
);

const testWithBYOLLMDisabledAndBetaProvidersEnabled = mergeTests(
	loginTest(),
	featureFlagsTest({
		'LPD-11319': {enabled: false},
		'LPS-122920': {enabled: true},
	}),
	semanticSearchConfigurationPageTest
);

const testWithBYOLLMProviderSelectable = mergeTests(
	loginTest(),
	featureFlagsTest({
		'LPD-11319': {enabled: true},
		'LPS-122920': {enabled: true},
	}),
	semanticSearchConfigurationPageTest
);

testWithBYOLLMDisabled(
	'Hides the BYO-LLM capability alert when the LPD-11319 feature flag is off',
	{tag: '@LPD-90488'},
	async ({page, semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		await expect(
			page.getByRole('alert').filter({
				hasText:
					'Bring your own LLM via Elasticsearch Inference Endpoints is unavailable.',
			})
		).not.toBeVisible();
	}
);

testWithBYOLLMEnabled(
	'Shows the BYO-LLM capability alert when the LPD-11319 feature flag is on and the capability is unavailable',
	{tag: '@LPD-90488'},
	async ({page, semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		await expect(
			page.getByRole('alert').filter({
				hasText:
					'Bring your own LLM via Elasticsearch Inference Endpoints is unavailable.',
			})
		).toBeVisible();
	}
);

testWithBYOLLMDisabledAndBetaProvidersEnabled(
	'Hides the BYO-LLM provider when the LPD-11319 feature flag is off',
	{tag: '@LPD-92310'},
	async ({semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		const optionTexts =
			await semanticSearchConfigurationPage.textEmbeddingProviderOptions.allTextContents();

		expect(optionTexts).not.toContain(
			'Bring Your Own LLM via Elasticsearch'
		);
		expect(optionTexts).toContain('OpenAI');
	}
);

testWithBYOLLMProviderSelectable(
	'Lists the BYO-LLM provider without an architectural suffix when it is visible',
	{tag: '@LPD-92310'},
	async ({semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		// Check the Picker labels

		const optionTexts =
			await semanticSearchConfigurationPage.getTextEmbeddingProviderOptionLabels();

		expect(optionTexts).toContain('Bring Your Own LLM via Elasticsearch');
		expect(optionTexts).toContain('OpenAI');

		for (const optionText of optionTexts) {
			expect(optionText).not.toContain('(Legacy)');
		}
	}
);

testWithBYOLLMProviderSelectable(
	'Hides the non-applicable settings when the BYO-LLM provider is selected',
	{tag: '@LPD-92317'},
	async ({semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		// Show the settings for a Liferay-integrated provider

		await semanticSearchConfigurationPage.selectTextEmbeddingProvider(
			'OpenAI'
		);

		await expect(
			semanticSearchConfigurationPage.maxCharacterCountInput
		).toBeVisible();
		await expect(
			semanticSearchConfigurationPage.textTruncationStrategySelect
		).toBeVisible();

		// Hide the settings for the BYO-LLM provider

		await semanticSearchConfigurationPage.selectTextEmbeddingProvider(
			'Bring Your Own LLM via Elasticsearch'
		);

		await expect(
			semanticSearchConfigurationPage.maxCharacterCountInput
		).toHaveCount(0);
		await expect(
			semanticSearchConfigurationPage.textTruncationStrategySelect
		).toHaveCount(0);

		// Show the settings again when switching back

		await semanticSearchConfigurationPage.selectTextEmbeddingProvider(
			'OpenAI'
		);

		await expect(
			semanticSearchConfigurationPage.maxCharacterCountInput
		).toBeVisible();
		await expect(
			semanticSearchConfigurationPage.textTruncationStrategySelect
		).toBeVisible();
	}
);

testWithBYOLLMProviderSelectable(
	'Renders the dynamic BYO-LLM provider creation form from the Elasticsearch services catalog',
	{tag: '@LPD-92319'},
	async ({semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		// Select the BYO-LLM provider

		await semanticSearchConfigurationPage.selectTextEmbeddingProvider(
			'Bring Your Own LLM via Elasticsearch'
		);

		// The service dropdown is populated from the Elasticsearch services
		// catalog

		await expect(
			semanticSearchConfigurationPage.inferenceServiceSelect
		).toBeVisible();

		await expect(async () => {
			const optionLabels =
				await semanticSearchConfigurationPage.getInferenceServiceOptionLabels();

			expect(optionLabels.length).toBeGreaterThan(1);
		}).toPass({timeout: 10000});

		// Selecting a service renders its fields dynamically, with the
		// sensitive fields as password inputs

		await semanticSearchConfigurationPage.selectInferenceService('openai');

		await expect(async () => {
			const passwordInputsCount =
				await semanticSearchConfigurationPage.page
					.locator('input[type="password"]')
					.count();

			expect(passwordInputsCount).toBeGreaterThan(0);
		}).toPass({timeout: 10000});
	}
);

testWithBYOLLMProviderSelectable(
	'Rejects an unsupported model with a per-field error and does not submit',
	{tag: '@LPD-92327'},
	async ({page, semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		// Select the BYO-LLM provider and the OpenAI service

		await semanticSearchConfigurationPage.selectTextEmbeddingProvider(
			'Bring Your Own LLM via Elasticsearch'
		);

		await expect(async () => {
			const optionLabels =
				await semanticSearchConfigurationPage.getInferenceServiceOptionLabels();

			expect(optionLabels.length).toBeGreaterThan(1);
		}).toPass({timeout: 10000});

		await semanticSearchConfigurationPage.selectInferenceService('openai');

		// Enter an unsupported model and an API key, then save. The fields
		// are targeted by their id (the ES field name), not by their label,
		// which Elasticsearch may localize.

		await page.locator('#byollm_api_key').fill('test-api-key');
		await page.locator('#byollm_model_id').fill('not-a-model');

		await semanticSearchConfigurationPage.saveButton.click();

		// The per-field error appears and the model is not accepted

		await expect(
			page.getByText('The model "not-a-model" is not supported.', {
				exact: false,
			})
		).toBeVisible();
	}
);

testWithBYOLLMProviderSelectable(
	'Shows an actionable error when testing the BYO-LLM provider without an active inference endpoint',
	{tag: '@LPD-92306'},
	async ({semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		// Select the BYO-LLM provider

		await semanticSearchConfigurationPage.selectTextEmbeddingProvider(
			'Bring Your Own LLM via Elasticsearch'
		);

		// Test the configuration without an active inference endpoint

		await clickAndExpectToBeVisible({
			target: semanticSearchConfigurationPage.testConfigurationResultAlert,
			trigger: semanticSearchConfigurationPage.testConfigurationButton,
		});

		await expect(
			semanticSearchConfigurationPage.testConfigurationResultAlert
		).toContainText(
			'There is no active Elasticsearch inference endpoint configured'
		);
	}
);
