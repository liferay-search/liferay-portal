/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../fixtures/loginTest';
import {systemSettingsPageTest} from '../../../fixtures/systemSettingsPageTest';

const FEATURE_FLAG_DISABLED_REASON =
	'The Bring-your-own-LLM feature flag is disabled.';

const test = mergeTests(
	loginTest(),
	systemSettingsPageTest,
	featureFlagsTest({
		'LPD-11319': {enabled: false},
	})
);

test.describe('Semantic Search Configuration BYO-LLM Capability', () => {
	test(
		'Disables the form and shows a tooltip when the LPD-11319 feature flag is off @LPD-90830',
		{tag: '@LPD-90830'},
		async ({page, systemSettingsPage}) => {

			// Open the System Settings → Search → Semantic Search form

			await systemSettingsPage.goToSystemSetting(
				'Search',
				'Semantic Search'
			);

			// Capability fieldset is rendered and disabled

			const capabilityFieldset = page.locator(
				'fieldset[data-qa-id="semanticSearchCapability"]'
			);

			await expect(capabilityFieldset).toBeVisible();
			await expect(capabilityFieldset).toBeDisabled();

			// Tooltip carries the localized feature-flag-disabled reason

			await expect(capabilityFieldset).toHaveAttribute(
				'title',
				FEATURE_FLAG_DISABLED_REASON
			);

			// Form controls inside the fieldset are disabled

			await expect(
				page.getByLabel('Text Embeddings Enabled')
			).toBeDisabled();
			await expect(
				page.getByRole('button', {name: 'Save'})
			).toBeDisabled();
		}
	);
});
