/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../fixtures/loginTest';
import {semanticSearchConfigurationPageTest} from '../../../fixtures/semanticSearchConfigurationPageTest';

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

testWithBYOLLMDisabled(
	'Labels text embedding providers without the architectural descriptor when the LPD-11319 feature flag is off',
	{tag: '@LPD-92310'},
	async ({semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		const optionLabels =
			await semanticSearchConfigurationPage.getTextEmbeddingProviderOptionLabels();

		expect(optionLabels).not.toContain('Elasticsearch Inference Endpoint');
		expect(optionLabels.join('\n')).not.toContain(
			'(through Liferay Integration)'
		);

		expect(optionLabels.join('\n')).not.toContain('(Legacy)');

		await expect(
			semanticSearchConfigurationPage.textEmbeddingProviderArchitectureHelp
		).toHaveCount(0);
	}
);

testWithBYOLLMEnabled(
	'Labels Liferay-integrated providers with the architectural descriptor when the LPD-11319 feature flag is on',
	{tag: '@LPD-92310'},
	async ({semanticSearchConfigurationPage}) => {
		await semanticSearchConfigurationPage.goto();

		const optionLabels =
			await semanticSearchConfigurationPage.getTextEmbeddingProviderOptionLabels();

		expect(optionLabels).toContain('Elasticsearch Inference Endpoint');

		expect(
			optionLabels.some((label) =>
				label.endsWith('(through Liferay Integration)')
			)
		).toBe(true);
		expect(optionLabels).not.toContain(
			'Elasticsearch Inference Endpoint (through Liferay Integration)'
		);

		expect(optionLabels.join('\n')).not.toContain('(Legacy)');

		await expect(
			semanticSearchConfigurationPage.textEmbeddingProviderArchitectureHelp
		).toBeVisible();
	}
);
