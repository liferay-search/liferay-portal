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
