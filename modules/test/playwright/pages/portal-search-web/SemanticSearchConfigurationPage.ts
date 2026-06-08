/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page, expect} from '@playwright/test';

import {PORTLET_URLS} from '../../utils/portletUrls';

export class SemanticSearchConfigurationPage {
	readonly bringYourOwnLLMCapabilityAlert: Locator;
	readonly bringYourOwnLLMEnabledCheckbox: Locator;
	readonly inferenceServiceSelect: Locator;
	readonly maxCharacterCountInput: Locator;
	readonly page: Page;
	readonly textEmbeddingProviderArchitectureHelp: Locator;
	readonly saveButton: Locator;
	readonly textEmbeddingProviderSelect: Locator;
	readonly textTruncationStrategySelect: Locator;

	constructor(page: Page) {
		this.page = page;

		this.bringYourOwnLLMCapabilityAlert = page.getByTestId(
			'bringYourOwnLLMCapabilityAlert'
		);
		this.bringYourOwnLLMEnabledCheckbox = page.getByTestId(
			'bringYourOwnLLMEnabledCheckbox'
		);
		this.inferenceServiceSelect = page.getByLabel('Service', {
			exact: true,
		});
		this.maxCharacterCountInput = page.getByLabel('Max Character Count');
		this.textEmbeddingProviderArchitectureHelp = page.getByText(
			'Choose where the embedding model runs'
		this.saveButton = page.getByRole('button', {exact: true, name: 'Save'});
		);
		this.textEmbeddingProviderSelect = page.getByLabel(
			'Text Embedding Provider'
		);
	}

	async getTextEmbeddingProviderOptionLabels(): Promise<string[]> {
		return this.textEmbeddingProviderSelect
			.locator('option')
			.allTextContents();
		this.textTruncationStrategySelect = page.getByLabel(
			'Text Truncation Strategy'
		);
	}

	async goto() {
		await this.page.goto(PORTLET_URLS.semanticSearchConfiguration);

		await expect(this.textEmbeddingProviderSelect).toBeVisible();
	}
}
