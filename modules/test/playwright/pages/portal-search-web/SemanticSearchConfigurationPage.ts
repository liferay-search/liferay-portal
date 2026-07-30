/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page, expect} from '@playwright/test';

import {clickAndExpectToBeVisible} from '../../utils/clickAndExpectToBeVisible';
import {PORTLET_URLS} from '../../utils/portletUrls';

export class SemanticSearchConfigurationPage {
	readonly inferenceServiceSelect: Locator;
	readonly maxCharacterCountInput: Locator;
	readonly page: Page;
	readonly saveButton: Locator;
	readonly testConfigurationButton: Locator;
	readonly testConfigurationResultAlert: Locator;
	readonly textEmbeddingProviderOptions: Locator;
	readonly textEmbeddingProviderSelect: Locator;
	readonly textTruncationStrategySelect: Locator;

	constructor(page: Page) {
		this.page = page;

		this.inferenceServiceSelect = page.getByLabel('Service', {
			exact: true,
		});
		this.maxCharacterCountInput = page.getByLabel('Max Character Count');
		this.saveButton = page.getByRole('button', {exact: true, name: 'Save'});
		this.testConfigurationButton = page.getByRole('button', {
			name: 'Test Configuration',
		});
		this.testConfigurationResultAlert = page.locator(
			'.test-configuration-button-root .alert'
		);
		this.textEmbeddingProviderSelect = page.getByLabel('Provider');
		this.textEmbeddingProviderOptions =
			this.textEmbeddingProviderSelect.locator('option');
		this.textTruncationStrategySelect = page.getByLabel(
			'Text Truncation Strategy'
		);
	}

	async goto() {
		await this.page.goto(PORTLET_URLS.semanticSearchConfiguration);

		await expect(this.textEmbeddingProviderSelect).toBeVisible();
	}

	async getInferenceServiceOptionLabels(): Promise<string[]> {
		return this._getPickerOptionLabels(this.inferenceServiceSelect);
	}

	async getTextEmbeddingProviderOptionLabels(): Promise<string[]> {
		return this._getPickerOptionLabels(this.textEmbeddingProviderSelect);
	}

	async selectInferenceService(optionLabel: string) {
		await this._selectPickerOption(
			this.inferenceServiceSelect,
			optionLabel
		);
	}

	async selectTextEmbeddingProvider(optionLabel: string) {
		await this._selectPickerOption(
			this.textEmbeddingProviderSelect,
			optionLabel
		);
	}

	private async _getPickerOptionLabels(picker: Locator): Promise<string[]> {
		await picker.click();

		await expect(picker).toHaveAttribute('aria-expanded', 'true');

		const listboxId = await picker.getAttribute('aria-controls');

		const labels = await this.page
			.locator(`#${listboxId} [role="option"]`)
			.allTextContents();

		await this.page.keyboard.press('Escape');

		return labels;
	}

	private async _selectPickerOption(picker: Locator, optionLabel: string) {
		if (((await picker.textContent()) ?? '').includes(optionLabel)) {
			return;
		}

		await clickAndExpectToBeVisible({
			autoClick: true,
			target: this.page.getByRole('option', {
				exact: true,
				name: optionLabel,
			}),
			trigger: picker,
		});
	}
}
