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
	async ({semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		await expect(
			semanticSearchConfigurationPage.bringYourOwnLLMCapabilityAlert
		).toHaveCount(0);

		await expect(
			semanticSearchConfigurationPage.bringYourOwnLLMEnabledCheckbox
		).toHaveCount(0);
	}
);

testWithBYOLLMEnabled(
	'Shows the BYO-LLM capability alert when the LPD-11319 feature flag is on and the capability is unavailable',
	{tag: '@LPD-90488'},
	async ({semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		await expect(
			semanticSearchConfigurationPage.bringYourOwnLLMCapabilityAlert
		).toContainText(
			'Bring your own LLM via Elasticsearch Inference Endpoints is unavailable.'
		);

		await expect(
			semanticSearchConfigurationPage.bringYourOwnLLMEnabledCheckbox
		).toHaveCount(0);
	}
);

testWithBYOLLMDisabledAndBetaProvidersEnabled(
	'Hides the BYO-LLM provider and the architectural suffix when the LPD-11319 feature flag is off',
	{tag: '@LPD-92310'},
	async ({semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		const optionTexts =
			await semanticSearchConfigurationPage.textEmbeddingProviderOptions.allTextContents();

		expect(optionTexts).toContain('OpenAI');
		expect(optionTexts).not.toContain('Elasticsearch Inference Endpoint');

		for (const optionText of optionTexts) {
			expect(optionText).not.toContain('(through Liferay Integration)');
		}

		await expect(
			semanticSearchConfigurationPage.providerArchitectureHelpText
		).toHaveCount(0);
	}
);

testWithBYOLLMProviderSelectable(
	'Labels the Liferay-integrated providers with the architectural suffix when the BYO-LLM provider is visible',
	{tag: '@LPD-92310'},
	async ({semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		// Check the dropdown labels

		const optionTexts =
			await semanticSearchConfigurationPage.textEmbeddingProviderOptions.allTextContents();

		expect(optionTexts).toContain('Elasticsearch Inference Endpoint');
		expect(optionTexts).toContain('OpenAI (through Liferay Integration)');

		for (const optionText of optionTexts) {
			expect(optionText).not.toContain('(Legacy)');
		}

		// Check the architectural help text below the dropdown

		await expect(
			semanticSearchConfigurationPage.providerArchitectureHelpText
		).toBeVisible();
	}
);

testWithBYOLLMProviderSelectable(
	'Hides the non-applicable settings when the BYO-LLM provider is selected',
	{tag: '@LPD-92317'},
	async ({semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		// Show the settings for a Liferay-integrated provider

		await semanticSearchConfigurationPage.textEmbeddingProviderSelect.selectOption(
			'openai'
		);

		await expect(
			semanticSearchConfigurationPage.maxCharacterCountInput
		).toBeVisible();
		await expect(
			semanticSearchConfigurationPage.textTruncationStrategySelect
		).toBeVisible();

		// Hide the settings for the BYO-LLM provider

		await semanticSearchConfigurationPage.textEmbeddingProviderSelect.selectOption(
			'Elasticsearch Inference Endpoint'
		);

		await expect(
			semanticSearchConfigurationPage.maxCharacterCountInput
		).toHaveCount(0);
		await expect(
			semanticSearchConfigurationPage.textTruncationStrategySelect
		).toHaveCount(0);

		// Show the settings again when switching back

		await semanticSearchConfigurationPage.textEmbeddingProviderSelect.selectOption(
			'openai'
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
	'Shows an actionable error when testing the BYO-LLM provider without an active inference endpoint',
	{tag: '@LPD-92306'},
	async ({semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		// Select the BYO-LLM provider

		await semanticSearchConfigurationPage.textEmbeddingProviderSelect.selectOption(
			'Elasticsearch Inference Endpoint'
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
